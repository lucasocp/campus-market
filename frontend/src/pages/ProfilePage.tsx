import { useEffect, useState, type FormEvent } from 'react';
import { BadgeCheck, CalendarDays, GraduationCap, Save, Star } from 'lucide-react';
import { api, date, errorMessage, initials } from '../api';
import type { Listing, Profile, User } from '../types';
import { Empty, ErrorBox, Modal, SectionTitle, Spinner } from '../components/ui';
import { ProductCard } from '../components/ProductCard';

export function ProfileContent({ id, revision, onOpen, onFavorite }: { id: string; revision: number; onOpen: (l: Listing) => void; onFavorite: (l: Listing) => void }) {
  const [profile, setProfile] = useState<Profile | null>(null); const [error, setError] = useState('');
  useEffect(() => { let active = true; api<Profile>(`/users/${id}`).then(p => { if (active) setProfile(p); }).catch(e => { if (active) setError(errorMessage(e)); }); return () => { active = false; }; }, [id, revision]);
  if (error) return <ErrorBox message={error}/>;
  if (!profile) return <Spinner/>;
  return <><section className="profile-banner"><span className="profile-avatar">{initials(profile.name)}</span><div><span className="eyebrow">MEMBRO DA COMUNIDADE</span><h1>{profile.name} <BadgeCheck size={24}/></h1><p><GraduationCap size={16}/>{profile.course}</p><small><CalendarDays size={14}/> No CampusMarket desde {date(profile.createdAt)}</small></div><div className="profile-score"><Star size={24} fill="#d5a24d" color="#d5a24d"/><strong>{profile.reviewCount ? profile.rating.toFixed(1) : '—'}</strong><span>{profile.reviewCount} avaliações</span></div></section>{profile.biography && <p className="profile-bio">{profile.biography}</p>}
    <SectionTitle title="O que a comunidade diz"/>{profile.reviews.length ? <div className="review-grid">{profile.reviews.map(r => <article className="review-card" key={r.id}><div className="rating">{'★'.repeat(r.rating)}{'☆'.repeat(5-r.rating)}</div><p>{r.comment}</p><strong>{r.authorName}</strong><small>{date(r.createdAt)}</small></article>)}</div> : <p className="muted">As avaliações aparecerão depois das primeiras negociações concluídas.</p>}
    <SectionTitle title="Anúncios ativos"/>{profile.listings.length ? <div className="product-grid">{profile.listings.map(l => <ProductCard key={l.id} listing={l} onOpen={onOpen} onFavorite={onFavorite}/>)}</div> : <Empty title="Nenhum anúncio ativo.">Novos achados poderão aparecer por aqui.</Empty>}
  </>;
}
export function ProfileDialog({ id, revision, onClose, onOpen, onFavorite }: { id: string; revision: number; onClose: () => void; onOpen: (l: Listing) => void; onFavorite: (l: Listing) => void }) {
  return <Modal title="Perfil na comunidade" onClose={onClose} wide><ProfileContent id={id} revision={revision} onOpen={onOpen} onFavorite={onFavorite}/></Modal>;
}
export function SettingsPage({ user, onUpdated }: { user: User; onUpdated: (u: User) => void }) {
  const [name, setName] = useState(user.name); const [course, setCourse] = useState(user.course); const [biography, setBiography] = useState(user.biography);
  const [error, setError] = useState(''); const [success, setSuccess] = useState(''); const [busy, setBusy] = useState(false);
  async function submit(e: FormEvent) { e.preventDefault(); setBusy(true); setError(''); setSuccess(''); try { const updated = await api<User>('/users/me', { method: 'PUT', body: JSON.stringify({ name, course, biography }) }); onUpdated(updated); setSuccess('Seu perfil foi atualizado.'); } catch (e) { setError(errorMessage(e)); } finally { setBusy(false); } }
  return <><div className="page-intro"><div className="eyebrow">DO SEU JEITO</div><h1>Configurações da conta.</h1><p>Cuide das informações que a comunidade vê sobre você.</p></div><form className="form-stack settings-card" onSubmit={submit}><ErrorBox message={error}/>{success && <div className="success-box" role="status">{success}</div>}<label>Registro Acadêmico<input value={user.ra} disabled/><small>Seu R.A. é privado e não pode ser alterado.</small></label><label>Nome de exibição<input required maxLength={100} value={name} onChange={e => setName(e.target.value)}/></label><label>Curso<input required maxLength={100} value={course} onChange={e => setCourse(e.target.value)}/></label><label>Sobre você<textarea rows={4} maxLength={500} value={biography} onChange={e => setBiography(e.target.value)}/></label><button className="button primary" disabled={busy}><Save size={17}/>{busy ? 'Salvando…' : 'Salvar alterações'}</button></form></>;
}
