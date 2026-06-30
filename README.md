### 2. File README.md cho kho App Admin (`linhlinhadmin/README.md`)

```markdown
## 1. Giới thiệu
Đây là mã nguồn Phần mềm Quản trị nội bộ (Desktop Client).
Ứng dụng được xây dựng bằng công nghệ **Java Swing**, cung cấp giao diện trực quan, ổn định và bảo mật dành riêng cho Quản trị viên (Admin) để thực hiện các nghiệp vụ:
- Quản lý kho hàng, sản phẩm, danh mục.
- Quản lý và kiểm duyệt đơn hàng.
- Quản lý tài khoản khách hàng.
- Xem báo cáo thống kê doanh thu trực quan.
- Tích hợp AI để bóc tách hóa đơn nhập hàng tự động và nhận tư vấn chiến lược kinh doanh.

## 2. Yêu cầu môi trường
- **Java Development Kit (JDK):** Phiên bản JDK 8 trở lên. (Kiểm tra bằng lệnh `java -version` trong Terminal/CMD).
- **IDE Khuyến nghị:** Apache NetBeans, IntelliJ IDEA, hoặc Eclipse.
- **Backend & Database:** Để phần mềm hoạt động đầy đủ chức năng, đảm bảo Server Node.js (API) chạy ở cổng `3000` và cơ sở dữ liệu MySQL (`dientu_store`) đã được khởi chạy thành công (xem hướng dẫn ở kho mã nguồn Web Client).

## 3. Hướng dẫn cài đặt và khởi chạy
1. **Mở dự án:** Mở IDE (ví dụ: NetBeans), chọn **File -> Open Project** và trỏ tới thư mục mã nguồn `linhlinhadmin` này.
2. **Cấu hình JDK:** Đảm bảo project đã được cấu hình sử dụng đúng phiên bản JDK 8+ (Click chuột phải vào project -> Properties -> Build/Libraries -> Chọn JDK tương ứng).
3. **Thêm thư viện (Nếu cần):** Nếu dự án sử dụng các file `.jar` bên ngoài (như JDBC driver cho MySQL, JSON parser...), hãy đảm bảo chúng đã được add đầy đủ vào thư mục **Libraries** của project.
4. **Cấu hình kết nối:**
   - Ứng dụng giao tiếp với hệ thống qua RESTful API tại địa chỉ mặc định: `http://localhost:3000/api`
   - Kết nối trực tiếp đến DB trỏ tới MySQL `localhost:3306`, database `dientu_store`.
5. **Khởi chạy:** Tìm đến file chứa hàm `main` (thường là giao diện Đăng nhập hoặc Home), click chuột phải và chọn **Run File** (hoặc nhấn `Shift + F6` trong NetBeans).

## 4. Tài khoản Test (Dành cho Hội đồng)
Sử dụng tài khoản có quyền Admin dưới đây để đăng nhập vào phần mềm Quản trị:

| Vai trò | Tên đăng nhập (Username) | Mật khẩu (Password) |
| :--- | :--- | :--- |
| **Quản trị viên (Admin)** | `admin` | `123456` |

