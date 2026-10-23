# Guess Number Game API

Spring Boot 3.5 + Java 21 + MySQL project scaffold for the Java test.

## Requirements

- JDK 21
- XAMPP with MySQL running on port 3306 (or another MySQL instance)
- Redis running on port 6379 (Docker Compose or a local Redis installation)

Maven Wrapper is included; a separate Maven installation is not required.

## Database configuration

The default configuration connects to `localhost:3306`, database `guess_number_game`, user `root`, blank password. The database is created on connection when the MySQL account permits it.

Override defaults with environment variables when needed:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `SERVER_PORT`


### Local environment file

The backend loads `.env` automatically. Put it in this backend folder beside `pom.xml`; the actual `.env` is ignored by Git. Start Spring Boot from this folder with `./mvnw.cmd spring-boot:run`—no `$env:...` commands are needed. Use `.env.example` as a template. Get the Client ID and Secret from a PayPal Developer Sandbox REST app; the sandbox buyer email/password are entered on PayPal's approval page.
## Run

From this directory in PowerShell:

```powershell
# Skip this if Redis is already running on localhost:6379.
docker compose up -d redis
./mvnw.cmd spring-boot:run
```

The leaderboard and user profile are cached in Redis for five seconds by default. Set `REDIS_HOST`, `REDIS_PORT`, or `LEADERBOARD_CACHE_TTL` to override these values. Profile cache entries are invalidated after turns or score change; the leaderboard cache is invalidated after registration or score change. These invalidations run after the MySQL transaction commits, and the TTL bounds staleness if an invalidation is missed.

### Compare leaderboard throughput

Keep MySQL and Redis running. Start the application on port 8081 once with Redis caching enabled, then run:

```powershell
python .\scripts\benchmark_leaderboard.py --url http://localhost:8081/api/game/leaderboard --requests 2000 --concurrency 50
```

Stop the application, restart it on the same port with caching disabled, and run the exact same command to compare. In PowerShell, set `$env:SPRING_CACHE_TYPE = "none"` before starting the second run. The script warms the endpoint first and reports throughput plus p50/p95/p99 latency. Use a populated database and repeat runs for a meaningful comparison; a small local dataset does not establish production-scale performance.

Registration and login are public endpoints. All other endpoints require a valid JWT.

### Register

```http
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{"username":"player1","email":"player1@example.com","password":"password123"}
```

### Login

```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{"username":"player1","password":"password123"}
```

Register returns HTTP 200 with a success message. Login returns HTTP 200 with `userId`, `username`, `tokenType` and `accessToken`; invalid credentials return HTTP 400.

For protected endpoints in Postman, open the Authorization tab, choose Bearer Token, and paste the `accessToken` returned by login. Tokens expire after one hour by default. The leaderboard is public and does not require a token.

### Get my profile

```http
GET http://localhost:8080/api/users/me
Authorization: Bearer <accessToken>
```

Returns `id`, `username`, `email`, `score` and `turns`. The password hash is never included.

### Buy turns with PayPal Sandbox

Read the package with `GET /api/payments/paypal/packages`, then create an order:

```http
POST http://localhost:8080/api/payments/paypal/orders
Authorization: Bearer <accessToken>
Content-Type: application/json

{"packageId":"turns-5"}
```

Open the returned `approvalUrl` and approve it using a PayPal Sandbox Personal buyer account. The frontend captures the approved order, and the backend adds turns only after PayPal confirms capture. Repeated capture requests do not award duplicate turns.

Store credentials in the backend `.env` file beside `pom.xml` using `PAYPAL_CLIENT_ID` and `PAYPAL_CLIENT_SECRET`; use `.env.example` as a template. The dotenv integration loads it automatically when Spring Boot starts from the backend directory. Never commit the real `.env` or share its secret.
The default package is 5 turns for 1.00 USD. Override it with `PAYPAL_TURNS_AMOUNT`, `PAYPAL_TURNS_PRICE`, or `PAYPAL_CURRENCY`. Keep the client secret out of source control.
### Leaderboard

```http
GET http://localhost:8080/api/game/leaderboard
```

Returns up to ten users ordered by score descending.

### Play one round

The user is identified from the JWT, so do not include a `userId` in the request body. A newly registered account starts with zero turns, so first purchase a package through PayPal Sandbox.

```http
POST http://localhost:8080/api/game/guess
Content-Type: application/json

{"guessedNumber":3}
```

The response includes `won`, `guessedNumber`, `secretNumber`, `remainingTurns` and `currentScore`. Each valid game request consumes one turn. A win is rolled at 5%, awards 10 points, and reveals the guessed number as the secret number; on a loss the secret number is a random different value from 1 to 5.

Set `JWT_SECRET` to a private secret of at least 32 characters before deploying. The configured default secret is only for local development.

## Current data model

`users` stores username, email, password hash, score, turns and an optimistic-lock version. New users start with zero score and zero turns. Passwords are hashed with BCrypt before saving.




