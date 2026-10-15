ConsultaCerta — Modelo de Domínio

O modelo de domínio descreve as entidades do sistema, seus atributos, relacionamentos e regras de negócio. Ele corresponde tanto à estrutura do banco de dados, definida nas migrations em src/main/resources/db/migration, quanto às classes Java em src/main/java/api/model.

Índice
1. Visão geral
2. Diagrama de entidades
3. Entidades
3.1. Profissional
3.2. Paciente
3.3. Consulta
4. Relacionamentos
5. Ciclo de vida da consulta
6. Regras de negócio e entidades
7. Restrições no banco de dados
1. Visão geral

O sistema possui três entidades principais:

Profissional: representa quem realiza os atendimentos na clínica.
Paciente: representa uma pessoa cadastrada que pode marcar consultas.
Consulta: representa um agendamento entre um paciente e um profissional.

As entidades Profissional e Paciente existem independentemente. A entidade Consulta conecta as duas, registrando o horário e o status do atendimento.

Um paciente pode ter várias consultas, assim como um profissional pode atender várias consultas. Por isso, as chaves estrangeiras ficam na tabela consultas, e não existe um relacionamento direto entre as tabelas pacientes e profissionais.

2. Diagrama de entidades

O diagrama abaixo representa as tabelas e seus relacionamentos no banco de dados.

erDiagram
    PACIENTES ||--o{ CONSULTAS : "faz"
    PROFISSIONAIS ||--o{ CONSULTAS : "atende"

    PACIENTES {
        bigint id PK
        varchar nome
        varchar email
        varchar telefone
    }

    PROFISSIONAIS {
        bigint id PK
        varchar nome
        varchar especialidade
        varchar registro_profissional UK
    }

    CONSULTAS {
        bigint id PK
        bigint paciente_id FK
        bigint profissional_id FK
        timestamp data_hora
        varchar status
    }


Legenda:

PK: chave primária (Primary Key).
FK: chave estrangeira (Foreign Key).
UK: restrição de unicidade (Unique Key).
||: exatamente um registro relacionado.
o{: zero ou muitos registros relacionados.
3. Entidades
3.1. Profissional

Representa quem atende na clínica.

Classe Java: Profissional
Tabela: profissionais
Campo Java	Coluna no banco	Tipo	Obrigatório	Restrições
id	id	BIGINT	Sim	Chave primária, gerada pelo banco
nome	nome	VARCHAR(255)	Sim	Nome do profissional
especialidade	especialidade	VARCHAR(100)	Sim	Texto livre, como Cardiologia
registroProfissional	registro_profissional	VARCHAR(30)	Sim	Valor único entre profissionais

Observações:

A especialidade é armazenada como texto. Não existe uma tabela específica de especialidades.
O registro profissional identifica o número no conselho da categoria, por exemplo, CRM-SC 48213.
Dois profissionais não podem possuir o mesmo registro profissional.
Um profissional que tenha consultas registradas não pode ser removido.
3.2. Paciente

Representa uma pessoa cadastrada que pode marcar consultas.

Classe Java: Paciente
Tabela: pacientes
Campo Java	Coluna no banco	Tipo	Obrigatório	Restrições
id	id	BIGINT	Sim	Chave primária, gerada pelo banco
nome	nome	VARCHAR(255)	Sim	Nome do paciente
email	email	VARCHAR(255)	Sim	Deve ser um e-mail válido
telefone	telefone	VARCHAR(15)	Sim	Apenas números, com DDD, 10 ou 11 dígitos

Observações:

A validade do e-mail é verificada pela API.
O telefone deve conter apenas números, incluindo o DDD.
As validações de formato são realizadas pela aplicação.
3.3. Consulta

Representa o horário marcado entre um paciente e um profissional.

Classe Java: Consulta
Tabela: consultas
Campo Java	Coluna no banco	Tipo	Obrigatório	Restrições
id	id	BIGINT	Sim	Chave primária, gerada pelo banco
paciente	paciente_id	BIGINT	Sim	Chave estrangeira para pacientes(id)
profissional	profissional_id	BIGINT	Sim	Chave estrangeira para profissionais(id)
dataHora	data_hora	TIMESTAMP	Sim	Data e hora de início da consulta
status	status	VARCHAR(20)	Sim	Valores permitidos: AGENDADA, CANCELADA ou REALIZADA

Observações:

Toda consulta precisa estar associada a um paciente e a um profissional existentes.
O status inicial de uma consulta é AGENDADA.
No código Java, a consulta mantém referências aos objetos Paciente e Profissional, utilizando relacionamentos @ManyToOne.
O status é representado pelo enum StatusConsulta.
4. Relacionamentos
Relacionamento	Cardinalidade	Implementação
Paciente → Consulta	Um para muitos (1:N)	consultas.paciente_id
Profissional → Consulta	Um para muitos (1:N)	consultas.profissional_id
Paciente ↔ Profissional	Muitos para muitos (N:N), de forma indireta	Relacionamento intermediado pela tabela consultas
Pontos importantes
Uma consulta não existe sem paciente e sem profissional, pois ambas as chaves estrangeiras são obrigatórias.
No código, os relacionamentos são unidirecionais: Consulta conhece Paciente e Profissional, mas essas entidades não mantêm listas de consultas.
Para consultar os agendamentos de um paciente ou profissional, o sistema utiliza o ConsultaRepository.
Um profissional com consultas registradas não pode ser removido pela API. As chaves estrangeiras também impedem a exclusão de um profissional referenciado por consultas.
Pacientes e profissionais podem aparecer em várias consultas, desde que as regras de negócio R1 e R2 sejam respeitadas.
5. Ciclo de vida da consulta

Toda consulta começa com o status AGENDADA e pode mudar de estado apenas uma vez: para CANCELADA ou REALIZADA.

stateDiagram-v2
    [*] --> AGENDADA: POST /consultas
    AGENDADA --> CANCELADA: PUT /consultas/{id}/cancelar (R4)
    AGENDADA --> REALIZADA: PUT /consultas/{id}/concluir
    CANCELADA --> [*]
    REALIZADA --> [*]

Significado dos status
Status	Significado	Ocupa horário do profissional (R1)?	Ocupa vaga da especialidade do paciente (R2)?
AGENDADA	Consulta marcada e ainda por acontecer	Sim	Sim
CANCELADA	Consulta desmarcada	Não	Não
REALIZADA	Paciente atendido	Não	Não

Importante: os status CANCELADA e REALIZADA são finais. Apenas consultas AGENDADA podem ser canceladas ou concluídas.

Esse comportamento permite que o cancelamento ou a conclusão libere o horário do profissional e a vaga da especialidade do paciente.

6. Regras de negócio e entidades

As regras de negócio são aplicadas principalmente nos serviços da aplicação.

Regra	Descrição	Campos e entidades envolvidos
R1	O profissional não pode ter duas consultas agendadas no mesmo horário	Consulta.profissional, Consulta.dataHora, Consulta.status
R2	O paciente pode ter apenas uma consulta agendada por especialidade	Consulta.paciente, Consulta.status, Profissional.especialidade
R3	Os atendimentos ocorrem de segunda a sexta-feira, das 08:00 às 17:30, em intervalos de 30 minutos	Consulta.dataHora
R4	O cancelamento precisa ocorrer com pelo menos duas horas de antecedência	Consulta.dataHora, Consulta.status e momento atual
Regras adicionais

Além das regras R1 a R4:

Registro profissional único: dois profissionais não podem possuir o mesmo registroProfissional.
Remoção de profissional: um profissional que possua consultas registradas, independentemente do status, não pode ser removido.
Validação dos dados: campos obrigatórios, formatos e limites de tamanho são verificados pela aplicação.
Onde as regras são aplicadas
ConsultaService: regras R1, R2, R3 e R4.
ProfissionalService: validação de unicidade do registro profissional e restrição de remoção.

Consulte o contrato da API para verificar as mensagens de erro e os códigos HTTP de cada regra.

7. Restrições no banco de dados

As migrations em src/main/resources/db/migration criam as tabelas e implementam restrições para garantir a integridade dos dados.

Restrição	Onde é aplicada	O que garante
Chave primária e geração automática	id das três tabelas	Cada registro possui um identificador único gerado pelo banco
NOT NULL	Todas as colunas obrigatórias	Impede valores nulos nos campos obrigatórios
UNIQUE	profissionais.registro_profissional	Impede registros profissionais duplicados
Chave estrangeira	consultas.paciente_id e consultas.profissional_id	Impede referências a pacientes ou profissionais inexistentes e a exclusão de registros referenciados
CHECK	consultas.status	Permite apenas AGENDADA, CANCELADA ou REALIZADA
Índice único parcial	consultas(profissional_id, data_hora)	Impede duas consultas agendadas para o mesmo profissional no mesmo horário
Índice único parcial — R1

O índice uk_consultas_profissional_horario é aplicado às colunas profissional_id e data_hora, considerando somente consultas com status AGENDADA.

Isso garante que a regra R1 também seja protegida no banco de dados, inclusive em situações de requisições simultâneas.

Como o índice considera apenas consultas agendadas, consultas canceladas ou realizadas não bloqueiam o horário.

Restrições verificadas pela aplicação

As regras R2, R3 e R4 são verificadas pela aplicação, na camada de serviço, e não por restrições equivalentes no banco de dados.

Estrutura relacionada no projeto:

Modelos: src/main/java/api/model
Migrations: src/main/resources/db/migration
Serviços: ConsultaService e ProfissionalService
Repositório: ConsultaRepository
