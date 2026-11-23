def test_health_reports_the_loaded_model(client):
    response = client.get("/health")
    assert response.status_code == 200
    assert response.json() == {"status": "UP", "model": "de_core_news_md", "modelVersion": "3.8.0"}


def test_analyze_returns_tokens_per_line(client):
    response = client.post(
        "/analyze",
        json={"lines": [{"index": 0, "text": "Ich ging nach Hause"}, {"index": 1, "text": ""}]},
    )
    assert response.status_code == 200
    body = response.json()
    assert body["model"] == "de_core_news_md"
    assert [line["index"] for line in body["lines"]] == [0, 1]
    assert body["lines"][0]["tokens"][1] == {"index": 1, "text": "ging", "lemma": "gehen", "pos": "VERB"}
    assert body["lines"][1]["tokens"] == []


def test_analyze_rejects_a_line_that_is_too_long(client):
    response = client.post("/analyze", json={"lines": [{"index": 0, "text": "la " * 400}]})
    assert response.status_code == 422


def test_analyze_rejects_too_many_lines(client):
    lines = [{"index": i, "text": "la"} for i in range(1001)]
    assert client.post("/analyze", json={"lines": lines}).status_code == 422


def test_analyze_needs_lines(client):
    assert client.post("/analyze", json={}).status_code == 422
