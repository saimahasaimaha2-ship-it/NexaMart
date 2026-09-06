# NexaMart

A full-stack e-commerce marketplace built with Java Servlets, JSP, and a layered MVC architecture, developed as a capstone project (Anna University R2025 Sem3).

**Live demo:** https://nexamart-mjkh.onrender.com

> ⚠️ **Note:** The live demo runs on Render's free tier, which has no persistent disk. The database resets to an empty (but schema-initialized) state whenever the server restarts (after ~15 minutes of inactivity). Any products, orders, or accounts created on the demo will not persist across restarts — this is expected behavior, not a bug.

---

## Features

- **Authentication** — Register/login with role-based access (Buyer, Seller, Admin), bcrypt password hashing, session-based auth
- **Product Browsing** — Search and filter products by keyword and category
- **Cart & Checkout** — Add to cart, view cart, checkout to place an order
- **Order History** — Buyers can view their past orders with line items
- **Seller Dashboard** — Sellers can create, edit, and delete their own product listings, and view incoming orders for their products
- **Admin Panel** — Admins can view all users, products, and orders, and remove products
- **Reviews & Ratings** — Buyers can rate and review products they've purchased (1–5 stars + comment)
- **Health Check** — `GET /api/v1/health` reports live DB connectivity status

---

## Tech Stack

| Layer          | Technology                          |
|----------------|--------------------------------------|
| Language       | Java 17                              |
| Web Framework  | Java Servlets + JSP (Jakarta EE)     |
| Build Tool     | Maven                                |
| Server         | Apache Tomcat 9                      |
| Database       | H2 (file-based in production, TCP server in local dev) |
| Connection Pool| HikariCP                             |
| Password Hash  | jBCrypt                              |
| JSON           | Gson                                 |
| Testing        | JUnit 5 + Mockito                    |
| CI             | GitHub Actions                       |
| Deployment     | Docker + Render.com                  |

---

## Architecture

The app follows a layered MVC pattern: