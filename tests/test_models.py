from entity_agent.models import Attribute, Entity


def test_entity_removes_duplicate_attribute_names() -> None:
    entity = Entity(
        entity_id="customer-acme",
        entity_type="customer",
        name="Acme",
        confidence=0.9,
        attributes=[
            Attribute(name="account_number", value="1", confidence=0.9),
            Attribute(name="Account_Number", value="2", confidence=0.7),
        ],
    )

    assert len(entity.attributes) == 1
    assert entity.attributes[0].value == "1"
