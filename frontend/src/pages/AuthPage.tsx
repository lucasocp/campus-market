import { useState, type FormEvent } from 'react';
import { ArrowRight, BookOpen, Check, GraduationCap, LockKeyhole, ShieldCheck, Sparkles } from 'lucide-react';
import { post, errorMessage } from '../api';
import type { User } from '../types';
import { ErrorBox, Logo } from '../components/ui';

export function AuthPage({ onLogin }: { onLogin: (user: User) => void }) {
  const [register, setRegister] = useState(false);
  const [ra, setRa] = useState(''); const [password, setPassword] = useState('');
  const [name, setName] = useState(''); const [course, setCourse] = useState(''); const [invitation, setInvitation] = useState('');
  const [busy, setBusy] = useState(false); const [error, setError] = useState(''); const [success, setSuccess] = useState('');
  async function submit(event?: FormEvent, demo = false) {
    event?.preventDefault(); setBusy(true); setError(''); setSuccess('');
    try {
      if (register && !demo) {
        await post('/auth/register', { ra, password, name, course, invitation });
        setRegister(false); setSuccess('Cadastro realizado! Entre com o R.A. e a senha que você criou.');
      } else onLogin(await post<User>('/auth/login', { ra: demo ? '20260001' : ra, password: demo ? 'Campus@123' : password }));
    } catch (e) { setError(errorMessage(e)); } finally { setBusy(false); }
  }
  return <main className="auth-layout">
    <section className="auth-story"><Logo/><div className="auth-story-body"><span className="pill"><span className="live-dot"/> DA COMUNIDADE, PARA A COMUNIDADE</span><h1>Seu próximo<br/>achado está<br/>no <em>campus.</em></h1><p>O livro do próximo semestre. Um notebook para os projetos. Uma nova história para o que você já não usa.</p>
      <div className="auth-product-collage"><div className="auth-art-card"><img src="/products/laptop.svg" alt="Ilustração de notebook"/><span><strong>Uma nova história começa aqui.</strong><Sparkles size={18}/></span></div><span className="floating-note"><BookOpen size={21}/> Menos gastos.<br/><strong>Mais possibilidades.</strong></span></div>
      <div className="auth-values"><span><ShieldCheck size={17}/> Comunidade acadêmica</span><span><Check size={17}/> Encontro no campus</span></div></div><small>CampusMarket · Projeto acadêmico</small></section>
    <section className="auth-form-side"><div className="auth-form-wrap"><span className="auth-icon"><GraduationCap size={27}/></span><div className="eyebrow">BEM-VINDO AO SEU MARKETPLACE</div><h2>{register ? 'Faça parte do campus.' : 'Que bom ter você aqui.'}</h2><p>{register ? 'Crie sua conta com os dados acadêmicos.' : 'Entre para descobrir, negociar e dar uma nova vida às suas coisas.'}</p>
      <div className="auth-tabs"><button className={!register ? 'active' : ''} onClick={() => { setRegister(false); setError(''); }}>Entrar</button><button className={register ? 'active' : ''} onClick={() => { setRegister(true); setError(''); }}>Criar conta</button></div>
      <form onSubmit={submit} className="form-stack"><ErrorBox message={error}/>{success && <div className="success-box" role="status">{success}</div>}
        {register && <><label>Nome completo<input required maxLength={100} autoComplete="name" value={name} onChange={e => setName(e.target.value)} placeholder="Como você se chama?"/></label><label>Curso<input required maxLength={100} value={course} onChange={e => setCourse(e.target.value)} placeholder="Ex.: Ciência da Computação"/></label></>}
        <label>Registro Acadêmico (R.A.)<input required inputMode="numeric" pattern="[0-9]{5,20}" autoComplete="username" value={ra} onChange={e => setRa(e.target.value)} placeholder="Digite seu R.A." maxLength={20}/></label>
        <label>Senha<input required type="password" autoComplete={register ? 'new-password' : 'current-password'} minLength={register ? 8 : undefined} maxLength={64} value={password} onChange={e => setPassword(e.target.value)} placeholder={register ? 'Crie uma senha de 8 a 64 caracteres' : 'Digite sua senha'}/></label>
        {register && <label>Convite acadêmico<input required value={invitation} onChange={e => setInvitation(e.target.value)} placeholder="Ex.: CAMPUS-0004"/><small>Base fictícia: R.A. 20260004 a 20260050; convite CAMPUS + hífen + os últimos 4 dígitos.</small></label>}
        <button className="button primary full" disabled={busy}>{busy ? 'Aguarde…' : register ? 'Criar minha conta' : 'Entrar no CampusMarket'}<ArrowRight size={18}/></button>
      </form>
      <div className="auth-divider"><span>conheça o projeto</span></div><button className="button secondary full" disabled={busy} onClick={() => void submit(undefined, true)}><Sparkles size={17}/> Explorar demonstração</button>
      <p className="auth-note"><LockKeyhole size={14}/> Acesso exclusivo para estudantes. Esta versão utiliza uma instituição e dados fictícios.</p>
    </div></section>
  </main>;
}
