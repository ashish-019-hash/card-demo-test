from typing import Any

from pydantic import BaseModel, Field, model_validator


class Evidence(BaseModel):
    page: int = Field(ge=1)
    quote: str = Field(min_length=1, max_length=500)


class Attribute(BaseModel):
    name: str = Field(min_length=1)
    value: Any
    confidence: float = Field(ge=0.0, le=1.0)
    evidence: list[Evidence] = Field(default_factory=list)


class Entity(BaseModel):
    entity_id: str = Field(min_length=1, description="Stable identifier unique in this result")
    entity_type: str = Field(min_length=1, description="Discovered business concept type")
    name: str = Field(min_length=1, description="Human-readable identity from the document")
    confidence: float = Field(ge=0.0, le=1.0)
    attributes: list[Attribute] = Field(default_factory=list)
    evidence: list[Evidence] = Field(default_factory=list)

    @model_validator(mode="after")
    def unique_attribute_names(self) -> "Entity":
        seen: set[str] = set()
        unique: list[Attribute] = []
        for attribute in self.attributes:
            key = attribute.name.strip().casefold()
            if key not in seen:
                seen.add(key)
                unique.append(attribute)
        self.attributes = unique
        return self


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
