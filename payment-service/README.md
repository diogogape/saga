# payment-service

Microsserviço de **pagamento** da SAGA orquestrada. Não tem API REST: ele é
**orientado a eventos** — consome comandos do orquestrador via Apache Kafka,
realiza/estorna o pagamento, persiste o estado no PostgreSQL e devolve o
resultado para o orquestrador.

- **Java 17** · **Spring Boot 4** · **Spring Kafka** · **Spring Data JPA / PostgreSQL** · **Lombok**
- Porta HTTP: `8091` (somente actuator/erros — não há endpoints de negócio)

---

## Papel na saga

```
                    (payment-success)
 orchestrator  ───────────────────────▶  payment-service ── processPayment()
      ▲                                          │  cobra e salva (SUCCESS)
      │  (orchestrator)                          ▼
      └──────────────────────────────────  publica o evento de volta

                    (payment-fail)
 orchestrator  ───────────────────────▶  payment-service ── rollbackPayment()
      ▲                                          │  estorna (REFUND)
      │  (orchestrator)                          ▼
      └──────────────────────────────────  publica o evento (status FAIL)
```

| Tópico Kafka      | Direção  | Ação no serviço                          |
|-------------------|----------|------------------------------------------|
| `payment-success` | consome  | `processPayment` — cobra o pedido        |
| `payment-fail`    | consome  | `rollbackPayment` — estorna (compensação)|
| `orchestrator`    | publica  | resultado da etapa (SUCCESS / ROLLBACK_PENDING / FAIL) |

---

## Estrutura de pacotes (Arquitetura Hexagonal)

O código segue **Ports & Adapters**. A regra de ouro é: **as dependências
apontam para dentro**. O `domain` não conhece ninguém; o `application` conhece
só o `domain`; o `infrastructure` conhece os dois e implementa as portas.

```
com.saga.payment
│
├── PaymentServiceApplication           # bootstrap do Spring Boot
│
├── domain/                             # NÚCLEO — regras de negócio puras (sem Spring/JPA/Kafka)
│   ├── model/                          #   Payment, Event, Order, OrderProduct, Product, History
│   ├── enums/                          #   EPaymentStatus, ESagaStatus
│   └── exception/                      #   ValidationException
│
├── application/                        # CASOS DE USO — orquestra o domínio através de portas
│   ├── port/
│   │   ├── in/                         #   ProcessPaymentUseCase, RollbackPaymentUseCase   (o que o serviço faz)
│   │   └── out/                        #   PaymentRepositoryPort, EventPublisherPort       (o que o serviço precisa)
│   └── service/
│       └── PaymentService              #   implementa os use cases; depende SÓ de portas
│
└── infrastructure/                     # ADAPTADORES — a tecnologia concreta
    ├── adapter/
    │   ├── in/messaging/
    │   │   └── KafkaConsumer           #   entrada: ouve os tópicos e chama os use cases
    │   └── out/
    │       ├── messaging/
    │       │   └── KafkaProducer       #   saída: implementa EventPublisherPort (publica no Kafka)
    │       └── persistence/
    │           ├── PaymentEntity       #   modelo @Entity (JPA)
    │           ├── PaymentRepository   #   Spring Data JPA
    │           └── PaymentPersistenceAdapter  # implementa PaymentRepositoryPort (mapeia entidade↔domínio)
    ├── config/
    │   ├── kafka/   KafkaConfig        #   beans de produtor/consumidor e criação de tópicos
    │   └── exception/  ExceptionGlobalHandler, ExceptionDetails
    └── utils/   JsonUtil               #   serialização JSON (Jackson)
```

### Como ler o fluxo de uma chamada

1. `KafkaConsumer` (entrada) recebe o JSON do tópico, desserializa com `JsonUtil`
   e chama a **porta de entrada** (`ProcessPaymentUseCase` / `RollbackPaymentUseCase`).
2. `PaymentService` (aplicação) coordena o caso de uso usando o `domain`
   (`Payment.createPending/validatePayment/refund`) e as **portas de saída**.
3. `PaymentPersistenceAdapter` (saída) salva/consulta via JPA; `KafkaProducer`
   (saída) publica o resultado no tópico `orchestrator`.

> Por que isso importa: o núcleo (`domain` + `application`) não importa Kafka,
> JPA nem Jackson. Dá para trocar a tecnologia mexendo só em `infrastructure`,
> e dá para testar o núcleo sem subir Spring/banco/broker.

---

## Como executar localmente

### Pré-requisitos
- **JDK 17+** (o build usa o `mvnw` incluso — não precisa instalar Maven)
- **Docker** + **Docker Compose** (para Kafka e PostgreSQL)

> Todos os comandos abaixo assumem que você está **na raiz do repositório**
> (onde fica o `docker-compose.yml`), salvo indicação contrária.

### 1. Subir a infraestrutura (Kafka + banco do pagamento)

```bash
docker compose up -d kafka payment-db
```

Isso disponibiliza:
- **Kafka** em `localhost:9092`
- **payment-db** (PostgreSQL) em `localhost:5433` (banco `payment-db`, user/senha `postgres`)
- (opcional) **Redpanda Console** para inspecionar os tópicos: `docker compose up -d redpanda-console` → http://localhost:8081

### 2. Rodar o serviço

```bash
cd payment-service
./mvnw spring-boot:run          # Linux/macOS
# .\mvnw.cmd spring-boot:run    # Windows (PowerShell)
```

O serviço sobe em **http://localhost:8091** e cria os tópicos
(`payment-success`, `payment-fail`, `orchestrator`) automaticamente.

Os valores padrão (em `src/main/resources/application.properties`) já apontam
para a infra acima, então **não é preciso configurar nada** para o cenário local.

### Configuração (variáveis de ambiente)

Para apontar para outra infra, sobrescreva via variáveis de ambiente:

| Variável      | Padrão            | Descrição                          |
|---------------|-------------------|------------------------------------|
| `KAFKA_BROKER`| `localhost:9092`  | endereço do broker Kafka           |
| `DB_HOST`     | `localhost`       | host do PostgreSQL                 |
| `DB_PORT`     | `5433`            | porta do PostgreSQL                |
| `DB_NAME`     | `payment-db`      | nome do banco                      |
| `DB_USER`     | `postgres`        | usuário                            |
| `DB_PASSWORD` | `postgres`        | senha                              |

### 3. (Opcional) Subir a stack completa

O `payment-service` sozinho fica ocioso esperando eventos. Para ver a saga
ponta a ponta (criar um pedido e acompanhar o pagamento), suba tudo:

```bash
docker compose up -d --build
```

Depois crie um pedido pelo `order-service` (http://localhost:3000) e acompanhe
as mensagens no Redpanda Console (http://localhost:8081).

---

## Testes

```bash
cd payment-service
./mvnw test          # Linux/macOS
# .\mvnw.cmd test    # Windows
```

A suíte é unitária/integração e **não precisa de Docker**:
- **Domínio e aplicação**: testes unitários puros (aplicação com mocks das portas via Mockito).
- **Persistência** (`PaymentPersistenceAdapterTest`): integração com `@DataJpaTest` + **H2** em memória.
- **Mensageria e serialização**: testes unitários do `KafkaConsumer`, `KafkaProducer` e `JsonUtil`.

---

## Build do artefato

```bash
cd payment-service
./mvnw clean package         # gera target/payment-service-0.0.1-SNAPSHOT.jar
java -jar target/payment-service-0.0.1-SNAPSHOT.jar
```
