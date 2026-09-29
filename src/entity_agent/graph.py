import json
from collections.abc import Callable
from pathlib import Path
from typing import TypedDict

from langchain_openai import AzureChatOpenAI
from langgraph.graph import END, START, StateGraph

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
    entities: list[Entity]
    warnings: list[str]
    result: ExtractionResult


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
    extractor = model.with_structured_output(ChunkExtraction)
    reconciler = model.with_structured_output(ChunkExtraction)

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
        for index, chunk in enumerate(state["chunks"], start=1):
            response = extractor.invoke(
                [
                    ("system", EXTRACTION_SYSTEM_PROMPT),
                    ("human", f"Extract entities from chunk {index}:\n\n{chunk}"),
                ]
            )
            entities.extend(response.entities)
        return {"chunk_results": entities}

    def reconcile_entities(state: AgentState) -> AgentState:
        candidates = state.get("chunk_results", [])
        if not candidates:
            return {"entities": []}
        payload = json.dumps([entity.model_dump(mode="json") for entity in candidates])
        response = reconciler.invoke(
            [
                ("system", RECONCILIATION_SYSTEM_PROMPT),
                ("human", f"Reconcile these candidates from one document:\n\n{payload}"),
            ]
        )
        return {"entities": response.entities}

    def validate_result(state: AgentState) -> AgentState:
        warnings = list(state.get("warnings", []))
        unique_ids: set[str] = set()
        valid_entities: list[Entity] = []
        for entity in state.get("entities", []):
            if entity.entity_id in unique_ids:
                warnings.append(f"Removed duplicate entity id: {entity.entity_id}")
                continue
            unique_ids.add(entity.entity_id)
            if not entity.evidence and not any(item.evidence for item in entity.attributes):
                warnings.append(f"Entity {entity.entity_id} has no source evidence.")
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
