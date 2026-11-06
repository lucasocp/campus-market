export interface User { id: string; ra: string; name: string; course: string; biography: string }
export type Mode = 'DIRETA' | 'LEILAO';
export type Condition = 'NOVO' | 'SEMINOVO' | 'USADO';
export type Status = 'ATIVO' | 'PAUSADO' | 'RESERVADO' | 'VENDIDO' | 'ENCERRADO' | 'EXCLUIDO';
export interface Listing {
  id: string; sellerId: string; sellerName: string; sellerCourse: string;
  title: string; description: string; category: string; condition: Condition;
  mode: Mode; status: Status; price: number; highestBid: number; minimumBid: number;
  bidCount: number; endsAt: string | null; createdAt: string; images: string[]; favorite: boolean;
}
export interface Catalog { items: Listing[]; total: number; pages: number; page: number }
export interface Conversation { id: string; listingId: string; title: string; image: string; otherId: string; otherName: string; otherCourse: string; lastMessage: string; updatedAt: string }
export interface Message { id: string; senderId: string; senderName: string; content: string; createdAt: string }
export interface Offer { id: string; listingId: string; title: string; sellerId: string; sellerName: string; buyerId: string; buyerName: string; amount: number; message: string; status: 'PENDENTE' | 'ACEITA' | 'RECUSADA'; createdAt: string }
export interface Trade { id: string; listingId: string; title: string; image: string; sellerId: string; sellerName: string; buyerId: string; buyerName: string; amount: number; buyerConfirmed: boolean; sellerConfirmed: boolean; completedAt: string | null }
export interface Review { id: string; authorName: string; rating: number; comment: string; createdAt: string }
export interface Profile { id: string; name: string; course: string; biography: string; createdAt: string; rating: number; reviewCount: number; reviews: Review[]; listings: Listing[] }
export interface Notice { id: string; content: string; read: boolean; createdAt: string }
export interface Bid { id: string; bidderName: string; amount: number; createdAt: string }
export type Route = 'explore' | 'auctions' | 'favorites' | 'mine' | 'inbox' | 'negotiations' | 'profile' | 'settings';
