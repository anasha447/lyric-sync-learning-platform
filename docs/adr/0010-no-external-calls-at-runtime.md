# External services are called only during ingestion

Dictionary data comes from the Wiktionary extract published by kaikki.org and is imported into Postgres. Line translations come from the DeepL API Free plan once per line when a song is ingested, and admins can edit them. Learners never trigger a call to an outside service, so running cost does not grow with users and the free quotas are enough. The song API returns lines, tokens and meanings in one response, so tapping a word needs no request.
