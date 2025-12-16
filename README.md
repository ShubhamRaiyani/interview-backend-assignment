  # Hotel Booking Backend Service

  A secure backend service for managing hotel bookings, built as a **take-home internship assignment **. The service focuses on clean backend design, secure authentication, booking conflict detection, and production-ready deployment practices.

  > **Backend Assignment – Java**

  ---

  ## 🎯 Objective

  The goal of this project is to design and deliver a **mini-product backend** that demonstrates:

  * Secure JWT-based authentication
  * Role-based access control (Staff, Reception, Admin)
  * Correct business logic for booking conflicts
  * Clean data modeling with MongoDB Atlas
  * Cloud-ready deployment

  The scope is intentionally limited to backend responsibilities, following the assignment guidelines.

  ---

  ## 🛠 Tech Stack

  * **Java 17 (LTS)**
  * **Spring Boot 3** (Web, Security, Data MongoDB, Mail)
  * **Spring Security – OAuth2 Resource Server**
  * **MongoDB Atlas** (Cloud-hosted NoSQL database)
  * **Supabase Authentication** (JWT / OIDC)
  * **Maven** (Build tool)
  * **Docker** (Containerization)
  * **Render** (Deployment)

  ---

  ## 🧱 Architecture Overview

  ### Authentication

  * Users authenticate via **Supabase Auth**.
  * Supabase issues a JWT.
  * This backend acts as an **OAuth2 Resource Server**, validating:
    * Token signature
    * Token expiry

  ### Authorization

  * The JWT `sub` claim represents the authenticated **staff / reception / admin user**.
  * Custom roles are read from Supabase `app_metadata.role`.
  * Role-based access control (RBAC) is enforced using Spring Security.
  * Three role levels are supported:
    * **STAFF** – Can create and view bookings
    * **RECEPTION** – Can create and view bookings
    * **ADMIN** – Can create and view bookings (same privileges as STAFF/RECEPTION, with potential for future expansions)

  ### Database

  * Business data is stored in **MongoDB Atlas**.
  * Authentication data is **not duplicated** in MongoDB.

  ### Notifications

  * Booking notifications are sent via **Email (SMTP)**.
  * Notifications are handled asynchronously and do not block the API response.

  ---

  ## 🔐 Authentication & Authorization

  ### JWT Validation

  * **Issuer (`iss`)**: Tokens are issued by Supabase. Issuer validation is implicit via shared-secret verification.
  * **Signature**: Verified using a symmetric JWT secret (HS256) configured in Supabase.
    This service validates the token signature using a shared secret supplied via environment configuration.
  * **Expiry (`exp`)**: Expired tokens are automatically rejected

  ### User Identity

  * The JWT `sub` claim is treated as the **unique user identifier**.
  * This value is stored as `createdBy` in booking records.

  ### Roles & Access Control

  | Role | Create Bookings | View Bookings | Future Admin Tasks |
  |-----|:---:|:---:|:---|
  | STAFF | ✅ | ✅ | — |
  | RECEPTION | ✅ | ✅ | — |
  | ADMIN | ✅ | ✅ | Future management or reporting features |
  | Any authenticated user (non-privileged role) | ❌ | ✅ | — |

  **Write operations** are restricted to `STAFF`, `RECEPTION`, and `ADMIN` roles.
  **Read operations** are available to any authenticated user.

  ---

  ## 🗄 Database Design (MongoDB Atlas)

  ### Collections

  #### 1️⃣ `hotels` (Seeded for demo)

  | Field    | Type   | Description                         |
  | -------- | ------ | ----------------------------------- |
  | `_id`    | String | Hotel identifier (e.g. `HOTEL_001`) |
  | `name`   | String | Hotel name                          |
  | `city`   | String | City                                |
  | `status` | String | `ACTIVE` / `INACTIVE`               |

  **Pre-seeded Hotels** (for local development):
  - HOTEL_001: Taj Palace, Mumbai
  - HOTEL_002: The Oberoi, Delhi
  - HOTEL_003: ITC Grand Chola, Chennai
  - HOTEL_004: Leela Palace, Bengaluru
  - HOTEL_005: Hyatt Regency, Pune

  #### 2️⃣ `bookings`

  | Field        | Type    | Description                      |
  | ------------ | ------- | -------------------------------- |
  | `_id`        | String  | Booking ID (MongoDB ObjectId)    |
  | `hotelId`    | String  | Associated hotel ID              |
  | `guestName`  | String  | Guest (customer) name            |
  | `guestEmail` | String  | Guest contact email              |
  | `startDate`  | Date    | Check-in date (YYYY-MM-DD)       |
  | `endDate`    | Date    | Check-out date (YYYY-MM-DD)      |
  | `createdBy`  | String  | Staff/Reception/Admin user ID (`sub`) |
  | `createdAt`  | Instant | Booking creation timestamp       |

  ### Indexing

  A **compound index** on:

  ```
  (hotelId, startDate, endDate)
  ```

  optimizes booking conflict detection and date-range queries.

  ---

  ## 📡 API Endpoints

  ### Get Bookings

  `GET /api/hotels/{hotelId}/bookings`

  * Returns all bookings for a hotel
  * Sorted by start date (ascending)
  * Requires authentication (any role)
  * **Future Enhancement**: Add optional `startDate` and `endDate` query parameters for range filtering

  **Response (200 OK)**
  ```json
  {
    "success": true,
    "message": "Bookings fetched successfully",
    "data": [
      {
        "id": "507f1f77bcf86cd799439011",
        "hotelId": "HOTEL_001",
        "guestName": "Rahul Sharma",
        "guestEmail": "rahul@gmail.com",
        "startDate": "2025-02-10",
        "endDate": "2025-02-15",
        "createdBy": "user-uuid-1234",
        "createdAt": "2025-01-15T10:30:00Z"
      }
    ]
  }
  ```

  ---

  ### Create Booking

  `POST /api/hotels/{hotelId}/bookings`

  * **Required Role**: `STAFF`, `RECEPTION`, or `ADMIN`
  * Performs date conflict validation
  * Uses authenticated user's `sub` claim as `createdBy`

  **Request Body**

  ```json
  {
    "guestName": "Rahul Sharma",
    "guestEmail": "rahul@gmail.com",
    "startDate": "2025-02-10",
    "endDate": "2025-02-15"
  }
  ```

  **Responses**

  | Status | Description | Example |
  |--------|---|---|
  | `201 CREATED` | Booking created successfully | `{ "success": true, "message": "Booking created successfully", "data": {...} }` |
  | `400 BAD_REQUEST` | Invalid date range (startDate >= endDate) | `{ "success": false, "message": "Start date must be before end date", "data": null }` |
  | `401 UNAUTHORIZED` | Missing or invalid JWT token | `{ "success": false, "message": "Unauthorized", ... }` |
  | `403 FORBIDDEN` | Insufficient role (not STAFF/RECEPTION/ADMIN) | `{ "success": false, "message": "Access Denied", ... }` |
  | `404 NOT_FOUND` | Hotel not found | `{ "success": false, "message": "Hotel not found with ID: HOTEL_999", ... }` |
  | `409 CONFLICT` | Overlapping booking exists | `{ "success": false, "message": "Booking dates overlap with existing booking", ... }` |

  ---

  ### Get Current User (Debug Endpoint)

  `GET /api/auth/me`

  * Returns authenticated user's identity and claims
  * Useful for verifying JWT structure and role extraction
  * Requires authentication

  **Response (200 OK)**
  ```json
  {
    "username": "user-uuid-1234",
    "authorities": [
      { "authority": "ROLE_ADMIN" }
    ],
    "claims": {
      "sub": "user-uuid-1234",
      "email": "admin@example.com",
      "app_metadata": {
        "role": "ADMIN"
      },
      ...
    }
  }
  ```

  Note: This endpoint is intended for development/debugging purposes and can be removed or restricted in production.

  ---

  ## 🚫 Booking Conflict Detection

  A new booking is **rejected** if it overlaps with an existing booking using this logic:

  ```
  existing.startDate < new.endDate
  AND
  existing.endDate > new.startDate
  ```

  **Examples:**

  | Existing Booking | New Request | Result | Reason |
  |---|---|---|---|
  | 2025-02-10 to 2025-02-15 | 2025-02-12 to 2025-02-18 | ❌ CONFLICT | Overlaps by 3 days |
  | 2025-02-10 to 2025-02-15 | 2025-02-15 to 2025-02-20 | ✅ ALLOWED | Check-out and check-in on same day is OK |
  | 2025-02-10 to 2025-02-15 | 2025-02-05 to 2025-02-10 | ✅ ALLOWED | Previous guest checks out exactly when new guest checks in |
  | 2025-02-10 to 2025-02-15 | 2025-02-20 to 2025-02-25 | ✅ ALLOWED | No overlap |

  ---

  ## 📧 Notifications

  * **Type**: Email (SMTP – Gmail)
  * **Trigger**: Immediately after successful booking creation
  * **Execution**: Asynchronous (`@Async`) – doesn't block API response
  * **Behavior**: Email failures are logged but **do not** affect booking persistence
  * **Recipient**: Configured support team email address

  **Email Content** includes:
  - Hotel ID
  - Guest name and email
  - Check-in and check-out dates
  - Staff/Admin user ID who created the booking

  **Note**: Slack notification is mentioned in requirements but not yet implemented. Email-only implementation allows for future enhancement.

  ---

  ## 🌱 Data Seeding

  * A **Data Seeder** component runs automatically on startup if `app.seed-data=true`.
  * Seeds 5 demo hotels for easier local testing.
  * Checks if hotels already exist before inserting (idempotent).
  * **Disabled in production** (controlled via environment configuration).

  ---

  ## ▶️ Running Locally

  ### Prerequisites

  1. **Java 17+** installed
  2. **Maven** installed
  3. **MongoDB Atlas** cluster created (free tier available)
  4. **Supabase** project with JWT secret configured
  5. **Gmail account** (or SMTP provider) for email notifications

  ### Environment Variables

  Create a `.env` file or set these in your IDE:

  ```bash
  # MongoDB
  SPRING_DATA_MONGODB_URI=mongodb+srv://username:password@cluster.mongodb.net/db

  # Supabase JWT
  SPRING_SECURITY_SECRET=your-supabase-jwt-secret

  # Email (Gmail SMTP)
  SPRING_MAIL_USERNAME=your-email@gmail.com
  SPRING_MAIL_PASSWORD=your-app-password
  SUPPORT_EMAIL=support@example.com

  # Data Seeding
  APP_SEED_DATA=true
  ```

  Note: JWT validation uses HS256 with a shared secret configured in Supabase.

  ### Run Spring Boot

  ```bash
  ./mvnw spring-boot:run
  ```

  The service will start on `http://localhost:8080`

  ### Test with cURL

  ```bash
  # Get bookings (requires valid JWT)
  curl -H "Authorization: Bearer YOUR_JWT" http://localhost:8080/api/hotels/HOTEL_001/bookings

  # Create booking (requires STAFF/RECEPTION/ADMIN role)
  curl -X POST http://localhost:8080/api/hotels/HOTEL_001/bookings \
    -H "Authorization: Bearer YOUR_JWT" \
    -H "Content-Type: application/json" \
    -d '{
      "guestName": "John Doe",
      "guestEmail": "john@example.com",
      "startDate": "2025-02-20",
      "endDate": "2025-02-25"
    }'
  ```

  ---

  ## 🚀 Deployment

  * **Platform**: Render.com (or similar cloud provider)
  * **Containerization**: Docker (Dockerfile included)
  * **Environment-based Configuration**: All secrets via environment variables (not committed to Git)
  * **Deployment Link**: [https://interview-backend-assignment.onrender.com](https://interview-backend-assignment.onrender.com)

  ### Docker Build & Run

  ```bash
  # Build Docker image
  docker build -t interview-backend-assignment
:latest .

  # Run container
  docker run -p 8080:8080 \
    -e SPRING_DATA_MONGODB_URI=... \
    -e SPRING_SECURITY_SECRET=... \
    interview-backend-assignment
:latest
  ```

  ---

  ## 📋 Assumptions & Design Decisions

  ### Core Assumptions & Design Decisions

  1. **Authentication is externalized**
    User lifecycle, credentials, and role assignment are fully managed by Supabase (external IdP). The backend acts strictly as an OAuth2 Resource Server.

  2. **No user data duplication**
    Only the JWT `sub` claim is stored as a reference (`createdBy`). User profiles are not persisted in MongoDB.

  3. **Roles are embedded in JWT claims**
    Roles are provided via `app_metadata.role` in the JWT. The backend explicitly enforces privileged roles (STAFF, RECEPTION, ADMIN) and remains role-agnostic for read operations.

  4. **ADMIN role is introduced for extensibility**
    In addition to STAFF and RECEPTION, an ADMIN role is supported. In the current scope, ADMIN has the same permissions as staff but is positioned for future capabilities (hotel management, analytics, staff administration).

  5. **Actor vs Guest separation**
    - `createdBy` represents the authenticated staff/admin user performing the action
    - `guestName` and `guestEmail` are customer metadata only
    - Guest users are not authenticated and not stored as system users

  6. **Bookings are date-based**
    The system operates on LocalDate (YYYY-MM-DD) only. Time-of-day and timezone handling are intentionally excluded.

  7. **Invalid date ranges are rejected**
    Requests where `startDate >= endDate` are rejected with `400 BAD REQUEST`.

  8. **Overlap detection is hotel-scoped**
    Booking conflicts are checked per hotel. The same guest may have overlapping bookings across different hotels.

  9. **Concurrent race conditions are not fully prevented**
    The application checks for conflicts before persistence. True concurrent race conditions are acknowledged as a limitation and would require transactional or locking mechanisms in a production system.

  10. **Hotel capacity & room inventory are out of scope**
      The system assumes unlimited availability per hotel. Room-level inventory is intentionally deferred.

  11. **Multiple bookings per user are allowed**
      Staff/admin users may create multiple bookings; guests may appear in multiple bookings.

  12. **GET API visibility is broad by design**
      Any authenticated user may view bookings for a hotel. This assumes staff/reception users are trusted internal actors.

  13. **Email notifications are best-effort and non-blocking**
      Notifications are sent asynchronously. Failures are logged and do not affect booking persistence.

  14. **Data seeding is environment-scoped**
      Test data seeding runs only when `app.seed-data=true` and is disabled in production via environment configuration.

  15. **No local authentication fallback**
      All endpoints require a valid JWT. For development, tokens must be obtained from Supabase or mocked.

  16. **Lenient role handling for non-privileged users**
      Authenticated users with unrecognized roles default to read-only access, preventing accidental authorization failures.

  17. **Pagination and booking lifecycle management are out of scope**
      Pagination, cancellation, and booking updates are not implemented as they were not required by the assignment.

  18. **User provisioning is handled externally**
      - New users (staff, reception, admin) are created and managed directly in the Supabase dashboard.
      - Roles are assigned via `app_metadata.role` at the identity provider level.
      - This service does not expose user registration, role assignment, or user management APIs.
      - This aligns with a microservice-style separation of concerns.

  19. **JWT Validation Trade-off**
      For this assignment, JWT validation uses HS256 with a shared secret for simplicity. In a production environment, RS256 with JWKS would be preferred.


  ---

  ## 👤 Author

  **Shubham Raiyani**
  Java Backend / Full-Stack Developer
  GitHub: [https://github.com/ShubhamRaiyani](https://github.com/ShubhamRaiyani)
