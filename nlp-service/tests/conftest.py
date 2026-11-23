import pytest
import spacy
from fastapi.testclient import TestClient
from spacy.language import Language

from nlp.main import app


@pytest.fixture(scope="session")
def german() -> Language:
    """The real model, loaded once: the tests check what spaCy actually returns for German lyrics."""
    return spacy.load("de_core_news_md")


@pytest.fixture(scope="session")
def client():
    # entering the client runs the app's lifespan, which loads the model
    with TestClient(app) as test_client:
        yield test_client
