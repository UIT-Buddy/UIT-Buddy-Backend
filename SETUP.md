### Requirements

#### Run with docker

- Install make (optional)
- Docker & docker compose required

#### Run locally

- JDK 25 required
- Docker & docker compose required
- Install make (optional)

### Run

Make help to show all command

```bash
make help
```

Run, use:

```bash
make run
```

If not included make, use command

```bash
docker compose up -d postgres redis
docker compose up -d
```

## API docs

- **API Base URL**: http://localhost:8080
- **API Documentation (Scalar)**: http://localhost:8080/docs
