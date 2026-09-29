import pymupdf
import pytesseract
import pytest

from entity_agent.pdf import extract_pdf_pages


def test_extracts_native_pdf_pages(tmp_path) -> None:
    path = tmp_path / "native.pdf"
    with pymupdf.open() as document:
        page = document.new_page()
        page.insert_text((72, 72), "Customer Acme account 123")
        document.save(path)

    pages, warnings = extract_pdf_pages(path, min_text_chars=5)

    assert warnings == []
    assert pages[0].page == 1
    assert pages[0].extraction_method == "native"
    assert "Customer Acme" in pages[0].text


def test_reports_missing_tesseract_for_textless_page(monkeypatch, tmp_path) -> None:
    path = tmp_path / "scan.pdf"
    with pymupdf.open() as document:
        document.new_page()
        document.save(path)
    monkeypatch.setattr(
        pytesseract,
        "image_to_string",
        lambda *_args, **_kwargs: (_ for _ in ()).throw(pytesseract.TesseractNotFoundError()),
    )

    with pytest.raises(RuntimeError, match="Tesseract is required"):
        extract_pdf_pages(path, min_text_chars=40)
