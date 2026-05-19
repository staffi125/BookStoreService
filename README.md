# Book Loft — Online Bookstore Platform

[![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Tests](https://img.shields.io/badge/tests-240%20passing-success)](./src/test)
[![License](https://img.shields.io/badge/license-MIT-lightgrey)](LICENSE)

> Full-stack bookstore with **role-based access** (customer & staff), **persistent shopping basket**, order workflow, bilingual UI, and a **REST API** alongside a polished **Thymeleaf** web app.

**Live demo (local):** `http://localhost:8084`

---

## Why this project matters 

| What you can evaluate | How it is demonstrated |
|----------------------|-------------------------|
| **Backend engineering** | Layered Spring Boot app, JPA, transactions, business rules (balance, order states) |
| **Security** | Spring Security, BCrypt, CSRF on web forms, role-based endpoints |
| **API design** | REST resources for books, clients, employees, orders, basket, profile |
| **Frontend craft** | Custom design system, responsive layout, progressive enhancement with vanilla JS |
| **Quality** | **240 automated tests**, seed data, validation, i18n (EN / UK) |
| **Real-world flows** | Catalog → basket → checkout → staff fulfillment pipeline |

---

## Screenshots

### Landing & authentication

| Home | Sign in |
|------|---------|
| ![Home page](docs/screenshots/home.png) | ![Login page](docs/screenshots/login.png) |

### Customer journey

| Catalog (search & filters) | Basket & checkout |
|---------------------------|-------------------|
| ![Catalog](docs/screenshots/catalog.png) | ![Basket](docs/screenshots/basket.png) |

| Profile (balance & orders) | Book details |
|---------------------------|--------------|
| ![Profile](docs/screenshots/profile.png) | ![Book detail](docs/screenshots/book-detail.png) |

### Staff workspace

| Dashboard | Order management |
|-----------|------------------|
| ![Staff dashboard](docs/screenshots/employee-dashboard.png) | ![Staff orders](docs/screenshots/employee-orders.png) |

---

## Features

### Customer (`ROLE_CLIENT`)

- Browse catalog with **live search**, price/page filters, pagination
- **AJAX add-to-basket** with toast notifications and basket badge
- **Persistent basket** stored in PostgreSQL (survives logout)
- Checkout charges **account balance**; view & cancel `NEW` orders in profile
- Self-service **registration** with client-side + server-side validation
- **Ukrainian / English** UI toggle

### Staff (`ROLE_EMPLOYEE`)

- **CRUD** book inventory with search & filters
- **Block / unblock** clients
- **Order pipeline:** `NEW` → `CONFIRMED` → `SHIPPED` → `DELIVERED`
- Staff dashboard + orders table with **auto-refresh** (30s)
- Assign themselves to open orders on confirm

### Platform

- **Dual interface:** human-friendly `/app/*` web UI + machine-friendly REST API
- Centralized error handling (JSON for API, friendly pages for web)
- SQL seed data for instant demo
- Optional `application-local.properties` for secrets (not committed)

---

## Tech stack

| Layer | Technology |
|-------|------------|
| Language | Java 17 |
| Framework | Spring Boot 3.2 (Web, Data JPA, Security, Validation, AOP) |
| View | Thymeleaf + Spring Security extras |
| Database | PostgreSQL (runtime), H2 (tests) |
| Mapping | ModelMapper |
| Build | Maven |
| UI | CSS3, vanilla JavaScript |
| Fonts | Fraunces, DM Sans (Google Fonts) |

---

## Architecture

```mermaid
flowchart TB
    subgraph clients [Clients]
        Browser[Browser / Thymeleaf UI]
        API[REST clients / Postman]
    end

    subgraph app [Spring Boot Application]
        WebCtrl[Web Controllers /app]
        RestCtrl[REST Controllers]
        Svc[Service Layer]
        Repo[JPA Repositories]
    end

    DB[(PostgreSQL)]

    Browser --> WebCtrl
    API --> RestCtrl
    WebCtrl --> Svc
    RestCtrl --> Svc
    Svc --> Repo
    Repo --> DB
```

**Packages:** `controller` (REST) · `controller.web` (MVC) · `service` · `repo` · `model` · `security` · `dto`

---

## Order lifecycle

```text
[Basket] --checkout--> NEW
NEW --cancel (client)--> CANCELLED (+ refund to balance)
NEW --confirm (staff)--> CONFIRMED (staff assigned)
CONFIRMED --ship--> SHIPPED
SHIPPED --deliver--> DELIVERED
```

---

## Quick start

### Prerequisites

- **JDK 17+**
- **Maven 3.9+**
- **PostgreSQL** with database `book_store`

### 1. Database

```sql
CREATE DATABASE book_store;
```

### 2. Local configuration

Copy the example and set your password:

```bash
cp src/main/resources/application-local.properties.example src/main/resources/application-local.properties
```

Edit `application-local.properties`:

```properties
spring.datasource.password=YOUR_PASSWORD
```

### 3. Run

```bash
mvn spring-boot:run
```

Open **http://localhost:8084**

Tables are created automatically (`ddl-auto=update`) and seed data is loaded from `src/main/resources/sql/sql.sql`.

### 4. Test

```bash
mvn test
```

---

## Demo accounts

All seeded users share the password **`password123`** (BCrypt in SQL).

| Role | Email | What to try |
|------|-------|-------------|
| Customer | `client1@example.com` | Catalog, basket, profile, orders |
| Staff | `john.doe@email.com` | `/app/employee` — books, clients, orders |

More clients: `client2@example.com` … `client10@example.com` · more staff in seed file.

---

## API overview (REST)

Authenticated via **HTTP Basic** or session (after form login). CSRF disabled for REST paths.

| Resource | Base path | Notes |
|----------|-----------|--------|
| Books | `/books` | Search, CRUD by title |
| Clients | `/clients` | CRUD, registration |
| Employees | `/employees` | CRUD, block/unblock clients |
| Orders | `/orders` | List, create, checkout, status transitions |
| Basket | `/basket` | Per authenticated client |
| Profile | `/profile` | Update account |

**Web-only JSON (AJAX):**

- `GET /app/basket/summary`
- `POST /app/basket/add?ajax=true`
- `GET /app/books/search`
- `GET /app/employee/orders/data`

---

## Project structure

```text
src/main/java/com/spring/project/
├── controller/          # REST API
├── controller/web/      # Thymeleaf MVC (/app/*)
├── service/             # Business logic
├── repo/                # Spring Data JPA
├── model/               # Entities & enums
├── security/            # UserDetailsService, login handler
├── dto/                 # API & form DTOs
└── conf/                # Security, ModelMapper

src/main/resources/
├── templates/           # Thymeleaf HTML
├── static/css|js/       # UI assets
├── messages*.properties # i18n
└── sql/sql.sql          # Seed data
```

---

## Testing

- **240** test cases (`mvn test`)
- **39** test classes
- H2 in-memory DB with profile `test` (`application-test.properties`)
- Coverage includes controllers (REST + web), services, models, DTOs, security seed hashes

---

## Configuration

| Property | Default | Description |
|----------|---------|-------------|
| `server.port` | `8084` | HTTP port |
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/book_store` | DB URL |
| `spring.jpa.hibernate.ddl-auto` | `update` | Schema management |
| `spring.sql.init.data-locations` | `classpath:/sql/sql.sql` | Seed on startup |

Environment variables: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`.

---

## Author

**Andriy**, [GitHub](https://github.com/staffi125), [LinkedIn](https://www.linkedin.com/in/andriy-hrytsenyuk-826939339/).

---

## License

MIT — see [LICENSE](LICENSE) (add a LICENSE file if you publish publicly).
