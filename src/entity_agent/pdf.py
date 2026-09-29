from pathlib import Path

import pymupdf
import pytesseract
from PIL import Image

from entity_agent.models import PageText


def extract_pdf_pages(
    path: Path, *, ocr_language: str = "eng", min_text_chars: int = 40
) -> tuple[list[PageText], list[str]]:
    """Extract native text and use OCR for pages with little or no embedded text."""
    if path.suffix.lower() != ".pdf":
        raise ValueError("Only PDF input is supported.")
    if not path.is_file():
        raise FileNotFoundError(path)

    pages: list[PageText] = []
    warnings: list[str] = []
    with pymupdf.open(path) as document:
        if document.page_count == 0:
            raise ValueError("The PDF has no pages.")
        for index, page in enumerate(document):
            text = page.get_text("text").strip()
            method = "native"
            if len(text) < min_text_chars:
                try:
                    pixmap = page.get_pixmap(dpi=300, alpha=False)
                    image = Image.frombytes("RGB", (pixmap.width, pixmap.height), pixmap.samples)
                    ocr_text = pytesseract.image_to_string(image, lang=ocr_language).strip()
                    if len(ocr_text) > len(text):
                        text = ocr_text
                        method = "ocr"
                except pytesseract.TesseractNotFoundError as exc:
                    raise RuntimeError(
                        "Tesseract is required for scanned PDF pages but was not found."
                    ) from exc
            if not text:
                warnings.append(f"Page {index + 1} contains no extractable text.")
            pages.append(PageText(page=index + 1, text=text, extraction_method=method))
    return pages, warnings


def chunk_pages(pages: list[PageText], chunk_size: int, overlap: int) -> list[str]:
    """Create bounded chunks while preserving page markers for evidence references."""
    if overlap >= chunk_size:
        raise ValueError("Chunk overlap must be smaller than chunk size.")
    chunks: list[str] = []
    for page in pages:
        if not page.text:
            continue
        marker = f"[PAGE {page.page}]\n"
        available = chunk_size - len(marker)
        if available <= overlap:
            raise ValueError("Chunk size is too small for page markers and overlap.")
        start = 0
        while start < len(page.text):
            end = min(start + available, len(page.text))
            if end < len(page.text):
                boundary = page.text.rfind("\n", start + available // 2, end)
                if boundary > start and boundary - overlap > start:
                    end = boundary
            chunks.append(marker + page.text[start:end])
            if end == len(page.text):
                break
            start = max(end - overlap, start + 1)
    return chunks
