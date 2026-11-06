package br.edu.campusmarket.mensagens.application;
import br.edu.campusmarket.anuncios.infrastructure.ListingRepository;
import br.edu.campusmarket.anuncios.domain.Listing;
import br.edu.campusmarket.common.*;
import br.edu.campusmarket.mensagens.domain.*;
import br.edu.campusmarket.mensagens.infrastructure.*;
import br.edu.campusmarket.usuarios.domain.AppUser;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static br.edu.campusmarket.common.BusinessException.*;

@Service @Transactional
public class ChatService {
    private final ConversationRepository conversations; private final MessageRepository messages;
    private final ListingRepository listings; private final UserNotifier notifications; private final Clock clock;
    public ChatService(ConversationRepository conversations, MessageRepository messages, ListingRepository listings,
                       UserNotifier notifications, Clock clock) {
        this.conversations = conversations; this.messages = messages; this.listings = listings;
        this.notifications = notifications; this.clock = clock;
    }
    public record ConversationView(UUID id, UUID listingId, String title, String image, UUID otherId,
        String otherName, String otherCourse, String lastMessage, Instant updatedAt) { }
    public record MessageView(UUID id, UUID senderId, String senderName, String content, Instant createdAt) { }
    private ConversationView view(Conversation c, AppUser user) {
        var other = c.other(user.id());
        return new ConversationView(c.id(), c.listing().id(), c.listing().title(), c.listing().images().getFirst(),
            other.id(), other.name(), other.course(), messages.findTopByConversationIdOrderByCreatedAtDesc(c.id()).map(Message::content).orElse("Comece uma conversa"), c.updatedAt());
    }
    public ConversationView start(AppUser user, UUID listingId) {
        var listing = listings.lockById(listingId).orElseThrow(BusinessException::notFound);
        require(listing.status() != Listing.Status.EXCLUIDO, "Este anúncio foi removido.");
        require(!listing.seller().id().equals(user.id()), "Abra as conversas recebidas na sua caixa de entrada.");
        var conversation = conversations.findByListingIdAndBuyerId(listingId, user.id())
            .orElseGet(() -> conversations.save(new Conversation(listing, user, clock.instant())));
        return view(conversation, user);
    }
    @Transactional(readOnly = true) public List<ConversationView> inbox(AppUser user) {
        return conversations.forUser(user.id()).stream().map(c -> view(c, user)).toList();
    }
    @Transactional(readOnly = true) public List<MessageView> history(AppUser user, UUID id) {
        var c = conversations.findById(id).orElseThrow(BusinessException::notFound);
        authorize(c.participant(user.id()));
        var result = new ArrayList<>(messages.findTop200ByConversationIdOrderByCreatedAtDesc(id));
        Collections.reverse(result);
        return result.stream().map(this::messageView).toList();
    }
    private MessageView messageView(Message m) { return new MessageView(m.id(), m.sender().id(), m.sender().name(), m.content(), m.createdAt()); }
    public MessageView send(AppUser user, UUID id, String content) {
        var c = conversations.lockById(id).orElseThrow(BusinessException::notFound);
        authorize(c.participant(user.id()));
        var message = messages.save(new Message(c, user, content, clock.instant()));
        c.touch(clock.instant());
        notifications.notify(c.other(user.id()), "Nova mensagem de " + user.name() + " sobre “" + c.listing().title() + "”.");
        return messageView(message);
    }
}
