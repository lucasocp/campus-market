# Registro de validação

Validação realizada em 6 de novembro de 2026, no Windows do ambiente de desenvolvimento. O projeto é uma demonstração acadêmica local; não foi publicado como serviço institucional.

## Ambiente utilizado

| Componente | Versão observada |
| --- | --- |
| Node.js | 24.22.1 |
| pnpm | 11.25.0 |
| JDK de execução dos testes | 25.0.3, incluído no Android Studio |
| Alvo de compilação Java | `release 21` |
| Maven | 3.9.16, portátil em `tools` |
| Spring Boot | 4.1.2 |
| PostgreSQL de teste | 17.5 |
| H2 | 2.4.240 |
| React | 19.3.1 |
| Vite | 7.3.7 |
| TypeScript | 5.9.3 |

O código foi compilado para Java 21, mas os testes desta execução rodaram no JDK 25. Não foi executada uma segunda validação com um JDK 21 separado. O checksum SHA-512 do ZIP Maven foi comparado com o publicado no repositório oficial e coincidiu.

## Resultados automatizados

| Verificação | Resultado |
| --- | --- |
| TypeScript e build Vite | Aprovados. |
| `backend.ps1 package` | Aprovado; JAR executável gerado. |
| Suíte com H2 | 36 testes, 0 falhas, 0 erros, 0 ignorados. |
| Mesma suíte com PostgreSQL 17.5 | 36 testes, 0 falhas, 0 erros, 0 ignorados. |
| Migração Flyway e validação JPA | Aprovadas em H2 e PostgreSQL. |
| Inicialização do JAR empacotado | Aprovada em modo `demo`. |

Distribuição dos testes:

| Classe | Quantidade | Comportamentos verificados |
| --- | --- | --- |
| `ListingRulesTest` | 13 | Preço, lance mínimo, autoria, prazo exato, edição, encerramento e confirmação bilateral. |
| `RegisterUserTest` | 5 | Cadastro válido, hash, convite incorreto, duplicidade e substituição do validador. |
| `UploadImageTest` | 3 | Conteúdo inválido, tamanho excessivo e conversão PNG para JPEG. |
| `UserAccountsContractTest` | 2 | Mesmo contrato de armazenamento em memória e JPA. |
| `HttpSecurityIntegrationTest` | 8 | Autenticação, CSRF, logout, privacidade e segurança de WebSocket. |
| `MarketplaceIntegrationTest` | 5 | Concorrência real de transações, idempotência, chat privado e negociação concluída. |

A suíte de concorrência inicia duas transações em threads distintas com lances iguais. Exige exatamente um aceite, um registro de lance e um maior valor consistente. Foi executada nos dois bancos.

Os testes WebSocket verificam handshake anônimo recusado, conexão autenticada, recusa de assinatura de canal arbitrário e fechamento da conexão após logout. A consulta do próprio perfil não expõe senha ou hash; o perfil público não expõe R.A.

O teste PostgreSQL criou uma base nova em um cluster exclusivo do projeto, vinculado a `127.0.0.1:55432`, e encerrou o servidor ao terminar. Não utilizou bancos de outras aplicações instaladas.

## Verificações no navegador

Foram observados diretamente:

- Login com o botão de demonstração.
- Catálogo com 12 produtos ativos e imagens locais carregadas.
- Pesquisa por `MacBook`, retornando um produto.
- Abertura dos detalhes e recuperação da reputação do vendedor.
- Abertura da conversa do anúncio, com histórico persistido.
- Interface em viewport desktop de 1280 × 900 e celular de 390 × 844.
- Busca específica no celular e painel de filtros acessível.
- Ausência de largura de conteúdo acima do viewport móvel observado.
- Lance fictício de R$ 120 na calculadora: sucesso, histórico atualizado e mínimo seguinte de R$ 121.
- Exibição de liderança em “Leilões que você acompanha”.
- Favorito da calculadora alterado de desmarcado para marcado.

O lance e o favorito de teste foram preservados na base local da demonstração. Podem ser consultados com a conta da Luana. O leilão será encerrado quando seu prazo real terminar.

As verificações de navegador foram manuais por automação de UI; não constituem uma suíte Playwright incluída no repositório. Os testes automatizados Java estão incluídos.

## Avisos de ferramentas

Flyway emitiu um aviso de que a versão H2 2.4.240 é posterior à versão H2 previamente verificada por ele. A migração e a validação do projeto passaram mesmo assim; o PostgreSQL também foi validado separadamente.

Mockito emitiu um aviso de carregamento dinâmico de agente no JDK 25. Os testes passaram. Uma atualização futura do JDK pode exigir configurar o agente explicitamente.

O inicializador PostgreSQL no Windows foi ajustado para aguardar apenas `pg_ctl`, evitando aguardar indefinidamente o processo servidor descendente. O servidor de teste é iniciado em console oculto e encerrado no bloco de limpeza.

## Limites da verificação

Não foram realizados teste de carga, auditoria externa de segurança, integração com matrícula real, pagamentos, publicação com HTTPS ou testes em aparelhos físicos. A validação responsiva ocorreu no navegador com os viewports indicados.

Os testes confirmam os cenários cobertos. Não representam garantia de ausência de defeitos ou prontidão para operação institucional. Os limites funcionais estão detalhados no README.
