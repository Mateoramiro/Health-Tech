Contrato da API: ConsultaCerta
Este documento descreve cada rota da API: o que ela recebe, o que devolve, as validações e os erros possíveis. As regras de negócio (R1 a R4) estão explicadas no README.

Índice
Informações gerais
Formato de erro
Profissionais
Pacientes
Consultas
Resumo dos status
Informações gerais
Item	Valor
Endereço base	http://localhost:8080
Formato	JSON (Content-Type: application/json)
Data e hora	yyyy-MM-ddTHH:mm:ss, sem fuso horário (exemplo: 2026-10-20T09:30:00)
Ids	Números inteiros, gerados pela API. Nunca são enviados no corpo da criação
Autenticação	Não há (fora do escopo desta etapa)
Documentação interativa	http://localhost:8080/swagger-ui.html
Contrato em JSON (OpenAPI)	http://localhost:8080/v3/api-docs
Listas sem nenhum item devolvem 200 com [], nunca 404. Nomes, especialidade, registro profissional e e-mail são gravados sem espaços no começo e no fim.

Formato de erro
Todo erro da API sai neste formato:

{
  "status": 400,
  "erro": "Bad Request",
  "mensagem": "R1: o profissional Dra. Helena Martins já tem uma consulta agendada nesse horário",
  "caminho": "/consultas",
  "timestamp": "2026-10-15T19:30:00",
  "campos": []
}
Campo	Tipo	O que é
status	número	Código HTTP do erro
erro	texto	Nome do código HTTP (Bad Request, Not Found)
mensagem	texto	O que deu errado, em português. Nos erros de regra de negócio, começa com a regra (R1:, R2:, R3:, R4:)
caminho	texto	O caminho da requisição que falhou
timestamp	data e hora	Momento do erro
campos	lista	Só preenchida em erro de validação: um item por campo inválido, ordenados pelo nome do campo
Cada item de campos tem o formato { "campo": "nome", "mensagem": "Nome é obrigatório" }.

Os tipos de erro:

Tipo	Status	Quando acontece	campos
Corpo ilegível	400	JSON quebrado, ou data e hora fora do formato yyyy-MM-ddTHH:mm:ss. A mensagem é Corpo da requisição inválido: confira o JSON e o formato da data e hora (yyyy-MM-ddTHH:mm:ss)	vazio
Campo inválido	400	Campo obrigatório ausente ou fora das regras de formato. A mensagem é Dados inválidos: confira a lista de campos	um item por campo
Regra de negócio	400	O pedido é válido, mas viola uma regra (R1 a R4, registro repetido, remoção bloqueada, consulta fora do status esperado)	vazio
Não encontrado	404	O id enviado não existe. A mensagem é <Entidade> não encontrado: <id>	vazio
Ordem das verificações: primeiro o formato do JSON, depois a validação dos campos, depois a existência dos ids e, por último, as regras de negócio.

Profissionais
Quem atende na clínica.

Objeto Profissional
Campo	Tipo	Exemplo
id	número	1
nome	texto	Dra. Helena Martins
especialidade	texto	Cardiologia
registroProfissional	texto	CRM-SC 48213
Corpo para cadastrar e atualizar
Campo	Obrigatório	Regras	Mensagem de erro
nome	sim	Não pode ser vazio. Até 255 caracteres	Nome é obrigatório, Nome tem no máximo 255 caracteres
especialidade	sim	Não pode ser vazia. Até 100 caracteres	Especialidade é obrigatória, Especialidade tem no máximo 100 caracteres
registroProfissional	sim	Não pode ser vazio. Até 30 caracteres. Não pode ser igual ao de outro profissional	Registro profissional é obrigatório, Registro profissional tem no máximo 30 caracteres
{
  "nome": "Dra. Helena Martins",
  "especialidade": "Cardiologia",
  "registroProfissional": "CRM-SC 48213"
}
POST /profissionais: cadastra um profissional
Item	Detalhe
Corpo	Os três campos acima
Regra	O registro profissional não pode já existir
Sucesso	201 Created, com o profissional e o id gerado
Resposta 201:

{
  "id": 1,
  "nome": "Dra. Helena Martins",
  "especialidade": "Cardiologia",
  "registroProfissional": "CRM-SC 48213"
}
Erro	Quando	Mensagem
400	Campo vazio, ausente ou grande demais	Dados inválidos: confira a lista de campos (com campos)
400	O registro já pertence a outro profissional	Já existe um profissional cadastrado com o registro CRM-SC 48213
GET /profissionais: lista os profissionais
Sem corpo e sem parâmetros. Devolve 200 com a lista de profissionais (vazia se não houver nenhum).

[
  { "id": 1, "nome": "Dra. Helena Martins", "especialidade": "Cardiologia", "registroProfissional": "CRM-SC 48213" }
]
GET /profissionais/{id}: busca um profissional
Parâmetro	Onde	Descrição
id	caminho	Id do profissional
Resposta	Quando	Corpo
200	O profissional existe	O objeto Profissional
404	Não existe profissional com esse id	Profissional não encontrado: 999
PUT /profissionais/{id}: atualiza um profissional
Substitui nome, especialidade e registro. O id não muda.

Parâmetro	Onde	Descrição
id	caminho	Id do profissional
corpo	corpo	Os três campos, todos obrigatórios (como no cadastro)
Resposta	Quando	Mensagem
200	Atualizado	O objeto Profissional com os novos dados
400	Campo inválido	Dados inválidos: confira a lista de campos (com campos)
400	O registro é de outro profissional	Já existe outro profissional cadastrado com o registro CRM-SC 48213
404	Não existe profissional com esse id	Profissional não encontrado: 999
Reenviar o próprio registro do profissional não é erro.

DELETE /profissionais/{id}: remove um profissional
Parâmetro	Onde	Descrição
id	caminho	Id do profissional
Resposta	Quando	Mensagem
204 No Content	Removido (sem corpo)	
400	O profissional já tem consultas registradas, de qualquer status	O profissional Dra. Helena Martins não pode ser removido: ele tem consultas registradas
404	Não existe profissional com esse id	Profissional não encontrado: 999
Pacientes
Quem pede consulta à recepção.

Objeto Paciente
Campo	Tipo	Exemplo
id	número	1
nome	texto	Marcos Oliveira
email	texto	marcos.oliveira@email.com
telefone	texto	48991234567
Corpo para cadastrar e atualizar
Campo	Obrigatório	Regras	Mensagem de erro
nome	sim	Não pode ser vazio. Até 255 caracteres	Nome é obrigatório, Nome tem no máximo 255 caracteres
email	sim	Não pode ser vazio. Precisa ser um e-mail válido. Até 255 caracteres	E-mail é obrigatório, E-mail inválido, E-mail tem no máximo 255 caracteres
telefone	sim	Só números, com DDD: 10 ou 11 dígitos, sem espaços, parênteses ou traço	Telefone é obrigatório, Telefone deve ter 10 ou 11 números (DDD + número)
{
  "nome": "Marcos Oliveira",
  "email": "marcos.oliveira@email.com",
  "telefone": "48991234567"
}
POST /pacientes: cadastra um paciente
Item	Detalhe
Corpo	Os três campos acima
Regra	Nenhuma além da validação dos campos
Sucesso	201 Created, com o paciente e o id gerado
Erro	Quando	Mensagem
400	Campo vazio, ausente ou fora do formato	Dados inválidos: confira a lista de campos (com campos)
GET /pacientes: lista os pacientes
Sem corpo e sem parâmetros. Devolve 200 com a lista de pacientes (vazia se não houver nenhum).

GET /pacientes/{id}: busca um paciente
Resposta	Quando	Corpo
200	O paciente existe	O objeto Paciente
404	Não existe paciente com esse id	Paciente não encontrado: 999
PUT /pacientes/{id}: atualiza um paciente
Substitui nome, e-mail e telefone. O id não muda. O corpo é o mesmo do cadastro, com os três campos obrigatórios.

Resposta	Quando	Mensagem
200	Atualizado	O objeto Paciente com os novos dados
400	Campo inválido	Dados inválidos: confira a lista de campos (com campos)
404	Não existe paciente com esse id	Paciente não encontrado: 999
Não há rota para remover paciente.

Consultas
O horário marcado entre um paciente e um profissional.

Objeto Consulta
Campo	Tipo	Descrição
id	número	Id da consulta
paciente	objeto	O paciente completo (objeto Paciente)
profissional	objeto	O profissional completo (objeto Profissional)
dataHora	data e hora	Início da consulta
status	texto	AGENDADA, CANCELADA ou REALIZADA
Ao marcar, entram só os ids, e a resposta devolve paciente e profissional completos.

Status da consulta
Status	Significado	Como chega nele
AGENDADA	Marcada e ainda por acontecer	Ao marcar (POST /consultas)
CANCELADA	Desmarcada	PUT /consultas/{id}/cancelar
REALIZADA	O paciente foi atendido	PUT /consultas/{id}/concluir
Só uma consulta AGENDADA pode ser cancelada ou concluída. Consultas CANCELADA e REALIZADA não ocupam o horário do profissional nem a vaga da especialidade do paciente (R1 e R2 só consideram consultas agendadas).

POST /consultas: marca uma consulta
Corpo:

Campo	Obrigatório	Regras	Mensagem de erro
pacienteId	sim	Id de um paciente existente	O id do paciente é obrigatório
profissionalId	sim	Id de um profissional existente	O id do profissional é obrigatório
dataHora	sim	Precisa estar no futuro e no formato yyyy-MM-ddTHH:mm:ss	A data e a hora da consulta são obrigatórias, A data e a hora da consulta precisam estar no futuro
{
  "pacienteId": 1,
  "profissionalId": 1,
  "dataHora": "2026-10-20T09:30:00"
}
Regras de negócio aplicadas, nesta ordem:

Regra	O que exige	Mensagem quando viola
R3	Segunda a sexta, das 08:00 às 17:30 (última consulta), só em ponto ou e meia	R3: a clínica atende de segunda a sexta, das 08:00 às 18:00, em horários de 30 em 30 minutos (o último começa às 17:30)
R1	O profissional não pode ter outra consulta agendada nesse horário	R1: o profissional Dra. Helena Martins já tem uma consulta agendada nesse horário
R2	O paciente não pode ter outra consulta agendada na mesma especialidade (sem diferenciar maiúsculas de minúsculas)	R2: o paciente já tem uma consulta agendada em Cardiologia. Cancele ou espere ela acontecer para marcar outra
Respostas:

Status	Quando
201 Created	Consulta marcada, com status AGENDADA
400	Corpo ilegível, campo inválido ou regra R1, R2 ou R3 violada (a mensagem diz qual)
404	O paciente ou o profissional não existe (Paciente não encontrado: 99 ou Profissional não encontrado: 99)
Resposta 201:

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
Exemplo de erro 400 por campo inválido (data no passado e id do paciente ausente):

{
  "status": 400,
  "erro": "Bad Request",
  "mensagem": "Dados inválidos: confira a lista de campos",
  "caminho": "/consultas",
  "timestamp": "2026-10-15T19:30:00",
  "campos": [
    { "campo": "dataHora", "mensagem": "A data e a hora da consulta precisam estar no futuro" },
    { "campo": "pacienteId", "mensagem": "O id do paciente é obrigatório" }
  ]
}
GET /consultas: lista as consultas
Sem corpo e sem parâmetros. Devolve 200 com todas as consultas, de qualquer status, da mais próxima para a mais distante (ordem crescente de dataHora). Sem consultas, a lista vem vazia.

GET /consultas/{id}: busca uma consulta
Resposta	Quando	Corpo
200	A consulta existe	O objeto Consulta
404	Não existe consulta com esse id	Consulta não encontrada: 999
PUT /consultas/{id}/cancelar: cancela uma consulta
Sem corpo. Muda o status para CANCELADA e libera o horário do profissional e a vaga da especialidade do paciente.

Regra	O que exige	Mensagem quando viola
(status)	A consulta precisa estar AGENDADA	A consulta 1 não pode ser cancelada: ela está CANCELADA (ou REALIZADA)
R4	O cancelamento precisa ser feito com pelo menos 2 horas de antecedência	R4: a consulta só pode ser cancelada com pelo menos 2 horas de antecedência
Resposta	Quando
200	Cancelada: devolve a consulta com status CANCELADA
400	A consulta não está agendada, ou a R4 foi violada
404	Não existe consulta com esse id
PUT /consultas/{id}/concluir: conclui uma consulta
Sem corpo. Registra que o paciente foi atendido e muda o status para REALIZADA. Também libera o horário e a vaga.

Regra	O que exige	Mensagem quando viola
(status)	A consulta precisa estar AGENDADA	A consulta 1 não pode ser concluída: ela está CANCELADA (ou REALIZADA)
Resposta	Quando
200	Concluída: devolve a consulta com status REALIZADA
400	A consulta não está agendada
404	Não existe consulta com esse id
Resumo dos status
Status	Significado	Onde aparece
200 OK	Deu certo	GET, PUT
201 Created	Registro criado	POST
204 No Content	Removido, sem corpo	DELETE /profissionais/{id}
400 Bad Request	Corpo ilegível, campo inválido ou regra de negócio violada	POST, PUT, DELETE
404 Not Found	O id não existe	Qualquer rota com {id} e POST /consultas (ids do corpo)
Todas as rotas
Verbo	Caminho	O que faz	Regra	Sucesso	Erros
POST	/profissionais	Cadastra um profissional	Registro não repetido	201	400
GET	/profissionais	Lista os profissionais	Nenhuma	200	Nenhum
GET	/profissionais/{id}	Busca um profissional pelo id	Nenhuma	200	404
PUT	/profissionais/{id}	Atualiza um profissional	Registro não pode ser o de outro profissional	200	400, 404
DELETE	/profissionais/{id}	Remove um profissional	Não remove quem já tem consultas	204	400, 404
POST	/pacientes	Cadastra um paciente	Nenhuma	201	400
GET	/pacientes	Lista os pacientes	Nenhuma	200	Nenhum
GET	/pacientes/{id}	Busca um paciente pelo id	Nenhuma	200	404
PUT	/pacientes/{id}	Atualiza um paciente	Nenhuma	200	400, 404
POST	/consultas	Marca uma consulta	R1, R2 e R3	201	400, 404
GET	/consultas	Lista as consultas, da mais próxima para a mais distante	Nenhuma	200	Nenhum
GET	/consultas/{id}	Busca uma consulta pelo id	Nenhuma	200	404
PUT	/consultas/{id}/cancelar	Cancela a consulta e libera o horário	R4	200	400, 404
PUT	/consultas/{id}/concluir	Registra que o paciente foi atendido (REALIZADA)	Só consulta agendada	200	400, 404
