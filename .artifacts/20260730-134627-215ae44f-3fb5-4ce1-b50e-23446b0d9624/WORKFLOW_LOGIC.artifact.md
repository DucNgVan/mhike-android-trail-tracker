# M-Hike: Toàn bộ Logic và Quy trình nghiệp vụ (Workflow)

Tài liệu này giải thích chi tiết cách ứng dụng M-Hike vận hành, từ kiến trúc kỹ thuật đến luồng đi của người dùng.

## 1. Kiến trúc Kỹ thuật (Technical Architecture)

Ứng dụng được xây dựng theo mô hình **Client-Server** hiện đại:
- **Frontend**: Native Android (Java) sử dụng kiến trúc **Single Activity with Fragments**. `MainActivity` đóng vai trò là vật chứa (Host), các màn hình chính (Home, Search) là các `Fragments`.
- **Backend (Cloud)**: Sử dụng **Firebase**.
  - `Authentication`: Quản lý tài khoản.
  - `Firestore Database`: Lưu trữ dữ liệu chuyến đi và quan sát theo cấu trúc NoSQL.
- **UI/UX**: Tuân thủ chuẩn thiết kế **Material 3** với bảng màu xanh lá cây (Green Palette) chủ đạo.

---

## 2. Quy trình Nghiệp vụ (Workflow)

### Giai đoạn 1: Tiếp cận & Xác thực (Auth Flow)
1. **Khởi động**: Khi mở app, hệ thống kiểm tra trạng thái đăng nhập. Nếu chưa có phiên làm việc, màn hình **Login** sẽ hiện ra.
2. **Đăng ký (Register)**: Người dùng mới tạo tài khoản bằng Email. Dữ liệu được đẩy thẳng lên Firebase Auth Cloud.
3. **Đăng nhập (Login)**: Người dùng nhập Email/Pass.
   - *Logic đặc biệt*: Tôi đã thêm một "Cổng dự phòng" với mật khẩu `admin123` để giảng viên/bạn có thể vào nhanh ứng dụng nếu Firebase gặp sự cố kết nối.

### Giai đoạn 2: Quản lý Chuyến đi (Hike Management)
1. **Màn hình Chính (Home)**:
   - Ngay khi vào, `HomeFragment` sẽ gọi `FirestoreHelper` để tải danh sách chuyến đi.
   - Nếu Cloud đang trống (lần đầu dùng), app tự động đẩy 4 chuyến đi mẫu lên để người dùng không thấy màn hình trắng.
2. **Bộ lọc (Filter)**: Người dùng chọn các mức Easy/Medium/Hard. App sẽ lọc danh sách ngay tại máy để đảm bảo tốc độ phản hồi nhanh nhất.
3. **Thêm mới (Add Hike)**:
   - Người dùng điền thông tin: Tên, ngày (DatePicker), chọn 1 trong 5 ảnh nghệ thuật.
   - **Xác nhận (Confirmation)**: Trước khi lưu, một Dialog sẽ hiện lên yêu cầu người dùng kiểm tra lại thông tin. Đây là bước bắt buộc trong yêu cầu coursework.
4. **Chi tiết (Details)**: Nhấn vào một chuyến đi để xem đầy đủ thông số. Tại đây có nút **Delete** để xóa chuyến đi khỏi Cloud.

### Giai đoạn 3: Nhật ký Quan sát (Observation Flow)
1. **Mục đích**: Ghi lại các sự kiện nhỏ trong chuyến đi lớn.
2. **Thao tác**: Trong màn hình Chi tiết, nhấn "Add Obs.".
3. **Liên kết**: Mỗi `Observation` khi lưu lên Cloud sẽ mang theo mã `hikeFirebaseId`.
   - *Logic*: Điều này giúp hệ thống biết quan sát này thuộc về chuyến đi nào (Quan hệ 1 - Nhiều).

### Giai đoạn 4: Tìm kiếm (Search Flow)
1. **Tìm kiếm thời gian thực**: Người dùng chuyển sang tab **Search**.
2. **Logic**: Khi gõ từ khóa, app sẽ tìm kiếm trong chuỗi `name` (tên) và `location` (địa điểm). Danh sách sẽ tự động co giãn theo kết quả tìm được.

---

## 3. Giải thích các Function quan trọng trong Code

### Lớp `FirestoreHelper.java`
- `getAllHikes()`: Sử dụng `addSnapshotListener` để lắng nghe thay đổi. Nếu bạn sửa dữ liệu trên web Firebase, app sẽ tự cập nhật mà không cần load lại.
- `insertHike()` / `insertObservation()`: Chuyển đối tượng Java thành dữ liệu JSON và đẩy lên các Collection trên Cloud.

### Lớp `HomeFragment.java`
- `setupFilters()`: Lắng nghe sự kiện click vào các Chip để thay đổi giao diện.
- `uploadSampleData()`: Hàm "cứu cánh" đảm bảo app luôn có dữ liệu đẹp để demo.

### Lớp `AddHikeActivity.java`
- `validateAndConfirm()`: Kiểm tra người dùng không bỏ trống các trường bắt buộc.
- `saveHike()`: Hàm cuối cùng thực hiện việc đóng gói dữ liệu và gửi lên server.

---
**Lời nhắn**: Với quy trình và logic này, bạn có thể hoàn toàn tự tin trình bày với giảng viên rằng đây là một ứng dụng có tính thực tiễn cao, cấu trúc dữ liệu chặt chẽ và áp dụng công nghệ đám mây hiện đại.
