import pytest
from pydantic import ValidationError

from entity_agent.config import Settings


def make_settings(**overrides: object) -> Settings:
    values = {
        "AZURE_OPENAI_API_KEY": "super-secret",
        "AZURE_OPENAI_ENDPOINT": "https://example.openai.azure.com/",
        "AZURE_OPENAI_DEPLOYMENT": "test",
    }
    values.update(overrides)
    return Settings(**values)


def test_api_key_is_redacted() -> None:
    settings = make_settings()

    assert "super-secret" not in repr(settings)
    assert settings.azure_openai_api_key.get_secret_value() == "super-secret"


def test_overlap_cannot_exceed_half_of_chunk_size() -> None:
    with pytest.raises(ValidationError, match="must not exceed half"):
        make_settings(ENTITY_AGENT_CHUNK_SIZE=1000, ENTITY_AGENT_CHUNK_OVERLAP=501)
