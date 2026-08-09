# Santander Dev Week 2026 - API & ETL Pipeline

Esta é uma evolução do projeto original da Santander Dev Week, agora integrando uma RESTful API robusta em Java com um Pipeline de Dados (ETL) inteligente em Python.

## 🚀 Novidades desta Versão (DevOps & Data)

Diferente da versão original, este repositório foca na automação de dados e resiliência de infraestrutura:

* **Ambiente M4 & Java 21:** Configuração otimizada para o chip Apple M4, utilizando Java 21 (LTS) para garantir compatibilidade total com o Spring Boot 3.4.

* **Migrations com Flyway:** Schema versionado (`db/migration`) com seed do usuário histórico (ID 1), executado tanto no H2 (dev) quanto no PostgreSQL (prd).

* **Data Seeding Automatizado:** Script Python para população em massa do banco de dados H2, permitindo testes de carga e processamento em lote.

* **Pipeline ETL:** Extração automatizada de dados da API, transformação de mensagens de marketing (segmentação de saldo) e carregamento em arquivos de saída JSON. O pipeline lê a lista de usuários via `GET /users`, sem depender de IDs contíguos.

## 🛠️ Tecnologias Utilizadas

* **Java 21 & Spring Boot 3:** Backend robusto com Spring Data JPA, Spring Security (JWT) e Spring Data Redis.

* **Spring Security + JWT:** Autenticação com access token de curta duração + **refresh token rotativo** (opaco, hasheado no banco), ownership por role (USER vs ADMIN) e logout com revogação server-side.

* **Redis:** Rate limit distribuído por IP (contador compartilhado entre instâncias via Lua atômico), com fallback in-memory quando o Redis está indisponível.

* **Python 3.11:** Engine do pipeline ETL utilizando a biblioteca requests.

* **OpenAPI (Swagger):** Documentação interativa disponível em <http://localhost:8080/swagger-ui/index.html>. A spec canônica da API está em [docs/openapi.yaml](docs/openapi.yaml), exportada do `/v3/api-docs`.

* **H2 Database:** Banco de dados em arquivo no perfil `dev` (persiste entre reinícios em `./data/sdw2023`) e em memória nos testes.

* **PostgreSQL:** Banco de produção-like no `docker-compose.prd.yml`.

* **Testes:** 77 testes backend (JUnit), 26 unit do frontend (vitest), 7 E2E (Playwright) e 10 do ETL (pytest). CI no GitHub Actions com integração PostgreSQL e E2E.

## 📊 Domínio da API (Diagrama de Classes)

```classDiagram
  class User {
    -String name
    -Account account
    -Feature[] features
    -Card card
    -News[] news
  }
  class Account {
    -String number
    -String agency
    -Number balance
    -Number limit
  }
  class Feature {
    -String icon
    -String description
  }
  class Card {
    -String number
    -Number limit
  }
  class News {
    -String icon
    -String description
  }
  class AuthUser {
    -String username
    -String password (bcrypt)
    -String role
  }
  class RefreshToken {
    -String tokenHash (SHA-256)
    -DateTime expiresAt
    -boolean revoked
  }
  class AuditLog {
    -String action
    -Long actorUserId
    -String targetEntity
    -Long targetId
    -String details
  }
  User "1" *-- "1" Account
  User "1" *-- "N" Feature
  User "1" *-- "1" Card
  User "1" *-- "N" News
  AuthUser "1" o-- "0..1" User
  AuthUser "1" *-- "N" RefreshToken
```

## 🖥️ Frontend (Angular)

Interface web (Angular + Material) em [frontend/](frontend/), que consome a API via CORS (porta 4200 já liberada).

> **Nota de versão do Node:** o Angular 21 é testado com Node 22 (LTS). O Node 26 funciona, mas o CLI emite um aviso de "unsupported". Recomendado: `nvm use 22`.

```Bash
cd frontend
npm install
npm start          # http://localhost:4200  (requer a API rodando na 8080)
```

Testes do frontend: `npm run test:ci` (vitest, modo CI sem watch).
Testes E2E: `npm run e2e` (Playwright — requer API em `:8080` e `npm start` rodando).

## 🔐 Credenciais de Acesso (seeds)

A API exige autenticação (JWT). Dois usuários são seedados pelo Flyway:

| Usuário | Senha | Role | Vinculado a |
|---------|-------|------|-------------|
| `devweekerson` | `admin123` | ADMIN | Usuário bancário 1 (Devweekerson) |
| `ana` | `senha123` | USER | Usuário bancário 2 (Ana Souza) |

- **ADMIN** pode ler/editar qualquer usuário/conta e acessar `/audit`.
- **USER** só acessa a própria conta e o próprio usuário (403 caso contrário).
- O access token expira em 15min; o frontend renova automaticamente via refresh token.

## 📖 Como Executar o Projeto

### 1. Backend (Java)

Certifique-se de estar usando o `JDK 21`. O perfil `dev` (H2 em arquivo, `./data/sdw2023`) é o padrão — o seed do usuário histórico é aplicado automaticamente pelo Flyway:

```Bash
./gradlew clean bootRun
```

### 2. Pipeline ETL (Python)

O ETL agora autentica contra a API (o `entrypoint.sh` faz login uma vez e compartilha o token). Rodando direto:

```Bash
python3 -m venv venv
source venv/bin/activate
pip install -r requirements.txt
python populate_api.py     # requer API em :8080 + credenciais (ETL_USERNAME/ETL_PASSWORD, default admin)
python main.py
```

## Via Docker (Recomendado) 🚀

Esta opção orquestra automaticamente a **API, o Pipeline ETL e o Frontend**, garantindo resiliência através de healthchecks.

Pré-requisitos: Docker Desktop instalado.

Gerar o artefato Java:
Na raiz do projeto, gere o JAR otimizado para o container:

```Bash
gradle clean bootJar
```

Subir o ecossistema:

```Bash
docker compose up --build
```

Depois de subir:
- **Frontend**: <http://localhost:4200> — faça login com `devweekerson`/`admin123`
- **API (Swagger)**: <http://localhost:8080/swagger-ui/index.html> — use "Authorize" com o token do `/auth/login`
- O pipeline irá aguardar a API ficar saudável, popular os dados e executar o ETL automaticamente.

Para produção-like com **PostgreSQL + Redis** (em vez de H2) — inclui rate limit distribuído:

```Bash
docker compose -f docker-compose.yml -f docker-compose.prd.yml up --build
```
