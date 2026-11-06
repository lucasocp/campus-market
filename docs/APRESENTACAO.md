# Roteiro para apresentar ao professor

Tempo sugerido: 12 a 15 minutos. A apresentação combina uso do sistema, modelagem e evidências nos testes.

## 1. Problema e objetivo — 1 minuto

“O CampusMarket facilita o reaproveitamento de produtos dentro da comunidade acadêmica. Os estudantes podem anunciar, enviar propostas, participar de leilões e conversar no mesmo ambiente.”

Explique que a demonstração utiliza uma instituição fictícia e não processa pagamentos. A validação real dependerá da integração com a faculdade.

## 2. Visão técnica — 2 minutos

Abra `docs/ARQUITETURA.md` e mostre o diagrama da aplicação.

- React e TypeScript apresentam a interface.
- Java e Spring Boot executam os casos de uso.
- JPA e Flyway tratam persistência e evolução do esquema.
- PostgreSQL é o banco previsto para implantação; H2 permite demonstrar sem instalar outro serviço.
- WebSocket informa mudanças depois da confirmação da transação.

Justifique o monólito modular: o projeto é executável por uma equipe acadêmica, com responsabilidades separadas e transações simples de verificar.

## 3. Demonstração das funcionalidades — 4 minutos

1. Abra `http://localhost:5173/` e entre pela demonstração ou com o R.A. `20260001` e senha `Campus@123`.
2. Pesquise por `MacBook` e abra o anúncio. Mostre preço, descrição, vendedor, proposta e chat.
3. Abra o chat e mostre que o histórico está persistido. Use somente mensagens fictícias na apresentação.
4. Volte ao catálogo e mostre categorias, filtros e favoritos.
5. Abra “Meus anúncios”, edite um anúncio de venda direta e demonstre a pausa e reativação.
6. Na criação de anúncio, mostre as duas modalidades e os limites de imagem.
7. Abra um leilão e explique o prazo e o incremento mínimo.
8. Abra o perfil do Pedro para mostrar uma avaliação vinculada à negociação fictícia já concluída.

Para demonstrar troca entre pessoas, abra uma janela anônima independente com o R.A. `20260002` e senha `Campus@123`. O R.A. `20260003` pertence à Beatriz, com a mesma senha de demonstração.

Para demonstrar uma negociação completa:

1. Como Luana, envie uma proposta em um anúncio de venda direta do Pedro.
2. Como Pedro, abra “Negociações” → “Propostas” e aceite.
3. Mostre que o produto passa a reservado e que foi criada uma negociação.
4. Confirme a entrega em cada conta. Ressalte que isso é uma declaração dos participantes.
5. Publique uma avaliação e mostre a atualização do perfil.

Para demonstrar o encerramento de um leilão sem esperar horas, crie um anúncio com duração de 1 minuto, dê um lance com outra conta e aguarde o prazo mais até 5 segundos da rotina de encerramento.

## 4. Orientação a objetos — 2 minutos

Mostre `Listing`, `Trade` e `Review` no backend.

- **Encapsulamento:** atributos privados e ausência de setters que alterem arbitrariamente o estado.
- **Abstração:** contratos `AcademicVerifier`, `UserAccounts`, `PasswordHasher` e `ImageStorage`.
- **Polimorfismo:** `RegisterUser` usa `UserAccounts` sem conhecer se o armazenamento é JPA ou em memória.
- **Composição:** serviços recebem colaboradores pelo construtor; comprador e vendedor são papéis do usuário.

Herança não é um objetivo obrigatório. Explique por que não foi criada uma hierarquia artificial de tipos de usuário ou de anúncio.

## 5. SOLID com código e testes — 3 minutos

Use `docs/SOLID.md` como guia. Mostre uma evidência por princípio:

| Princípio | Arquivo e explicação |
| --- | --- |
| SRP | `RegisterUser` coordena o cadastro; hash e persistência ficam em outras classes. |
| OCP | Outra implementação de `AcademicVerifier` pode ser injetada. |
| LSP | `UserAccountsContractTest` verifica o mesmo contrato em memória e JPA. |
| ISP | `PasswordHasher`, `CatalogEvents` e `UserNotifier` possuem contratos pequenos. |
| DIP | O construtor de `RegisterUser` depende de contratos, não de BCrypt ou SQL. |

Execute os testes:

```powershell
.\scripts\backend.ps1 test
```

Mostre especialmente:

- `RegisterUserTest`: cadastro isolado do framework e do banco.
- `UserAccountsContractTest`: substituição de implementações.
- `MarketplaceIntegrationTest.simultaneousEqualBidsProduceExactlyOneWinner`: duas transações simultâneas com um único lance aceito.
- `HttpSecurityIntegrationTest`: HTTP e WebSocket protegidos, incluindo fechamento de sockets no logout.

Os mesmos testes podem rodar em PostgreSQL por `scripts/test-postgres.ps1`.

## Perguntas prováveis

**“Como vocês garantem que duas pessoas não vencem ao mesmo tempo?”**

O anúncio é bloqueado na transação antes de conferir e registrar o lance. A segunda transação relê o estado depois de obter o bloqueio. O encerramento usa esse mesmo mecanismo e uma restrição de unicidade da negociação por anúncio.

**“Por que atualizar pelo WebSocket não resolve a concorrência?”**

O WebSocket informa os clientes. A consistência precisa estar protegida no servidor e no banco, independentemente da tela utilizada.

**“Qual a diferença entre interface e DIP?”**

A interface descreve um contrato. DIP define a direção da dependência: a regra de negócio usa uma abstração adequada, e o detalhe técnico a implementa. Uma interface que apenas reproduz detalhes do banco pode não atingir esse objetivo.

**“Vocês aplicaram SOLID em todo lugar?”**

Os princípios guiam as divisões. O cadastro contém uma aplicação explícita dos cinco princípios. Outros módulos usam uma organização em camadas com entidades JPA e interfaces Spring Data; isso é um compromisso documentado, não uma arquitetura hexagonal completa.

**“Como o sistema valida os alunos de verdade?”**

A versão demonstrada usa uma base fictícia e convites de teste. A integração institucional ainda não foi implementada. O perfil de PostgreSQL recusa novos cadastros até que essa integração seja configurada.

**“Uma avaliação pode ser feita por qualquer pessoa?”**

Não. Ela exige participação na negociação, confirmação de ambos os lados e apenas uma avaliação por autor e negociação. Há validação de domínio e restrição no banco.

**“Por que não usar somente o banco em memória?”**

H2 simplifica a apresentação local; PostgreSQL permite verificar o comportamento no banco de destino, inclusive bloqueios, migrações e restrições.

## Preparação antes da aula

1. Execute os testes e o build com antecedência para baixar dependências.
2. Inicie os dois servidores.
3. Abra as contas em sessões independentes.
4. Prepare uma foto JPEG ou PNG para publicar um anúncio.
5. Verifique se o leilão de demonstração terá tempo suficiente ou crie um novo.
6. Deixe `SOLID.md` e os testes abertos no editor.
7. Não inclua `tools`, `node_modules`, bancos ou senhas de ambiente na entrega de código.
