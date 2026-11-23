"""The service's contract, in the project's terms (GLOSSARY.md: Line, Token, Lemma)."""

from typing import Annotated

from pydantic import BaseModel, ConfigDict, Field
from pydantic.alias_generators import to_camel

# A lyric line is short; the limits keep one request from tying up the model.
MAX_LINE_CHARS = 500
MAX_LINES = 1000


class CamelModel(BaseModel):
    """JSON in camelCase, like the backend's API."""

    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)


class LineIn(CamelModel):
    index: Annotated[int, Field(ge=0)]
    text: Annotated[str, Field(max_length=MAX_LINE_CHARS)]


class AnalyzeRequest(CamelModel):
    lines: Annotated[list[LineIn], Field(max_length=MAX_LINES)]


class Token(CamelModel):
    """One word occurrence at a fixed position inside a line. Punctuation isn't a token."""

    index: int
    text: str
    lemma: str
    pos: str  # Universal POS tag: NOUN, VERB, ADJ, …


class AnalyzedLine(CamelModel):
    index: int
    tokens: list[Token]


class AnalyzeResponse(CamelModel):
    model: str
    model_version: str
    lines: list[AnalyzedLine]


class Health(CamelModel):
    status: str
    model: str
    model_version: str
