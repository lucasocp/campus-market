package br.edu.campusmarket.configuracao;

import br.edu.campusmarket.anuncios.domain.Listing;
import br.edu.campusmarket.anuncios.infrastructure.ListingRepository;
import br.edu.campusmarket.avaliacoes.*;
import br.edu.campusmarket.mensagens.domain.*;
import br.edu.campusmarket.mensagens.infrastructure.*;
import br.edu.campusmarket.negociacoes.domain.Trade;
import br.edu.campusmarket.negociacoes.infrastructure.TradeRepository;
import br.edu.campusmarket.usuarios.domain.AppUser;
import br.edu.campusmarket.usuarios.infrastructure.UserJpaRepository;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Dados inteiramente fictícios, criados uma única vez no perfil demo. */
@Component @Profile("demo")
public class DemoData implements CommandLineRunner {
    private final UserJpaRepository users; private final ListingRepository listings;
    private final ConversationRepository conversations; private final MessageRepository messages;
    private final TradeRepository trades; private final ReviewRepository reviews; private final PasswordEncoder passwords;
    public DemoData(UserJpaRepository users, ListingRepository listings, ConversationRepository conversations,
                    MessageRepository messages, TradeRepository trades, ReviewRepository reviews, PasswordEncoder passwords) {
        this.users = users; this.listings = listings; this.conversations = conversations;
        this.messages = messages; this.trades = trades; this.reviews = reviews; this.passwords = passwords;
    }
    @Override @Transactional public void run(String... args) {
        if (users.count() > 0) return;
        String hash = passwords.encode("Campus@123");
        var luana = users.save(new AppUser("20260001", "Luana Martins", "Ciência da Computação", hash));
        var pedro = users.save(new AppUser("20260002", "Pedro Almeida", "Engenharia de Software", hash));
        var bia = users.save(new AppUser("20260003", "Beatriz Costa", "Administração", hash));
        luana.updateProfile(luana.name(), luana.course(), "Entre um café e outro, estudando programação e dando uma nova história às minhas coisas. 🌿");
        pedro.updateProfile(pedro.name(), pedro.course(), "Apaixonado por tecnologia. Entrego no campus, perto da biblioteca.");
        bia.updateProfile(bia.name(), bia.course(), "Livros, ideias e um consumo mais consciente. Vamos negociar!");
        var laptop = product(pedro, "MacBook Air M1 · 256 GB", "MacBook Air com 8 GB de RAM e SSD de 256 GB. Muito bem conservado, bateria com boa autonomia. Acompanha carregador original. Podemos testar juntos na biblioteca.", "Tecnologia", "3290.00", "laptop", Listing.Condition.SEMINOVO, false, 0);
        product(bia, "Box de livros de programação", "Três livros para começar bem o semestre: lógica, algoritmos e orientação a objetos. Sem anotações, páginas completas e capas bem conservadas.", "Livros", "89.90", "books", Listing.Condition.SEMINOVO, false, 0);
        product(pedro, "Calculadora científica Casio", "Calculadora científica funcionando perfeitamente, ideal para cálculo, física e estatística. Acompanha tampa protetora e manual.", "Materiais", "120.00", "calculator", Listing.Condition.SEMINOVO, true, 10800);
        product(bia, "Mochila para notebook", "Mochila com compartimento acolchoado para notebook de até 15 polegadas. Leve, confortável e com bolsos para organizar os materiais.", "Moda", "75.00", "backpack", Listing.Condition.USADO, false, 0);
        product(luana, "Fone sem fio com estojo", "Fone Bluetooth com estojo de carregamento. Ótimo para estudar e ouvir podcasts. Higienizado e com cabo USB incluso.", "Tecnologia", "149.00", "headphones", Listing.Condition.SEMINOVO, false, 0);
        product(bia, "Kit de desenho e papelaria", "Kit com caderno pontilhado, marcadores em tons pastel e lápis. Materiais novos, ideais para organizar as aulas ou começar um bullet journal.", "Materiais", "45.00", "stationery", Listing.Condition.NOVO, false, 0);
        product(pedro, "Headphone Bluetooth over-ear", "Headphone com almofadas macias, conexão Bluetooth e cabo auxiliar. Som equilibrado e bateria de longa duração. Pode testar antes de comprar.", "Tecnologia", "180.00", "headset", Listing.Condition.SEMINOVO, true, 21600);
        product(luana, "Código Limpo · Robert C. Martin", "Livro Código Limpo em português. Excelente para quem quer melhorar o código e conhecer boas práticas. Algumas marcações discretas a lápis.", "Livros", "65.00", "clean-code", Listing.Condition.USADO, false, 0);
        product(bia, "Luminária de mesa articulada", "Luminária para sua mesa de estudos. Braço ajustável e iluminação confortável. Acompanha lâmpada LED e funciona na tomada.", "Outros", "55.00", "lamp", Listing.Condition.SEMINOVO, false, 0);
        product(pedro, "Teclado mecânico compacto", "Teclado mecânico com conexão USB, layout compacto e teclas confortáveis para programar. Acompanha cabo e caixa original.", "Tecnologia", "210.00", "keyboard", Listing.Condition.SEMINOVO, false, 0);
        product(bia, "Cálculo · volume 1", "Livro de cálculo com exercícios resolvidos e explicações detalhadas. Edição em português, páginas completas e boas condições para estudar.", "Livros", "60.00", "math-book", Listing.Condition.USADO, true, 7200);
        product(pedro, "Garrafa térmica de inox", "Garrafa térmica de 500 ml, com tampa de rosca e acabamento fosco. Sem uso, recebida de presente. Perfeita para levar café para as aulas.", "Outros", "40.00", "bottle", Listing.Condition.NOVO, false, 0);
        var conversation = conversations.save(new Conversation(laptop, luana, Instant.now()));
        messages.save(new Message(conversation, luana, "Oi, Pedro! O MacBook ainda está disponível? Posso ver na faculdade?", Instant.now().minusSeconds(1800)));
        messages.save(new Message(conversation, pedro, "Oi, Luana! Está sim :) Podemos combinar na biblioteca depois da aula. Levo o carregador para você testar.", Instant.now().minusSeconds(1500)));
        var sold = product(pedro, "Algoritmos · edição anterior", "Livro utilizado em negociação fictícia já concluída, para demonstrar avaliações reais do fluxo do sistema.", "Livros", "35.00", "books", Listing.Condition.USADO, false, 0);
        sold.reserve();
        var trade = trades.save(new Trade(sold, luana, new BigDecimal("35.00"), Instant.now().minusSeconds(86400)));
        trade.confirm(luana.id(), Instant.now()); trade.confirm(pedro.id(), Instant.now());
        reviews.save(new Review(trade, luana, 5, "Muito atencioso! O livro estava como descrito e combinamos a entrega no campus.", Instant.now()));
    }
    private Listing product(AppUser seller, String title, String description, String category, String price,
                            String image, Listing.Condition condition, boolean auction, long duration) {
        Instant now = Instant.now();
        return listings.save(new Listing(seller, title, description, category, condition,
            auction ? Listing.Mode.LEILAO : Listing.Mode.DIRETA, new BigDecimal(price),
            auction ? now.plusSeconds(duration) : null, List.of("/products/" + image + ".svg"), now));
    }
}
