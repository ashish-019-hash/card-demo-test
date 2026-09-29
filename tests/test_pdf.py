from entity_agent.models import PageText
from entity_agent.pdf import chunk_pages


def test_chunk_pages_keeps_page_markers_and_overlap() -> None:
    pages = [PageText(page=1, text="A" * 900, extraction_method="native")]

    chunks = chunk_pages(pages, chunk_size=600, overlap=100)

    assert len(chunks) == 2
    assert chunks[0].startswith("[PAGE 1]")
    assert chunks[0][-100:] == chunks[1].removeprefix("[PAGE 1]\n")[:100]


def test_chunk_pages_returns_empty_for_blank_pages() -> None:
    pages = [PageText(page=1, text="", extraction_method="native")]

    assert chunk_pages(pages, chunk_size=1000, overlap=100) == []


def test_chunk_pages_makes_progress_with_large_overlap() -> None:
    pages = [PageText(page=1, text="a" * 590 + "\n" + "b" * 2000, extraction_method="native")]

    chunks = chunk_pages(pages, chunk_size=1000, overlap=800)

    assert 1 < len(chunks) < 20
    assert all(chunk.startswith("[PAGE 1]\n") for chunk in chunks)
