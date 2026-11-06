import { useEffect, useState, type FormEvent } from 'react';
import { ArrowRight, BadgeCheck, Check, Gavel, Heart, MapPin, MessageCircle, ShieldCheck, Star, Timer } from 'lucide-react';
import { api, conditions, errorMessage, initials, money, post, statuses } from '../api';
import type { Bid, Listing, Profile, User } from '../types';
import { ErrorBox, Modal } from '../components/ui';
import { Countdown } from '../components/ProductCard';

export function ListingDialog({ listing, user, revision, onClose, onFavorite, onChat, onProfile, onEdit, onChanged }: {
  listing: Listing; user: User; revision: number; onClose: () => void; onFavorite: (l: Listing) => void;
  onChat: (listingId: string) => void; onProfile: (id: string) => void; onEdit: (l: Listing) => void; onChanged: () => void;
}) {
  const [item, setItem] = useState(listing); const [profile, setProfile] = useState<Profile | null>(null);
  const [bids, setBids] = useState<Bid[]>([]); const [amount, setAmount] = useState(''); const [message, setMessage] = useState('');
  const [image, setImage] = useState(0); const [error, setError] = useState(''); const [success, setSuccess] = useState(''); const [busy, setBusy] = useState(false);
  useEffect(() => {
    let active = true;
    api<Listing>(`/listings/${listing.id}`).then(l => { if (active) setItem(l); }).catch(e => { if (active) setError(errorMessage(e)); });
    api<Profile>(`/users/${listing.sellerId}`).then(p => { if (active) setProfile(p); }).catch(() => {});
    if (listing.mode === 'LEILAO') api<Bid[]>(`/listings/${listing.id}/bids`).then(b => { if (active) setBids(b); }).catch(() => {});
    return () => { active = false; };
  }, [listing.id, listing.sellerId, listing.mode, revision]);
  const own = item.sellerId === user.id, auction = item.mode === 'LEILAO';
  async function submit(e: FormEvent) {
    e.preventDefault(); setBusy(true); setError(''); setSuccess('');
    try {
      await post(`/listings/${item.id}/${auction ? 'bids' : 'offers'}`, { amount: Number(amount || (auction ? item.minimumBid : item.price)), message });
      setSuccess(auction ? 'Lance registrado! Acompanhe a disputa por aqui.' : 'Proposta enviada! Acompanhe a resposta em Negociações.');
      setAmount(''); onChanged();
    } catch (e) { setError(errorMessage(e)); } finally { setBusy(false); }
  }
  return <Modal title={auction ? 'Um achado em disputa' : 'Conheça seu próximo achado'} onClose={onClose} wide>
    <div className="detail-grid"><div><div className="detail-image"><img src={item.images[image] || item.images[0]} alt={item.title}/><span className="condition-badge">{conditions[item.condition]}</span><button className={`favorite-button ${item.favorite ? 'is-favorite' : ''}`} aria-label="Alternar favorito" aria-pressed={item.favorite} onClick={() => onFavorite(item)}><Heart size={20} fill={item.favorite ? 'currentColor' : 'none'}/></button></div>
      {item.images.length > 1 && <div className="image-thumbnails">{item.images.map((src, i) => <button key={src} className={image === i ? 'active' : ''} onClick={() => setImage(i)} aria-label={`Ver imagem ${i + 1}`}><img src={src} alt=""/></button>)}</div>}
      <div className="safe-tip"><ShieldCheck size={20}/><span><strong>Uma comunidade, mais confiança.</strong><br/>Combine um encontro no campus e confira o produto antes de concluir.</span></div>
    </div><div className="detail-content"><div className="eyebrow">{item.category} <span> / </span> {auction ? 'LEILÃO' : 'VENDA DIRETA'}</div><h1>{item.title}</h1><span className="detail-location"><MapPin size={14}/> Retirada a combinar no campus</span><div className="detail-price"><small>{auction ? item.bidCount ? 'Maior lance atual' : 'Valor inicial' : 'Valor anunciado'}</small><strong>{money(auction && item.bidCount ? item.highestBid : item.price)}</strong></div>
      {auction && <div className="auction-clock"><Timer size={21}/><div><small>{item.status === 'ATIVO' ? 'Encerra em' : statuses[item.status]}</small><strong>{item.status === 'ATIVO' ? <Countdown endsAt={item.endsAt}/> : 'Disputa encerrada'}</strong></div><span>{item.bidCount} lances</span></div>}
      <ErrorBox message={error}/>{success && <div className="success-box" role="status"><Check size={17}/>{success}</div>}
      {own ? <div className="owner-actions"><p>Este anúncio é seu.</p>{['ATIVO', 'PAUSADO'].includes(item.status) && <button className="button primary full" onClick={() => onEdit(item)}>Editar anúncio</button>}</div> : item.status === 'ATIVO' ? <form className="form-stack offer-form" onSubmit={submit}><label>{auction ? `Seu lance · mínimo ${money(item.minimumBid)}` : 'Quanto você quer oferecer?'}<div className="currency-input"><span>R$</span><input aria-label={auction ? 'Valor do lance' : 'Valor da proposta'} type="number" min={auction ? item.minimumBid : 0.01} max="99999999.99" step="0.01" value={amount} onChange={e => setAmount(e.target.value)} placeholder={String(auction ? item.minimumBid : item.price)}/></div></label>{!auction && <label>Mensagem para o vendedor<textarea maxLength={1000} value={message} onChange={e => setMessage(e.target.value)} placeholder="Oi! Tenho interesse. Podemos combinar a entrega?" rows={2}/></label>}<button className="button primary full" disabled={busy}>{auction ? <Gavel size={18}/> : <ArrowRight size={18}/>} {busy ? 'Enviando…' : auction ? 'Dar meu lance' : 'Enviar proposta'}</button><button type="button" className="button secondary full" onClick={() => onChat(item.id)}><MessageCircle size={17}/> Conversar com vendedor</button></form> : <div className="notice-box">{statuses[item.status]}. {item.status === 'RESERVADO' ? 'O produto está em negociação.' : 'Este anúncio não recebe novas propostas.'}</div>}
      <button className="seller-profile" onClick={() => onProfile(item.sellerId)}><span className="avatar">{initials(item.sellerName)}</span><span><strong>{item.sellerName} <BadgeCheck size={15}/></strong><small>{item.sellerCourse}</small><small className="rating"><Star size={13}/>{profile?.reviewCount ? `${profile.rating.toFixed(1)} · ${profile.reviewCount} avaliações` : 'Novo na comunidade'}</small></span><ArrowRight size={17}/></button>
    </div></div>
    <section className="detail-description"><h3>Sobre este achado</h3><p>{item.description}</p></section>
    {auction && <section className="bid-history"><h3>Últimos lances</h3>{bids.length ? bids.map(b => <div key={b.id}><span>{b.bidderName}</span><small>{new Date(b.createdAt).toLocaleString('pt-BR')}</small><strong>{money(b.amount)}</strong></div>) : <p>Seja a primeira pessoa a dar um lance.</p>}</section>}
  </Modal>;
}
