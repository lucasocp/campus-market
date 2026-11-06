let csrf: { token: string; headerName: string } | null = null;
let csrfRequest: Promise<{ token: string; headerName: string }> | null = null;

async function token() {
  if (csrf) return csrf;
  csrfRequest ??= fetch('/api/auth/csrf', { credentials: 'same-origin' })
    .then(async response => {
      if (!response.ok) throw new Error('Não foi possível conectar ao servidor.');
      return response.json();
    });
  try { csrf = await csrfRequest; return csrf!; }
  finally { csrfRequest = null; }
}

/** Único ponto de comunicação HTTP: sessão em cookie HttpOnly e proteção CSRF. */
export async function api<T>(path: string, options: RequestInit = {}): Promise<T> {
  const headers = new Headers(options.headers);
  if (options.body && !(options.body instanceof FormData)) headers.set('Content-Type', 'application/json');
  if (options.method && !['GET', 'HEAD'].includes(options.method.toUpperCase())) {
    const protection = await token();
    headers.set(protection.headerName, protection.token);
  }
  let response: Response;
  try { response = await fetch(`/api${path}`, { ...options, headers, credentials: 'same-origin' }); }
  catch { throw new Error('Não foi possível conectar. Verifique se o backend está em execução.'); }
  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: 'O servidor não conseguiu concluir a operação.' }));
    if (response.status === 401 && path !== '/auth/me' && path !== '/auth/login') window.dispatchEvent(new Event('session-expired'));
    if (response.status === 403) csrf = null;
    throw new Error(error.message || 'Não foi possível concluir a operação.');
  }
  const text = await response.text();
  return text ? JSON.parse(text) as T : undefined as T;
}
export function post<T>(path: string, body?: unknown) { return api<T>(path, { method: 'POST', body: body === undefined ? undefined : JSON.stringify(body) }); }
export function resetSessionToken() { csrf = null; }
export const money = (value: number) => new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(value);
export const date = (value: string) => new Date(value).toLocaleDateString('pt-BR', { day: '2-digit', month: 'short' });
export const initials = (name: string) => name.split(' ').filter(Boolean).slice(0, 2).map(n => n[0]).join('').toUpperCase();
export const errorMessage = (error: unknown) => error instanceof Error ? error.message : 'Ocorreu um erro inesperado.';
export const conditions = { NOVO: 'Novo', SEMINOVO: 'Seminovo', USADO: 'Usado' };
export const statuses = { ATIVO: 'Ativo', PAUSADO: 'Pausado', RESERVADO: 'Em negociação', VENDIDO: 'Vendido', ENCERRADO: 'Encerrado', EXCLUIDO: 'Removido' };
