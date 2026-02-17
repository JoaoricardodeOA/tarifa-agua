# Tarifa de Agua - API REST

API REST para o gerenciamento de tabelas tarifarias e cálculo de consumo de água, construída com Spring Boot.

## Tecnologias

- Java 17
- Spring Boot 3.5.10
- Spring Data JPA / Hibernate
- PostgreSQL
- Bean Validation (Jakarta)
- Lombok
- JUnit 5 + Mockito

## Pré-requisitos

- **Java 17+** — [Download OpenJDK](https://adoptium.net/)
- **PostgreSQL 14+** — [Download PostgreSQL](https://www.postgresql.org/download/)
- **Maven 3.8+** (ou usar o wrapper `mvnw` incluso no projeto)

Verificar versões instaladas:

```bash
java -version
psql --version
```

## Configuração do Banco de Dados

1. Criar o banco de dados no PostgreSQL:

```sql
CREATE DATABASE tarifa_agua;
```

2. A aplicação usa as seguintes credenciais por padrão (configuráveis em `src/main/resources/application.yaml`):

| Propriedade | Valor padrão                                   |
|-------------|------------------------------------------------|
| URL         | `jdbc:postgresql://localhost:5432/tarifa_agua` |
| Usuario     | `postgres`                                     |
| Senha       | `postgres`                                     |

3. O schema e criado automaticamente pelo Hibernate (`ddl-auto: update`).

## Instalacao e Execucao

```bash
# Clonar o repositorio
git clone <url-do-repositorio>
cd tarifa-agua

# Executar a aplicacao
./mvnw spring-boot:run
```

A API estará disponível em `http://localhost:8080`.

## Endpoints da API

### 1. Criar Tabela Tarifaria

```
POST /api/tabelas-tarifarias
```

Cria uma nova tabela tarifaria. Apenas uma tabela pode estar ativa por vez. Todas as 4 categorias sao obrigatórias: `COMERCIAL`, `INDUSTRIAL`, `PARTICULAR`, `PUBLICO`.

**Request:**

```json
{
  "nome": "Tabela 2025",
  "categorias": [
    {
      "categoria": "COMERCIAL",
      "faixas": [
        { "inicio": 0, "fim": 10, "valorUnitario": 5.00 },
        { "inicio": 11, "fim": 20, "valorUnitario": 8.50 },
        { "inicio": 21, "fim": null, "valorUnitario": 12.00 }
      ]
    },
    {
      "categoria": "INDUSTRIAL",
      "faixas": [
        { "inicio": 0, "fim": 15, "valorUnitario": 7.00 },
        { "inicio": 16, "fim": null, "valorUnitario": 15.00 }
      ]
    },
    {
      "categoria": "PARTICULAR",
      "faixas": [
        { "inicio": 0, "fim": 10, "valorUnitario": 4.00 },
        { "inicio": 11, "fim": null, "valorUnitario": 9.00 }
      ]
    },
    {
      "categoria": "PUBLICO",
      "faixas": [
        { "inicio": 0, "fim": 20, "valorUnitario": 3.00 },
        { "inicio": 21, "fim": null, "valorUnitario": 6.00 }
      ]
    }
  ]
}
```

**Response — 201 Created:**

```json
{
  "id": 1,
  "dataVigencia": "2025-06-15T10:30:00",
  "mensagem": "Tabela criada com sucesso"
}
```

**Response — 400 Bad Request (tabela ativa já existe):**

```json
{
  "timestamp": "2025-06-15T10:31:00",
  "status": 400,
  "message": "Já existe uma tabela ativa com id: 1. Desative-a antes de criar uma nova."
}
```

**Regras de validação das faixas:**
- Devem iniciar em `0`
- Não podem ter lacunas nem sobreposicao entre faixas
- A última faixa deve ter `fim: null` (consumo ilimitado)
- `valorUnitario` não pode ser negativo

---

### 2. Listar Tabelas Tarifarias (paginado)

```
GET /api/tabelas-tarifarias?page=0&size=10&sort=dataVigencia,desc
```

Retorna todas as tabelas tarifárias com paginacao.

**Parâmetros de query (opcionais):**

| Parâmetro | Padrão | Descrição                   |
|-----------|--------|-----------------------------|
| `page`    | `0`    | Número da pagina            |
| `size`    | `20`   | Itens por pagina            |
| `sort`    | —      | Campo e direção de ordenação |

**Response — 200 OK:**

```json
{
  "content": [
    {
      "id": 1,
      "nome": "Tabela 2025",
      "dataVigencia": "2025-06-15T10:30:00",
      "ativa": true,
      "categorias": [
        {
          "categoria": "COMERCIAL",
          "faixas": [
            { "inicio": 0, "fim": 10, "valorUnitario": 5.00 },
            { "inicio": 11, "fim": 20, "valorUnitario": 8.50 },
            { "inicio": 21, "fim": null, "valorUnitario": 12.00 }
          ]
        },
        {
          "categoria": "INDUSTRIAL",
          "faixas": [
            { "inicio": 0, "fim": 15, "valorUnitario": 7.00 },
            { "inicio": 16, "fim": null, "valorUnitario": 15.00 }
          ]
        }
      ]
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 1,
  "totalPages": 1,
  "last": true,
  "first": true
}
```

---

### 3. Desativar Tabela Tarifaria

```
DELETE /api/tabelas-tarifarias/{id}
```

Desativa uma tabela tarifaria (soft delete). A tabela não é removida do banco, apenas marcada como inativa.

**Response — 200 OK:**

```json
{
  "id": 1,
  "ativa": false,
  "mensagem": "Tabela desativada com sucesso"
}
```

**Response — 400 Bad Request (tabela já inativa):**

```json
{
  "timestamp": "2025-06-15T10:35:00",
  "status": 400,
  "message": "Tabela já está inativa."
}
```

**Response — 400 Bad Request (tabela não encontrada):**

```json
{
  "timestamp": "2025-06-15T10:35:00",
  "status": 400,
  "message": "Tabela não encontrada"
}
```

---

### 4. Calcular Consumo de Agua

```
POST /api/calculos
```

Calcula o valor do consumo de água com base na tabela tarifaria ativa e na categoria do consumidor.

**Request:**

```json
{
  "categoria": "COMERCIAL",
  "consumo": 25
}
```

**Response — 200 OK:**

```json
{
  "categoria": "COMERCIAL",
  "consumoTotal": 25,
  "valorTotal": 198.50,
  "detalhamento": [
    {
      "faixa": { "inicio": 0, "fim": 10 },
      "m3Cobrados": 10,
      "valorUnitario": 5.00,
      "subtotal": 50.00
    },
    {
      "faixa": { "inicio": 11, "fim": 20 },
      "m3Cobrados": 9,
      "valorUnitario": 8.50,
      "subtotal": 76.50
    },
    {
      "faixa": { "inicio": 21, "fim": null },
      "m3Cobrados": 6,
      "valorUnitario": 12.00,
      "subtotal": 72.00
    }
  ]
}
```

**Response — 400 Bad Request (sem tabela ativa):**

```json
{
  "timestamp": "2025-06-15T10:40:00",
  "status": 400,
  "message": "Não existe tabela ativa."
}
```

**Categorias validas:** `COMERCIAL`, `INDUSTRIAL`, `PARTICULAR`, `PUBLICO`

---

## Testes

### Executar todos os testes

```bash
./mvnw test
```

### Executar apenas testes unitários dos services

```bash
./mvnw test -Dtest="CalculoServiceTest,TabelaTarifariaServiceTest"
```

### Cobertura de testes

Os testes unitários cobrem:

- **CalculoService** — calculo com múltiplas faixas, consumo zero, categoria inválida, categoria não encontrada, lowercase
- **TabelaTarifariaService** — criação com tabela ativa existente, categorias duplicadas, faltando ou inválidas, validação de faixas (lacunas, sobreposição, valores negativos, inicio diferente de zero), desativação de tabela

## Estrutura do Projeto

```
src/main/java/com/desafio_tecnico/tarifa_agua/
├── controller/
│   ├── CalculoController.java
│   └── TabelaTarifariaController.java
├── dto/
│   ├── CalculoRequest.java
│   ├── CalculoResponse.java
│   ├── CategoriaDTO.java
│   ├── CategoriaResponse.java
│   ├── DetalhamentoResponse.java
│   ├── FaixaDTO.java
│   ├── FaixaInfo.java
│   ├── FaixaResponse.java
│   ├── TabelaTarifariaCriadaResponse.java
│   ├── TabelaTarifariaExclusaoResponse.java
│   ├── TabelaTarifariaRequest.java
│   └── TabelaTarifariaResponse.java
├── entity/
│   ├── CategoriaConsumidor.java
│   ├── FaixaConsumo.java
│   ├── TabelaTarifaria.java
│   └── TarifaCategoria.java
├── exception/
│   ├── BusinessException.java
│   ├── ErrorResponse.java
│   └── GlobalExceptionHandler.java
├── repository/
│   ├── FaixaConsumoRepository.java
│   ├── TabelaTarifariaRepository.java
│   └── TarifaCategoriaRepository.java
└── service/
    ├── CalculoService.java
    └── TabelaTarifariaService.java
```
