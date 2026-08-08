# TÀI LIỆU THIẾT KẾ GIAO DIỆN VÀ TƯƠNG TÁC UX/UI (DESIGN & UI/UX INTERACTION)

## 1. NGUYÊN TẮC THIẾT KẾ NỀN TẢNG (DESIGN PRINCIPLES)

Giao diện ứng dụng M-Hike được thiết kế tuân thủ theo **Material Design 3 (Material You)** của Google, hướng tới trải nghiệm người dùng mượt mà, hiện đại và sang trọng.

### 1.1 Nguyên tắc thiết kế chính
1. **Visual Hierarchy (Phân cấp thị giác rõ ràng):** 
   - Sử dụng font chữ tiêu chuẩn, thẻ Card nâng cao (Elevated Cards) và màu sắc tương phản giúp mắt người dùng dễ dàng định vị thông tin quan trọng như Tên chuyến đi, Địa điểm và Nút bấm hành động.
2. **Consistency (Tính nhất quán):**
   - Mọi nút bấm, ô nhập dữ liệu (TextInputLayout), thẻ Card và biểu tượng (Icons) đều dùng chung một bộ giao diện quy chuẩn trong `styles.xml` và `colors.xml`.
3. **Feedback & Affordance (Phản hồi tương tác tức thì):**
   - Mọi hành động của người dùng (Chạm bản đồ, Bấm nút GPS, Lưu chuyến đi, Xóa ghi chú) đều có phản hồi trực quan bằng Toast, hiệu ứng ghim nhảy hoặc Dialog xác nhận.

---

## 2. QUY CHUẨN ĐỘ TƯƠNG THÍCH MÀN HÌNH (SCREEN COMPATIBILITY)

Ứng dụng đảm bảo hiển thị hoàn hảo trên hàng ngàn dòng máy Android với nhiều kích thước màn hình khác nhau (từ màn hình nhỏ 4.7 inch tới Tablet / Màn hình gập) thông qua các quy chuẩn:

1. **Đơn vị đo lường tương thích:**
   - **`dp` (Density-independent Pixels):** Được sử dụng cho toàn bộ kích thước phần tử (`layout_width`, `layout_height`), khoảng cách margins (`layout_margin`) và padding (`padding`).
   - **`sp` (Scale-independent Pixels):** Được sử dụng duy nhất cho kích thước chữ (`textSize`), giúp phông chữ tự động điều chỉnh theo cài đặt khả năng truy cập (Accessibility font size) của người dùng.
2. **Đồ họa Vector linh hoạt (Vector Drawables):**
   - Sử dụng biểu tượng dạng `<vector>` XML thay vì ảnh bitmap cố định, giúp biểu tượng hiển thị sắc nét 100% trên màn hình HD, Full HD, 2K và 4K mà không bị vỡ nét hay tăng dung lượng app.
3. **Bố cục linh hoạt (ConstraintLayout & CoordinatorLayout):**
   - Sử dụng `ConstraintLayout` và `CoordinatorLayout` để tự động giãn trải giao diện theo tỷ lệ màn hình thực tế của thiết bị.

---

## 3. HỆ THỐNG PHÔNG CHỮ VÀ PHÂN CẤP TYPOGRAPHY (TYPOGRAPHY HIERARCHY)

Ứng dụng tuân theo hệ thống phân cấp font chữ tiêu chuẩn Material 3:

| Loại Văn Bản | Kích Thước (`sp`) | Kiểu Chữ (`textStyle`) | Mục Đích Sử Dụng |
| :--- | :--- | :--- | :--- |
| **Header Title** | `22sp` - `24sp` | `bold` | Tiêu đề màn hình chính (M-Hike, Plan New Hike) |
| **Section Title** | `16sp` - `18sp` | `bold` | Tiêu đề các vùng thông tin (Trip Details, Observations) |
| **Card Title** | `16sp` | `bold` | Tên chuyến đi trong phần tử danh sách (Hike Name) |
| **Sub-title / Location**| `14sp` | `normal` | Vị trí địa lý, Tên tác giả |
| **Body Text** | `14sp` | `normal` | Nội dung mô tả chi tiết, Ghi chú quan sát |
| **Caption / Helper** | `12sp` | `bold` / `normal` | Nhãn trợ giúp bản đồ (*Tap map to pin location*), Ngày tháng |
| **Button Text** | `14sp` | `bold` | Chữ hiển thị trên các nút hành động |

---

## 4. QUY CHUẨN KHOẢNG CÁCH VÀ KÍCH THƯỚC (SPACING & ELEVATION GRID)

Toàn bộ khoảng cách trong ứng dụng tuân thủ theo **Hệ thống Lưới 8dp (8dp Rhythm Grid System)**:

```
[8dp] -> [12dp] -> [16dp] -> [24dp] -> [32dp]
```

### 4.1 Chi tiết khoảng cách (Margins & Paddings)
- **Screen Margin (Lề trang):** `24dp` (Khoảng cách từ mép màn hình vào nội dung chính).
- **Element Spacing (Khoảng cách giữa các ô nhập):** `16dp` đến `24dp`.
- **Card Content Padding (Khoảng trống trong thẻ Card):** `16dp`.
- **Badge Padding (Khoảng trống nhãn bản đồ):** `12dp` (ngang) x `6dp` (dọc).

### 4.2 Kích thước các thẻ Card và Vùng giao diện chính
- **Card Góc bo (`cardCornerRadius`):** 
  - Thẻ Chuyến đi (Hike Card Item): `16dp`.
  - Thẻ Bản đồ Preview Card: `20dp`.
  - Dialog xác nhận: `24dp`.
- **Độ nổi của Card (`cardElevation`):** `2dp` - `4dp` (Tạo độ sâu thị giác mượt mà với bóng mờ tự nhiên).
- **Chiều cao bản đồ (`addHikeMapView`):** `240dp` (Đủ rộng để thao tác vuốt rê bản đồ mà không chiếm hết màn hình).
- **Chiều cao nút bấm chính (`MaterialButton`):** `56dp` (Kích thước vùng chạm tiêu chuẩn cho ngón tay người dùng - Touch Target Size >= 48dp).

---

## 5. HỆ THỐNG MÀU SẮC VÀ THÊM THỊ GIÁC (COLOR SYSTEM TOKENS)

Ứng dụng sử dụng bảng màu HSL hài hòa, tạo cảm giác phiêu lưu ngoài trời (Outdoor Adventure Theme):

- **Primary Color (`#2E7D32` - Forest Green):** Màu xanh lá rừng rực rỡ, đại diện cho thiên nhiên và chuyến đi cắm trại/leo núi.
- **Secondary Color (`#0288D1` - Ocean Blue):** Màu xanh dương nổi bật dùng cho nút GPS và trạng thái thông tin.
- **Accent Color (`#FF1744` - Vibrant Red):** Màu đỏ tươi dùng cho Ghim vị trí màu đỏ (Red Marker Pin) để thu hút thị giác 100%.
- **Background (`#F8F9FA` - Soft Grey):** Màu nền xám dịu nhẹ giúp các thẻ Card màu trắng nổi bật mà không làm mỏi mắt.
- **Surface & Cards (`#FFFFFF` - Pure White):** Nền thẻ Card màu trắng tinh khiết với đường viền mảnh `1dp` stroke color `@color/outline`.
