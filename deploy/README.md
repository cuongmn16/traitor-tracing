# Triển khai gateway (Cách 3)

## Yêu cầu

- Docker & Docker Compose
- Đã build Angular: `cd ../font-end && npm install && npm run build`

## Database — hai schema

| Schema | Service | Nội dung |
|--------|---------|----------|
| `spring` | Spring Boot | User, ảnh, download, trace log |
| `tracing` | Python | `sift_vectors` (metadata SIFT) |

Init SQL: `postgres/init/`. Lần đầu chạy Postgres container sẽ tạo schema.

**Nếu đã có DB cũ (bảng ở `public`):** xóa volume `pgdata` hoặc migrate thủ công sang schema `spring`.

```bash
docker compose down -v   # xóa volume DB (mất dữ liệu cũ)
```

## Chạy

```bash
cd deploy
cp .env.example .env
# Sửa DB_PASSWORD, JWT_SIGNER_KEY, INTERNAL_SERVICE_KEY trong .env

docker compose up -d --build
```

Truy cập: http://localhost  
API Spring: http://localhost/api/...  
API Python (qua gateway): http://localhost/api/tracing/health  

## Dev local (không Docker)

1. PostgreSQL port `5434`, DB `fingerprint`
2. Python: `cd traiter_tracing-python && pip install -r requirements.txt && uvicorn main:app --port 8000`
3. Spring: `cd traitor-tracing && mvn spring-boot:run`
4. Angular: `cd font-end && ng serve` (dùng `proxy.conf.json`)

## Tài khoản admin mặc định

- username: `admin`
- password: `admin` (tạo lúc khởi động Spring lần đầu)
