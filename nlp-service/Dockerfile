# syntax=docker/dockerfile:1

# ---- build: install the locked packages into a virtual environment ----
FROM ghcr.io/astral-sh/uv:0.13.9 AS uv
FROM python:3.13-slim AS build
COPY --from=uv /uv /usr/local/bin/uv
# a long timeout: on a slow network the default restarts the big wheels (spaCy, the model) from zero
ENV UV_COMPILE_BYTECODE=1 UV_LINK_MODE=copy UV_PYTHON_DOWNLOADS=never UV_HTTP_TIMEOUT=600
WORKDIR /app

# packages (and the 42 MB model) change less often than code, so they get their own cached layer.
# --frozen installs straight from uv.lock; CI checks that the lock matches pyproject.toml.
COPY pyproject.toml uv.lock README.md ./
RUN --mount=type=cache,target=/root/.cache/uv uv sync --frozen --no-dev --no-install-project
COPY src ./src
RUN --mount=type=cache,target=/root/.cache/uv uv sync --frozen --no-dev --no-editable

# ---- runtime: Python and the virtual environment only, non-root ----
FROM python:3.13-slim
RUN groupadd --system app && useradd --system --gid app --no-create-home app
USER app
WORKDIR /app
COPY --from=build /app/.venv /app/.venv
ENV PATH="/app/.venv/bin:$PATH" PYTHONUNBUFFERED=1

EXPOSE 8000
HEALTHCHECK --interval=10s --timeout=5s --start-period=30s --retries=6 \
  CMD ["python", "-c", "import urllib.request; urllib.request.urlopen('http://localhost:8000/health', timeout=4)"]
ENTRYPOINT ["uvicorn", "nlp.main:app", "--host", "0.0.0.0", "--port", "8000"]
