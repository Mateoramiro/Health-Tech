# Contrato da API

## Convenções

| Item | Valor |
|---|---|
| Endereço base | `http://localhost:8080` |
| Formato | JSON, na requisição e na resposta |
| Datas | `yyyy-MM-ddTHH:mm:ss` (dia e hora, sem fuso) |
| Erros | Sempre no formato `ErroDTO` (ver [Erros](#erros)) |
| Documentação viva | `http://localhost:8080/swagger-ui.html` |

## Rotas

Coluna **Funciona**: `sim` para as rotas que já respondem nesta etapa. Todas as rotas abaixo estão implementadas.

### Profissionais

| Verbo | Caminho | O que faz | Entrada | Saída | Sucesso | Erros | Funciona |
|---|---|---|---|---|---|---|---|
| `POST` | `/profissionais` | Cadastra um profissional. Recusa registro profissional repetido | `ProfissionalRequestDTO` | `ProfissionalResponseDTO` | `201` | `400` | sim |
| `GET` | `/profissionais` | Lista os profissionais | Nenhuma | lista de `ProfissionalResponseDTO` | `200` | Nenhum | sim |
| `GET` | `/profissionais/{id}` | Busca um profissional | Nenhuma | `ProfissionalResponseDTO` | `200` | `404` | sim |
| `PUT` | `/profissionais/{id}` | Atualiza um profissional. O registro não pode ser o de outro profissional | `ProfissionalRequestDTO` | `ProfissionalResponseDTO` | `200` | `400`, `404` | sim |
| `DELETE` | `/profissionais/{id}` | Remove um profissional. Não remove quem já tem consultas | Nenhuma | Nenhuma | `204` | `400`, `404` | sim |

### Pacientes

| Verbo | Caminho | O que faz | Entrada | Saída | Sucesso | Erros | Funciona |
|---|---|---|---|---|---|---|---|
| `POST` | `/pacientes` | Cadastra um paciente. Sem paciente não há consulta | `PacienteRequestDTO` | `PacienteResponseDTO` | `201` | `400` | sim |
| `GET` | `/pacientes` | Lista os pacientes | Nenhuma | lista de `PacienteResponseDTO` | `200` | Nenhum | sim |
| `GET` | `/pacientes/{id}` | Busca um paciente | Nenhuma | `PacienteResponseDTO` | `200` | `404` | sim |
| `PUT` | `/pacientes/{id}` | Atualiza um paciente | `PacienteRequestDTO` | `PacienteResponseDTO` | `200` | `400`, `404` | sim |

### Consultas

As rotas de marcar e cancelar nascem das regras de negócio do README e já funcionam.

| Verbo | Caminho | O que faz | Regra | Entrada | Saída | Sucesso | Erros | Funciona |
|---|---|---|---|---|---|---|---|---|
| `POST` | `/consultas` | Marca uma consulta | R1, R2 e R3 | `ConsultaRequestDTO` | `ConsultaResponseDTO` | `201` | `400`, `404` | sim |
| `GET` | `/consultas` | Lista as consultas, da mais próxima para a mais distante | Nenhuma | Nenhuma | lista de `ConsultaResponseDTO` | `200` | Nenhum | sim |
| `GET` | `/consultas/{id}` | Busca uma consulta | Nenhuma | Nenhuma | `ConsultaResponseDTO` | `200` | `404` | sim |
| `PUT` | `/consultas/{id}/cancelar` | Cancela a consulta e libera o horário | R4 | Nenhuma | `ConsultaResponseDTO` | `200` | `400`, `404` | sim |
| `PUT` | `/consultas/{id}/concluir` | Registra que o paciente foi atendido (`REALIZADA`). Só consulta agendada | Nenhuma | Nenhuma | `ConsultaResponseDTO` | `200` | `400`, `404` | sim |

## DTOs

| DTO | Direção | Campos | Validações |
|---|---|---|---|
| `ProfissionalRequestDTO` | entrada | `nome`, `especialidade`, `registroProfissional` | os três obrigatórios; `nome` até 255, `especialidade` até 100 e `registroProfissional` até 30 caracteres |
| `ProfissionalResponseDTO` | saída | `id`, `nome`, `especialidade`, `registroProfissional` | Nenhuma |
| `PacienteRequestDTO` | entrada | `nome`, `email`, `telefone` | os três obrigatórios; `nome` e `email` até 255 caracteres; `email` em formato válido; `telefone` com 10 ou 11 números |
| `PacienteResponseDTO` | saída | `id`, `nome`, `email`, `telefone` | Nenhuma |
| `ConsultaRequestDTO` | entrada | `pacienteId`, `profissionalId`, `dataHora` | os três obrigatórios; `dataHora` no futuro |
| `ConsultaResponseDTO` | saída | `id`, `paciente`, `profissional`, `dataHora`, `status` | Nenhuma. `status` é `AGENDADA`, `CANCELADA` ou `REALIZADA`, e muda pelas regras R4 e pela conclusão |

## Exemplos

### `POST /profissionais`

Requisição:

```json
{
  "nome": "Dra. Helena Martins",
  "especialidade": "Cardiologia",
  "registroProfissional": "CRM-SC 48213"
}
```

Resposta, `201 Created`:

```json
{
  "id": 1,
  "nome": "Dra. Helena Martins",
  "especialidade": "Cardiologia",
  "registroProfissional": "CRM-SC 48213"
}
```

### `POST /pacientes`

Requisição:

```json
{
  "nome": "Marcos Oliveira",
  "email": "marcos.oliveira@email.com",
  "telefone": "48991234567"
}
```

Resposta, `201 Created`:

```json
{
  "id": 1,
  "nome": "Marcos Oliveira",
  "email": "marcos.oliveira@email.com",
  "telefone": "48991234567"
}
```

### `POST /consultas`

Entram só os ids; paciente e profissional voltam completos, e o status nasce `AGENDADA`.

Requisição:

```json
{
  "pacienteId": 1,
  "profissionalId": 1,
  "dataHora": "2026-10-20T09:30:00"
}
```

Resposta, `201 Created`:

```json
{
  "id": 1,
  "paciente": { "id": 1, "nome": "Marcos Oliveira", "email": "marcos.oliveira@email.com", "telefone": "48991234567" },
  "profissional": { "id": 1, "nome": "Dra. Helena Martins", "especialidade": "Cardiologia", "registroProfissional": "CRM-SC 48213" },
  "dataHora": "2026-10-20T09:30:00",
  "status": "AGENDADA"
}
```

### `PUT /consultas/{id}/cancelar`

Sem corpo de entrada. Resposta, `200 OK`, com o mesmo formato de saída e o status trocado:

```json
{
  "id": 1,
  "paciente": { "id": 1, "nome": "Marcos Oliveira", "email": "marcos.oliveira@email.com", "telefone": "48991234567" },
  "profissional": { "id": 1, "nome": "Dra. Helena Martins", "especialidade": "Cardiologia", "registroProfissional": "CRM-SC 48213" },
  "dataHora": "2026-10-20T09:30:00",
  "status": "CANCELADA"
}
```

## Erros

Todo erro sai no mesmo formato. Este é o `404` de `GET /profissionais/999`:

```json
{
  "status": 404,
  "erro": "Not Found",
  "mensagem": "Profissional não encontrado: 999",
  "caminho": "/profissionais/999",
  "timestamp": "2026-10-13T19:30:00",
  "campos": []
}
```

No `400` de validação, a lista `campos` traz um item por campo inválido: `{"campo": "nome", "mensagem": "Nome é obrigatório"}`.

Quando uma regra de negócio barra o pedido, o `400` vem com a regra na `mensagem` e `campos` vazio. Este é o de `POST /consultas` num horário que o profissional já ocupou:

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

As mensagens de cada regra:

| Regra | Rota | Mensagem |
|---|---|---|
| R1 | `POST /consultas` | `R1: o profissional <nome> já tem uma consulta agendada nesse horário` |
| R2 | `POST /consultas` | `R2: o paciente já tem uma consulta agendada em <especialidade>. Cancele ou espere ela acontecer para marcar outra` |
| R3 | `POST /consultas` | `R3: a clínica atende de segunda a sexta, das 08:00 às 18:00, em horários de 30 em 30 minutos (o último começa às 17:30)` |
| R4 | `PUT /consultas/{id}/cancelar` | `R4: a consulta só pode ser cancelada com pelo menos 2 horas de antecedência` |

| Status | Quando acontece neste projeto |
|---|---|
| `400` | JSON ilegível, data fora do formato `yyyy-MM-ddTHH:mm:ss` ou campo inválido no DTO de entrada |
| `400` | Regra R1, R2, R3 ou R4 violada. A `mensagem` explica a regra |
| `400` | Registro profissional repetido, remoção de profissional que já tem consultas, ou cancelar e concluir uma consulta que não está mais `AGENDADA` |
| `404` | Id de profissional, paciente ou consulta que não existe. Em `POST /consultas`, vale também para o `pacienteId` e o `profissionalId` do corpo |
