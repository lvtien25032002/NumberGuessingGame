# FE - API Test UI

UI đơn giản để test nhanh backend Number Guessing Game.

## Cấu hình

- Mặc định gọi API tại: `http://localhost:8080/api/v1`
- Có thể đổi qua biến môi trường:

```bash
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080/api/v1
```

## Chạy dự án

```bash
npm install
npm run dev
```

Mở: `http://localhost:3000`

## Chức năng có sẵn

- Đăng ký: `POST /auth/register`
- Đăng nhập: `POST /auth/login`
- Đoán số: `POST /games/guess`
- Mua lượt: `POST /users/me/buy-turns`
- Hồ sơ: `GET /users/me`
- Bảng xếp hạng: `GET /leaderboards`
