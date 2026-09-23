# Vinyl Encyclopedia

**Project Assignment for the course Back End Programming - SOF003AS3AE-3007**

Vinyl Encyclopedia is a Spring Boot web application for managing a personal vinyl collection.  
Users can store records in **My Collection** or **Wishlist**, browse them in a visual catalog, search and sort them, and open a detailed page for each record.

The project also includes **authentication**, **role-based access**, **PostgreSQL database connection**, **deployment to the internet**, and a small **REST API**.

## Live Demo

**Deployed application:**  
https://vinyl-encyclopedia-74073e5d3306.herokuapp.com

**GitHub repository:**  
https://github.com/rakhimichi/vinyl-encyclopedia

## Main Features

- User registration and login
- Authentication with Spring Security
- Role-based access for **USER** and **ADMIN**
- Add, edit and delete vinyl records
- Separate sections for:
  - **My Collection**
  - **Wishlist**
- Record detail page
- Search records by artist, album, genre or label
- Sort records alphabetically:
  - by artist
  - by album title
- Visual catalog with album cover images
- Admin panel with user overview
- Admin access to other users' collections
- Admin ability to delete records from other users' collections
- Responsive layout for desktop and mobile
- Minimal REST API for selected resources

## User Roles

### USER
A regular user can:
- register a new account
- log in
- manage only their own records
- create, edit and delete records in their personal collection and wishlist

### ADMIN
An admin can:
- access a separate admin panel
- view all users
- open other users' collections
- delete records from other users' collections

## Technologies Used

- **Java 17**
- **Spring Boot**
- **Spring MVC**
- **Spring Security**
- **Spring Data JPA**
- **Thymeleaf**
- **PostgreSQL**
- **Maven**
- **JUnit 5**
- **Mockito**
- **Heroku**
- **GitHub**

## Database

The application uses **PostgreSQL** as the main database.

- Local development uses a local PostgreSQL database
- The deployed version uses **Heroku Postgres**

## REST API

The project includes a small REST API in addition to the Thymeleaf-based web interface.

### User API
- `GET /api/records`
- `GET /api/records/{id}`

Examples:
- `/api/records?sortBy=artist`
- `/api/records?sortBy=title`
- `/api/records?query=daft`
- `/api/records?section=my-collection`
- `/api/records?section=wishlist`

### Admin API
- `GET /api/admin/users`
- `GET /api/admin/users/{userId}/records`
- `DELETE /api/admin/records/{recordId}`

## How to Run the Project Locally
1. Clone the repository
- `git clone https://github.com/rakhimichi/vinyl-encyclopedia.git`
- `cd vinyl-encyclopedia`
2. Configure PostgreSQL
- Create a PostgreSQL database, for example:
- CREATE DATABASE vinyldb;
- Update database settings in `application.properties` if needed.
3. Run the project & tests
- `./mvnw clean test`
- `./mvnw spring-boot:run`
4. Open in browser
http://localhost:8080

## The project includes lightweight unit tests for:

- registration logic
- vinyl record service logic
- admin service logic
- API controller logic
- Deployment

## The application is deployed to the internet using Heroku.

Deployment setup includes:

- Procfile
- system.properties
- PostgreSQL add-on
- automatic deployment from GitHub
- Demo Accounts

## Example demo accounts can be created by the application initializer:

**Admin**

username: admin

password: admin123

**User**

username: user

password: user123

## Course Goals Covered

This project covers the most important Grade 5 criteria of the course:

- wide scope
- authentication
- PostgreSQL database connection
- GitHub repository
- deployment to the internet
- own independently learned features
- Spring Boot functionality beyond basic lecture examples
- structured source code
- commented source code
- optional REST API support

## Author

Kirill Rakhimov

Haaga-Helia University of Applied Sciences

Back End Programming - SOF003AS3AE-3007


## Naruto watch log

`/pet-projects/naruto` opens a shared watch log after the existing account login and the Naruto password gate. Both friends see the same progress; all tracker API routes require the unlocked session and mutations require CSRF protection.

- The custom order in `src/main/resources/naruto/watchlist.txt` contains 592 separate items: 176 episodes + 2 OVA in season 1, and 409 episodes + 5 specials in season 2. Omitted episodes stay omitted. Each episode, OVA, and film counts as one item.
- Checkmarks are stored in PostgreSQL (`naruto_watches`) with a viewing date and the account that added them. A second mark does not count twice. Unchecking removes that item's contribution from progress and daily activity. Other sessions refresh every 20 seconds while visible.
- The challenge starts on 24 September 2026. The deadline is 24 September 2027, inclusive, in `Europe/Zurich`. The initial plan uses three items per day. After launch, the forecast uses the last seven calendar days (or days since launch during the first week), including zero-view days and the current day. A zero pace pauses the forecast. Completing after the deadline is a loss.
- The character of the day is deterministic per account and Zurich calendar date. The bundled 145-name catalog comes from [FandoHub](https://naruto.fandohub.com/ru/character), retrieved on 23 September 2026; biographies are not copied.
- `./mvnw test` runs the full test suite against an isolated in-memory H2 database. PostgreSQL remains the application database. The added table is created by the existing Hibernate `ddl-auto=update` configuration.

Deploy updates by pushing `main` to GitHub; this project's Heroku app is configured to deploy automatically. Wait for the new release before checking the page. Heroku restarts end the current in-memory sessions, so users may need to sign in and unlock Naruto again; database checkmarks persist.
