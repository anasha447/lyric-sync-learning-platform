# Launch on Docker Compose, move to Kubernetes after launch

Version 1 runs on a single Hetzner VPS with Docker Compose: Traefik with Let's Encrypt in front, images from GHCR, and deploys from GitHub Actions over SSH. Single-node k3s with Argo CD and CloudNativePG follows as a stretch goal once the product is live. A working public demo matters more than the platform under it, and Kubernetes work must not delay it. The apps are built as twelve-factor containers, so the move is a packaging change, not a rewrite.
