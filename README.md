# CampusMarket — Marketplace Acadêmico

Aplicação acadêmica de compra, venda e negociação entre estudantes. Frontend em React e TypeScript, backend em Java com Spring Boot, persistência com JPA e migrações Flyway.

O projeto contém uma implementação funcional para demonstração: autenticação, anúncios, busca, filtros, favoritos, propostas, leilões, chat, notificações, perfis e avaliações. Os dados iniciais e a instituição são fictícios. O cadastro real depende de uma integração autorizada pela faculdade.

## Começar neste computador

Abra dois terminais PowerShell na pasta `campus-market`.

Terminal 1 — backend:

```powershell
.\scripts\backend.ps1 run
```

Terminal 2 — frontend:

```powershell
.\scripts\frontend.ps1 dev
```

Abra **http://localhost:5173/** e clique em **Explorar demonstração**.

O backend usa a porta 8080. O Vite encaminha `/api` e `/ws` para ele, mantendo uma única origem no navegador. Use o mesmo endereço durante a sessão: cookies de `localhost` e `127.0.0.1` são distintos.

A porta pode ser alterada por `SERVER_PORT`; ao alterar a porta do backend, ajuste também o proxy em `frontend/vite.config.ts`.

O script do backend procura `JAVA_HOME`, ferramentas portáteis em `tools` e o JDK do Android Studio. Neste computador foi encontrado o JDK 25.0.3 do Android Studio; o Maven compila com `release 21`, gerando código para Java 21. Nenhuma variável permanente do Windows foi alterada.

O script do frontend aceita pnpm ou npm instalado. Neste computador também identifica o pnpm fornecido pelo ambiente Codex.

Encerre cada servidor com **Ctrl+C** no respectivo terminal.

## Contas de demonstração

| R.A. | Nome fictício | Senha |
| --- | --- | --- |
| `20260001` | Luana Martins | `Campus@123` |
| `20260002` | Pedro Almeida | `Campus@123` |
| `20260003` | Beatriz Costa | `Campus@123` |

O botão de demonstração acessa a conta da Luana. Para simular comprador e vendedor simultaneamente, use janelas com sessões independentes, como um navegador normal e uma janela anônima. Duas abas da mesma sessão compartilham o login.

Para experimentar o cadastro, utilize um R.A. ainda não cadastrado entre `20260004` e `20260050`, com convite `CAMPUS-` seguido dos últimos quatro dígitos. Exemplo: `20260004` e `CAMPUS-0004`. Os convites são públicos e previsíveis exclusivamente nesta simulação.

Os dados são criados apenas quando a base está vazia. Anúncios, mensagens, cadastros e avaliações permanecem salvos entre reinicializações no perfil `demo`.

## Instalação em outro computador

Pré-requisitos:

- JDK 21 ou superior compatível com Spring Boot 4.1.2.
- Maven 3.9 ou superior; a preparação local utiliza 3.9.16.
- Node.js 22.12 ou superior compatível com Vite 7; foi usado Node.js 24.22.1.
- pnpm 11+ recomendado ou npm. O arquivo `pnpm-lock.yaml` fixa as versões resolvidas com pnpm.
- PostgreSQL é opcional para a demonstração local, que utiliza H2 em arquivo.

Com Java e Maven disponíveis, instale as dependências do frontend:

```powershell
.\scripts\frontend.ps1 install
```

A primeira execução do backend baixa as dependências Maven. Depois utilize os dois comandos de inicialização apresentados acima. Com ferramentas no PATH, também é possível executar `mvn spring-boot:run` em `backend` e `pnpm dev` em `frontend`.

A pasta `tools` contém ferramentas e caches locais e está ignorada pelo Git. Ela não deve ser enviada ao professor ou adicionada ao repositório. A pasta `node_modules` também não deve ser versionada.

## Tecnologias

| Tecnologia | Uso |
| --- | --- |
| React + TypeScript | Componentes, formulários e interface responsiva. |
| Vite | Desenvolvimento, proxy local e build do frontend. |
| Lucide React | Ícones. |
| Java + Spring Boot | API, casos de uso e regras de negócio. |
| Spring Security | Sessão HTTP, autorização e CSRF. |
| BCrypt | Hash das senhas. |
| Spring Data JPA | Repositórios e bloqueios transacionais. |
| PostgreSQL | Banco relacional previsto para implantação. |
| H2 | Demonstração local e testes rápidos. |
| Flyway | Migrações do esquema em ambos os bancos. |
| WebSocket + STOMP | Avisos de novos lances e mensagens. |
| JUnit + Mockito | Testes de domínio, contratos e integração. |

As versões efetivas estão no `backend/pom.xml` e no `frontend/pnpm-lock.yaml`.

## Funcionalidades implementadas

- Cadastro com validador acadêmico substituível, login por R.A., sessão e logout.
- Catálogo autenticado, busca por título/descrição, categorias, condição, preços e ordenação.
- Publicação com até cinco imagens; validação e normalização das imagens no backend.
- Edição, pausa de venda direta e exclusão lógica, respeitando o histórico.
- Favoritos vinculados ao estudante.
- Propostas de venda direta com aceite ou recusa pelo vendedor.
- Reserva do produto após aceite e confirmação de entrega pelos dois participantes.
- Leilões com prazo de 1 minuto a 7 dias, incremento de R$ 1,00 e bloqueio de lances simultâneos.
- Encerramento automático e idempotente; criação da negociação para o vencedor.
- Acompanhamento dos próprios lances.
- Chat privado persistido, vinculado ao anúncio e aos participantes.
- Notificações persistidas e atualizações em tempo real após commit.
- Perfil público, edição dos dados de exibição e avaliações após conclusão.
- Interface adaptada para desktop e celular, diálogos nativos e navegação por teclado.

As ilustrações de produtos da demonstração são SVGs locais. O gerador está em `scripts/generate-demo-assets.mjs`. Anúncios criados pelo usuário utilizam as imagens enviadas pelo formulário.

## Testes e build

```powershell
# Testes de domínio, contratos, HTTP, WebSocket e integração com H2
.\scripts\backend.ps1 test

# Os mesmos testes em PostgreSQL local isolado
.\scripts\test-postgres.ps1

# Verificação TypeScript e build de produção do frontend
.\scripts\frontend.ps1 build

# Pacote Java executável e testes
.\scripts\backend.ps1 package
```

O teste PostgreSQL utiliza os binários instalados em `C:\Program Files\PostgreSQL\17\bin`, cria um cluster exclusivo em `tools/postgres-test`, inicia somente em `127.0.0.1:55432`, cria um banco de teste novo por execução e encerra o servidor ao terminar. Não utiliza os bancos existentes na máquina. Para outro local ou porta, use os parâmetros `-PostgresBin` e `-Port`.

Os relatórios JUnit ficam em `backend/target/surefire-reports`. Consulte `docs/VALIDACAO.md` para o registro das verificações realizadas.

## PostgreSQL na aplicação

O perfil `postgres` utiliza `DB_URL`, `DB_USER` e `DB_PASSWORD`. O arquivo `compose.yml` oferece um PostgreSQL opcional, caso Docker esteja instalado e em execução.

```powershell
$env:DB_PASSWORD = 'defina-uma-senha-local'
docker compose up -d postgres
$env:DB_URL = 'jdbc:postgresql://localhost:5432/campusmarket'
$env:DB_USER = 'campusmarket'
# Somente para HTTP local de desenvolvimento:
$env:SESSION_COOKIE_SECURE = 'false'
.\scripts\backend.ps1 run -Profile postgres
```

O perfil `postgres` não cria contas de demonstração e recusa novos cadastros até que o validador institucional seja implementado. Para demonstrar os dados fictícios usando PostgreSQL, configure as variáveis `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` e `SPRING_DATASOURCE_PASSWORD` e utilize o perfil `demo`.

Em uma implantação com HTTPS, mantenha o cookie seguro habilitado. O frontend precisa ser servido com um proxy de `/api` e `/ws` para o backend; o servidor de desenvolvimento Vite não é o servidor de produção.

## Organização

```text
campus-market/
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/java/br/edu/campusmarket/
│       │   ├── usuarios/
│       │   ├── anuncios/
│       │   ├── leiloes/
│       │   ├── negociacoes/
│       │   ├── mensagens/
│       │   ├── avaliacoes/
│       │   ├── notificacoes/
│       │   ├── common/
│       │   └── configuracao/
│       ├── main/resources/db/migration/
│       └── test/
├── frontend/
│   ├── public/products/
│   └── src/{components,hooks,pages}/
├── docs/
├── scripts/
├── compose.yml
└── README.md
```

Nos módulos maiores, `api`, `application`, `domain` e `infrastructure` separam transporte, casos de uso, regras e detalhes técnicos. Módulos menores mantêm seus poucos arquivos juntos, com classes de responsabilidades distintas.

## Material para estudar e apresentar

- [Arquitetura e modelagem](docs/ARQUITETURA.md)
- [SOLID com evidências no código](docs/SOLID.md)
- [Roteiro de apresentação ao professor](docs/APRESENTACAO.md)
- [Referência da API](docs/API.md)
- [Validação e limites](docs/VALIDACAO.md)

## Limites desta versão acadêmica

A validação da faculdade é simulada. Não há integração de matrícula, recuperação de senha por e-mail, pagamento online, painel de moderação, denúncias ou análise automática de imagens. As mensagens são de texto; fotos adicionais podem ser publicadas editando anúncios que ainda permitam edição.

O broker WebSocket e as sessões são locais a uma instância da aplicação. Há reconexão e atualização periódica de 20 segundos para recuperar informações persistidas. Uma implantação com várias instâncias precisará compartilhar sessões, armazenamento e mensageria.

O histórico de chat retorna as últimas 200 mensagens, e as notificações exibem as 50 mais recentes. Paginação de conversas e avaliações, limpeza de imagens não vinculadas e políticas de moderação são evoluções previstas.

As confirmações de entrega são declarações dos participantes: a aplicação não comprova pagamento nem entrega física.
