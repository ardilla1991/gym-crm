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
