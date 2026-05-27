-- Bảng thuộc Python tracing service (schema tracing)
CREATE TABLE IF NOT EXISTS tracing.sift_vectors (
    image_id        UUID PRIMARY KEY,
    original_w      INTEGER NOT NULL,
    original_h      INTEGER NOT NULL,
    vector_path     TEXT NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_sift_vectors_created_at
    ON tracing.sift_vectors (created_at DESC);
