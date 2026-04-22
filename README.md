# ms-aws-ecs-demo

Spring Boot Todo CRUD API built with Java 25, Maven, Spring Data JPA, and an in-memory H2 database.

## Tech Stack

- Java 25
- Spring Boot 3.5.7
- Maven
- Spring Web
- Spring Data JPA
- H2 Database (in-memory)
- JUnit 5 + Mockito + Spring MockMvc

## Project Structure

```text
src/main/java/com/example/todo
  |- controller
  |- entity
  |- exception
  |- repository
  |- service
```

## Getting Started

### Prerequisites

- JDK 25 installed
- Maven 3.9+ (or use Maven Wrapper)

### Run the Application

Using Maven Wrapper:

```bash
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

Using local Maven:

```bash
mvn spring-boot:run
```

The application starts on:

- `http://localhost:8080`

## H2 Database

- JDBC URL: `jdbc:h2:mem:todo-db`
- Username: `sa`
- Password: *(empty)*
- Console: `http://localhost:8080/h2-console`

## API Endpoints

Base path: `/api/todos`

- `GET /api/todos` - list all todos
- `GET /api/todos/{id}` - get a todo by id
- `POST /api/todos` - create a todo
- `PUT /api/todos/{id}` - update a todo
- `DELETE /api/todos/{id}` - delete a todo

### Example Create Request

```json
{
  "title": "Learn Spring Boot",
  "description": "Build a Todo CRUD service",
  "completed": false
}
```

## Run Tests

Using Maven Wrapper:

```bash
./mvnw test
```

Windows PowerShell:

```powershell
.\mvnw.cmd test
```

Using local Maven:

```bash
mvn test
```