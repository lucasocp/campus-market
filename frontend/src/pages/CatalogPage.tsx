import { useEffect, useState } from 'react';
import { ArrowDownUp, ArrowRight, BookOpen, Check, ChevronLeft, ChevronRight, Grid2X2, Headphones, Laptop, Leaf, Package, PencilRuler, SearchX, Shirt, SlidersHorizontal, Sparkles, X } from 'lucide-react';
import type { Catalog, Listing, Route } from '../types';
import { api, errorMessage, money } from '../api';
import { Empty, ErrorBox, SectionTitle, Spinner } from '../components/ui';
import { ProductCard } from '../components/ProductCard';

const categories = [{ name: 'Todos', icon: Grid2X2 }, { name: 'Livros', icon: BookOpen }, { name: 'Tecnologia', icon: Laptop }, { name: 'Materiais', icon: PencilRuler }, { name: 'Moda', icon: Shirt }, { name: 'Outros', icon: Package }];
export function CatalogPage({ route, search, revision, onOpen, onFavorite, onPublish, onAuctions }: {
  route: Route; search: string; revision: number; onOpen: (l: Listing) => void; onFavorite: (l: Listing) => void; onPublish: () => void; onAuctions: () => void;
}) {
  const [category, setCategory] = useState('Todos'); const [condition, setCondition] = useState('');
  const [min, setMin] = useState(''); const [max, setMax] = useState(''); const [order, setOrder] = useState('recent');
  const [filters, setFilters] = useState(false); const [page, setPage] = useState(0);
  const [catalog, setCatalog] = useState<Catalog | null>(null); const [error, setError] = useState('');
  const [debounced, setDebounced] = useState(search);
  const [participations, setParticipations] = useState<{listing: Listing; myHighestBid: number; leading: boolean}[]>([]);
  useEffect(() => {
    if (route !== 'auctions') return;
    let active = true;
    api<typeof participations>('/bids/mine').then(items => { if (active) setParticipations(items); }).catch(e => { if (active) setError(errorMessage(e)); });
    return () => { active = false; };
  }, [route, revision]);
  useEffect(() => { const id = setTimeout(() => setDebounced(search), 300); return () => clearTimeout(id); }, [search]);
  useEffect(() => { setPage(0); }, [category, condition, min, max, order, debounced, route]);
  useEffect(() => {
    let active = true;
    const params = new URLSearchParams({ q: debounced, category: category === 'Todos' ? '' : category, condition, order, page: String(page), mode: route === 'auctions' ? 'LEILAO' : '', favorites: String(route === 'favorites') });
    if (min) params.set('min', min); if (max) params.set('max', max);
    api<Catalog>(`/listings?${params}`).then(data => { if (active) { setCatalog(data); setError(''); } }).catch(e => { if (active) setError(errorMessage(e)); });
    return () => { active = false; };
  }, [category, condition, min, max, order, debounced, route, page, revision]);
  return <>
    {route === 'explore' && !search && <section className="hero"><div className="hero-content"><span className="hero-eyebrow"><span className="live-dot"/> SEU CAMPUS. SUAS CONEXÕES.</span><h1>Entre aulas,<br/><span>bons negócios.</span></h1><p>Encontre o que precisa. Venda o que já não usa.<br/>Tudo isso com quem divide o campus com você.</p><button className="button dark" onClick={onPublish}>Começar a vender <ArrowRight size={17}/></button><div className="hero-proof"><span className="avatar-stack"><i>L</i><i>P</i><i>B</i></span><span>Feito por estudantes,<br/><strong>para estudantes.</strong></span></div></div><div className="hero-art"><div className="hero-orbit"/><span className="hero-spark spark-one">✳</span><span className="hero-spark spark-two">✦</span><div className="hero-product"><img src="/products/laptop.svg" alt="Notebook disponível no campus"/><span className="hero-product-label"><span><small>SEU PRÓXIMO ACHADO</small><strong>Pronto para novos projetos.</strong></span><span className="round-arrow"><ArrowRight size={20}/></span></span></div><span className="hero-sticker"><Leaf size={17}/> Reutilizar faz bem.</span><span className="verified-sticker"><Check size={13}/> Conexões no campus</span></div></section>}
    {route !== 'explore' && <div className="page-intro"><div className="eyebrow">SEU CAMPUSMARKET</div><h1>{route === 'auctions' ? 'Um lance. Novas possibilidades.' : 'Seus próximos achados.'}</h1><p>{route === 'auctions' ? 'Acompanhe as disputas e dê o seu lance antes do tempo acabar.' : 'Os produtos que você salvou, reunidos em um só lugar.'}</p></div>}
    {route === 'auctions' && participations.length > 0 && <section className="my-bids"><SectionTitle title="Leilões que você acompanha"/><div>{participations.map(p => <button className="my-bid-card" key={p.listing.id} onClick={() => onOpen(p.listing)}><img src={p.listing.images[0]} alt=""/><span><strong>{p.listing.title}</strong><small>Seu maior lance: {money(p.myHighestBid)}</small><span className={`status-tag ${p.leading ? 'complete' : ''}`}>{p.listing.status === 'ATIVO' ? p.leading ? 'Você está liderando' : 'Seu lance foi superado' : p.leading ? 'Você venceu!' : 'Leilão encerrado'}</span></span><ArrowRight size={16}/></button>)}</div></section>}
    <div className="categories" aria-label="Categorias">{categories.map(({ name, icon: Icon }) => <button key={name} className={`category ${category === name ? 'active' : ''}`} onClick={() => setCategory(name)}><Icon size={19}/><span>{name}</span></button>)}</div>
    <SectionTitle eyebrow={route === 'auctions' ? 'DISPUTAS ABERTAS' : 'DÊ UMA OLHADA'} title={search ? `Resultados para “${search}”` : route === 'favorites' ? 'Salvos por você' : route === 'auctions' ? 'Leilões no campus' : 'Novidades no campus'}>
      <div className="catalog-tools"><button className={`button filter-toggle ${filters ? 'selected' : ''}`} onClick={() => setFilters(!filters)} aria-expanded={filters}><SlidersHorizontal size={15}/> Filtros{(condition || min || max) && <span className="filter-dot"/>}</button><label className="sort-select"><ArrowDownUp size={14}/><span className="sr-only">Ordenar anúncios</span><select value={order} onChange={e => setOrder(e.target.value)}><option value="recent">Mais recentes</option><option value="price-asc">Menor preço</option><option value="price-desc">Maior preço</option><option value="ending">Encerrando primeiro</option></select></label></div>
    </SectionTitle>
    {filters && <div className="filter-panel"><label>Conservação<select value={condition} onChange={e => setCondition(e.target.value)}><option value="">Todas as condições</option><option value="NOVO">Novo</option><option value="SEMINOVO">Seminovo</option><option value="USADO">Usado</option></select></label><label>Preço mínimo (R$)<input type="number" min="0" step="0.01" placeholder="0,00" value={min} onChange={e => setMin(e.target.value)}/></label><label>Preço máximo (R$)<input type="number" min="0" step="0.01" placeholder="Sem limite" value={max} onChange={e => setMax(e.target.value)}/></label><button className="text-button" onClick={() => { setCondition(''); setMin(''); setMax(''); }}><X size={14}/> Limpar</button></div>}
    <ErrorBox message={error}/>{!catalog && !error ? <Spinner/> : catalog && <><div className="results-caption"><span>{catalog.total} {catalog.total === 1 ? 'oportunidade encontrada' : 'oportunidades encontradas'}</span><span><span className="live-dot"/> Entre estudantes</span></div>{catalog.items.length ? <div className="product-grid">{catalog.items.map(l => <ProductCard key={l.id} listing={l} onOpen={onOpen} onFavorite={onFavorite}/>)}</div> : <Empty title="Nenhum achado por aqui ainda.">Experimente outra busca, ajuste os filtros ou publique um anúncio para a comunidade.</Empty>}
      {catalog.pages > 1 && <div className="pagination"><button className="button secondary" disabled={page === 0} onClick={() => setPage(p => p - 1)}><ChevronLeft size={16}/> Anterior</button><span>Página {page + 1} de {catalog.pages}</span><button className="button secondary" disabled={page + 1 >= catalog.pages} onClick={() => setPage(p => p + 1)}>Próxima <ChevronRight size={16}/></button></div>}</>}
    {route === 'explore' && <section className="bottom-banner"><div className="banner-icon"><Headphones size={28}/></div><div><span className="eyebrow">A PRÓXIMA OPORTUNIDADE PODE SER SUA</span><h3>De olho em um bom negócio?</h3><p>Entre nos leilões e encontre o seu próximo achado.</p></div><button className="button secondary" onClick={onAuctions}>Explorar leilões <ArrowRight size={16}/></button><Sparkles className="banner-spark" size={35}/></section>}
  </>;
}
