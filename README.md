# Otelier Backend Assignment - Java

A secure backend service for hotel bookings built with **Java Spring Boot**, **MongoDB**, and **Supabase**.

## 🚀 Tech Stack
- **Languages**: Java 17+
- **Framework**: Spring Boot 3.4.12 (Web, Security, Validation, Mail)
- **Database**: MongoDB Atlas
- **Authentication**: Supabase (JWT / OIDC)
- **Deployment**: Docker / Railway (Ready for deployment)

## ✅ Features Implemented
1.  **Authentication & Security**:
    - Stateless JWT authentication using **Supabase**.
    - Role-Based Access Control (**RBAC**):
        - `ROLE_STAFF` / `ROLE_RECEPTION`: Can create bookings.
        - Authenticated Users: Can list bookings.
2.  **Core Booking Logic**:
    - **Conflict Detection**: Prevents double bookings using an efficient MongoDB query (`existing.start < new.end && existing.end > new.start`).
    - **Validation**: Ensures `startDate` is before `endDate`.
    - **Guest Metadata**: Stores `guestName` and `guestEmail` as part of the booking document (see Assumptions).
3.  **Notifications**:
    - Asynchronous **Email Service** (SMTP) via Gmail to notify support staff of new regular bookings.
4.  **Sorting**:
    - Efficient database-level sorting of bookings by date.

## 🧠 Key Assumptions (Interview Ready)
1.  **Guest Details as Metadata**:
    - `guestName` and `guestEmail` are stored strictly as **metadata** on the booking.
    - Guests are **NOT** authenticated users of the system.
    - The `userId` field tracks the **Staff Member** who performed the booking action.
    - *Reasoning*: keeps authentication concerns isolated from business data.
2.  **Email Service**:
    - Configured for Gmail SMTP using App Passwords. Requires environment variables to be set.
3.  **Timezones**:
    - All dates (`LocalDate`) are assumed to be in the hotel's local timezone.

## 🛠️ Setup & Configuration

### 1. Prerequisites
- Java 17+
- Maven
- MongoDB Atlas Cluster
- Supabase Project
- Gmail Account (with App Password)

### 2. Environment Variables
The application requires the following environment variables. Create a `.env` file or export them in your shell:

```properties
# Database
MONGODB_URI=mongodb+srv://<user>:<password>@<cluster>.mongodb.net/?appName=Cluster0
MONGODB_DATABASE=otelierdemo

# Authentication (Supabase)
SUPABASE_ISSUER=https://<your-project>.supabase.co/auth/v1
SUPABASE_JWT_SECRET=<your-jwt-secret>

# Email Service (Gmail SMTP)
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
SUPPORT_EMAIL=support@otelier-demo.com
```

### 3. Run the Application
```bash
./mvnw spring-boot:run
```

## 🔌 API Endpoints

### 1. List Bookings
**GET** `/api/hotels/{hotelId}/bookings`
- **Auth**: Required
- **Response**: List of bookings sorted by `startDate` (Ascending).

### 2. Create Booking
**POST** `/api/hotels/{hotelId}/bookings`
- **Auth**: `ROLE_STAFF` or `ROLE_RECEPTION`
- **Body**:
```json
{
  "startDate": "2025-02-10",
  "endDate": "2025-02-15",
  "guestName": "Jane Doe",
  "guestEmail": "jane@example.com"
}
```
- **Responses**:
    - `201 Created`: Success.
    - `400 Bad Request`: Validation failure (e.g., missing fields, invalid dates).
    - `409 Conflict`: Double booking prevented.

## 🧪 Testing

### Create Booking (cURL)
```bash
curl -X POST http://localhost:8080/api/hotels/HOTEL_123/bookings \
  -H "Authorization: Bearer <YOUR_JWT>" \
  -H "Content-Type: application/json" \
  -d '{
    "startDate": "2025-02-01",
    "endDate": "2025-02-05",
    "guestName": "John Doe",
    "guestEmail": "john@example.com"
  }'
```

### List Bookings (cURL)
```bash
curl -H "Authorization: Bearer <YOUR_JWT>" http://localhost:8080/api/hotels/HOTEL_123/bookings
```
