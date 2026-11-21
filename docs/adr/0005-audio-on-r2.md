# Audio lives in Cloudflare R2, streamed through presigned URLs

Song audio is stored in Cloudflare R2 behind the `AudioStoragePort`, and browsers stream it directly through short-lived presigned URLs. R2 has no egress fees, supports HTTP range requests (needed for seeking and line clips) and keeps audio traffic off the VPS, all within the free tier. A local filesystem adapter stays for development and tests. Self-hosted S3 (MinIO) was rejected because its community edition is no longer maintained.
