"""HTTP entry point. In the ingestion pipeline (BE-4.4) the same analysis runs from RabbitMQ;
`/analyze` is for tests and debugging; Traefik has no route to it."""

from collections.abc import AsyncIterator
from contextlib import asynccontextmanager
from typing import Annotated

import spacy
from fastapi import Depends, FastAPI, Request
from spacy.language import Language

from nlp.analysis import analyse_lines
from nlp.models import AnalyzeRequest, AnalyzeResponse, Health

MODEL = "de_core_news_md"


@asynccontextmanager
async def lifespan(app: FastAPI) -> AsyncIterator[None]:
    # Loading takes a few seconds and ~150 MB: once per process, never per request.
    app.state.nlp = spacy.load(MODEL)
    yield


app = FastAPI(title="lyricsync nlp-service", version="0.1.0", lifespan=lifespan)


def get_nlp(request: Request) -> Language:
    return request.app.state.nlp


NlpDep = Annotated[Language, Depends(get_nlp)]


@app.get("/health")
def health(nlp: NlpDep) -> Health:
    return Health(status="UP", model=MODEL, model_version=nlp.meta["version"])


# A plain `def`: spaCy is CPU-bound, so FastAPI runs it in its thread pool, not on the event loop.
@app.post("/analyze")
def analyze(body: AnalyzeRequest, nlp: NlpDep) -> AnalyzeResponse:
    return AnalyzeResponse(
        model=MODEL, model_version=nlp.meta["version"], lines=analyse_lines(nlp, body.lines)
    )
