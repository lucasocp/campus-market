import { useEffect, useState } from 'react';
import { ArrowUpRight, Heart, MapPin, Timer } from 'lucide-react';
import type { Listing } from '../types';
import { conditions, money, statuses } from '../api';

export function timeLeft(endsAt: string | null, now = Date.now()) {
  const remaining = Math.max(0, (new Date(endsAt || 0).getTime() - now) / 1000);
  if (!remaining) return 'Encerrando';
  const hours = Math.floor(remaining / 3600), minutes = Math.floor(remaining / 60) % 60, seconds = Math.floor(remaining % 60);
  return `${hours.toString().padStart(2, '0')}h ${minutes.toString().padStart(2, '0')}m ${seconds.toString().padStart(2, '0')}s`;
}
export function Countdown({ endsAt }: { endsAt: string | null }) {
  const [now, setNow] = useState(Date.now());
  useEffect(() => { const id = setInterval(() => setNow(Date.now()), 1000); return () => clearInterval(id); }, []);
  return <span>{timeLeft(endsAt, now)}</span>;
}
export function ProductCard({ listing: l, onOpen, onFavorite }: { listing: Listing; onOpen: (l: Listing) => void; onFavorite: (l: Listing) => void }) {
  return <article className="product-card">
    <div className="product-picture">
      <button className="picture-link" onClick={() => onOpen(l)} aria-label={`Ver ${l.title}`}><img src={l.images[0]} alt={l.title} loading="lazy"/></button>
      <span className={`condition-badge ${l.mode === 'LEILAO' ? 'auction-badge' : ''}`}>{l.mode === 'LEILAO' ? <><span className="live-dot"/> Leilão</> : conditions[l.condition]}</span>
      <button className={`favorite-button ${l.favorite ? 'is-favorite' : ''}`} aria-label={l.favorite ? `Desfavoritar ${l.title}` : `Favoritar ${l.title}`} aria-pressed={l.favorite} onClick={() => onFavorite(l)}><Heart size={17} fill={l.favorite ? 'currentColor' : 'none'}/></button>
      {l.mode === 'LEILAO' && <div className="card-countdown"><Timer size={13}/><Countdown endsAt={l.endsAt}/></div>}
    </div>
    <div className="product-information"><span className="product-category">{l.category}</span><button className="product-title" onClick={() => onOpen(l)}>{l.title}</button>
      <div className="price-row"><strong>{money(l.mode === 'LEILAO' && l.bidCount > 0 ? l.highestBid : l.price)}</strong>{l.mode === 'LEILAO' && <span>{l.bidCount} {l.bidCount === 1 ? 'lance' : 'lances'}</span>}<ArrowUpRight size={17}/></div>
      <div className="product-footer"><span className="seller-dot">{l.sellerName[0]}</span><span>{l.sellerName.split(' ').slice(0, 2).join(' ')}</span><span className="campus-label"><MapPin size={11}/> No campus</span></div>
      {l.status !== 'ATIVO' && <span className="status-tag">{statuses[l.status]}</span>}
    </div>
  </article>;
}
