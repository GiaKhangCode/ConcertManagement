# ConcertManagement

ConcertManagement là hệ thống quản lý sự kiện/concert gồm:

- **Backend**: Spring Boot REST API
- **Frontend**: HTML/CSS/JavaScript tĩnh
- **Database**: Oracle Database

> Lưu ý: Trong repo hiện tại, backend là Spring Boot Maven Java 17. Frontend là web tĩnh dùng HTML/CSS/JavaScript và gọi API backend tại `http://localhost:8081`.

---

## 1. Công nghệ sử dụng

### Backend

- Java 17
- Spring Boot 3.2.3
- Spring Web
- Spring Data JPA
- Spring Security
- JWT
- Oracle JDBC Driver `ojdbc11`
- Maven

### Frontend

- HTML
- CSS
- JavaScript
- Fetch API

### Database

- Oracle Database
- Schema/User: `ConcertManagementDB`

---

## 2. Cấu trúc thư mục

```txt
ConcertManagement/
├── Database/
│   ├── CONCERTMANAGEMENTDB.sql
│   ├── mock_data.sql
│   ├── seed_roles.sql
│   ├── seed_staff_role.sql
│   ├── create_analytics_views.sql
│   ├── create_sodo.sql
│   ├── alter_phase6.sql
│   ├── alter_refund_history.sql
│   ├── alter_reject_logic.sql
│   ├── sp_optimization.sql
│   ├── update_db_images.sql
│   └── update_logging_tables.sql
│
├── backend/
│   ├── src/
│   ├── .mvn/
│   ├── mvnw.cmd
│   └── pom.xml
│
├── frontend/
│   ├── assets/
│   ├── css/
│   ├── js/
│   ├── index.html
│   ├── auth.html
│   ├── booking.html
│   ├── checkout.html
│   ├── event-detail.html
│   ├── event-management.html
│   ├── admin-analytics.html
│   ├── admin-approval.html
│   ├── admin-create.html
│   ├── organizer-promotions.html
│   └── profile.html
│
└── README.md
```

---

## 3. Yêu cầu cài đặt

Trước khi chạy project, cần cài:

- JDK 17
- Maven hoặc dùng Maven Wrapper trong thư mục `backend`
- Node.js
- Oracle Database
- SQL Developer hoặc công cụ chạy SQL tương đương
- Git

Kiểm tra phiên bản:

```bash
java -version
node -v
npm -v
git --version
```

---

## 4. Clone project

```bash
git clone https://github.com/GiaKhangCode/ConcertManagement.git
cd ConcertManagement
```

---

## 5. Tạo Oracle user/schema

Đăng nhập Oracle bằng tài khoản có quyền DBA, ví dụ `SYS AS SYSDBA`, sau đó chạy:

```sql
alter session set "_ORACLE_SCRIPT"=true;

CREATE USER ConcertManagementDB IDENTIFIED BY "Admin123";

GRANT CONNECT, RESOURCE TO ConcertManagementDB;

ALTER USER ConcertManagementDB QUOTA UNLIMITED ON USERS;

GRANT CREATE VIEW TO ConcertManagementDB;
```

Sau khi tạo user, đăng nhập lại bằng:

```txt
Username: ConcertManagementDB
Password: Admin123
```

---

## 6. Import database

Chạy script SQL trong thư mục `Database/`.

```txt
Database/CONCERTMANAGEMENTDB.sql
```

Lưu ý:

- Đoạn mã phân quyền nhóm phải chạy sau khi tạo 1 tài khoản mới

```sql
INSERT INTO PHAN_QUYEN_NHOM (MaTaiKhoan, MaNhomQuyen)
VALUES (1, (SELECT MaNhomQuyen FROM NHOM_QUYEN WHERE TenNhomQuyen = 'ROLE_ADMIN'));

COMMIT;
```

---

## 7. Cấu hình backend

File cấu hình backend nằm tại:

```txt
backend/src/main/resources/application.yml
```

Cấu hình database mặc định:

```yml
spring:
  application:
    name: StellarPortalBackend

  datasource:
    url: ${DB_URL:jdbc:oracle:thin:@localhost:1521:orcl}
    username: ${DB_USERNAME:ConcertManagementDB}
    password: ${DB_PASSWORD:Admin123}
    driver-class-name: oracle.jdbc.OracleDriver

  jpa:
    database-platform: org.hibernate.dialect.OracleDialect
    hibernate:
      ddl-auto: none
      naming:
        physical-strategy: org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl
    show-sql: true
    properties:
      hibernate:
        format_sql: true

  jackson:
    serialization:
      write-dates-as-timestamps: false
    deserialization:
      fail-on-unknown-properties: false

  mail:
    host: smtp.gmail.com
    port: 587
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true

server:
  port: ${SERVER_PORT:8081}
```

Nếu Oracle của bạn dùng service name thay vì SID, có thể cần đổi URL thành:

```yml
spring:
  datasource:
    url: jdbc:oracle:thin:@localhost:1521/orcl
```

Hoặc với Oracle XE:

```yml
spring:
  datasource:
    url: jdbc:oracle:thin:@localhost:1521/XEPDB1
```

Tùy cấu hình Oracle trên máy bạn.

---

## 8. Chạy backend

Mở terminal tại thư mục project:

```bash
cd backend
```

### Windows

```bash
mvnw.cmd spring-boot:run
```

Hoặc nếu máy đã cài Maven:

```bash
mvn spring-boot:run
```

Backend chạy tại:

```txt
http://localhost:8081
```

Nếu backend chạy thành công, console sẽ in:

```txt
--------- Ve'ryGood BACKEND STARTED ---------
```

---

## 9. Chạy frontend

Frontend hiện là web tĩnh. Có thể chạy bằng Node.js static server.

Từ thư mục gốc project:

```bash
npx http-server frontend -p 3000 -c-1
```

Sau đó mở trình duyệt:

```txt
http://localhost:3000
```

Trang chính:

```txt
http://localhost:3000/index.html
```

Trang đăng nhập/đăng ký:

```txt
http://localhost:3000/auth.html
```

Frontend đang gọi backend tại:

```txt
http://localhost:8081
```

Vì vậy cần chạy backend trước khi thao tác đăng nhập, đăng ký, đặt vé, quản lý sự kiện.

---

## 10. Tài khoản và phân quyền

Project có các role như:

- `CUSTOMER`
- `ORGANIZER`
- `ADMIN`
- `STAFF`

Role được seed từ các file SQL trong thư mục `Database/`.

Nếu không đăng nhập được, kiểm tra lại:

- Đã chạy đoạn mã setup role hay chưa
- Đã chạy dữ liệu mẫu chưa
- Backend có kết nối được Oracle chưa
- API `http://localhost:8081/api/auth/login` có hoạt động không

---

## 11. Một số lỗi thường gặp

### Lỗi không kết nối được Oracle

Kiểm tra trong `application.yml`:

```yml
spring:
  datasource:
    url: jdbc:oracle:thin:@localhost:1521:orcl
    username: ConcertManagementDB
    password: Admin123
```

Kiểm tra Oracle listener:

```bash
lsnrctl status
```

Nếu dùng Oracle XE, service name thường là:

```txt
XEPDB1
```

Khi đó đổi URL thành:

```yml
url: jdbc:oracle:thin:@localhost:1521/XEPDB1
```

### Lỗi port 8081 đã được sử dụng

Đổi port trong:

```txt
backend/src/main/resources/application.yml
```

Ví dụ:

```yml
server:
  port: 8082
```

Nếu đổi port backend, cần sửa các URL API trong frontend từ:

```txt
http://localhost:8081
```

sang port mới.

### Frontend báo hệ thống đang bảo trì

Thường do một trong các nguyên nhân:

- Backend chưa chạy
- Backend chạy sai port
- Oracle chưa chạy
- Database chưa import
- API backend lỗi
- CORS bị chặn

---


## 12. Quy trình chạy nhanh

```bash
git clone https://github.com/GiaKhangCode/ConcertManagement.git
cd ConcertManagement

# 1. Tạo Oracle user ConcertManagementDB
# 2. Import SQL trong thư mục Database

# 3. Chạy backend
cd backend
mvnw.cmd spring-boot:run

# 4. Mở terminal khác, chạy frontend
cd ..
npx http-server frontend -p 3000 -c-1
```

Mở trình duyệt:

```txt
http://localhost:3000
```
