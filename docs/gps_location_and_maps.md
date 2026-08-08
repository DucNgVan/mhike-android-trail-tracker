# TÀI LIỆU ĐỊNH VỊ GPS VÀ BẢN ĐỒ MAPBOX (GPS LOCATION & MAPS)

## 1. ĐỊNH VỊ GPS TỰ ĐỘNG VÀ QUẢN LÝ QUYỀN TRUY CẬP

### 1.1 Quyền ứng dụng (App Permissions)
Ứng dụng khai báo quyền truy cập vị trí chính xác trong tệp `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.INTERNET" />
```

### 1.2 Lấy vị trí tự động với `FusedLocationProviderClient`
Ứng dụng sử dụng Google Play Services Location SDK (`LocationServices.getFusedLocationProviderClient`) để lấy vị trí GPS hiện tại với độ chính xác cao (`PRIORITY_HIGH_ACCURACY`).

#### Giải thích mã nguồn chi tiết trong [`AddHikeActivity.java`](file:///Users/3o/Documents/Study/Semescher%207/COMP1785_Androi_studio/Hiking_application_coursework/app/src/main/java/com/example/hiking_application_coursework/activities/AddHikeActivity.java):

```java
private void fetchCurrentLocation() {
    // 1. Kiểm tra quyền ACCESS_FINE_LOCATION đã được cấp chưa
    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        // Yêu cầu người dùng cấp quyền nếu chưa có
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
        return;
    }

    // 2. Cấu hình yêu cầu lấy vị trí độ chính xác cao
    CurrentLocationRequest request = new CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build();

    // 3. Gọi hàm lấy vị trí hiện tại bất đồng bộ
    fusedLocationClient.getCurrentLocation(request, null).addOnSuccessListener(this, location -> {
        if (location != null) {
            // Lấy Kinh độ và Vĩ độ
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            // Hiển thị tọa độ lên 2 trường EditText
            editLat.setText(String.valueOf(latitude));
            editLng.setText(String.valueOf(longitude));

            // Cập nhật Ghim đỏ và di chuyển camera bản đồ đến tọa độ này
            updatePreviewMarker();

            // Tự động tìm tên địa chỉ từ tọa độ GPS này
            reverseGeocode(latitude, longitude);

            Toast.makeText(this, "Location pinned!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Searching for GPS signal...", Toast.LENGTH_SHORT).show();
        }
    });
}
```

---

## 2. TÍCH HỢP BẢN ĐỒ MAPBOX MAPS SDK V11

### 2.1 Cấu hình Thư viện và Cấu trúc Mã nguồn
Dự án tích hợp Mapbox SDK v11 bản NDK 27 tương thích Android 16 KB Page Alignment trong `app/build.gradle.kts`:
```kotlin
implementation("com.mapbox.maps:android-ndk27:11.11.0")
```

### 2.2 Khởi tạo Bản đồ và Căn camera mặc định tại Việt Nam
```java
private void initPreviewMap() {
    // Tải style đường phố tiêu chuẩn Mapbox Streets
    previewMap.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS, style -> {
        setupMapClickListener(); // Lắng nghe sự kiện chạm bản đồ
        
        // Nếu tọa độ rỗng, tự động đưa tâm bản đồ về Việt Nam (Kinh độ: 108.206230, Vĩ độ: 16.047079)
        if (TextUtils.isEmpty(editLat.getText().toString()) || TextUtils.isEmpty(editLng.getText().toString())) {
            isUpdatingMarkerFromText = true;
            editLat.setText("16.047079");
            editLng.setText("108.206230");
            isUpdatingMarkerFromText = false;
        }
        updatePreviewMarker(); // Cập nhật ghim đỏ
    });
}
```

---

## 3. TẠO GHIM 2 LỚP (RED MARKER PIN) VÀ CHẤM VỊ TRÍ TRÊN BẢN ĐỒ

Để ghim màu đỏ hiển thị **100% rực rỡ và chính xác** trên mọi thiết bị và emulator, ứng dụng áp dụng kỹ thuật ghim 2 lớp (Dual-Layer Annotation):

```java
private void updatePreviewMarker() {
    try {
        String latStr = editLat.getText().toString();
        String lngStr = editLng.getText().toString();
        if (TextUtils.isEmpty(latStr) || TextUtils.isEmpty(lngStr)) return;

        double lat = Double.parseDouble(latStr);
        double lng = Double.parseDouble(lngStr);
        Point p = Point.fromLngLat(lng, lat); // Tạo điểm GeoJSON Point (Kinh độ, Vĩ độ)
        
        // 1. Di chuyển camera bản đồ trực tiếp tới tọa độ mục tiêu với Zoom 14.0
        if (previewMap != null && previewMap.getMapboxMap() != null) {
            previewMap.getMapboxMap().setCamera(new CameraOptions.Builder().center(p).zoom(14.0).build());
        }

        // 2. Lấy Plugin Annotation thông qua ID đăng ký chuẩn Mapbox "mapbox-annotation"
        AnnotationPlugin annotationApi = (AnnotationPlugin) previewMap.getPlugin(Plugin.MAPBOX_ANNOTATION_PLUGIN_ID);
        if (annotationApi != null) {
            
            // --- LỚP 1: Vòng tròn đỏ neon nổi bật (#FF1744) ---
            if (circleAnnotationManager == null) {
                circleAnnotationManager = CircleAnnotationManagerKt.createCircleAnnotationManager(annotationApi, new AnnotationConfig());
            }
            if (circleAnnotationManager != null) {
                circleAnnotationManager.deleteAll(); // Xóa ghim cũ
                CircleAnnotationOptions circleOptions = new CircleAnnotationOptions()
                        .withPoint(p)
                        .withCircleRadius(12.0)             // Bán kính 12dp
                        .withCircleColor("#FF1744")          // Màu đỏ tươi
                        .withCircleStrokeWidth(3.0)          // Viền trắng 3dp
                        .withCircleStrokeColor("#FFFFFF");
                circleAnnotationManager.create(circleOptions);
            }

            // --- LỚP 2: Ghim đỏ Vector (PointAnnotation với IconAnchor.BOTTOM) ---
            if (pointAnnotationManager == null) {
                pointAnnotationManager = PointAnnotationManagerKt.createPointAnnotationManager(annotationApi, new AnnotationConfig());
            }
            if (pointAnnotationManager != null) {
                pointAnnotationManager.deleteAll();
                Bitmap bitmap = getBitmapFromDrawableRes(R.drawable.ic_red_marker);
                if (bitmap != null) {
                    PointAnnotationOptions options = new PointAnnotationOptions()
                            .withPoint(p)
                            .withIconImage(bitmap)
                            .withIconSize(1.2)
                            .withIconAnchor(IconAnchor.BOTTOM); // Chân ghim đè đúng tâm tọa độ
                    pointAnnotationManager.create(options);
                }
            }
        }
    } catch (Exception ignored) {}
}
```

---

## 4. LUỒNG TÌM KIẾM ĐỊA CHỈ (SEARCH LOCATION) VÀ TÁI TẠO BẢN ĐỒ CHI TIẾT

### 4.1 Luồng Tìm kiếm Địa chỉ (VD: "Đà Nẵng")
Khi người dùng nhập từ khóa địa điểm vào ô tìm kiếm:
1. Ứng dụng gọi `searchLocation(query)`.
2. Chạy **Android System Geocoder** trước để lấy tọa độ.
3. Nếu Geocoder không có kết quả, tự động fallback sang **OpenStreetMap Nominatim REST API** (`https://nominatim.openstreetmap.org/search?q=...&accept-language=en,vi`).
4. Nếu Nominatim bận, tiếp tục fallback sang **Komoot Photon Geocoding API**.
5. Khi nhận được tọa độ Kinh độ/Vĩ độ, hàm `updatePreviewMarker()` được gọi để di chuyển camera bản đồ đến Đà Nẵng và cắm ghim đỏ.

### 4.2 Lưu trữ và Tái tạo Bản đồ tại Màn hình Chi tiết (`HikeDetailActivity`)

#### 1. Lưu vào Firestore (chỉ lưu tọa độ gọn nhẹ):
Khi bấm lưu chuyến đi, ứng dụng đóng gói tọa độ thành mảng JSON trong đối tượng `Hike`:
```java
List<Map<String, Double>> points = new ArrayList<>();
Map<String, Double> p = new HashMap<>();
p.put("lat", lat); 
p.put("lng", lng);
points.add(p);
hike.setPlannedRoute(points);
```

#### 2. Tái tạo Bản đồ & Ghim đỏ tại màn hình Chi tiết (`HikeDetailActivity.java`):
Khi người dùng bấm vào 1 chuyến đi từ danh sách:
1. `HikeDetailActivity` nhận đối tượng `Hike` qua `Intent`.
2. Trích xuất danh sách tọa độ: `plannedPoints = convertToPoints(hike.getPlannedRoute())`.
3. Khi Mapbox khởi tạo xong (`initDetailMap`), gọi `drawHikeMarker()` để vẽ lại ghim đỏ 2 lớp ngay tại tọa độ `plannedPoints.get(0)`.
4. Gọi `focusOnHikeLocation()` để di chuyển camera tập trung vào ghim đỏ với zoom 15.0:
```java
private void focusOnHikeLocation() {
    if (!plannedPoints.isEmpty()) {
        mapView.getMapboxMap().setCamera(new CameraOptions.Builder()
                .center(plannedPoints.get(0))
                .zoom(15.0)
                .build());
    }
}
```
