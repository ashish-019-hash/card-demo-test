from pydantic import BaseModel, Field


class Evidence(BaseModel):
    page: int = Field(ge=1)
    quote: str = Field(min_length=1, max_length=500)


class Attribute(BaseModel):
    name: str = Field(min_length=1)
    value: str | int | float | bool | None
    confidence: float = Field(ge=0.0, le=1.0)
    evidence: list[Evidence] = Field(default_factory=list)


class Entity(BaseModel):
    entity_id: str = Field(min_length=1, description="Stable identifier unique in this result")
    entity_type: str = Field(min_length=1, description="Discovered business concept type")
    name: str = Field(min_length=1, description="Human-readable identity from the document")
    confidence: float = Field(ge=0.0, le=1.0)
    attributes: list[Attribute] = Field(default_factory=list)
    evidence: list[Evidence] = Field(default_factory=list)


class ExtractionResult(BaseModel):
    document_name: str = ""
    entities: list[Entity] = Field(default_factory=list)
    warnings: list[str] = Field(default_factory=list)


class ChunkExtraction(BaseModel):
    entities: list[Entity] = Field(default_factory=list)


class PageText(BaseModel):
    page: int
    text: str
    extraction_method: str
