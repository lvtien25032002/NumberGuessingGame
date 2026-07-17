# Number Guessing Game Backend API

Spring Boot backend cho game đoán số trực tuyến.

## Công nghệ

- Java 21, Spring Boot, Spring Security
- PostgreSQL + Flyway migration
- JWT (Bearer token)
- Spring Cache (Caffeine) cho leaderboard

## Chạy dự án

1. Cấu hình database PostgreSQL trong `src/main/resources/application.yaml`.
2. Chạy ứng dụng:

```bash
./gradlew bootRun
```

Flyway sẽ tự tạo bảng `users` và index cần thiết.

## API

### Public

- `POST /api/v1/auth/register`
    - Body: `{"username":"string","email":"string","password":"string"}`
    - 201: `{"message":"Đăng ký thành công"}`
- `POST /api/v1/auth/login`
    - Body: `{"username":"string","password":"string"}`
    - 200: `{"token":"...","tokenType":"Bearer"}`

### Authenticated (Bearer Token)

- `POST /api/v1/games/guesses`
    - Body: `{"guess":3}`
    - 200: `{"serverResult":5,"isCorrect":false,"currentScore":12,"remainingTurns":4}`
- `POST /api/v1/users/me/turns`
    - 200: `{"message":"Mua lượt chơi thành công","remainingTurns":9}`
- `GET /api/v1/users/me`
    - 200: `{"email":"user@example.com","score":12,"turns":4}`
- `GET /api/v1/leaderboards`
    - 200: `[{"username":"player1","score":150}]`

## Nghiệp vụ chính

- Tài khoản mới: `score = 0`, `turns = 5`.
- Mỗi lượt đoán hợp lệ trừ đúng `1 turns`.
- Đoán đúng số ngẫu nhiên `[1..5]` thì cộng `1 score`.
- Mỗi lần mua lượt cộng `+5 turns`.
- `/games/guesses` dùng pessimistic locking để tránh race condition khi spam request đồng thời.
- Leaderboard lấy top 10 theo `score DESC, id ASC`, có cache 20 giây.
