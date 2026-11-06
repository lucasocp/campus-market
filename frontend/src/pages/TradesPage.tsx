import { useEffect, useState, type FormEvent } from 'react';
import { Check, CheckCheck, MessageCircle, Star } from 'lucide-react';
import { api, errorMessage, money, post } from '../api';
import type { Offer, Trade, User } from '../types';
import { Empty, ErrorBox, Modal, SectionTitle, Spinner } from '../components/ui';

export function TradesPage({ user, revision, onChanged, onChat }: { user: User; revision: number; onChanged: () => void; onChat: (id: string) => void }) {
  const [offers, setOffers] = useState<Offer[] | null>(null); const [trades, setTrades] = useState<Trade[]>([]);
  const [tab, setTab] = useState<'trades' | 'offers'>('trades'); const [error, setError] = useState(''); const [busy, setBusy] = useState('');
  const [review, setReview] = useState<Trade | null>(null); const [rating, setRating] = useState(5); const [comment, setComment] = useState(''); const [reviewError, setReviewError] = useState('');
  useEffect(() => {
    let active = true;
    Promise.all([api<Offer[]>('/offers'), api<Trade[]>('/trades')]).then(([o, t]) => { if (active) { setOffers(o); setTrades(t); } }).catch(e => { if (active) setError(errorMessage(e)); });
    return () => { active = false; };
  }, [revision]);
  async function action(id: string, path: string, body?: unknown) {
    setBusy(id); setError('');
    try { await post(path, body); onChanged(); } catch (e) { setError(errorMessage(e)); } finally { setBusy(''); }
  }
  async function submitReview(e: FormEvent) {
    e.preventDefault(); if (!review) return; setBusy(review.id); setReviewError('');
    try { await post(`/trades/${review.id}/review`, { rating, comment }); setReview(null); setComment(''); onChanged(); }
    catch (e) { setReviewError(errorMessage(e)); } finally { setBusy(''); }
  }
  return <><div className="page-intro"><div className="eyebrow">DO PRIMEIRO OI AO NEGÓCIO FECHADO</div><h1>Suas negociações.</h1><p>Acompanhe propostas, confirme entregas e compartilhe sua experiência.</p></div>
    <div className="page-tabs"><button className={tab === 'trades' ? 'active' : ''} onClick={() => setTab('trades')}>Negociações <span>{trades.length}</span></button><button className={tab === 'offers' ? 'active' : ''} onClick={() => setTab('offers')}>Propostas <span>{offers?.length || 0}</span></button></div><ErrorBox message={error}/>
    {!offers ? <Spinner/> : tab === 'trades' ? trades.length ? <div className="trade-list">{trades.map(t => { const buyer = t.buyerId === user.id; const confirmed = buyer ? t.buyerConfirmed : t.sellerConfirmed; return <article className="trade-card" key={t.id}><img src={t.image} alt=""/><div className="trade-body"><span className={`status-tag ${t.completedAt ? 'complete' : ''}`}>{t.completedAt ? 'Concluída' : 'Em andamento'}</span><h3>{t.title}</h3><p>{buyer ? `Vendedor: ${t.sellerName}` : `Comprador: ${t.buyerName}`}</p><strong>{money(t.amount)}</strong><div className="confirmation-state"><span className={t.buyerConfirmed ? 'done' : ''}><Check size={14}/> Comprador {t.buyerConfirmed ? 'confirmou' : 'pendente'}</span><span className={t.sellerConfirmed ? 'done' : ''}><Check size={14}/> Vendedor {t.sellerConfirmed ? 'confirmou' : 'pendente'}</span></div></div><div className="trade-actions">{t.completedAt ? <button className="button primary" onClick={() => { setReview(t); setReviewError(''); }}><Star size={16}/> Avaliar experiência</button> : <button className="button primary" disabled={!!busy || confirmed} onClick={() => void action(t.id, `/trades/${t.id}/confirm`)}><CheckCheck size={17}/>{confirmed ? 'Aguardando outra pessoa' : busy === t.id ? 'Confirmando…' : 'Confirmar entrega'}</button>}{buyer && <button className="button secondary" onClick={() => onChat(t.listingId)}><MessageCircle size={16}/> Conversar</button>}</div></article>; })}</div> : <Empty title="Seus bons negócios vão aparecer aqui.">Quando uma proposta for aceita ou você vencer um leilão, acompanhe a negociação nesta página.</Empty>
    : offers.length ? <div className="offers-list">{offers.map(o => <article className="offer-card" key={o.id}><div><span className="eyebrow">{o.sellerId === user.id ? 'PROPOSTA RECEBIDA' : 'PROPOSTA ENVIADA'}</span><h3>{o.title}</h3><p>{o.sellerId === user.id ? o.buyerName : o.sellerName}</p>{o.message && <blockquote>{o.message}</blockquote>}</div><div className="offer-value"><strong>{money(o.amount)}</strong><span className="status-tag">{o.status === 'PENDENTE' ? 'Aguardando resposta' : o.status === 'ACEITA' ? 'Aceita' : 'Recusada'}</span>{o.sellerId === user.id && o.status === 'PENDENTE' && <div className="button-row"><button className="button primary" disabled={!!busy} onClick={() => void action(o.id, `/offers/${o.id}/response`, { accept: true })}>Aceitar</button><button className="button secondary" disabled={!!busy} onClick={() => void action(o.id, `/offers/${o.id}/response`, { accept: false })}>Recusar</button></div>}</div></article>)}</div> : <Empty title="As primeiras propostas estão por vir.">Encontre um produto no catálogo e envie sua oferta ao vendedor.</Empty>}
    <div className="notice-box">Confirme a entrega somente após conferir o produto e combinar o pagamento. A negociação termina quando as duas pessoas confirmam.</div>
    {review && <Modal title="Como foi essa experiência?" onClose={() => setReview(null)}><form className="form-stack" onSubmit={submitReview}><p className="muted">Sua avaliação ajuda a construir uma comunidade de confiança.</p><ErrorBox message={reviewError}/><div className="rating-picker" role="group" aria-label="Nota da experiência">{[1,2,3,4,5].map(n => <button type="button" key={n} aria-label={`Nota ${n}`} aria-pressed={rating === n} onClick={() => setRating(n)}><Star size={32} fill={n <= rating ? '#d5a24d' : 'none'} color="#d5a24d"/></button>)}</div><label>Conte como foi<textarea required minLength={3} maxLength={1500} rows={4} value={comment} onChange={e => setComment(e.target.value)} placeholder="Comunicação, atendimento, estado do produto…"/></label><button className="button primary" disabled={!!busy}>Publicar avaliação</button></form></Modal>}
  </>;
}
