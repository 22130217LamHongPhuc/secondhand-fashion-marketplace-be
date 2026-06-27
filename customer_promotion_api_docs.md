# Tài liệu hướng dẫn API Khuyến mãi dành cho User (Customer)

Tài liệu này giải thích chi tiết 3 API dùng để xử lý các nghiệp vụ liên quan đến khuyến mãi (Promotion) dành cho người dùng cuối (Customer) khi họ muốn xem và lưu mã giảm giá từ các cửa hàng.

---

## 1. API Lấy danh sách khuyến mãi của một Cửa hàng

**Mô tả:** 
API này cho phép người mua xem tất cả các mã khuyến mãi (voucher) khả dụng đang được áp dụng tại một cửa hàng (Shop) cụ thể. Người mua thường gọi API này khi họ truy cập vào trang chi tiết của một cửa hàng hoặc khi đang xem các sản phẩm của cửa hàng đó.

- **URL:** `/api/v1/promotions/shops/{shopId}`
- **Method:** `GET`
- **Auth required:** Không bắt buộc / Có thể yêu cầu (tùy thuộc cấu hình Security)
- **Path Variables:**
  - `shopId` (Long): ID của cửa hàng cần lấy danh sách khuyến mãi.
- **Query Parameters (Pagination):**
  - `page` (int, mặc định `0`): Trang hiện tại cần lấy.
  - `size` (int, mặc định `10`): Số lượng khuyến mãi tối đa trên một trang.
- **Dữ liệu trả về (Response):** 
  Trả về một đối tượng phân trang `Page<Promotion>` chứa danh sách các khuyến mãi khả dụng (ví dụ: đang Active, còn số lượng, trong thời hạn...).

---

## 2. API Lưu/Nhận (Claim) Khuyến mãi vào Ví

**Mô tả:** 
Khi người mua thấy một mã khuyến mãi hấp dẫn từ cửa hàng, họ có thể nhấn nút "Lưu" (Claim). API này sẽ gắn mã khuyến mãi đó vào tài khoản của người dùng (tạo một bản ghi `UserPromotion`), cho phép họ sử dụng mã này trong quá trình thanh toán sau này.

- **URL:** `/api/v1/promotions/{promotionId}/claim`
- **Method:** `POST`
- **Auth required:** Bắt buộc (Yêu cầu phải đăng nhập)
- **Path Variables:**
  - `promotionId` (Long): ID của khuyến mãi mà người dùng muốn lưu.
- **Headers:** 
  - `Authorization: Bearer <JWT_TOKEN>` (Token đăng nhập của người dùng).
- **Dữ liệu trả về (Response):** 
  - Trả về HTTP Status `201 CREATED`.
  - Body trả về đối tượng `UserPromotion` (thông tin liên kết giữa User và Promotion).
- **Lưu ý nghiệp vụ:** Backend sẽ tự động lấy thông tin người dùng từ token (`@AuthenticationPrincipal User user`) để biết ai là người đang thực hiện lưu mã.

---

## 3. API Xem Ví Voucher cá nhân (My Wallet)

**Mô tả:** 
API này giúp người mua xem lại toàn bộ các mã khuyến mãi mà họ đã lưu (claim) thành công trước đó. Giao diện frontend sẽ sử dụng API này để hiển thị màn hình "Ví Voucher" (My Wallet) của người dùng, giúp họ quản lý những mã chưa dùng hoặc đã dùng.

- **URL:** `/api/v1/promotions/my-wallet`
- **Method:** `GET`
- **Auth required:** Bắt buộc (Yêu cầu phải đăng nhập)
- **Headers:** 
  - `Authorization: Bearer <JWT_TOKEN>`
- **Query Parameters (Pagination):**
  - `page` (int, mặc định `0`): Trang hiện tại.
  - `size` (int, mặc định `10`): Số lượng voucher tối đa trên một trang.
- **Dữ liệu trả về (Response):** 
  Trả về một đối tượng phân trang `Page<UserPromotion>` chứa danh sách các voucher có trong ví của người dùng đang đăng nhập.
- **Lưu ý nghiệp vụ:** Tương tự API Claim, Backend tự nhận diện người dùng thông qua Token.
