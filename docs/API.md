# Referência da API

Base local: `http://localhost:8080/api`. No frontend, utilize `/api` através do proxy Vite. As rotas de negócio exigem sessão; somente saúde, token CSRF, login e cadastro são acessíveis antes da autenticação.

## Autenticação e CSRF

1. Faça `GET /auth/csrf` e preserve o cookie de sessão recebido.
2. A resposta contém `token` e `headerName`.
3. Em POST, PUT e DELETE, envie o token no header informado, normalmente `X-CSRF-TOKEN`, junto ao cookie.
4. Faça login com `POST /auth/login`.
5. Após logout, obtenha um novo token para uma nova sessão.

Exemplo de login:

```json
{
  "ra": "20260001",
  "password": "Campus@123"
}
```

O cookie é HttpOnly; não existe token de autenticação salvo em localStorage. A identidade do autor é obtida na API a partir da sessão.

| Método | Rota | Finalidade |
| --- | --- | --- |
| GET | `/health` | Estado da aplicação. |
| GET | `/auth/csrf` | Token CSRF e nome do header. |
| POST | `/auth/register` | Cadastro com R.A., nome, curso, senha e convite. |
| POST | `/auth/login` | Autenticação e estabelecimento da sessão. |
| GET | `/auth/me` | Dados privados da própria conta. |
| POST | `/auth/logout` | Invalidação da sessão e fechamento dos sockets associados. |

Cadastro de demonstração:

```json
{
  "ra": "20260004",
  "name": "Ana Silva",
  "course": "Ciência da Computação",
  "password": "Senha@123",
  "invitation": "CAMPUS-0004"
}
```

## Anúncios

| Método | Rota | Finalidade |
| --- | --- | --- |
| GET | `/listings` | Catálogo paginado com 24 itens por página. |
| GET | `/listings/mine` | Anúncios do estudante autenticado. |
| GET | `/listings/{id}` | Detalhes de um anúncio não excluído. |
| POST | `/listings` | Criar anúncio. |
| PUT | `/listings/{id}` | Editar anúncio próprio permitido pelas regras. |
| POST | `/listings/{id}/pause` | Alternar pausa de venda direta. |
| DELETE | `/listings/{id}` | Exclusão lógica, quando permitida. |
| POST | `/listings/{id}/favorite` | Alternar favorito; retorna `favorite`. |

Parâmetros do catálogo:

- `q`: busca por título e descrição.
- `category`: `Livros`, `Tecnologia`, `Materiais`, `Moda` ou `Outros`.
- `condition`: `NOVO`, `SEMINOVO` ou `USADO`.
- `mode`: `DIRETA` ou `LEILAO`.
- `min` e `max`: preço inicial ou anunciado, em reais.
- `order`: `recent`, `price-asc`, `price-desc` ou `ending`.
- `page`: índice de página a partir de zero.
- `favorites=true`: filtrar pelos favoritos da sessão.

O filtro e a ordenação de preços utilizam o preço anunciado/inicial, não o maior lance. A resposta possui `items`, `total`, `pages` e `page`.

Exemplo de publicação, utilizando a URL de uma imagem previamente enviada:

```json
{
  "title": "Livro de Java conservado",
  "description": "Livro completo, sem páginas faltantes. Entrego no campus.",
  "category": "Livros",
  "condition": "SEMINOVO",
  "mode": "DIRETA",
  "price": 80.00,
  "endsAt": null,
  "images": ["/api/images/00000000-0000-0000-0000-000000000001.jpg"]
}
```

A URL acima é ilustrativa: substitua pelo retorno real do upload. Para leilão, informe `mode: LEILAO` e `endsAt` em ISO 8601 UTC, com duração entre 1 minuto e 7 dias.

## Imagens

| Método | Rota | Finalidade |
| --- | --- | --- |
| POST | `/images` | Upload multipart no campo `image`; retorna `{ "url": "..." }`. |
| GET | `/images/{key}` | Leitura de JPEG armazenado, com autenticação. |

Limites: 5 MB por arquivo e 16 megapixels. O backend normaliza o conteúdo para JPEG. O formulário de publicação aceita até cinco fotos.

## Lances e propostas

| Método | Rota | Finalidade |
| --- | --- | --- |
| GET | `/listings/{id}/bids` | Últimos 30 lances, com participantes pseudonimizados. |
| POST | `/listings/{id}/bids` | Registrar lance: `{ "amount": 120.00 }`. |
| GET | `/bids/mine` | Participações, maior lance pessoal e indicação de liderança. |
| POST | `/listings/{id}/offers` | Criar proposta: `{ "amount": 75.00, "message": "Retiro no campus." }`. |
| GET | `/offers` | Propostas enviadas e recebidas pela sessão. |
| POST | `/offers/{id}/response` | Vendedor responde: `{ "accept": true }` ou `false`. |

O primeiro lance pode ser igual ao preço inicial. Os próximos precisam superar o maior lance em pelo menos R$ 1,00. Valores monetários admitem até duas casas decimais. O vendedor não participa do próprio leilão.

## Negociações e avaliações

| Método | Rota | Finalidade |
| --- | --- | --- |
| GET | `/trades` | Negociações de que o estudante participa. |
| POST | `/trades/{id}/confirm` | Confirmar a entrega pelo participante da sessão. |
| POST | `/trades/{id}/review` | Avaliar: `{ "rating": 5, "comment": "Excelente experiência." }`. |

A conclusão exige as duas confirmações. Avaliações exigem nota de 1 a 5, comentário de 3 a 1500 caracteres e negociação concluída. Não é permitido avaliar duas vezes a mesma negociação como o mesmo autor.

## Conversas

| Método | Rota | Finalidade |
| --- | --- | --- |
| GET | `/conversations` | Conversas da sessão. |
| POST | `/conversations` | Iniciar ou recuperar conversa: `{ "listingId": "uuid" }`. |
| GET | `/conversations/{id}/messages` | Últimas 200 mensagens, em ordem cronológica. |
| POST | `/conversations/{id}/messages` | Enviar: `{ "content": "Olá! Ainda está disponível?" }`. |

Mensagens têm até 2000 caracteres. Somente comprador da conversa e vendedor do anúncio podem ler ou escrever. O vendedor acessa conversas recebidas pela caixa de entrada.

## Perfil e notificações

| Método | Rota | Finalidade |
| --- | --- | --- |
| GET | `/users/{id}` | Perfil público interno, anúncios ativos e avaliações. |
| PUT | `/users/me` | Atualizar `name`, `course` e `biography`. |
| GET | `/notifications` | As 50 notificações mais recentes do usuário. |
| POST | `/notifications/read` | Marcar notificações próprias como lidas. |

O perfil público não contém R.A. nem hash de senha. O R.A. é exibido somente na própria conta e não pode ser alterado por essa API.

## WebSocket

Endpoint: `/ws`, no mesmo host da API ou através do proxy de desenvolvimento. O cliente usa STOMP 1.2 e a sessão HTTP autenticada.

- `/topic/catalog`: mudanças de anúncios, sem dados privados de negociação.
- `/user/queue/updates`: aviso privado para atualizar as consultas da sessão.

Outras assinaturas e comandos STOMP de envio são recusados. As mensagens são enviadas pela API HTTP. A invalidade da sessão encerra as conexões associadas.

## Erros

Falhas esperadas retornam JSON com `message`:

```json
{ "message": "O prazo deste leilão terminou." }
```

| Código | Situação |
| --- | --- |
| 400 | Formato ou parâmetros inválidos. |
| 401 | Sessão necessária ou credenciais inválidas. |
| 403 | Operação não autorizada ou CSRF inválido. |
| 404 | Registro inexistente ou não disponível. |
| 409 | Conflito com restrição do banco. |
| 413 | Upload acima do limite. |
| 422 | Regra de negócio não atendida. |
| 503 | Integração institucional ou armazenamento indisponível. |
