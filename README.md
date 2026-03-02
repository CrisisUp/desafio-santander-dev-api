# Santander Dev Week 2026 - API & ETL Pipeline

Esta é uma evolução do projeto original da Santander Dev Week, agora integrando uma RESTful API robusta em Java com um Pipeline de Dados (ETL) inteligente em Python.

## 🚀 Novidades desta Versão (DevOps & Data)

Diferente da versão original, este repositório foca na automação de dados e resiliência de infraestrutura:

* **Ambiente M4 & Java 21:** Configuração otimizada para o chip Apple M4, utilizando Java 21 (LTS) para garantir compatibilidade total com o Spring Boot 3.4.

* **Data Seeding Automatizado:** Script Python para população em massa do banco de dados H2, permitindo testes de carga e processamento em lote.

* **Pipeline ETL:** Extração automatizada de dados da API, transformação de mensagens de marketing (segmentação de saldo) e carregamento em arquivos de saída JSON.

## 🛠️ Tecnologias Utilizadas

* **Java 21 & Spring Boot 3:** Backend robusto com Spring Data JPA.

* **Python 3.14:** Engine do pipeline ETL utilizando as bibliotecas requests e pandas.

* **OpenAPI (Swagger):** Documentação interativa disponível em <http://localhost:8080/swagger-ui.html>.

* **H2 Database:** Banco de dados em memória para desenvolvimento ágil.

## 📊 Domínio da API (Diagrama de Classes)

Snippet de código

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
  User "1" *-- "1" Account
  User "1" *-- "N" Feature
  User "1" *-- "1" Card
  User "1" *-- "N" News
```

## 📖 Como Executar o Projeto

### 1. Backend (Java)

Certifique-se de estar usando o `JDK 21`:

```Bash
./gradlew clean bootRun
```

### 2. Pipeline ETL (Python)

Navegue até a pasta etl-pipeline, ative o ambiente virtual e execute o processamento:

```Bash
python3 -m venv venv
source venv/bin/activate
pip install -r requirements.txt
python populate_api.py
python main.py
```

## Via Docker (Recomendado) 🚀

Esta opção orquestra automaticamente a API e o Pipeline, garantindo resiliência através de healthchecks.

Pré-requisitos: Docker Desktop instalado.

Gerar o artefato Java:
Na raiz do projeto, gere o JAR otimizado para o container:

```Bash
gradle clean bootJar
```

Subir o ecossistema:

```Bash
docker-compose up --build
```

O pipeline irá aguardar a API ficar saudável, popular os dados e executar o ETL automaticamente.
