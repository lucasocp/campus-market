import { useEffect, useRef, type ReactNode } from 'react';
import { ArrowUpRight, LoaderCircle, X, ShoppingBag } from 'lucide-react';

export function Logo() {
  return <span className="brand"><span className="brand-mark"><ShoppingBag size={23} strokeWidth={2.3} /></span><span>campus<span className="brand-light">market</span><span className="brand-dot">.</span></span></span>;
}
export function Spinner() { return <div className="loading" role="status"><LoaderCircle className="spin" size={25} /><span>Carregando…</span></div>; }
export function Empty({ title, children }: { title: string; children: ReactNode }) {
  return <div className="empty"><span className="empty-icon"><ShoppingBag size={30}/></span><h3>{title}</h3><p>{children}</p></div>;
}
export function ErrorBox({ message }: { message: string }) { return message ? <div className="error-box" role="alert">{message}</div> : null; }
export function Modal({ title, onClose, children, wide = false }: { title: string; onClose: () => void; children: ReactNode; wide?: boolean }) {
  const dialog = useRef<HTMLDialogElement>(null);
  const close = useRef(onClose); close.current = onClose;
  useEffect(() => {
    const element = dialog.current!;
    const previous = document.activeElement as HTMLElement | null;
    element.showModal();
    const cancel = (event: Event) => { event.preventDefault(); close.current(); };
    element.addEventListener('cancel', cancel);
    return () => { element.removeEventListener('cancel', cancel); element.close(); previous?.focus(); };
  }, []);
  return <dialog ref={dialog} className={`modal ${wide ? 'modal-wide' : ''}`} aria-label={title} onClick={e => { if (e.target === e.currentTarget) onClose(); }}>
    <div className="modal-heading"><h2>{title}</h2><button className="icon-button" aria-label="Fechar janela" onClick={onClose}><X size={21}/></button></div>
    {children}
  </dialog>;
}
export function SectionTitle({ eyebrow, title, children }: { eyebrow?: string; title: string; children?: ReactNode }) {
  return <div className="section-heading"><div>{eyebrow && <div className="eyebrow">{eyebrow}</div>}<h2>{title}</h2></div>{children}</div>;
}
export function Tip({ onClick }: { onClick: () => void }) {
  return <button className="sidebar-tip" onClick={onClick}><span className="tip-art">↗</span><strong>Parado na gaveta?<br/>Pode virar um bom negócio.</strong><span>Crie seu primeiro anúncio <ArrowUpRight size={15}/></span></button>;
}
