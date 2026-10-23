# Guess Number Game

A Java 21 / Spring Boot game API with JWT authentication, MySQL persistence, Redis caching, a React frontend, and PayPal Sandbox purchases.

## Implementation status / Trạng thái so với đề

| Requirement / Yêu cầu | Status / Trạng thái |
|---|---|
| Registration and login / Đăng ký, đăng nhập | Implemented with BCrypt and JWT. / Đã làm với BCrypt và JWT. |
| Guess 1-5, consume one turn, update score / Đoán 1-5, trừ lượt, cập nhật điểm | Implemented, with a gameplay rule difference noted below. / Đã làm, nhưng có khác biệt luật chơi được ghi bên dưới. |
| Buy 5 turns / Mua 5 lượt | PayPal Sandbox purchase flow implemented. / Đã làm luồng mua qua PayPal Sandbox. |
| Top 10 leaderboard / Bảng xếp hạng top 10 | Implemented with score ordering and Redis cache. / Đã làm, sắp xếp theo điểm và cache Redis. |
| Current user profile / Hồ sơ người dùng | Implemented at `/api/users/me`, Redis cached. / Đã làm tại `/api/users/me`, có Redis cache. |
| Concurrent guesses for one user / Nhiều lượt đoán đồng thời của cùng user | Serialized by a MySQL pessimistic row lock inside a transaction. / Tuần tự hóa bằng khóa ghi trên hàng MySQL trong transaction. |
| Fast `/leaderboard` and `/me` under high load / Tăng tốc `/leaderboard` và `/me` khi tải cao | Redis caching, cache invalidation, and a benchmark script are present. There is no recorded load-test result, so high-load performance is not yet proven. / Đã có Redis cache, invalidation và script benchmark. Chưa có kết quả tải được ghi nhận nên chưa thể khẳng định hiệu năng tải cao. |

### Gameplay rule to confirm / Luật chơi cần xác nhận

The assignment describes choosing a random secret number from 1 to 5, awarding `score + 1` when the guess matches, and suggests a 5% win probability. Current code follows the earlier requested variation: it rolls a 5% win independently of the guess, awards 10 points on a win, and makes the revealed secret equal the guess on a win. If strict assignment compliance is required, change this rule to compare the guess with a random secret and award one point for a match (retaining a 5% chance requires a separate, explicit probability design).

Đề mô tả server chọn số bí mật ngẫu nhiên từ 1 đến 5, đoán đúng thì `score + 1`, đồng thời gợi ý xác suất thắng 5%. Code hiện theo biến thể đã yêu cầu trước đó: quay xác suất thắng 5% độc lập với số đoán, cộng 10 điểm khi thắng và công bố số bí mật bằng số người chơi đoán. Nếu cần bám sát đề tuyệt đối, hãy đổi luật thành so sánh với số bí mật ngẫu nhiên và cộng 1 điểm khi trùng (nếu vẫn giữ xác suất 5%, cần định nghĩa rõ cách kết hợp hai luật).

## Technology / Công nghệ

- Backend: Java 21, Spring Boot 3.5, Spring Web, Spring Security, Spring Data JPA, Spring Data Redis, Bean Validation.
- Database: MySQL (XAMPP is suitable for local development).
- Cache and concurrency: Redis cache; MySQL transactions with pessimistic row locking and JPA optimistic versioning.
- Authentication: JWT (JJWT) and BCrypt password hashing.
- Payments: PayPal Orders v2 REST API, Sandbox, server-side capture and database-backed fulfillment.
- Local secrets: `springboot3-dotenv` reads a backend `.env` file.
- Frontend: React, TypeScript, Vite, Axios, lucide-react.

## Repository layout / Cấu trúc

- `guess-number-game/` — Spring Boot backend, Maven Wrapper, API documentation, Redis Compose file, benchmark script.
- `guess-number-game-frontend/` — React + TypeScript frontend.

## Requirements / Yêu cầu môi trường

- JDK 21.
- Node.js 20.19+ or 22.12+.
- MySQL running locally (XAMPP defaults: `localhost:3306`, user `root`, blank password).
- Docker Desktop for the included Redis service, or another Redis server on port 6379.
- PayPal Developer Sandbox REST app credentials to complete a sandbox purchase.

## Configuration / Cấu hình

Create `guess-number-game/.env` beside its `pom.xml`. Copy `guess-number-game/.env.example` and fill in the PayPal REST app Client ID and Secret. The dotenv integration loads it when Spring Boot is launched with the backend directory as the working directory. The real `.env` is ignored by Git; never commit or share it.

Tạo `guess-number-game/.env` cạnh `pom.xml`, sao chép từ `guess-number-game/.env.example` rồi điền Client ID và Secret của PayPal REST app. Thư viện dotenv tự nạp file khi chạy Spring Boot với thư mục backend làm working directory. File `.env` thật được Git bỏ qua; không commit hoặc chia sẻ file này.

```dotenv
PAYPAL_CLIENT_ID=your-sandbox-client-id
PAYPAL_CLIENT_SECRET=your-sandbox-client-secret
PAYPAL_TURNS_PRICE=1.00
PAYPAL_TURNS_AMOUNT=5
PAYPAL_CURRENCY=USD
```

The backend also accepts `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `REDIS_HOST`, `REDIS_PORT`, `JWT_SECRET`, `JWT_EXPIRATION_MS`, `SERVER_PORT`, and `LEADERBOARD_CACHE_TTL`. Defaults are for local development only. Use a private JWT secret outside development.

Backend còn nhận các biến `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `REDIS_HOST`, `REDIS_PORT`, `JWT_SECRET`, `JWT_EXPIRATION_MS`, `SERVER_PORT` và `LEADERBOARD_CACHE_TTL`. Giá trị mặc định chỉ dành cho phát triển local. Khi triển khai, hãy đặt JWT secret riêng.

## Run locally / Chạy local

Start MySQL in XAMPP. Then start Redis and the backend in PowerShell:

Bật MySQL trong XAMPP. Sau đó bật Redis và backend trong PowerShell:

```powershell
cd E:\1javatest\baiTest\guess-number-game\guess-number-game
docker compose up -d redis
.\mvnw.cmd spring-boot:run
```

In a second terminal, start the frontend:

Mở terminal thứ hai và chạy frontend:

```powershell
cd E:\1javatest\baiTest\guess-number-game\guess-number-game-frontend
npm install
npm run dev
```

Open `http://localhost:5173`. Vite proxies `/api` requests to `http://localhost:8080`.

Mở `http://localhost:5173`. Vite chuyển tiếp request `/api` tới `http://localhost:8080`.

## Build / Đóng gói

```powershell
# Backend
cd E:\1javatest\baiTest\guess-number-game\guess-number-game
.\mvnw.cmd -DskipTests package

# Frontend
cd E:\1javatest\baiTest\guess-number-game\guess-number-game-frontend
npm run build
npm run lint
```

## API quick start / Thử API nhanh

All API paths below include the `/api` prefix. Register and login are public. All other APIs require a bearer JWT except the public leaderboard.

Các API bên dưới đều có tiền tố `/api`. Đăng ký và đăng nhập là public. Các API còn lại cần JWT Bearer, ngoại trừ leaderboard public.

### 1. Register / Đăng ký

```http
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{"username":"player1","email":"player1@example.com","password":"password123"}
```

### 2. Login / Đăng nhập

```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{"username":"player1","password":"password123"}
```

Copy `accessToken` from the response. In Postman, select **Authorization → Bearer Token** and paste it for protected endpoints.

Sao chép `accessToken` trong response. Trong Postman, chọn **Authorization → Bearer Token** rồi dán token cho các API cần đăng nhập.

### 3. Profile / Hồ sơ hiện tại

```http
GET http://localhost:8080/api/users/me
Authorization: Bearer <accessToken>
```

Returns id, username, email, score, and remaining turns. Password hash is not exposed.

Trả về id, username, email, score và số lượt còn lại; không trả password hash.

### 4. Buy turns / Mua lượt

```http
GET http://localhost:8080/api/payments/paypal/packages

POST http://localhost:8080/api/payments/paypal/orders
Authorization: Bearer <accessToken>
Content-Type: application/json

{"packageId":"turns-5"}
```

Open `approvalUrl` from the order response and approve with a PayPal Sandbox **Personal buyer** account. The frontend then calls the capture API. The backend grants turns only after PayPal reports a completed capture and the amount, currency, and order reference match the stored order. Duplicate capture does not grant turns twice.

Mở `approvalUrl` trong response và duyệt bằng tài khoản **Personal buyer** của PayPal Sandbox. Frontend sẽ gọi capture sau khi quay về. Backend chỉ cộng lượt khi PayPal xác nhận capture hoàn tất và số tiền, tiền tệ, mã đơn khớp với đơn đã lưu. Gọi capture trùng không cộng lượt lần hai.

### 5. Guess / Đoán số

```http
POST http://localhost:8080/api/game/guess
Authorization: Bearer <accessToken>
Content-Type: application/json

{"guessedNumber":3}
```

A valid guess must be from 1 to 5 and consumes one turn. A new account starts with zero turns; buy a package first.

Số đoán hợp lệ từ 1 đến 5 và mỗi request hợp lệ dùng một lượt. Tài khoản mới có 0 lượt nên cần mua gói trước.

### 6. Leaderboard / Bảng xếp hạng

```http
GET http://localhost:8080/api/game/leaderboard
```

Returns up to 10 users ordered by score descending. This endpoint is public.

Trả tối đa 10 user, sắp xếp điểm giảm dần. API này không cần đăng nhập.

## Design notes / Giải thích thiết kế

- **Same-user concurrency / Đồng thời cùng user:** `findByUsernameForUpdate` locks the user row with `PESSIMISTIC_WRITE`; guess changes and turn deduction run in one database transaction. Requests for the same user serialize, preventing two requests from spending the last turn. Different users can proceed independently, subject to MySQL capacity.
- **Read latency / Độ trễ đọc:** Redis caches `/api/users/me` by username and the top-10 leaderboard for five seconds. After a committed user update, an event listener evicts that user's profile cache; score changes also evict the leaderboard. The database has a score index for the top-10 query.
- **Performance evidence / Bằng chứng hiệu năng:** `guess-number-game/scripts/benchmark_leaderboard.py` supports repeatable leaderboard throughput and latency measurements. Run it against representative data and compare Redis on/off before making a high-load claim. It does not by itself demonstrate production capacity.
- **Caching limits / Giới hạn cache:** Redis is a runtime dependency for the configured Redis cache. The short TTL bounds staleness, but deployments with strict consistency or very high cache-write volume may need a more robust invalidation/event strategy.
- **Payment safety / An toàn thanh toán:** The browser never supplies the price or number of turns. The backend snapshots package price and turns into a database order, verifies PayPal's capture and grants turns transactionally once per order.

## Known gaps / Điểm cần hoàn thiện

1. Align the gameplay rule with the assignment if strict grading expects a random secret comparison and `+1` score; current code uses the earlier 5% / `+10` variation.
2. Run and save an actual concurrency test for simultaneous guesses by one user, and a load benchmark for `/me` and `/leaderboard`. Current locking/cache design is implemented, but evidence is not included yet.
3. Configure production-grade secret management, database migrations, monitoring, Redis availability/failover, and PayPal webhooks before a production deployment. Current PayPal flow relies on the browser returning to request capture; a webhook can improve recovery when the user does not return to the site.

## License and scope

This is a local assessment project and PayPal Sandbox integration. It is not a production payment configuration.
