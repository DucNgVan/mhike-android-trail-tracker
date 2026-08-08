# 🌲 M-Hike: Backend Architecture & Implementation Guide

Tài liệu này trình bày chi tiết về cấu trúc tầng Backend và logic xử lý dữ liệu của ứng dụng M-Hike. Đây là tài liệu kỹ thuật cốt lõi phục vụ cho việc báo cáo và giải trình coursework.

---

## ☁️ 1. Công nghệ sử dụng (Technology Stack)

Ứng dụng được xây dựng trên nền tảng **Cloud-Native**, sử dụng hệ sinh thái **Google Firebase** để quản lý toàn bộ vòng đời dữ liệu:

*   **Firebase Authentication**: Quản lý định danh người dùng (Email/Password).
*   **Cloud Firestore**: Cơ sở dữ liệu NoSQL dạng văn bản (Document-oriented) hỗ trợ đồng bộ thời gian thực.
*   **Google Services Plugin**: Cầu nối giao tiếp giữa ứng dụng Android và hạ tầng Google Cloud.

---

## 📊 2. Cấu trúc dữ liệu (Data Modeling)

Vì Firestore là database NoSQL, dữ liệu được tổ chức theo các **Collections** (Bộ sưu tập) và **Documents** (Tài liệu).

### A. Collection: `hikes`
Mỗi tài liệu trong đây đại diện cho một chuyến đi bộ.
| Field | Type | Description |
| :--- | :--- | :--- |
| `name` | String | Tên chuyến đi (Primary Identifier) |
| `location` | String | Địa danh diễn ra chuyến đi |
| `date` | String | Ngày thực hiện (Định dạng dd/MM/yyyy) |
| `difficulty` | String | Mức độ (Easy, Medium, Hard) |
| `length` | String | Độ dài quãng đường (km) |
| `weather` | String | Điều kiện thời tiết (Sunny, Cloudy, v.v.) |
| `imageResName`| String | Tên resource ảnh trong app (img1 -> img5) |
| `firebaseId` | String | ID duy nhất do Cloud cấp (Dùng để liên kết dữ liệu) |

### B. Collection: `observations`
Mỗi tài liệu là một ghi chú cụ thể trong một chuyến đi.
| Field | Type | Description |
| :--- | :--- | :--- |
| `observation` | String | Nội dung quan sát được |
| `time` | String | Thời điểm ghi nhận (HH:mm) |
| `comments` | String | Bình luận bổ sung |
| `hikeFirebaseId`| String | **Foreign Key**: Liên kết trực tiếp tới ID của một Hike |

---

## ⚙️ 3. Logic xử lý Backend cốt lõi (Core Logic)

### 🔄 Đồng bộ thời gian thực (Real-time Sync)
Thay vì sử dụng phương thức `get()` thông thường, ứng dụng sử dụng `addSnapshotListener` trong lớp `FirestoreHelper.java`.
*   **Cơ chế**: Thiết lập một "đường ống" dữ liệu liên tục giữa app và server.
*   **Lợi ích**: Khi dữ liệu trên server thay đổi (do người dùng khác nhập hoặc sửa trên Console), giao diện người dùng sẽ tự động cập nhật ngay lập tức mà không cần tắt app mở lại.

### 🛡️ Cơ chế Xác thực (Authentication Flow)
1.  **Firebase Auth**: Sử dụng phương thức `createUserWithEmailAndPassword` để đăng ký người dùng thật trên Cloud.
2.  **Debug Bypass**: Để thuận tiện cho việc chấm bài, ứng dụng tích hợp một mật khẩu đặc quyền (`admin123`). Nếu nhập mật khẩu này, hệ thống sẽ bỏ qua bước xác thực Firebase để đảm bảo giảng viên luôn vào được app ngay cả khi mất mạng.

### 🏗️ Khởi tạo dữ liệu tự động (Auto-initialization)
Để ứng dụng không bị trống (Empty State) khi nộp bài, hàm `loadHikes()` trong `HomeFragment` có logic:
```java
if (hikes.isEmpty()) {
    uploadSampleData(); // Đẩy 4 bản ghi mẫu lên Cloud
}
```
Việc này đảm bảo tính thẩm mỹ và đầy đủ cho bài demo.

---

## 🏗️ 4. Quy trình vận hành Backend (Backend Workflow)

Hệ thống xử lý dữ liệu thông qua lớp trung gian `FirestoreHelper.java` để đảm bảo tính đóng gói (Encapsulation).

### 📥 A. Lấy dữ liệu (Read/Get Data)
*   **Hàm xử lý**: `getAllHikes(final DataStatus status)`
*   **Vị trí gọi**: `HomeFragment` và `SearchFragment`.
*   **Cơ chế**:
    1.  App gửi yêu cầu tới Firestore Collection `hikes`.
    2.  Hệ thống sử dụng `.orderBy("name")` để sắp xếp danh sách.
    3.  `.addSnapshotListener` được thiết lập để duy trì kết nối. Mỗi khi có bất kỳ thay đổi nào trên Cloud, hàm `onEvent` sẽ được kích hoạt để cập nhật `RecyclerView` trên điện thoại ngay lập tức.

### 📤 B. Thêm dữ liệu (Create/Insert Data)
*   **Hàm xử lý**: `insertHike(Hike hike, final DataStatus status)`
*   **Vị trí gọi**: `AddHikeActivity`.
*   **Cơ chế**:
    1.  Người dùng hoàn tất form và nhấn "Save".
    2.  Dữ liệu được đóng gói vào đối tượng `Hike`.
    3.  Lệnh `.add(hike)` gửi đối tượng này lên Firestore. Cloud sẽ tự động sinh một mã băm (Hash ID) duy nhất cho bản ghi này.

### 🗑️ C. Xóa dữ liệu (Delete Data)
*   **Hàm xử lý**: `deleteHike(String id, final DataStatus status)`
*   **Vị trí gọi**: `HikeDetailActivity`.
*   **Cơ chế**:
    1.  App lấy `firebaseId` của chuyến đi đang xem.
    2.  Lệnh `.document(id).delete()` thực hiện lệnh xóa trực tiếp trên server. Danh sách ở trang Home sẽ tự động biến mất do cơ chế Real-time Sync đã nêu ở mục A.

### 📝 D. Liên kết Observation (Sub-data Workflow)
*   **Hàm xử lý**: `getObservationsForHike(String hikeId, ...)`
*   **Cơ chế**: Sử dụng lệnh `.whereEqualTo("hikeFirebaseId", hikeId)` để lọc ra các ghi chú chỉ thuộc về chuyến đi đang xem.

---

## 🔒 5. Cấu hình bảo mật (Security Rules)

Để ứng dụng có thể đọc/ghi dữ liệu, quy tắc bảo mật (Security Rules) trên Firebase Console được thiết lập như sau:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if true;
    }
  }
}
```
*Lưu ý: Trong môi trường sản xuất, quy tắc này cần được siết chặt bằng `request.auth != null`.*

---

## 🚀 5. Hướng dẫn triển khai Backend cho Giảng viên

1.  Dự án đã được tích hợp sẵn tệp `google-services.json` với Project ID `hikingapp-81d90`.
2.  Hệ thống Database đặt tại vùng: **asia-southeast1 (Singapore)** để tối ưu tốc độ tại Việt Nam.
3.  Thư viện Gradle: Sử dụng `firebase-bom:34.16.0` để tự động quản lý phiên bản các SDK.

---
*Tài liệu được soạn thảo chuyên biệt cho dự án Coursework M-Hike - 2025.*
# hiking_androi_app_coursework
# HikingAndroiApplication_COMP1785_GRE
