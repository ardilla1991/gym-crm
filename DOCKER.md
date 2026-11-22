# Docker task

Service: Main (Gym CRM). The Report microservice is not implemented in this project.

Requirements: Docker. All commands run from the project root.

## SubTask 1. Disabled integrations

**1. Dockerfile:** [Dockerfile](Dockerfile)

**2. Create the image**
```powershell
docker build -t gym-crm .
```

**3. Run the application** (profile `local`, H2)
```powershell
docker run --rm -p 8080:8080 --name gym-crm gym-crm
```

Result: http://localhost:8080/actuator/health shows `UP`, database `H2`.

Stop:
```powershell
docker stop gym-crm
```

## SubTask 2. Enabled integrations

**1. Network configuration:** [docker-compose.yml](docker-compose.yml), network `gym-net`, database host `postgres`

**2. Run the application with the database** (profile `dev`, PostgreSQL)

First start (empty database), creates the tables and loads `data.sql`:
```powershell
docker compose run --build --rm app --spring.jpa.hibernate.ddl-auto=create-only --spring.main.web-application-type=none
```

Start:
```powershell
docker compose up -d --build
```

Result: http://localhost:8080/actuator/health shows `UP`, database `PostgreSQL`.

Stop:
```powershell
docker compose down
```
