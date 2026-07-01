# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.0.0] - 2025-10-03

### Added

#### Core Architecture & Setup
- Initial project setup with Java and Apache Maven.
- Implemented a **Layered Architecture** (UI, Service, Repository, Model) for clear separation of concerns.
- Integrated **JPA with Hibernate** for the persistence layer, standardizing database operations.

#### Core Functionalities
- **User Management:** Secure user login and a dynamic UI based on user roles.
- **Patient Management:** Comprehensive module for patient registration, record viewing, and medical history management.
- **Appointment Scheduling:** Efficient, decoupled module for booking and managing appointments.
- **Billing & Claims:** A flexible, multi-step workflow for processing bills and insurance claims.
- **Permissions Management:** Hierarchical permission control for staff and departments.
- **Report Generation:** Module to generate diverse reports like Patient Summaries and Financial Overviews.
- **Admin Dashboard:** A secure, admin-only dashboard for system statistics and viewing audit logs.

#### Design Pattern Implementation
- **Decorator Pattern:** To dynamically add logging and validation to patient data operations.
- **Mediator Pattern:** To manage complex backend coordination in the appointment scheduling process.
- **Chain of Responsibility Pattern:** To handle the multi-step billing and claims workflow.
- **Composite Pattern:** To treat individual staff and entire departments uniformly for permissions.
- **Visitor Pattern:** To decouple report generation logic from core data models.
- **Proxy & Decorator Pattern:** To create a layered security model for access control and audit logging.

#### Documentation
- Initial version of `README.md` with project overview, architecture, and setup instructions.
- Added `LICENSE.md` file (Apache 2.0).
- Created this `CHANGELOG.md` file to track project versions.