# LangGraph PDF Entity Agent

This CLI application discovers business entities in unstructured PDFs. It maps each extracted
attribute to its owning entity and returns evidence, page references, and confidence scores.

## Workflow

The LangGraph workflow uses five nodes:

1. `load_document` extracts embedded PDF text and applies OCR to low-text pages.
2. `split_document` creates overlapping chunks with page markers.
3. `extract_chunks` discovers entities and nests attributes under each entity.
4. `reconcile_entities` merges cross-chunk duplicates and corrects ambiguous mappings.
5. `validate_result` removes duplicate identifiers and reports missing evidence.

Attributes cannot exist outside an entity in the output model. Each attribute contains its value,
confidence score, and source evidence. The reconciliation prompt merges entities only when their
type and identity match.

## Prerequisites

- Python 3.11 or later
- An Azure OpenAI resource with a chat model deployment that supports structured output
- Tesseract OCR for scanned PDFs

Install Tesseract on Ubuntu or Debian:

```bash
sudo apt-get update
sudo apt-get install -y tesseract-ocr
```

Install Tesseract on macOS:

```bash
brew install tesseract
```

## Local setup

Create and activate a virtual environment:

```bash
python3.11 -m venv .venv
source .venv/bin/activate
```

Install the application and development tools:

```bash
python -m pip install --upgrade pip
python -m pip install -e '.[dev]'
```

Create the local configuration:

```bash
cp .env.example .env
```

Set these required values in `.env`:

```dotenv
AZURE_OPENAI_API_KEY=your-key
AZURE_OPENAI_ENDPOINT=https://your-resource.openai.azure.com/
AZURE_OPENAI_DEPLOYMENT=your-chat-deployment
AZURE_OPENAI_API_VERSION=2024-10-21
```

Do not commit `.env`. The file is excluded by `.gitignore`.

## Run the CLI

Write the result to standard output:

```bash
extract-entities document.pdf
```

Write the result to a JSON file:

```bash
extract-entities document.pdf --output output/entities.json
```

You can also run the module directly:

```bash
python -m entity_agent.cli document.pdf -o output/entities.json
```

## Output structure

```json
{
  "document_name": "document.pdf",
  "entities": [
    {
      "entity_id": "customer-acme-corp",
      "entity_type": "customer",
      "name": "Acme Corp",
      "confidence": 0.97,
      "attributes": [
        {
          "name": "account_number",
          "value": "12345",
          "confidence": 0.99,
          "evidence": [{"page": 2, "quote": "Account number: 12345"}]
        }
      ],
      "evidence": [{"page": 2, "quote": "Customer: Acme Corp"}]
    }
  ],
  "warnings": []
}
```

## Tests and quality checks

Run unit tests without Azure credentials:

```bash
pytest
```

Run lint checks:

```bash
ruff check .
ruff format --check .
```

## Reset the local environment

```bash
deactivate 2>/dev/null || true
rm -rf .venv .pytest_cache .ruff_cache .coverage htmlcov output
```

This command does not remove `.env`. Remove `.env` separately if you need to clear credentials.

## Troubleshooting

- `TesseractNotFoundError`: Install Tesseract and confirm that `tesseract --version` works.
- Azure `401`: Check `AZURE_OPENAI_API_KEY` and `AZURE_OPENAI_ENDPOINT`.
- Azure `404`: Check the deployment name and API version.
- Empty results: Confirm that the PDF contains readable text or a clear 300-DPI scan.
- Incorrect mapping: Inspect each attribute's evidence. Low confidence indicates ambiguous context.
- Large-document latency: Increase `ENTITY_AGENT_CHUNK_SIZE` within the model context limit, or use a faster Azure deployment.
