# SAGA — Guia de Inicialização e Recursos

Ambiente local de estudo do **padrão Saga orquestrado** com **Spring Boot** e **Kafka**: cinco microsserviços, bancos independentes e uma stack completa de observabilidade, tudo orquestrado via Docker Compose.

---

## Sumário

- [Pré-requisitos](#pré-requisitos)
- [Inicialização](#inicialização)
- [Recursos disponíveis](#recursos-disponíveis)
  - [Aplicação](#aplicação)
  - [Mensageria](#mensageria)
  - [Bancos de dados](#bancos-de-dados)
  - [Observabilidade](#observabilidade)
- [Comandos úteis](#comandos-úteis)
- [Solução de problemas](#solução-de-problemas)

---

## Pré-requisitos

| Ferramenta | Versão |
|---|---|
| JDK | 25 |
| Maven | 3.9+ |
| Docker + Docker Compose | recentes |

Portas livres no host: `2181`, `3000`, `3100`, `3200`, `3300`, `4317`, `4318`, `5432`, `5433`, `5434`, `8080`, `8081`, `8090`, `8091`, `8092`, `9090`, `9092`, `27017`.

## Inicialização

### 1. Clonar o repositório

```bash
git clone <url-do-repositorio>
cd saga
```

### 2. Compilar os módulos (se necessário)

Na raiz do projeto, onde está o POM pai `saga-parent`:

```bash
mvn clean install
```

> Necessário caso os `Dockerfile` dos serviços esperem o `.jar` já gerado. Se o build ocorre dentro do Docker, este passo pode ser ignorado.

### 3. Subir o ambiente

```bash
cd .\deploy\
docker-compose up --build -d
```

### 4. Parar

```bash
docker-compose down
```

---

## Recursos disponíveis

### Aplicação

| Recurso | URL / Endereço | Descrição |
|---|---|---|
| **Pedidos** (via load balancer) | http://localhost:3000 | Entrada da aplicação. O nginx distribui entre as **3 réplicas** do `order-service` |
| **Orquestrador** | http://localhost:8080 | Coordena o fluxo da Saga |
| **Produtos** | http://localhost:8090 | `product-service` |
| **Pagamentos** | http://localhost:8091 | `payment-service` |
| **Estoque** | http://localhost:8092 | `inventory-service` |

> O `order-service` não expõe porta direta no host: o acesso é sempre pelo `order-lb` na porta `3000`.


### Bancos de dados

| Banco | Tecnologia | Host:Porta | Database | Usuário / Senha |
|---|---|---|---|---|
| `order-db` | MongoDB | `localhost:27017` | `admin` (auth) | `admin` / `123456` |
| `product-db` | PostgreSQL | `localhost:5432` | `product-db` | `postgres` / `postgres` |
| `payment-db` | PostgreSQL | `localhost:5433` | `payment-db` | `postgres` / `postgres` |
| `inventory-db` | PostgreSQL | `localhost:5434` | `inventory-db` | `postgres` / `postgres` |

Exemplos de conexão:

```bash
# PostgreSQL
psql -h localhost -p 5432 -U postgres -d product-db

# MongoDB
mongosh "mongodb://admin:123456@localhost:27017/admin"
```

> ⚠️ Credenciais **somente para desenvolvimento local**.

### Observabilidade

Os serviços enviam telemetria ao **OpenTelemetry Collector**, que encaminha traces, logs e métricas para os backends abaixo. Tudo é consultado no Grafana.

| Recurso | URL | Descrição |
|---|---|---|
| **Grafana** | http://localhost:3300 | Painel unificado (login `admin` / `admin`) |
| **Prometheus** | http://localhost:9090 | Métricas (receptor OTLP habilitado) |



