# Number Guessing Game Backend API
Spring Boot backend cho game đoán số trực tuyến và UI đơn giản để test API.

## Công nghệ
- Java 21, Spring Boot, Spring Security
- PostgreSQL + Flyway migration
- JWT (Bearer token)
- Spring Cache (Caffeine) cho leaderboard
- Next.JS để build giao diện đơn giản
- Dùng axios để gọi API
- VNPay Payment

## Chạy dự án
1. Cần chạy Docker Desktop
2. cd vào thư mục root dự án sau đó chạy lệnh: "docker-compose up -d" để init docker image
3. Run API server bằng 1 trong 2 cách:
   1: Chạy Spring Boot bằng intelliJ 
   2: cd folder be trong VSC and run "./gradlew bootRun" in cmd 
Flyway sẽ tự tạo bảng `users` và index cần thiết.
4. cd folder fe sau đó run cmd "npm run dev" để chạy UI

## PaymentInfo để mock VNPay
<img width="619" height="123" alt="image" src="https://github.com/user-attachments/assets/97fc9048-8d0c-4a62-8d7d-12d89e3bdc2f" />

## API
### Public
- `POST /api/v1/auth/register`
    - Body: `{"username":"string","email":"string","password":"string"}`
    - 201: `{"message":"Đăng ký thành công"}`
- `POST /api/v1/auth/login`
    - Body: `{"username":"string","password":"string"}`
    - 200: `{"token":"...","tokenType":"Bearer"}`
- `GET /api/v1/leaderboards`
    - 200: `[{"username":"player1","score":150}]`
- `GET /api/v1/payment/vnpay-callback`
### Authenticated (Bearer Token)
- `POST /api/v1/games/guesses`
    - Body: `{"guess":3}`
    - 200: `{"serverResult":5,"isCorrect":false,"currentScore":12,"remainingTurns":4}`
- `POST /api/v1/users/me/turns`
    - 200: `{"message":"Mua lượt chơi thành công","remainingTurns":9}`
- `GET /api/v1/users/me`
    - 200: `{"email":"user@example.com","score":12,"turns":4}`
- `POST /api/v1/payment/create`
    - 200: `{"paymentUrl":"url....","code":"00", "message":"success"}`

## Nghiệp vụ chính
- Tài khoản mới: `score = 0`, `turns = 5`.
- Mỗi lượt đoán hợp lệ trừ đúng `1 turns`.
- Đoán đúng số ngẫu nhiên `[1..5]` thì cộng `1 score`.
- Mỗi lần mua lượt cộng `+5 turns` thông qua VNPay.
- `/games/guess` dùng pessimistic locking để tránh race condition khi spam request đồng thời.
- Leaderboard lấy top 10 theo `score DESC, id ASC`, có cache 20 giây.

## UI 
<img width="1036" height="915" alt="image" src="https://github.com/user-attachments/assets/ba27f788-186b-4ebb-946a-7c605eba10e5" />

