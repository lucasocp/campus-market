import { useCallback, useEffect, useState } from 'react';
import { ArrowRight, Bell, ChevronDown, Compass, Gavel, GraduationCap, Handshake, Heart, HelpCircle, LayoutGrid, LogOut, Menu, MessageCircle, Plus, Search, Settings, ShieldCheck, UserRound, X } from 'lucide-react';
import { api, errorMessage, initials, post, resetSessionToken } from './api';
import type { Conversation, Listing, Notice, Route, User } from './types';
import { Logo, Spinner, Tip } from './components/ui';
import { AuthPage } from './pages/AuthPage';
import { CatalogPage } from './pages/CatalogPage';
import { ListingDialog } from './pages/ListingDialog';
import { EditorDialog } from './pages/EditorDialog';
import { InboxPage } from './pages/InboxPage';
import { TradesPage } from './pages/TradesPage';
import { ProfileContent, ProfileDialog, SettingsPage } from './pages/ProfilePage';
import { MyListingsPage } from './pages/MyListingsPage';
import { useLiveUpdates } from './hooks/useLiveUpdates';
import './styles.css';

const navigation = [
  { id: 'explore', label: 'Explorar campus', icon: Compass },
  { id: 'auctions', label: 'Leilões ao vivo', icon: Gavel },
  { id: 'favorites', label: 'Meus favoritos', icon: Heart },
] as const;
const personal = [
  { id: 'mine', label: 'Meus anúncios', icon: LayoutGrid },
  { id: 'negotiations', label: 'Negociações', icon: Handshake },
  { id: 'inbox', label: 'Mensagens', icon: MessageCircle },
  { id: 'profile', label: 'Meu perfil', icon: UserRound },
] as const;

export default function App() {
  const [user, setUser] = useState<User | null>(null); const [loading, setLoading] = useState(true);
  const [route, setRoute] = useState<Route>('explore'); const [search, setSearch] = useState('');
  const [revision, setRevision] = useState(0); const refresh = useCallback(() => setRevision(n => n + 1), []);
  const [selected, setSelected] = useState<Listing | null>(null); const [editor, setEditor] = useState<Listing | 'new' | null>(null); const [profileId, setProfileId] = useState<string | null>(null);
  const [conversationId, setConversationId] = useState<string | null>(null);
  const [mobileMenu, setMobileMenu] = useState(false); const [noticeOpen, setNoticeOpen] = useState(false); const [notices, setNotices] = useState<Notice[]>([]);
  const [toast, setToast] = useState('');
  const [isMobile, setIsMobile] = useState(window.matchMedia('(max-width: 780px)').matches);
  useEffect(() => {
    const media = window.matchMedia('(max-width: 780px)');
    const update = () => setIsMobile(media.matches);
    media.addEventListener('change', update);
    return () => media.removeEventListener('change', update);
  }, []);
  useEffect(() => {
    const escape = (event: KeyboardEvent) => { if (event.key === 'Escape') { setMobileMenu(false); setNoticeOpen(false); } };
    window.addEventListener('keydown', escape);
    return () => window.removeEventListener('keydown', escape);
  }, []);
  useEffect(() => { api<User>('/auth/me').then(setUser).catch(() => {}).finally(() => setLoading(false)); }, []);
  useEffect(() => { const expire = () => { setUser(null); setSelected(null); setEditor(null); setProfileId(null); resetSessionToken(); }; window.addEventListener('session-expired', expire); return () => window.removeEventListener('session-expired', expire); }, []);
  useEffect(() => { if (!user) return; let active = true; api<Notice[]>('/notifications').then(n => { if (active) setNotices(n); }).catch(() => {}); return () => { active = false; }; }, [user?.id, revision]);
  useEffect(() => { if (!toast) return; const id = setTimeout(() => setToast(''), 7000); return () => clearTimeout(id); }, [toast]);
  useLiveUpdates(!!user, refresh);
  const navigate = (next: Route) => { setRoute(next); setMobileMenu(false); setNoticeOpen(false); if (next !== 'explore') setSearch(''); window.scrollTo({ top: 0, behavior: 'smooth' }); };
  const publish = () => { setEditor('new'); setMobileMenu(false); };
  const open = (l: Listing) => { setProfileId(null); setSelected(l); };
  async function favorite(l: Listing) { try { await post(`/listings/${l.id}/favorite`); refresh(); } catch (e) { setToast(errorMessage(e)); } }
  async function chat(listingId: string) { try { const c = await post<Conversation>('/conversations', { listingId }); setSelected(null); setProfileId(null); setConversationId(c.id); navigate('inbox'); refresh(); } catch (e) { setToast(errorMessage(e)); } }
  async function logout() { try { await post('/auth/logout'); setUser(null); setSelected(null); setEditor(null); setProfileId(null); setNotices([]); setConversationId(null); setSearch(''); setRoute('explore'); resetSessionToken(); } catch (e) { setToast(errorMessage(e)); } }
  async function toggleNotices() { setNoticeOpen(!noticeOpen); if (!noticeOpen) { try { await post('/notifications/read'); refresh(); } catch (e) { setToast(errorMessage(e)); } } }
  if (loading) return <div className="boot-screen"><Logo/><Spinner/></div>;
  if (!user) return <AuthPage onLogin={u => { setUser(u); setRoute('explore'); refresh(); }}/>;
  return <div className="app-shell">
    <header className="app-header"><button className="icon-button mobile-menu-button" aria-label="Abrir navegação" onClick={() => setMobileMenu(!mobileMenu)}><Menu size={22}/></button><button className="logo-button" onClick={() => navigate('explore')} aria-label="CampusMarket início"><Logo/></button><div className="header-search"><Search size={18}/><input aria-label="Pesquisar produtos" placeholder="O que você está procurando no campus?" value={search} onChange={e => { setSearch(e.target.value); if (route !== 'explore') setRoute('explore'); }}/><kbd>⌕</kbd></div><div className="header-actions"><div className="notification-wrap"><button className={`icon-button ${noticeOpen ? 'selected' : ''}`} aria-label="Notificações" aria-expanded={noticeOpen} onClick={() => void toggleNotices()}><Bell size={20}/>{notices.some(n => !n.read) && <span className="notification-dot"/>}</button>{noticeOpen && <section className="notification-popover"><div><strong>Suas notificações</strong><button className="icon-button" aria-label="Fechar notificações" onClick={() => setNoticeOpen(false)}><X size={16}/></button></div>{notices.length ? <ul>{notices.map(n => <li key={n.id}><span className="notice-icon"><Bell size={15}/></span><span>{n.content}<small>{new Date(n.createdAt).toLocaleString('pt-BR')}</small></span></li>)}</ul> : <p>Por enquanto, tudo em dia. Novas mensagens e propostas aparecerão aqui.</p>}</section>}</div><span className="header-separator"/><button className="header-profile" onClick={() => navigate('profile')} aria-label="Abrir meu perfil"><span className="avatar small">{initials(user.name)}</span><span>{user.name.split(' ')[0]} <ChevronDown size={13}/></span></button><button className="button primary header-publish" onClick={publish}><Plus size={17}/> Anunciar</button></div></header>
    {mobileMenu && <button className="sidebar-scrim" aria-label="Fechar navegação" onClick={() => setMobileMenu(false)}/>}
    <aside className={`sidebar ${mobileMenu ? 'open' : ''}`} inert={isMobile && !mobileMenu}><div className="campus-selector"><span className="campus-icon"><GraduationCap size={22}/></span><span><small>SUA COMUNIDADE</small><strong>Campus universitário</strong><span><span className="live-dot"/> Ambiente de demonstração</span></span></div><nav aria-label="Navegação principal"><div className="nav-group">{navigation.map(({ id, label, icon: Icon }) => <button key={id} className={`nav-item ${route === id ? 'active' : ''}`} onClick={() => navigate(id)}><Icon size={19}/><span>{label}</span>{id === 'auctions' && <span className="nav-live">AO VIVO</span>}</button>)}</div><div className="nav-heading">MEU ESPAÇO</div><div className="nav-group">{personal.map(({ id, label, icon: Icon }) => <button key={id} className={`nav-item ${route === id ? 'active' : ''}`} onClick={() => navigate(id)}><Icon size={19}/><span>{label}</span></button>)}</div></nav><Tip onClick={publish}/><div className="sidebar-bottom"><button className={`nav-item ${route === 'settings' ? 'active' : ''}`} onClick={() => navigate('settings')}><Settings size={18}/> Configurações</button><button className="nav-item" onClick={() => void logout()}><LogOut size={18}/> Sair da conta</button><span className="sidebar-footer"><span className="tiny-brand-dot"/> Conectando o campus.</span></div></aside>
    <main className="main-content"><div className="mobile-search"><Search size={17}/><input aria-label="Pesquisar produtos no celular" placeholder="O que você procura no campus?" value={search} onChange={e => { setSearch(e.target.value); if (route !== 'explore') setRoute('explore'); }}/></div><div className="content-topline"><span><span className="live-dot"/> COMPRE, VENDA E CONECTE-SE.</span><span><ShieldCheck size={14}/> Uma comunidade de estudantes</span></div>
      {['explore', 'auctions', 'favorites'].includes(route) && <CatalogPage key={route} route={route} search={search} revision={revision} onOpen={open} onFavorite={favorite} onPublish={publish} onAuctions={() => navigate('auctions')}/>}
      {route === 'mine' && <MyListingsPage revision={revision} onOpen={open} onFavorite={favorite} onEdit={setEditor} onPublish={publish} onChanged={refresh}/>}
      {route === 'inbox' && <InboxPage user={user} revision={revision} selectedId={conversationId} onSelected={setConversationId} onOpen={open}/>}
      {route === 'negotiations' && <TradesPage user={user} revision={revision} onChanged={refresh} onChat={chat}/>}
      {route === 'profile' && <ProfileContent id={user.id} revision={revision} onOpen={open} onFavorite={favorite}/>}
      {route === 'settings' && <SettingsPage user={user} onUpdated={u => { setUser(u); refresh(); }}/>}<footer className="main-footer"><span>Menos coisas paradas. Mais histórias compartilhadas.</span><span>CampusMarket © {new Date().getFullYear()} · Projeto acadêmico</span></footer>
    </main>
    {selected && <ListingDialog listing={selected} user={user} revision={revision} onClose={() => setSelected(null)} onFavorite={favorite} onChat={chat} onProfile={id => { setSelected(null); setProfileId(id); }} onEdit={l => { setSelected(null); setEditor(l); }} onChanged={refresh}/>}
    {editor && <EditorDialog listing={editor === 'new' ? undefined : editor} onClose={() => setEditor(null)} onSaved={() => { setEditor(null); refresh(); navigate('mine'); setToast('Anúncio salvo! Seu produto ganhou uma nova oportunidade.'); }}/>} 
    {profileId && <ProfileDialog id={profileId} revision={revision} onClose={() => setProfileId(null)} onOpen={open} onFavorite={favorite}/>}
    {toast && <div className="toast" role="status"><span>{toast}</span><button aria-label="Fechar aviso" onClick={() => setToast('')}><X size={17}/></button></div>}
  </div>;
}
