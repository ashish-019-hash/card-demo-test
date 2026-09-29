import json

from typer.testing import CliRunner

from entity_agent.cli import app
from entity_agent.models import Attribute, Entity, Evidence, ExtractionResult


def test_cli_writes_nested_json_to_output(monkeypatch, tmp_path) -> None:
    pdf = tmp_path / "sample.pdf"
    pdf.write_bytes(b"placeholder")
    output = tmp_path / "nested" / "result.json"
    evidence = Evidence(page=1, quote="Account 123")
    result = ExtractionResult(
        document_name="sample.pdf",
        entities=[
            Entity(
                entity_id="account-123",
                entity_type="account",
                name="123",
                confidence=0.9,
                attributes=[
                    Attribute(name="balance", value=42, confidence=0.8, evidence=[evidence])
                ],
                evidence=[evidence],
            )
        ],
    )
    monkeypatch.setattr("entity_agent.cli.Settings", lambda: object())
    monkeypatch.setattr("entity_agent.cli.extract_document", lambda *_args: result)

    response = CliRunner().invoke(app, [str(pdf), "--output", str(output)])

    assert response.exit_code == 0
    payload = json.loads(output.read_text(encoding="utf-8"))
    assert payload["entities"][0]["attributes"][0]["value"] == 42
    assert output.read_bytes().endswith(b"\n")
