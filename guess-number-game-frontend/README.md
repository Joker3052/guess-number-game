# Lucky Five frontend

Frontend cho Guess Number Game, xĂ¢y dá»±ng báº±ng React, TypeScript vĂ  Vite.

## YĂªu cáº§u

- Node.js 20.19+ hoáº·c 22.12+ (Ä‘Ă£ kiá»ƒm tra vá»›i Node 24.14.1)
- Backend Spring Boot cháº¡y táº¡i `http://localhost:8080`

## Cháº¡y local

```powershell
npm install
npm run dev
```

Má»Ÿ URL Vite in ra (máº·c Ä‘á»‹nh `http://localhost:5173`). Vite chuyá»ƒn tiáº¿p cĂ¡c request `/api/*` Ä‘áº¿n Spring Boot táº¡i `http://localhost:8080`, vĂ¬ váº­y khĂ´ng cáº§n báº­t CORS riĂªng khi phĂ¡t triá»ƒn local.

## TĂ­nh nÄƒng hiá»‡n cĂ³

- ÄÄƒng kĂ½ vĂ  Ä‘Äƒng nháº­p báº±ng cĂ¡c endpoint `/api/auth/register` vĂ  `/api/auth/login`.
- LÆ°u JWT trong `localStorage`, tá»± gáº¯n Bearer token cho request.
- Hiá»ƒn thá»‹ há»“ sÆ¡ qua `/api/users/me`.
- Chá»n sá»‘ 1â€“5, chÆ¡i qua `/api/game/guess`, xem káº¿t quáº£ vĂ  Ä‘iá»ƒm/lÆ°á»£t cáº­p nháº­t.
- Buy lượt chơi qua PayPal Sandbox; lượt chỉ được cộng sau khi PayPal xác nhận thanh toán.
- Xem vĂ  táº£i láº¡i top 10 tá»« `/api/game/leaderboard`.
- Loading state, thĂ´ng bĂ¡o lá»—i/thĂ nh cĂ´ng, vĂ  bá»‘ cá»¥c responsive cho mĂ n hĂ¬nh nhá».

## PayPal Sandbox

Backend cần `PAYPAL_CLIENT_ID` và `PAYPAL_CLIENT_SECRET` của REST app trong PayPal Developer Dashboard. Tài khoản Personal sandbox dùng để đăng nhập và duyệt thanh toán khi checkout; không đặt email/mật khẩu buyer trong cấu hình ứng dụng.

## Build

```powershell
npm run build
```

CĂ¡c dependency Ä‘Æ°á»£c khĂ³a trong `package-lock.json` Ä‘á»ƒ cĂ³ thá»ƒ cĂ i láº¡i cĂ¹ng phiĂªn báº£n.


