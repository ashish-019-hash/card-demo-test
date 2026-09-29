from pydantic import Field, SecretStr, model_validator
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    """Runtime settings loaded from environment variables or a local .env file."""

    model_config = SettingsConfigDict(env_file=".env", extra="ignore")

    azure_openai_api_key: SecretStr = Field(alias="AZURE_OPENAI_API_KEY")
    azure_openai_endpoint: str = Field(alias="AZURE_OPENAI_ENDPOINT")
    azure_openai_deployment: str = Field(alias="AZURE_OPENAI_DEPLOYMENT")
    azure_openai_api_version: str = Field(default="2024-10-21", alias="AZURE_OPENAI_API_VERSION")
    chunk_size: int = Field(default=12_000, alias="ENTITY_AGENT_CHUNK_SIZE", ge=1000)
    chunk_overlap: int = Field(default=800, alias="ENTITY_AGENT_CHUNK_OVERLAP", ge=0)
    ocr_language: str = Field(default="eng", alias="ENTITY_AGENT_OCR_LANGUAGE")
    min_text_chars_per_page: int = Field(
        default=40, alias="ENTITY_AGENT_MIN_TEXT_CHARS_PER_PAGE", ge=0
    )

    @model_validator(mode="after")
    def validate_chunk_window(self) -> "Settings":
        if self.chunk_overlap > self.chunk_size // 2:
            raise ValueError(
                "ENTITY_AGENT_CHUNK_OVERLAP must not exceed half of ENTITY_AGENT_CHUNK_SIZE"
            )
        return self
