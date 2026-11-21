# Traefik at the edge instead of Spring Cloud Gateway

Traefik routes `/api/auth/*` to identity-service, `/api/*` to the backend and everything else to the web app. It also terminates TLS and applies rate limits and security headers. It is the default ingress of k3s, so the same setup carries over to Kubernetes. A Spring Cloud Gateway would be one more JVM doing what the ingress already does, and JWT checks happen in each service anyway.
