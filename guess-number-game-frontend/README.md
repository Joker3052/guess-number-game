# Lucky Five frontend

Frontend cho Guess Number Game, xây dựng bằng React, TypeScript và Vite.

## Yêu cầu

- Node.js 20.19+ hoặc 22.12+ (đã kiểm tra với Node 24.14.1)
- Backend Spring Boot chạy tại `http://localhost:8080`

## Chạy local

```powershell
npm install
npm run dev
```

Mở URL Vite in ra (mặc định `http://localhost:5173`). Vite chuyển tiếp các request `/api/*` đến Spring Boot tại `http://localhost:8080`, vì vậy không cần bật CORS riêng khi phát triển local.

## Tính năng hiện có

- Đăng ký và đăng nhập bằng các endpoint `/api/auth/register` và `/api/auth/login`.
- Lưu JWT trong `localStorage`, tự gắn Bearer token cho request.
- Hiển thị hồ sơ qua `/api/users/me`.
- Chọn số 1–5, chơi qua `/api/game/guess`, xem kết quả và điểm/lượt cập nhật.
- Nhận thêm 5 lượt qua `/api/game/buy-turns`.
- Xem và tải lại top 10 từ `/api/game/leaderboard`.
- Loading state, thông báo lỗi/thành công, và bố cục responsive cho màn hình nhỏ.

## Build

```powershell
npm run build
```

Các dependency được khóa trong `package-lock.json` để có thể cài lại cùng phiên bản.
