# Health Tech


Sistema de agendamento para uma clínica de bairro: a recepção marca e cancela consultas dos pacientes com cada profissional de saúde, e o sistema impede conflito de horário, vaga duplicada e consulta fora do expediente.

Projeto Final da disciplina de Backend (Engenharia de Software, SATC). **Etapa 1: arquitetura do backend.**

## Índice

- [Integrantes](#integrantes)
- [O problema](#o-problema)
- [Tecnologias](#tecnologias)
- [Arquitetura](#arquitetura)
- [Entidades](#entidades)
- [Rotas](#rotas)
- [Como executar](#como-executar)
- [Situação do projeto](#situação-do-projeto)
- [Documentação](#documentação)

## Integrantes

| Nome | GitHub |
|---|---|
| Mateo Ramiro Zapelini | [@mateoramiro](https://github.com/mateoramiro) |
| Caua Medeiros | [@caua-mr](https://github.com/caua-mr) |
| NOME COMPLETO | [@USUARIO-DO-3º-INTEGRANTE](https://github.com/USUARIO-DO-3º-INTEGRANTE) |

## O problema

**Tema sorteado:** Saúde e Bem-Estar

Numa clínica pequena de bairro, a agenda dos profissionais fica num caderno e em conversas de WhatsApp. A recepcionista confere a página à mão, e é assim que acontece de dois pacientes serem marcados no mesmo horário com o mesmo médico, de uma consulta ser marcada no sábado, quando ninguém atende, e de uma mesma pessoa pegar duas vagas de cardiologia enquanto outros pacientes esperam semanas por uma. Quando alguém desmarca em cima da hora, a vaga se perde, porque ninguém consegue ocupá-la a tempo. O Health Tech guarda profissionais, pacientes e consultas, e só deixa a consulta ser marcada ou cancelada se o pedido respeitar as regras da clínica.

**Quem usa o sistema:**

| Quem | O que faz no sistema |
|---|---|
| Recepcionista | Cadastra profissionais e pacientes, marca consultas e cancela quando o paciente desiste |
| Profissional de saúde | Atende nos horários que a recepção marcou (a consulta da própria agenda fica para a próxima etapa) |
| Paciente | Pede a consulta à recepção; no futuro, consulta os próprios horários |

**Regras de negócio.** As quatro regras saíram dos problemas do caderno: o conflito de horário, a vaga duplicada, a consulta fora do expediente e a vaga perdida no cancelamento tardio.

| # | Regra | Onde está no código | Rota que usa |
|---|---|---|---|
| R1 | Um profissional não pode ter duas consultas agendadas no mesmo horário | `ConsultaService.agendar` | `POST /consultas` |
| R2 | Um paciente só pode ter uma consulta agendada por especialidade ao mesmo tempo. Para marcar outra, precisa cancelar a primeira | `ConsultaService.agendar` | `POST /consultas` |
| R3 | A clínica atende de segunda a sexta, das 08:00 às 18:00, e as consultas começam na hora cheia ou na meia hora (a última começa às 17:30) | `ConsultaService.agendar` | `POST /consultas` |
| R4 | O cancelamento precisa ser feito com pelo menos 2 horas de antecedência da consulta | `ConsultaService.cancelar` | `PUT /consultas/{id}/cancelar` |

Quando uma regra barra um pedido, a API devolve `400` com a regra explicada na mensagem. Além das quatro regras, o cadastro de profissional recusa um registro profissional que já exista (também `400`), porque o registro no conselho identifica o profissional.

**Fora do escopo:** lembrete de consulta por e-mail ou SMS, prontuário, convênios e pagamento, fila de espera, remarcação (por enquanto, cancela e marca de novo) e tela de frontend.

## Tecnologias

| O quê | Qual |
|---|---|
| Linguagem | Java 25 |
| Framework | Spring Boot 3.5 (Web, Validation, Data JPA) |
| Banco de dados | PostgreSQL 16, em Docker (ou PostgreSQL instalado no computador, veja "Como executar") |
| Migrations | Flyway |
| Documentação da API | springdoc-openapi (Swagger UI) |

## Arquitetura

A API é organizada em camadas. Uma requisição entra pelo Controller, passa pelo Service, que aplica as regras de negócio, e chega ao banco pelo Repository; a resposta faz o caminho de volta. O Mapper converte DTO em entidade e entidade em DTO.

**Mapa da estrutura:**

| Entidade | Migration | Model | Repository | DTOs | Mapper | Service | Controller |
|---|---|---|---|---|---|---|---|
| Profissional | `V1__criar_profissionais.sql`, pronta | `Profissional`, pronta | `ProfissionalRepository`, pronto | `ProfissionalRequestDTO`, `ProfissionalResponseDTO`, prontos | `ProfissionalMapper`, implementado | `ProfissionalService`, implementado | `ProfissionalController`, implementado |
| Paciente | `V2__criar_pacientes.sql`, pronta | `Paciente`, pronta | `PacienteRepository`, pronto | `PacienteRequestDTO`, `PacienteResponseDTO`, prontos | `PacienteMapper`, implementado | `PacienteService`, em parte: só o cadastro | `PacienteController`, em parte: só o `POST` |
| Consulta | `V3__criar_consultas.sql`, pronta | `Consulta` e `StatusConsulta`, prontas | `ConsultaRepository`, pronto | `ConsultaRequestDTO`, `ConsultaResponseDTO`, prontos | `ConsultaMapper`, implementado | `ConsultaService`, implementado (R1 a R4) | `ConsultaController`, implementado |

Também fazem parte do projeto, vindas do template: `ApiExceptionHandler`, `ErroDTO`, `CampoErroDTO`, `RecursoNaoEncontradoException`, `RegraDeNegocioException` e `OpenApiConfig`. Cada entidade tem a sua exceção de "não encontrado": `ProfissionalNaoEncontradoException`, `PacienteNaoEncontradoException` e `ConsultaNaoEncontradaException`.

Do paciente, só o cadastro foi feito, porque é o que as regras precisam: sem paciente cadastrado, não há consulta. As consultas de pacientes (listar e buscar) ficam para a próxima etapa.

## Entidades

| Entidade | O que representa | Como se relaciona |
|---|---|---|
| Profissional | Quem atende na clínica, com a especialidade e o registro no conselho | Aparece em várias consultas |
| Paciente | Uma pessoa cadastrada que pode marcar consulta | Faz várias consultas |
| Consulta | O horário marcado entre um paciente e um profissional, com a situação (agendada, cancelada ou realizada) | Pertence a um paciente e a um profissional |

Atributos, tipos e o diagrama das tabelas estão em [docs/modelo-de-dominio.md](docs/modelo-de-dominio.md).

## Rotas

### As que já funcionam

| Verbo | Caminho | O que faz | Regra | Sucesso | Erros |
|---|---|---|---|---|---|
| `POST` | `/profissionais` | Cadastra um profissional | Registro não repetido | `201` | `400` |
| `GET` | `/profissionais` | Lista os profissionais | Nenhuma | `200` | Nenhum |
| `GET` | `/profissionais/{id}` | Busca um profissional pelo id | Nenhuma | `200` | `404` |
| `POST` | `/pacientes` | Cadastra um paciente | Nenhuma | `201` | `400` |
| `POST` | `/consultas` | Marca uma consulta | R1, R2 e R3 | `201` | `400`, `404` |
| `PUT` | `/consultas/{id}/cancelar` | Cancela a consulta e libera o horário | R4 | `200` | `400`, `404` |

Exemplo de requisição:

```http
POST /consultas
Content-Type: application/json

{
  "pacienteId": 1,
  "profissionalId": 1,
  "dataHora": "2026-10-20T09:30:00"
}
```

Resposta, `201 Created`. Entram os ids; paciente e profissional voltam completos, e o status nasce `AGENDADA`:

```json
{
  "id": 1,
  "paciente": {
    "id": 1,
    "nome": "Marcos Oliveira",
    "email": "marcos.oliveira@email.com",
    "telefone": "48991234567"
  },
  "profissional": {
    "id": 1,
    "nome": "Dra. Helena Martins",
    "especialidade": "Cardiologia",
    "registroProfissional": "CRM-SC 48213"
  },
  "dataHora": "2026-10-20T09:30:00",
  "status": "AGENDADA"
}
```

### Erros

Todo erro sai no mesmo formato. Este é o `400` da regra R1, quando alguém tenta marcar um horário que o profissional já ocupou:

```json
{
  "status": 400,
  "erro": "Bad Request",
  "mensagem": "R1: o profissional Dra. Helena Martins já tem uma consulta agendada nesse horário",
  "caminho": "/consultas",
  "timestamp": "2026-10-13T19:30:00",
  "campos": []
}
```

Um campo inválido também devolve `400`, com a lista `campos` dizendo qual campo corrigir. Um profissional, paciente ou consulta que não existe devolve `404`.

### As planejadas

Estão no contrato, mas ainda não respondem.

| Recurso | O que vai fazer |
|---|---|
| `GET /pacientes` e `GET /pacientes/{id}` | Listar e buscar pacientes |
| `GET /consultas` e `GET /consultas/{id}` | Listar e buscar consultas |
| `PUT /consultas/{id}/concluir` | Registrar que o paciente foi atendido (status `REALIZADA`) |
| `PUT` e `DELETE` de `/profissionais` e `PUT` de `/pacientes` | Atualizar e remover cadastros |

O contrato completo, com todas as rotas e os DTOs, está em [docs/contrato-da-api.md](docs/contrato-da-api.md).

## Como executar

Pré-requisitos: JDK 25, Docker Desktop aberto e Git.

```bash
git clone https://github.com/mateoramiro/healthtech.git
cd healthtech

docker compose up -d          # sobe o PostgreSQL na porta 5437
./mvnw spring-boot:run        # Windows: .\mvnw.cmd spring-boot:run
```

Com a API no ar:

| Endereço | O que é |
|---|---|
| <http://localhost:8080/swagger-ui.html> | Swagger UI: as rotas, com **Try it out** para enviar requisições |
| <http://localhost:8080/v3/api-docs> | O contrato OpenAPI em JSON |

Para testar as rotas, use o Swagger UI ou o **Postman**. No Postman, importe todas as rotas de uma vez: **Import** → **Link** → `http://localhost:8080/v3/api-docs`.

Para ver as regras funcionando, siga esta ordem (use uma data futura, de segunda a sexta, nas consultas):

1. Cadastre dois profissionais da mesma especialidade (`POST /profissionais`, por exemplo dois cardiologistas) e um de outra (uma nutricionista).
2. Cadastre dois pacientes (`POST /pacientes`).
3. Marque uma consulta do primeiro paciente com o primeiro cardiologista (`POST /consultas`).
4. Tente marcar o segundo paciente com o mesmo cardiologista no mesmo horário: a **R1** barra.
5. Tente marcar o primeiro paciente com o segundo cardiologista: a **R2** barra, porque ele já tem cardiologia. Com a nutricionista, passa.
6. Tente uma data de sábado, ou um horário como `09:15`: a **R3** barra.
7. Cancele a primeira consulta (`PUT /consultas/{id}/cancelar`) e repita o passo 5: agora passa. A **R4** barra o cancelamento de uma consulta que começa em menos de 2 horas.

Para parar: `Ctrl+C` na API e `docker compose stop` no banco. Para apagar o banco e rodar as migrations do zero: `docker compose down -v` e suba de novo.

### Sem Docker (PostgreSQL instalado no computador)

Use esta opção se o Docker não roda na sua máquina (por exemplo, um Windows sem suporte a virtualização). Pré-requisitos: JDK 25, Git e o PostgreSQL instalado, com um cliente como o DBeaver ou o pgAdmin.

1. Com o PostgreSQL rodando na porta `5432`, abra um editor SQL conectado como `postgres` e rode, como **script**, o que o `docker-compose.yml` faria:

   ```sql
   CREATE USER consultacerta WITH PASSWORD 'consultacerta';
   DROP DATABASE consultacerta WITH (FORCE);
   CREATE DATABASE consultacerta OWNER consultacerta;
   ALTER DATABASE consultacerta RENAME TO healthtech;
   SELECT datname FROM pg_database;
   ```

   Não crie tabelas: o Flyway cria as três tabelas quando a API sobe.
2. Em `src/main/resources/application.properties`, troque a porta na primeira linha de `5437` para `5432`:

   ```
   spring.datasource.url=jdbc:postgresql://localhost:5432/healthtech
   ```
3. Suba a API, dentro da pasta do projeto: `./mvnw spring-boot:run` (Windows: `.\mvnw.cmd spring-boot:run`).

Se aparecer `Migration checksum mismatch`, o banco já tinha migrations de outra cópia do projeto. Apague o banco e crie de novo (`DROP DATABASE healthtech WITH (FORCE);`, conectado como `postgres`, e depois `CREATE DATABASE healthtech OWNER consultacerta;`). Se aparecer `Connection refused` na porta `5437`, a troca do passo 2 não foi feita.

Antes de entregar ou de subir pelo Docker, volte a porta para `5437`.

## Situação do projeto

| Parte | Situação |
|---|---|
| Desenho | Completo: problema e regras neste README, modelo e contrato em `docs/` |
| Estrutura | As 3 entidades têm migration, entidade, Repository, DTOs, Mapper, Service e Controller |
| API e regras | Profissionais com as 3 rotas da API simples; cadastro de pacientes; marcação e cancelamento de consultas com R1, R2, R3 e R4 implementadas |

**Ainda não funciona:** as rotas da seção "As planejadas": listar e buscar pacientes e consultas, concluir consulta, e atualizar ou remover cadastros.

## Documentação

| Documento | O que tem |
|---|---|
| [Modelo de domínio](docs/modelo-de-dominio.md) | Entidades, relacionamentos, diagrama das tabelas, migrations |
| [Contrato da API](docs/contrato-da-api.md) | Todas as rotas, os DTOs, exemplos de JSON e erros |
| [Autoavaliação](docs/autoavaliacao.md) | Os critérios da revisão do código, marcados pelo grupo |
