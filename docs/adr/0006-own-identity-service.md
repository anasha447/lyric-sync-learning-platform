# Own identity service issuing RS256 JWTs

Sign-up, login, email verification and password reset are handled by a small Spring Boot identity-service. It issues short-lived RS256 access tokens and rotating refresh tokens, both in httpOnly cookies, and publishes its public keys as JWKS. The backend only verifies tokens. Keycloak was rejected because it needs far more memory than the whole backend and adds a large surface for a simple email-and-password MVP. Google login is planned after launch.
