# Лабораторная 2 — микросервисная архитектура

Два независимых Kotlin/Spring Boot сервиса демонстрируют работу простейшей системы заказов:

- **inventory-service** — управляет остатками товаров и выполняет резервирование.
- **order-service** — создаёт заказы, синхронно обращаясь к inventory для подтверждения резерва.

Каждый сервис имеет отдельную базу данных PostgreSQL, управляемую через Flyway. Общение между сервисами идёт по HTTP/REST.

## Быстрый старт локально

```powershell
cd C:\Users\stukenvitalii\IdeaProjects\DistributedSystems2026\labs\lab2
mvn clean package
```

После сборки можно стартовать Docker Compose:

```powershell
docker compose up --build
```

### Ручная проверка API

1. Создать SKU в inventory:
   ```powershell
   curl -X POST http://localhost:8081/api/items -H "Content-Type: application/json" -d '{"sku":"book-001","name":"Clean Architecture","quantity":10}'
   ```
2. Создать заказ:
   ```powershell
   curl -X POST http://localhost:8082/api/orders -H "Content-Type: application/json" -d '{"externalId":"order-001","sku":"book-001","quantity":2}'
   ```
3. Проверить остаток:
   ```powershell
   curl http://localhost:8081/api/items/book-001
   ```

## Миграции

Оба сервиса запускают Flyway автоматически при старте (скрипты лежат в `inventory-service/src/main/resources/db/migration` и `order-service/src/main/resources/db/migration`).
Если базу нужно подготовить отдельно, можно выполнить миграции вручную:

```powershell
cd C:\Users\stukenvitalii\IdeaProjects\DistributedSystems2026\labs\lab2
..\mvnw.cmd -pl inventory-service flyway:migrate
..\mvnw.cmd -pl order-service flyway:migrate
```

После этого сервисы можно поднимать в любом порядке — Hibernate лишь валидирует схему.

## Тестирование

```powershell
mvn -pl inventory-service,order-service test
```

## Стек технологий

- Kotlin 1.9+/JDK 21, Spring Boot 3.2
- Spring Web, Spring Data JPA, WebClient
- PostgreSQL, Flyway
- Testcontainers, MockWebServer
- Docker/Docker Compose

## Структура

```
lab2/
  inventory-service/
  order-service/
  docker-compose.yml
  README.md
```

Каждый сервис упакован в отдельный Docker-образ; совместный запуск обеспечивается `docker-compose.yml`.
