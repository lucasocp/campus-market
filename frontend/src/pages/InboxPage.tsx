import { useEffect, useRef, useState, type FormEvent } from 'react';
import { ArrowUpRight, MessageCircle, Send } from 'lucide-react';
import { api, errorMessage, initials, post } from '../api';
import type { Conversation, Listing, Message, User } from '../types';
import { Empty, ErrorBox, Spinner } from '../components/ui';

export function InboxPage({ user, revision, selectedId, onSelected, onOpen }: { user: User; revision: number; selectedId: string | null; onSelected: (id: string) => void; onOpen: (l: Listing) => void }) {
  const [conversations, setConversations] = useState<Conversation[] | null>(null); const [messages, setMessages] = useState<Message[]>([]);
  const [text, setText] = useState(''); const [error, setError] = useState(''); const [busy, setBusy] = useState(false);
  const end = useRef<HTMLDivElement>(null);
  const selected = conversations?.find(c => c.id === selectedId);
  useEffect(() => {
    let active = true;
    api<Conversation[]>('/conversations').then(items => { if (active) { setConversations(items); if (!selectedId && items.length) onSelected(items[0].id); } }).catch(e => { if (active) setError(errorMessage(e)); });
    return () => { active = false; };
  }, [revision, selectedId, onSelected]);
  useEffect(() => {
    let active = true;
    if (!selectedId) return;
    api<Message[]>(`/conversations/${selectedId}/messages`).then(items => { if (active) setMessages(items); }).catch(e => { if (active) setError(errorMessage(e)); });
    return () => { active = false; };
  }, [selectedId, revision]);
  useEffect(() => { end.current?.scrollIntoView({ block: 'nearest', behavior: 'smooth' }); }, [messages.length]);
  async function send(e: FormEvent) {
    e.preventDefault(); if (!text.trim() || !selectedId) return; setBusy(true); setError('');
    try { const message = await post<Message>(`/conversations/${selectedId}/messages`, { content: text }); setMessages(m => [...m, message]); setText(''); }
    catch (e) { setError(errorMessage(e)); } finally { setBusy(false); }
  }
  return <><div className="page-intro"><div className="eyebrow">A CONVERSA FAZ PARTE DO NEGÓCIO</div><h1>Sua caixa de entrada.</h1><p>Tire dúvidas, combine os detalhes e encontre a comunidade.</p></div><ErrorBox message={error}/>
    {!conversations ? <Spinner/> : !conversations.length ? <Empty title="A próxima conversa começa com um achado.">Abra um anúncio e clique em “Conversar com vendedor”.</Empty> : <div className={`inbox-layout ${selected ? 'has-selection' : ''}`}><aside className="conversation-list"><div className="conversation-list-heading"><strong>Conversas</strong><span>{conversations.length}</span></div>{conversations.map(c => <button key={c.id} className={`conversation-item ${c.id === selectedId ? 'active' : ''}`} onClick={() => { setMessages([]); onSelected(c.id); }}><span className="avatar">{initials(c.otherName)}</span><span><strong>{c.otherName}</strong><small>{c.title}</small><p>{c.lastMessage}</p></span></button>)}</aside>
      <section className="chat-panel">{selected ? <><div className="chat-header"><span className="avatar">{initials(selected.otherName)}</span><span><strong>{selected.otherName}</strong><small>{selected.otherCourse}</small></span><span className="chat-private"><MessageCircle size={14}/> Conversa privada</span></div><button className="chat-product" onClick={() => { api<Listing>(`/listings/${selected.listingId}`).then(onOpen).catch(e => setError(errorMessage(e))); }}><img src={selected.image} alt=""/><span>Sobre o anúncio<strong>{selected.title}</strong></span><ArrowUpRight size={18}/></button><div className="chat-messages"><p className="chat-safety">Combine a entrega em um local movimentado do campus.</p>{messages.map(m => <div key={m.id} className={`message-bubble ${m.senderId === user.id ? 'mine' : ''}`}><p>{m.content}</p><time>{new Date(m.createdAt).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })}</time></div>)}{!messages.length && <p className="muted text-center">Dê um oi e comece a conversa.</p>}<div ref={end}/></div><form className="chat-composer" onSubmit={send}><input aria-label="Sua mensagem" placeholder="Escreva sua mensagem…" maxLength={2000} value={text} onChange={e => setText(e.target.value)}/><button className="button primary" aria-label="Enviar mensagem" disabled={busy || !text.trim()}><Send size={19}/></button></form></> : <Empty title="Vamos conversar?">Selecione uma conversa para continuar.</Empty>}</section>
    </div>}</>;
}
