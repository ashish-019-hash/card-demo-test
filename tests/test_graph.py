from pathlib import Path

from entity_agent.config import Settings
from entity_agent.graph import build_graph
from entity_agent.models import Attribute, ChunkExtraction, Entity, Evidence, PageText


class FakeStructuredModel:
    def __init__(self, responses: list[ChunkExtraction]) -> None:
        self.responses = responses

    def invoke(self, _messages: object) -> ChunkExtraction:
        return self.responses.pop(0)


class FakeModel:
    def __init__(self, responses: list[ChunkExtraction]) -> None:
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
