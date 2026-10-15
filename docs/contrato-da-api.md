ConsultaCerta — Contrato da API

Este documento descreve as rotas da API ConsultaCerta, incluindo os dados recebidos, as respostas, as validações e os possíveis erros.

As regras de negócio R1 a R4 estão detalhadas na seção Regras de negócio.

Índice
1. Informações gerais
2. Formato de erro
3. Profissionais
4. Pacientes
5. Consultas
6. Regras de negócio
7. Resumo dos status HTTP
8. Resumo de todas as rotas
1. Informações gerais
Item	Descrição
Endereço base	http://localhost:8080
Formato	JSON (Content-Type: application/json)
Data e hora	yyyy-MM-dd'T'HH:mm:ss, sem fuso horário
IDs	Números inteiros gerados pela API
Autenticação	Não implementada nesta etapa
Swagger UI	http://localhost:8080/swagger-ui.html
Documentação OpenAPI	http://localhost:8080/v3/api-docs
Convenções
Os IDs são gerados automaticamente pela API e não devem ser enviados no corpo das requisições de criação.
Nomes, especialidades, registros profissionais e e-mails são armazenados sem espaços no início ou no final.
Listas vazias retornam 200 OK com [], nunca 404 Not Found.
Os corpos das requisições e respostas utilizam JSON, salvo quando indicado que não há corpo.

Exemplo de data e hora:

2026-10-20T09:30:00

2. Formato de erro

Todos os erros da API seguem o mesmo formato JSON:

{
  "status": 400,
  "erro": "Bad Request",
  "mensagem": "R1: o profissional Dra. Helena Martins já tem uma consulta agendada nesse horário",
  "caminho": "/consultas",
  "timestamp": "2026-10-15T19:30:00",
  "campos": []
}

Campos do erro
Campo	Tipo	Descrição
status	Número	Código HTTP do erro
erro	Texto	Nome do status HTTP
mensagem	Texto	Descrição do erro em português
caminho	Texto	Caminho da requisição que falhou
timestamp	Data e hora	Momento em que o erro ocorreu
campos	Lista	Campos inválidos, quando houver erro de validação

Cada item da lista campos possui o seguinte formato:

{
  "campo": "nome",
  "mensagem": "Nome é obrigatório"
}

Tipos de erro
Tipo	HTTP	Quando ocorre
Corpo ilegível	400 Bad Request	JSON inválido ou data e hora fora do formato esperado
Campo inválido	400 Bad Request	Campo obrigatório ausente ou fora das regras de validação
Regra de negócio	400 Bad Request	Operação válida sintaticamente, mas que viola uma regra de negócio
Não encontrado	404 Not Found	ID informado não existe

Mensagens padronizadas:

Corpo ilegível: Corpo da requisição inválido: confira o JSON e o formato da data e hora (yyyy-MM-ddTHH:mm:ss)
Campo inválido: Dados inválidos: confira a lista de campos
Não encontrado: <Entidade> não encontrado: <id>

Nos erros de validação, a lista campos contém um item por campo inválido, ordenados pelo nome do campo. Nos demais erros, a lista fica vazia.

Ordem das verificações
Validação do formato do JSON.
Validação dos campos.
Verificação da existência dos IDs.
Aplicação das regras de negócio.
3. Profissionais

Representam os profissionais responsáveis pelos atendimentos da clínica.

3.1. Objeto Profissional
Campo	Tipo	Exemplo
id	Número inteiro	1
nome	Texto	Dra. Helena Martins
especialidade	Texto	Cardiologia
registroProfissional	Texto	CRM-SC 48213
3.2. Validação dos campos
Campo	Obrigatório	Regras
nome	Sim	Não pode ser vazio; máximo de 255 caracteres
especialidade	Sim	Não pode ser vazia; máximo de 100 caracteres
registroProfissional	Sim	Não pode ser vazio; máximo de 30 caracteres; não pode pertencer a outro profissional

Mensagens de validação:

Nome é obrigatório
Nome tem no máximo 255 caracteres
Especialidade é obrigatória
Especialidade tem no máximo 100 caracteres
Registro profissional é obrigatório
Registro profissional tem no máximo 30 caracteres
3.3. Cadastrar profissional

POST /profissionais

Cadastra um novo profissional. O registro profissional não pode estar associado a outro profissional.

Corpo da requisição:

{
  "nome": "Dra. Helena Martins",
  "especialidade": "Cardiologia",
  "registroProfissional": "CRM-SC 48213"
}


Resposta de sucesso: 201 Created

{
  "id": 1,
  "nome": "Dra. Helena Martins",
  "especialidade": "Cardiologia",
  "registroProfissional": "CRM-SC 48213"
}


Possíveis erros:

HTTP	Situação	Mensagem
400	Campo inválido	Dados inválidos: confira a lista de campos
400	Registro já cadastrado	Já existe um profissional cadastrado com o registro CRM-SC 48213
3.4. Listar profissionais

GET /profissionais

Retorna todos os profissionais cadastrados.

Resposta: 200 OK

[
  {
    "id": 1,
    "nome": "Dra. Helena Martins",
    "especialidade": "Cardiologia",
    "registroProfissional": "CRM-SC 48213"
  }
]


Se não houver profissionais cadastrados, retorna 200 OK com [].

3.5. Buscar profissional por ID

GET /profissionais/{id}

Exemplo: GET /profissionais/1

HTTP	Situação
200 OK	Profissional encontrado
404 Not Found	Profissional não encontrado

Exemplo de erro:

{
  "status": 404,
  "erro": "Not Found",
  "mensagem": "Profissional não encontrado: 999",
  "caminho": "/profissionais/999",
  "timestamp": "2026-10-15T19:30:00",
  "campos": []
}

3.6. Atualizar profissional

PUT /profissionais/{id}

Substitui o nome, a especialidade e o registro profissional. O ID não muda.

Corpo da requisição:

{
  "nome": "Dra. Helena Martins",
  "especialidade": "Clínica Geral",
  "registroProfissional": "CRM-SC 48213"
}


Todos os campos são obrigatórios e seguem as mesmas validações do cadastro.

HTTP	Situação
200 OK	Profissional atualizado
400 Bad Request	Campos inválidos ou registro pertencente a outro profissional
404 Not Found	Profissional não encontrado

O próprio profissional pode manter o seu registro atual sem gerar erro.

Mensagem para registro pertencente a outro profissional:

Já existe outro profissional cadastrado com o registro CRM-SC 48213

3.7. Remover profissional

DELETE /profissionais/{id}

Remove um profissional, desde que ele não possua consultas registradas, independentemente do status.

HTTP	Situação
204 No Content	Profissional removido, sem corpo na resposta
400 Bad Request	Profissional possui consultas registradas
404 Not Found	Profissional não encontrado

Mensagem quando a remoção é bloqueada:

O profissional Dra. Helena Martins não pode ser removido: ele tem consultas registradas

4. Pacientes

Representam as pessoas que solicitam consultas na clínica.

4.1. Objeto Paciente
Campo	Tipo	Exemplo
id	Número inteiro	1
nome	Texto	Marcos Oliveira
email	Texto	marcos.oliveira@email.com
telefone	Texto	48991234567
4.2. Validação dos campos
Campo	Obrigatório	Regras
nome	Sim	Não pode ser vazio; máximo de 255 caracteres
email	Sim	Deve ser um e-mail válido; máximo de 255 caracteres
telefone	Sim	Apenas números, com DDD, contendo 10 ou 11 dígitos

Mensagens de validação:

Nome é obrigatório
Nome tem no máximo 255 caracteres
E-mail é obrigatório
E-mail inválido
E-mail tem no máximo 255 caracteres
Telefone é obrigatório
Telefone deve ter 10 ou 11 números (DDD + número)

O telefone não pode conter espaços, parênteses, traços ou outros caracteres.

4.3. Cadastrar paciente

POST /pacientes

Corpo da requisição:

{
  "nome": "Marcos Oliveira",
  "email": "marcos.oliveira@email.com",
  "telefone": "48991234567"
}


Resposta de sucesso: 201 Created

{
  "id": 1,
  "nome": "Marcos Oliveira",
  "email": "marcos.oliveira@email.com",
  "telefone": "48991234567"
}


Possível erro:

HTTP	Situação
400 Bad Request	Campo ausente ou inválido
4.4. Listar pacientes

GET /pacientes

Retorna todos os pacientes cadastrados.

Resposta: 200 OK

Se não houver pacientes cadastrados, retorna 200 OK com [].

4.5. Buscar paciente por ID

GET /pacientes/{id}

Exemplo: GET /pacientes/1

HTTP	Situação
200 OK	Paciente encontrado
404 Not Found	Paciente não encontrado

Mensagem de erro:

Paciente não encontrado: 999

4.6. Atualizar paciente

PUT /pacientes/{id}

Substitui o nome
