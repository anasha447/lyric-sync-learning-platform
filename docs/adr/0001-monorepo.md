# One repository for all apps

The backend, identity-service, nlp-service and web app live in one repository, each in its own top-level folder. One developer builds one product whose parts change together (an API change touches backend and web), and one CI with path filters builds only what changed. Separate repositories would only pay off with independent teams and release cycles.
