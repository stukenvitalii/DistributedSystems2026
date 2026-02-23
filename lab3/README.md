# Lab 3 — Secure Microservices

Два микросервиса (auth-service и data-service) написаны на Spring Boot + Kotlin. Auth выдаёт JWT, data обслуживает
публичные и защищённые данные. Сервисы общаются по HTTPS, все запросы логируются.

## Как подготовить окружение

1. Сгенерировать TLS-ключи (однократно):
   ```powershell
   cd C:\Users\stukenvitalii\IdeaProjects\DistributedSystems2026\labs\lab3
   python generate_keystores.py
   ```
   В репозиторий эти `.p12` не попадают (см. `.gitignore`).
2. Запустить тесты локально:
   ```powershell
   cd C:\Users\stukenvitalii\IdeaProjects\DistributedSystems2026\labs
   mvn -pl lab3 -am test
   ```

## Docker Compose

Собрать и запустить оба сервиса можно одной командой из каталога `lab3`:

```powershell
cd C:\Users\stukenvitalii\IdeaProjects\DistributedSystems2026\labs\lab3
docker compose up --build
```

- `auth-service` слушает `https://localhost:8081`
- `data-service` слушает `https://localhost:8082`

Обе службы используют одну и ту же секретную строку `SECURITY_JWT_SECRET`; при необходимости её можно переопределить в
`.env` или через переменные окружения Docker Compose.

## API шпаргалка

- `POST https://localhost:8081/api/auth/login` — выдаёт JWT (Basic-тестовые логины: `alice/alicePass123`,
  `bob/bobService!`, `service-client/svc-client-key`).
- `GET https://localhost:8082/api/data/public` — публичные данные.
- `GET https://localhost:8082/api/data/protected` — требует `ROLE_USER`.
- `GET https://localhost:8082/api/data/admin` — требует `ROLE_ADMIN`.
- `POST https://localhost:8082/api/data/secure-message` — сервис-сервис обмен, требует `ROLE_SERVICE`.

## Локальный вызов защищённого API

1. Берём токен у auth-service.
2. Вызываем data-service с заголовком `Authorization: Bearer <token>`.
3. Для тестовых вызовов из Postman нужно включить проверку сертификата client-side или добавить самоподписанные
   сертификаты в доверенные.

