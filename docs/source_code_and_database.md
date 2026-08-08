# TÀI LIỆU MÃ NGUỒN VÀ CƠ SỞ DỮ LIỆU (SOURCE CODE & DATABASE)

## 1. QUẢN LÝ XÁC THỰC VÀ CƠ SỞ DỮ LIỆU FIREBASE

### 1.1 Xác thực người dùng với Firebase Authentication
Ứng dụng sử dụng `FirebaseAuth` để quản lý phiên đăng nhập và xác thực người dùng an toàn.

#### 1. Đăng ký tài khoản mới (`RegisterActivity.java`):
```java
FirebaseAuth mAuth = FirebaseAuth.getInstance();

mAuth.createUserWithEmailAndPassword(email, password)
    .addOnCompleteListener(this, task -> {
        if (task.isSuccessful()) {
            FirebaseUser firebaseUser = mAuth.getCurrentUser();
            if (firebaseUser != null) {
                // Cập nhật Display Name trên Firebase Profile
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setDisplayName(fullName)
                        .build();
                firebaseUser.updateProfile(profileUpdates);
                
                // Lưu hồ sơ người dùng vào Firestore
                saveUserProfileToFirestore(firebaseUser.getUid(), email, fullName);
            }
        } else {
            Toast.makeText(RegisterActivity.this, "Đăng ký thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
        }
    });
```

#### 2. Đăng nhập tài khoản (`LoginActivity.java`):
```java
mAuth.signInWithEmailAndPassword(email, password)
    .addOnCompleteListener(this, task -> {
        if (task.isSuccessful()) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
        }
    });
```

---

### 1.2 Thao tác dữ liệu Firestore Cloud Database (`FirestoreHelper.java`)
Lớp [`FirestoreHelper.java`](file:///Users/3o/Documents/Study/Semescher%207/COMP1785_Androi_studio/Hiking_application_coursework/app/src/main/java/com/example/hiking_application_coursework/helpers/FirestoreHelper.java) bao bọc toàn bộ câu lệnh truy vấn NoSQL Firestore.

#### 1. Cấu trúc Firestore Collections:
- **`hikes` Collection:** Chứa các tài liệu chuyến đi (`Hike`).
- **`observations` Collection:** Chứa các ghi chú quan sát (`Observation`).
- **`users` Collection:** Chứa hồ sơ người dùng (`User`).

#### 2. Thêm mới tài liệu (`insertHike`):
```java
public void insertHike(Hike hike, DataStatus status) {
    db.collection("hikes")
        .add(hike)
        .addOnSuccessListener(documentReference -> {
            // Cập nhật lại ID tài liệu vừa tạo
            hike.setFirebaseId(documentReference.getId());
            db.collection("hikes").document(documentReference.getId()).set(hike);
            status.dataIsInserted();
        })
        .addOnFailureListener(e -> Log.e("Firestore", "Error adding document", e));
}
```

#### 3. Tải danh sách tài liệu theo thời gian (`getAllHikes`):
```java
public void getAllHikes(DataStatus status) {
    db.collection("hikes")
        .orderBy("date", Query.Direction.DESCENDING)
        .get()
        .addOnSuccessListener(queryDocumentSnapshots -> {
            List<Hike> hikes = new ArrayList<>();
            for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                Hike hike = snapshot.toObject(Hike.class);
                if (hike != null) {
                    hike.setFirebaseId(snapshot.getId());
                    hikes.add(hike);
                }
            }
            status.dataIsLoaded(hikes);
        });
}
```

#### 4. Cập nhật tài liệu (`updateHike`):
```java
public void updateHike(Hike hike, DataStatus status) {
    if (hike.getFirebaseId() != null) {
        db.collection("hikes").document(hike.getFirebaseId())
            .set(hike)
            .addOnSuccessListener(aVoid -> status.dataIsInserted());
    }
}
```

#### 5. Xóa tài liệu (`deleteHike`):
```java
public void deleteHike(String hikeId, DataStatus status) {
    db.collection("hikes").document(hikeId)
        .delete()
        .addOnSuccessListener(aVoid -> status.dataIsDeleted());
}
```

---

## 2. KIỂM TRA HỢP LỆ DỮ LIỆU (INPUT VALIDATION)

Trong [`AddHikeActivity.java`](file:///Users/3o/Documents/Study/Semescher%207/COMP1785_Androi_studio/Hiking_application_coursework/app/src/main/java/com/example/hiking_application_coursework/activities/AddHikeActivity.java), toàn bộ thông tin đầu vào được kiểm tra nghiêm ngặt trước khi hiển thị dialog xác nhận hoặc lưu vào cơ sở dữ liệu:

```java
private void validateAndConfirm() {
    String name = editHikeName.getText().toString().trim();
    String location = editLocation.getText().toString().trim();
    String duration = editDuration.getText().toString().trim();

    // 1. Kiểm tra các trường bắt buộc có dấu *
    if (TextUtils.isEmpty(name) || TextUtils.isEmpty(location) || TextUtils.isEmpty(duration)) {
        Toast.makeText(this, "Fields marked with * are required", Toast.LENGTH_SHORT).show();
        return;
    }
    
    // 2. Hiển thị Dialog xác nhận dữ liệu đã nhập
    showConfirmationDialog();
}
```

---

## 3. HIỂN THỊ DỮ LIỆU VỚI RECYCLERVIEW VÀ ADAPTER

### 3.1 Cấu trúc `HikeAdapter.java`
Lớp [`HikeAdapter.java`](file:///Users/3o/Documents/Study/Semescher%207/COMP1785_Androi_studio/Hiking_application_coursework/app/src/main/java/com/example/hiking_application_coursework/adapters/HikeAdapter.java) chịu trách nhiệm ánh xạ đối tượng `Hike` vào tệp giao diện phần tử `item_hike.xml`.

```java
public class HikeAdapter extends RecyclerView.Adapter<HikeAdapter.HikeViewHolder> {
    private List<Hike> hikeList;
    private OnHikeClickListener listener;

    public interface OnHikeClickListener {
        void onHikeClick(Hike hike);
    }

    public HikeAdapter(List<Hike> hikeList, OnHikeClickListener listener) {
        this.hikeList = hikeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HikeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hike, parent, false);
        return new HikeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HikeViewHolder holder, int position) {
        Hike hike = hikeList.get(position);
        holder.txtName.setText(hike.getName());
        holder.txtLocation.setText(hike.getLocation());
        holder.txtDate.setText(hike.getDate());
        holder.txtLength.setText(hike.getLength() + " km");
        holder.chipDifficulty.setText(hike.getDifficulty());

        // Lấy tài nguyên ảnh động từ drawable theo resName
        int resId = holder.itemView.getContext().getResources().getIdentifier(
                hike.getImageResName(), "drawable", holder.itemView.getContext().getPackageName());
        if (resId != 0) {
            holder.imgThumb.setImageResource(resId);
        }

        holder.itemView.setOnClickListener(v -> listener.onHikeClick(hike));
    }

    @Override
    public int getItemCount() {
        return hikeList != null ? hikeList.size() : 0;
    }
}
```

---

## 4. LUỒNG DỮ LIỆU ĐỒNG BỘ 2 CHIỀU GIỮA UX/UI VÀ CLOUD FIRESTORE

### 4.1 Chiều đi: Từ Input UI -> Validation -> Firestore DB
```
[User nhập trên EditText UI]
           │
           ▼
[Sự kiện click button Save]
           │
           ▼
[Hàm validateAndConfirm()] (Kiểm tra dữ liệu rỗng)
           │
           ▼
[Đóng gói dữ liệu vào POJO model Hike]
           │
           ▼
[Gọi firestoreHelper.insertHike(hike)]
           │
           ▼
[Firestore SDK thực hiện POST request lên Cloud]
```

### 4.2 Chiều về: Từ Firestore DB -> Java Objects -> View UX/UI
```
[Màn hình HomeFragment mở / Load dữ liệu]
           │
           ▼
[Gọi firestoreHelper.getAllHikes(callback)]
           │
           ▼
[Firestore trả về danh sách DocumentSnapshot]
           │
           ▼
[Chuyển đổi từng Snapshot -> Hike POJO Object]
           │
           ▼
[Trả về danh sách List<Hike> qua Callback DataStatus]
           │
           ▼
[Khởi tạo / Cập nhật HikeAdapter với List<Hike>]
           │
           ▼
[RecyclerView render lại item_hike.xml lên UI màn hình]
```
