import json
from pathlib import Path
from typing import Annotated

import typer
from openai import OpenAIError
from pydantic import ValidationError
from rich.console import Console

from entity_agent.config import Settings
from entity_agent.graph import extract_document

app = typer.Typer(help="Discover entities and map attributes from a PDF document.")
console = Console(stderr=True)


@app.command()
def extract(
    pdf: Annotated[Path, typer.Argument(help="Input PDF file", exists=True, dir_okay=False)],
    output: Annotated[Path | None, typer.Option("--output", "-o", help="Output JSON file")] = None,
) -> None:
    """Extract discovered entities and their correctly nested attributes."""
    try:
        result = extract_document(pdf, Settings())
    except ValidationError as exc:
        details = ", ".join(".".join(map(str, item["loc"])) or item["msg"] for item in exc.errors())
        console.print(f"Extraction failed: invalid configuration: {details}", markup=False)
        raise typer.Exit(code=1) from exc
    except (OpenAIError, ValueError, RuntimeError, OSError) as exc:
        console.print(f"Extraction failed: {type(exc).__name__}: {exc}", markup=False)
        raise typer.Exit(code=1) from exc

    serialized = json.dumps(result.model_dump(mode="json"), indent=2, ensure_ascii=False)
    if output:
        output.parent.mkdir(parents=True, exist_ok=True)
        output.write_text(serialized + "\n", encoding="utf-8")
        console.print(f"Wrote {len(result.entities)} entities to {output}")
    else:
        typer.echo(serialized)


if __name__ == "__main__":
    app()
