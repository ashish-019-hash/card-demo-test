import json
import re
from collections.abc import Callable
from pathlib import Path
from typing import TypedDict

from langchain_core.exceptions import OutputParserException
from langchain_openai import AzureChatOpenAI
from langgraph.graph import END, START, StateGraph
from openai import (
    AuthenticationError,
    BadRequestError,
    NotFoundError,
    OpenAIError,
    PermissionDeniedError,
)
from pydantic import ValidationError

from entity_agent.config import Settings
from entity_agent.models import ChunkExtraction, Entity, ExtractionResult, PageText
from entity_agent.pdf import chunk_pages, extract_pdf_pages
from entity_agent.prompts import EXTRACTION_SYSTEM_PROMPT, RECONCILIATION_SYSTEM_PROMPT


class AgentState(TypedDict, total=False):
    input_path: str
    document_name: str
    pages: list[PageText]
    chunks: list[str]
    chunk_results: list[Entity]
    failed_chunks: int
    entities: list[Entity]
    warnings: list[str]
    result: ExtractionResult


MODEL_ERRORS = (OpenAIError, OutputParserException, ValidationError, ValueError, TypeError)
FATAL_MODEL_ERRORS = (AuthenticationError, PermissionDeniedError, NotFoundError, BadRequestError)


def create_model(settings: Settings) -> AzureChatOpenAI:
    return AzureChatOpenAI(
        api_key=settings.azure_openai_api_key,
        azure_endpoint=settings.azure_openai_endpoint,
        azure_deployment=settings.azure_openai_deployment,
        api_version=settings.azure_openai_api_version,
        temperature=0,
        max_retries=3,
    )


def build_graph(
    settings: Settings | None = None,
    *,
    model_factory: Callable[[Settings], AzureChatOpenAI] = create_model,
):
    """Build the extraction workflow. A model factory can be injected for tests."""
    config = settings or Settings()
    model = model_factory(config)
    structured_model = model.with_structured_output(ChunkExtraction)

    def load_document(state: AgentState) -> AgentState:
        path = Path(state["input_path"])
        pages, warnings = extract_pdf_pages(
            path,
            ocr_language=config.ocr_language,
            min_text_chars=config.min_text_chars_per_page,
        )
        return {"document_name": path.name, "pages": pages, "warnings": warnings}

    def split_document(state: AgentState) -> AgentState:
        chunks = chunk_pages(state["pages"], config.chunk_size, config.chunk_overlap)
        if not chunks:
            return {"chunks": [], "warnings": state.get("warnings", []) + ["No text found."]}
        return {"chunks": chunks}

    def extract_chunks(state: AgentState) -> AgentState:
        entities: list[Entity] = []
        warnings = list(state.get("warnings", []))
        failed_chunks = 0
        for index, chunk in enumerate(state["chunks"], start=1):
            try:
                response = structured_model.invoke(
                    [
                        ("system", EXTRACTION_SYSTEM_PROMPT),
                        ("human", f"Extract entities from chunk {index}:\n\n{chunk}"),
                    ]
                )
                entities.extend(response.entities)
            except FATAL_MODEL_ERRORS:
                raise
            except MODEL_ERRORS as exc:
                failed_chunks += 1
                warnings.append(f"Chunk {index} extraction failed: {type(exc).__name__}")
        if state["chunks"] and failed_chunks == len(state["chunks"]):
            raise RuntimeError("Entity extraction failed for every document chunk.")
        return {
            "chunk_results": entities,
            "failed_chunks": failed_chunks,
            "warnings": warnings,
        }

    def reconcile_entities(state: AgentState) -> AgentState:
        candidates = state.get("chunk_results", [])
        if not candidates:
            return {"entities": []}
        warnings = list(state.get("warnings", []))
        grouped: dict[tuple[str, str], list[Entity]] = {}
        for entity in candidates:
            key = (entity.entity_type.strip().casefold(), entity.name.strip().casefold())
            grouped.setdefault(key, []).append(entity)

        merged_candidates: list[Entity] = []
        for group in grouped.values():
            merged = max(group, key=lambda item: item.confidence).model_copy(deep=True)
            evidence_keys = {(item.page, item.quote) for item in merged.evidence}
            attribute_keys = {
                (item.name.strip().casefold(), json.dumps(item.value, sort_keys=True))
                for item in merged.attributes
            }
            for duplicate in group:
                for evidence in duplicate.evidence:
                    key = (evidence.page, evidence.quote)
                    if key not in evidence_keys:
                        merged.evidence.append(evidence)
                        evidence_keys.add(key)
                for attribute in duplicate.attributes:
                    key = (
                        attribute.name.strip().casefold(),
                        json.dumps(attribute.value, sort_keys=True),
                    )
                    if key not in attribute_keys:
                        merged.attributes.append(attribute)
                        attribute_keys.add(key)
            merged_candidates.append(merged)

        batches: list[list[Entity]] = []
        current_batch: list[Entity] = []
        current_size = 0
        for entity in merged_candidates:
            entity_size = len(entity.model_dump_json())
            if current_batch and (len(current_batch) >= 40 or current_size + entity_size > 50_000):
                batches.append(current_batch)
                current_batch = []
                current_size = 0
            current_batch.append(entity)
            current_size += entity_size
        if current_batch:
            batches.append(current_batch)

        entities: list[Entity] = []
        for batch in batches:
            payload = json.dumps([entity.model_dump(mode="json") for entity in batch])
            try:
                response = structured_model.invoke(
                    [
                        ("system", RECONCILIATION_SYSTEM_PROMPT),
                        ("human", f"Reconcile these candidates from one document:\n\n{payload}"),
                    ]
                )
                entities.extend(response.entities)
            except FATAL_MODEL_ERRORS:
                raise
            except MODEL_ERRORS as exc:
                warnings.append(
                    f"Reconciliation batch failed ({type(exc).__name__}); kept raw candidates."
                )
                entities.extend(batch)
        return {"entities": entities, "warnings": warnings}

    def validate_result(state: AgentState) -> AgentState:
        warnings = list(state.get("warnings", []))
        unique_ids: set[str] = set()
        valid_entities: list[Entity] = []
        page_text = {page.page: page.text for page in state["pages"]}

        def normalize(value: str) -> str:
            return re.sub(r"\s+", " ", value).strip().casefold()

        for entity in state.get("entities", []):
            if entity.entity_id in unique_ids:
                base_id = entity.entity_id
                suffix = 2
                while f"{base_id}-{suffix}" in unique_ids:
                    suffix += 1
                entity.entity_id = f"{base_id}-{suffix}"
                warnings.append(f"Renamed duplicate entity id to: {entity.entity_id}")
            unique_ids.add(entity.entity_id)
            if not entity.evidence and not any(item.evidence for item in entity.attributes):
                warnings.append(f"Entity {entity.entity_id} has no source evidence.")
            for evidence in entity.evidence + [
                item for attribute in entity.attributes for item in attribute.evidence
            ]:
                source = page_text.get(evidence.page)
                if source is None or normalize(evidence.quote) not in normalize(source):
                    warnings.append(
                        f"Entity {entity.entity_id} has unverified evidence on page {evidence.page}."
                    )
            valid_entities.append(entity)
        result = ExtractionResult(
            document_name=state["document_name"], entities=valid_entities, warnings=warnings
        )
        return {"result": result}

    graph = StateGraph(AgentState)
    graph.add_node("load_document", load_document)
    graph.add_node("split_document", split_document)
    graph.add_node("extract_chunks", extract_chunks)
    graph.add_node("reconcile_entities", reconcile_entities)
    graph.add_node("validate_result", validate_result)
    graph.add_edge(START, "load_document")
    graph.add_edge("load_document", "split_document")
    graph.add_edge("split_document", "extract_chunks")
    graph.add_edge("extract_chunks", "reconcile_entities")
    graph.add_edge("reconcile_entities", "validate_result")
    graph.add_edge("validate_result", END)
    return graph.compile()


def extract_document(path: Path, settings: Settings | None = None) -> ExtractionResult:
    final_state = build_graph(settings).invoke({"input_path": str(path)})
    return final_state["result"]
