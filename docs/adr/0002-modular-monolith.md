# Modular monolith plus two separate services

The backend is one Spring Boot application split into Spring Modulith modules (catalog, dictionary, learning). Identity and NLP run as separate services. Everything runs on one 8 GB VPS, and four JVMs plus spaCy, Postgres and RabbitMQ would not fit comfortably. Saving a word and scheduling its first review must also be one transaction, which a single process gives us for free.

Identity is separate because it is a security boundary with its own lifecycle that other apps could reuse. NLP is separate because it needs Python and spaCy and only runs during ingestion.

## Consequences

- Each module owns its own Postgres schema. No foreign keys cross schemas; other modules hold plain ids.
- Modules call each other only through a module's public API (queries) or through events (state changes). `ApplicationModules.verify()` fails the build otherwise.
- Learning reads word and line data live through the catalog and dictionary APIs instead of copying it, so admin fixes show up in reviews immediately.
- Any module can later be extracted into its own service along these seams.

## Considered Options

- **Microservices for identity, content and learning plus a gateway:** rejected because of RAM, distributed transactions and a large ops cost for one developer.
- **Everything in one app including identity:** rejected because it would mix security concerns into the learning app.
