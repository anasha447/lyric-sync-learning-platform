# nlp-service

Analyses German lyric lines with spaCy (`de_core_news_md`): each line's tokens with their lemma and part of speech. Separable verbs, offsets and morphology arrive in NLP-4.2; the RabbitMQ pipeline in BE-4.4.

The spaCy model is installed from spaCy's own GitHub release (pinned by URL in `pyproject.toml`, hash in `uv.lock`), not from PyPI, where spaCy's models aren't published (the `de-core-news-md` there is an empty 999.9.9 stub against dependency confusion).

## Run

```bash
uv sync                 # install the locked packages
uv run pytest           # tests (with the real model)
uv run ruff check .     # lint
uv run uvicorn nlp.main:app --reload   # http://localhost:8000/docs
```

## API

| Method & path | What it does |
|---|---|
| `GET /health` | `{"status": "UP", "model": "de_core_news_md", "modelVersion": "3.8.0"}` once the model is loaded |
| `POST /analyze` | `{"lines": [{"index": 0, "text": "Ich ging nach Hause"}]}` → each line's tokens: `{"index", "text", "lemma", "pos"}` |

Lines are at most 500 characters, at most 1000 per request. Punctuation isn't a token.
