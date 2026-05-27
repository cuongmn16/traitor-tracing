-- Schema riêng cho từng microservice (cùng database fingerprint)
CREATE SCHEMA IF NOT EXISTS spring;
CREATE SCHEMA IF NOT EXISTS tracing;

GRANT ALL ON SCHEMA spring TO admin;
GRANT ALL ON SCHEMA tracing TO admin;
