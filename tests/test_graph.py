from pathlib import Path

import httpx
import pytest
from openai import AuthenticationError

from entity_agent.config import Settings
from entity_agent.graph import build_graph
from entity_agent.models import Attribute, ChunkExtraction, Entity, Evidence, PageText


class FakeStructuredModel:
    def __init__(self, responses: list[ChunkExtraction | Exception]) -> None:
        self.responses = responses

    def invoke(self, _messages: object) -> ChunkExtraction:
        response = self.responses.pop(0)
        if isinstance(response, Exception):
            raise response
        return response


class FakeModel:
    def __init__(self, responses: list[ChunkExtraction | Exception]) -> None:
        self.responses = responses

    def with_structured_output(self, _schema: object) -> FakeStructuredModel:
        return FakeStructuredModel(self.responses)


def test_graph_reconciles_attributes_under_their_entity(monkeypatch) -> None:
    evidence = Evidence(page=1, quote="Customer Acme, account 123")
    candidate = Entity(
        entity_id="customer-acme",
        entity_type="customer",
        name="Acme",
        confidence=0.95,
        attributes=[
            Attribute(name="account_number", value="123", confidence=0.98, evidence=[evidence])
        ],
        evidence=[evidence],
    )
    responses = [ChunkExtraction(entities=[candidate]), ChunkExtraction(entities=[candidate])]
    settings = Settings(
        AZURE_OPENAI_API_KEY="test",
        AZURE_OPENAI_ENDPOINT="https://example.openai.azure.com/",
        AZURE_OPENAI_DEPLOYMENT="test",
        ENTITY_AGENT_CHUNK_SIZE=1000,
        ENTITY_AGENT_CHUNK_OVERLAP=100,
    )
    monkeypatch.setattr(
        "entity_agent.graph.extract_pdf_pages",
        lambda *_args, **_kwargs: (
            [PageText(page=1, text="Customer Acme, account 123", extraction_method="native")],
            [],
        ),
    )

    graph = build_graph(settings, model_factory=lambda _settings: FakeModel(responses))
    state = graph.invoke({"input_path": str(Path("sample.pdf"))})

    result = state["result"]
    assert result.document_name == "sample.pdf"
    assert result.entities[0].attributes[0].name == "account_number"
    assert result.entities[0].attributes[0].value == "123"


def test_graph_fails_when_all_chunks_fail(monkeypatch) -> None:
    settings = Settings(
        AZURE_OPENAI_API_KEY="test",
        AZURE_OPENAI_ENDPOINT="https://example.openai.azure.com/",
        AZURE_OPENAI_DEPLOYMENT="test",
    )
    monkeypatch.setattr(
        "entity_agent.graph.extract_pdf_pages",
        lambda *_args, **_kwargs: (
            [PageText(page=1, text="Customer Acme", extraction_method="native")],
            [],
        ),
    )
    graph = build_graph(settings, model_factory=lambda _settings: FakeModel([ValueError("bad")]))

    try:
        graph.invoke({"input_path": "sample.pdf"})
    except RuntimeError as exc:
        assert "every document chunk" in str(exc)
    else:
        raise AssertionError("Expected all-chunk failure")


def test_graph_keeps_candidates_when_reconciliation_fails(monkeypatch) -> None:
    evidence = Evidence(page=1, quote="Customer Acme")
    candidate = Entity(
        entity_id="customer-acme",
        entity_type="customer",
        name="Acme",
        confidence=0.9,
        evidence=[evidence],
    )
    settings = Settings(
        AZURE_OPENAI_API_KEY="test",
        AZURE_OPENAI_ENDPOINT="https://example.openai.azure.com/",
        AZURE_OPENAI_DEPLOYMENT="test",
    )
    monkeypatch.setattr(
        "entity_agent.graph.extract_pdf_pages",
        lambda *_args, **_kwargs: (
            [PageText(page=1, text="Customer Acme", extraction_method="native")],
            [],
        ),
    )
    responses: list[ChunkExtraction | Exception] = [
        ChunkExtraction(entities=[candidate]),
        ValueError("bad reconciliation"),
    ]
    graph = build_graph(settings, model_factory=lambda _settings: FakeModel(responses))

    result = graph.invoke({"input_path": "sample.pdf"})["result"]

    assert result.entities == [candidate]
    assert any("kept raw candidates" in warning for warning in result.warnings)


def test_graph_propagates_fatal_azure_errors(monkeypatch) -> None:
    settings = Settings(
        AZURE_OPENAI_API_KEY="test",
        AZURE_OPENAI_ENDPOINT="https://example.openai.azure.com/",
        AZURE_OPENAI_DEPLOYMENT="test",
    )
    monkeypatch.setattr(
        "entity_agent.graph.extract_pdf_pages",
        lambda *_args, **_kwargs: (
            [PageText(page=1, text="Customer Acme", extraction_method="native")],
            [],
        ),
    )
    response = httpx.Response(401, request=httpx.Request("POST", "https://example.com"))
    error = AuthenticationError("bad credentials", response=response, body=None)
    graph = build_graph(settings, model_factory=lambda _settings: FakeModel([error]))

    with pytest.raises(AuthenticationError):
        graph.invoke({"input_path": "sample.pdf"})


def test_graph_renames_duplicate_ids_and_warns_for_bad_evidence(monkeypatch) -> None:
    settings = Settings(
        AZURE_OPENAI_API_KEY="test",
        AZURE_OPENAI_ENDPOINT="https://example.openai.azure.com/",
        AZURE_OPENAI_DEPLOYMENT="test",
    )
    valid = Evidence(page=1, quote="Customer Acme")
    invalid = Evidence(page=9, quote="Invented quote")
    first = Entity(
        entity_id="customer",
        entity_type="customer",
        name="Acme",
        confidence=0.9,
        evidence=[valid],
    )
    second = Entity(
        entity_id="customer",
        entity_type="customer",
        name="Beta",
        confidence=0.8,
        evidence=[invalid],
    )
    monkeypatch.setattr(
        "entity_agent.graph.extract_pdf_pages",
        lambda *_args, **_kwargs: (
            [PageText(page=1, text="Customer Acme and Customer Beta", extraction_method="native")],
            [],
        ),
    )
    responses = [
        ChunkExtraction(entities=[first, second]),
        ChunkExtraction(entities=[first, second]),
    ]
    graph = build_graph(settings, model_factory=lambda _settings: FakeModel(responses))

    result = graph.invoke({"input_path": "sample.pdf"})["result"]

    assert [entity.entity_id for entity in result.entities] == ["customer", "customer-2"]
    assert any("unverified evidence" in warning for warning in result.warnings)
