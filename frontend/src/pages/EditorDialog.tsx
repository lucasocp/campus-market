import { useState, type FormEvent } from 'react';
import { Camera, Check, Gavel, ShoppingBag, Upload, X } from 'lucide-react';
import { api, errorMessage, post } from '../api';
import type { Condition, Listing, Mode } from '../types';
import { ErrorBox, Modal } from '../components/ui';

export function EditorDialog({ listing, onClose, onSaved }: { listing?: Listing; onClose: () => void; onSaved: () => void }) {
  const [title, setTitle] = useState(listing?.title || ''); const [description, setDescription] = useState(listing?.description || '');
  const [category, setCategory] = useState(listing?.category || 'Livros'); const [condition, setCondition] = useState<Condition>(listing?.condition || 'SEMINOVO');
  const [mode, setMode] = useState<Mode>(listing?.mode || 'DIRETA'); const [price, setPrice] = useState(listing ? String(listing.price) : '');
  const [duration, setDuration] = useState('24'); const [images, setImages] = useState<string[]>(listing?.images || []);
  const [busy, setBusy] = useState(false); const [uploading, setUploading] = useState(false); const [error, setError] = useState('');
  async function upload(files: FileList | null) {
    if (!files) return; setUploading(true); setError('');
    try {
      if (files.length + images.length > 5) throw new Error('Adicione no máximo 5 imagens.');
      const urls: string[] = [];
      for (const file of Array.from(files)) {
        if (file.size > 5 * 1024 * 1024) throw new Error('Cada imagem deve ter até 5 MB.');
        const body = new FormData(); body.append('image', file);
        urls.push((await api<{url: string}>('/images', { method: 'POST', body })).url);
      }
      setImages(current => [...current, ...urls]);
    } catch (e) { setError(errorMessage(e)); } finally { setUploading(false); }
  }
  async function submit(e: FormEvent) {
    e.preventDefault(); setError(''); setBusy(true);
    try {
      if (!images.length) throw new Error('Adicione pelo menos uma imagem do produto.');
      const body = { title, description, category, condition, mode, price: Number(price), images, endsAt: listing?.endsAt || (mode === 'LEILAO' ? new Date(Date.now() + Number(duration) * 3600000).toISOString() : null) };
      if (listing) await api(`/listings/${listing.id}`, { method: 'PUT', body: JSON.stringify(body) });
      else await post('/listings', body);
      onSaved();
    } catch (e) { setError(errorMessage(e)); } finally { setBusy(false); }
  }
  return <Modal title={listing ? 'Editar anúncio' : 'Uma nova história para o seu produto'} onClose={onClose}>
    <form className="form-stack editor-form" onSubmit={submit}><p className="muted">Conte um pouco sobre o seu achado. Boas fotos e uma descrição honesta fazem a diferença.</p><ErrorBox message={error}/>
      <label>Fotos do produto <span className="muted">({images.length}/5)</span></label><div className="upload-grid">{images.map((url, i) => <div className="upload-preview" key={`${url}-${i}`}><img src={url} alt={`Foto ${i + 1}`}/><button type="button" aria-label={`Remover foto ${i + 1}`} onClick={() => setImages(images.filter((_, n) => i !== n))}><X size={14}/></button></div>)}{images.length < 5 && <label className="upload-button"><Camera size={24}/><span>{uploading ? 'Enviando…' : 'Adicionar foto'}</span><small>JPG ou PNG · até 5 MB</small><input type="file" accept="image/jpeg,image/png" multiple disabled={uploading} onChange={e => { void upload(e.target.files); e.target.value = ''; }}/></label>}</div>
      <label>Título do anúncio<input required minLength={3} maxLength={120} value={title} onChange={e => setTitle(e.target.value)} placeholder="Ex.: Livro de cálculo em ótimo estado"/></label>
      <div className="form-columns"><label>Categoria<select value={category} onChange={e => setCategory(e.target.value)}>{['Livros', 'Tecnologia', 'Materiais', 'Moda', 'Outros'].map(c => <option key={c}>{c}</option>)}</select></label><label>Estado de conservação<select value={condition} onChange={e => setCondition(e.target.value as Condition)}><option value="NOVO">Novo</option><option value="SEMINOVO">Seminovo</option><option value="USADO">Usado</option></select></label></div>
      <label>Descrição<textarea required minLength={10} maxLength={4000} value={description} onChange={e => setDescription(e.target.value)} rows={4} placeholder="Descreva os detalhes, o tempo de uso e o que acompanha o produto."/></label>
      <label>Como você quer vender?</label><div className="mode-options"><button type="button" disabled={!!listing} className={mode === 'DIRETA' ? 'active' : ''} onClick={() => setMode('DIRETA')}><ShoppingBag size={20}/><strong>Venda direta</strong><small>Receba propostas e negocie.</small></button><button type="button" disabled={!!listing} className={mode === 'LEILAO' ? 'active' : ''} onClick={() => setMode('LEILAO')}><Gavel size={20}/><strong>Leilão</strong><small>Defina o início. Receba lances.</small></button></div>
      <div className="form-columns"><label>{mode === 'LEILAO' ? 'Valor inicial (R$)' : 'Preço (R$)'}<input required type="number" min="0.01" max="99999999.99" step="0.01" value={price} onChange={e => setPrice(e.target.value)} placeholder="0,00"/></label>{mode === 'LEILAO' && !listing && <label>Duração<select value={duration} onChange={e => setDuration(e.target.value)}><option value="0.016666667">1 minuto · demonstração</option><option value="1">1 hora</option><option value="6">6 horas</option><option value="24">24 horas</option><option value="72">3 dias</option><option value="168">7 dias</option></select></label>}</div>
      {mode === 'LEILAO' && <div className="notice-box">O primeiro lance deve atingir o valor inicial. Depois, o incremento mínimo é R$ 1,00. O prazo não pode ser alterado após a publicação.</div>}
      <div className="modal-actions"><button type="button" className="button secondary" onClick={onClose}>Cancelar</button><button className="button primary" disabled={busy || uploading}>{listing ? <Check size={17}/> : <Upload size={17}/>} {busy ? 'Salvando…' : listing ? 'Salvar alterações' : 'Publicar anúncio'}</button></div>
    </form>
  </Modal>;
}
