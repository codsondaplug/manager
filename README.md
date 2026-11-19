# Task Manager

Минимальное учебное приложение на Spring Boot. В проекте оставлены только функции, необходимые для демонстрации технологий из задания.

## Технологии

- REST API — создание, просмотр, изменение и удаление задач.
- Spring Data JPA — работа с пользователями, задачами и журналом аудита.
- DTO и Validation — отдельные объекты запросов/ответов и проверка входных данных.
- AOP — логирование методов с аннотацией `@Loggable`.
- Audit Log — сохранение действий с задачами в базе данных.
- Spring Security и JWT — вход, регистрация и защита API.
- Роли `USER` и `ADMIN` — пользователь видит только свои задачи, администратор видит все задачи и аудит.
- Thymeleaf — две простые HTML-страницы без CSS и JavaScript.
- Flyway — SQL-миграция `V1__create_tables.sql`.
- Docker Compose — запуск приложения и PostgreSQL.
- Swagger UI — документация REST API.
- JUnit 5 и Mockito — тесты сервисов и запуск контекста приложения.

## Требования

- Java 21
- Docker Desktop (только для запуска через Docker)

## Локальный запуск (H2, без Docker)

В корне проекта:

```powershell
.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:run
```

Откройте:

- Веб-интерфейс: http://localhost:8080/login
- Swagger: http://localhost:8080/swagger-ui/index.html

Локально используется база H2 в памяти. Таблицы создаются миграцией Flyway.

## Тестовые пользователи

| Роль | Email | Пароль |
|---|---|---|
| ADMIN | admin@test.com | admin123 |
| USER | user@test.com | pass123 |

Демонстрационные пользователи создаются автоматически при запуске. Новые пользователи регистрируются через `POST /api/auth/register` и получают роль USER.

## REST API

### Авторизация

`POST /api/auth/register`

```json
{
  "email": "new@example.com",
  "password": "secret123"
}
```

`POST /api/auth/login`

```json
{
  "email": "user@test.com",
  "password": "pass123"
}
```

Оба метода возвращают JWT в поле `token`. В Swagger нажмите **Authorize** и вставьте токен.

### Задачи (требуется JWT)

| Метод | URL | Назначение |
|---|---|---|
| GET | `/api/tasks` | Список своих задач; ADMIN видит все |
| GET | `/api/tasks/{id}` | Получить задачу |
| POST | `/api/tasks` | Создать задачу |
| PUT | `/api/tasks/{id}` | Изменить задачу |
| PATCH | `/api/tasks/{id}/status?status=DONE` | Изменить статус |
| DELETE | `/api/tasks/{id}` | Удалить задачу |
| GET | `/api/admin/audit` | Журнал аудита, только ADMIN |

Пример тела для POST и PUT:

```json
{
  "title": "Подготовить отчёт",
  "description": "Учебная задача",
  "priority": "MEDIUM"
}
```

Возможные приоритеты: `LOW`, `MEDIUM`, `HIGH`. Статусы: `NEW`, `IN_PROGRESS`, `DONE`.

## Docker + PostgreSQL

Создайте `.env` из примера:

```powershell
Copy-Item .env.example .env
```

При необходимости измените пароли в `.env`, затем выполните:

```powershell
docker compose up --build -d
```

Открыть Swagger: http://localhost:8080/swagger-ui/index.html

Остановка:

```powershell
docker compose down
```

Для удаления контейнеров вместе с данными БД используйте `docker compose down -v`.

## Структура проекта

```text
src/main/java/com/example/taskmanager/
  annotation/  — аннотация @Loggable
  aspect/      — AOP-логирование
  config/      — Security, OpenAPI, тестовые пользователи
  controller/  — REST и Thymeleaf контроллеры
  dto/         — DTO запросов и ответов
  model/       — User, Task, AuditLog и enum
  repository/  — Spring Data JPA
  security/    — JWT и загрузка пользователя
  service/     — бизнес-логика и аудит
src/main/resources/
  db/migration/ — Flyway SQL
  templates/    — две Thymeleaf-страницы
```

