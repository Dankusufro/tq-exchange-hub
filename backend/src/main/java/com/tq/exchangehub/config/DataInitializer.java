package com.tq.exchangehub.config;

import com.tq.exchangehub.entity.Category;
import com.tq.exchangehub.entity.Item;
import com.tq.exchangehub.entity.Message;
import com.tq.exchangehub.entity.Profile;
import com.tq.exchangehub.entity.Review;
import com.tq.exchangehub.entity.Trade;
import com.tq.exchangehub.entity.TradeStatus;
import com.tq.exchangehub.entity.UserAccount;
import com.tq.exchangehub.repository.CategoryRepository;
import com.tq.exchangehub.repository.ItemRepository;
import com.tq.exchangehub.repository.MessageRepository;
import com.tq.exchangehub.repository.ProfileRepository;
import com.tq.exchangehub.repository.ReviewRepository;
import com.tq.exchangehub.repository.TradeRepository;
import com.tq.exchangehub.repository.UserAccountRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(value = "application.data.initializer.enabled", havingValue = "true")
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ProfileRepository profileRepository;
    private final UserAccountRepository userAccountRepository;
    private final ItemRepository itemRepository;
    private final TradeRepository tradeRepository;
    private final MessageRepository messageRepository;
    private final ReviewRepository reviewRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            CategoryRepository categoryRepository,
            ProfileRepository profileRepository,
            UserAccountRepository userAccountRepository,
            ItemRepository itemRepository,
            TradeRepository tradeRepository,
            MessageRepository messageRepository,
            ReviewRepository reviewRepository,
            PasswordEncoder passwordEncoder) {
        this.categoryRepository = categoryRepository;
        this.profileRepository = profileRepository;
        this.userAccountRepository = userAccountRepository;
        this.itemRepository = itemRepository;
        this.tradeRepository = tradeRepository;
        this.messageRepository = messageRepository;
        this.reviewRepository = reviewRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (profileRepository.count() > 0) {
            return;
        }

        OffsetDateTime now = OffsetDateTime.now();

        Category electronics = new Category();
        electronics.setName("Electrónica");
        electronics.setDescription("Dispositivos y gadgets en perfecto estado");
        electronics.setIcon("lucide:cpu");
        electronics.setCreatedAt(now.minusDays(30));

        Category sports = new Category();
        sports.setName("Deportes");
        sports.setDescription("Equipamiento deportivo listo para usar");
        sports.setIcon("lucide:dumbbell");
        sports.setCreatedAt(now.minusDays(28));

        Category services = new Category();
        services.setName("Servicios");
        services.setDescription("Talentos y habilidades para intercambiar");
        services.setIcon("lucide:briefcase");
        services.setCreatedAt(now.minusDays(27));

        categoryRepository.saveAll(List.of(electronics, sports, services));

        Profile maria = new Profile();
        maria.setDisplayName("María López");
        maria.setBio("Apasionada por la fotografía y el ciclismo.");
        maria.setAvatarUrl("https://images.unsplash.com/photo-1524504388940-b1c1722653e1");
        maria.setLocation("Madrid, España");
        maria.setPhone("+34 600 123 456");
        maria.setRating(4.9);
        maria.setTotalTrades(18);
        maria.setCreatedAt(now.minusMonths(6));
        maria.setUpdatedAt(now.minusDays(2));

        Profile luis = new Profile();
        luis.setDisplayName("Luis García");
        luis.setBio("Coleccionista de tecnología vintage y entusiasta del bricolaje.");
        luis.setAvatarUrl("https://images.unsplash.com/photo-1527980965255-d3b416303d12");
        luis.setLocation("Barcelona, España");
        luis.setPhone("+34 611 987 654");
        luis.setRating(4.7);
        luis.setTotalTrades(12);
        luis.setCreatedAt(now.minusMonths(4));
        luis.setUpdatedAt(now.minusDays(3));

        Profile ana = new Profile();
        ana.setDisplayName("Ana Ruiz");
        ana.setBio("Diseñadora freelance y amante de la jardinería.");
        ana.setAvatarUrl("https://images.unsplash.com/photo-1521572267360-ee0c2909d518");
        ana.setLocation("Valencia, España");
        ana.setPhone("+34 622 456 789");
        ana.setRating(4.8);
        ana.setTotalTrades(9);
        ana.setCreatedAt(now.minusMonths(5));
        ana.setUpdatedAt(now.minusDays(5));

        profileRepository.saveAll(List.of(maria, luis, ana));

        createAccountForProfile(maria, "maria@example.com", now.minusMonths(6));
        createAccountForProfile(luis, "luis@example.com", now.minusMonths(4));
        createAccountForProfile(ana, "ana@example.com", now.minusMonths(5));

        Item camera = new Item();
        camera.setOwner(maria);
        camera.setCategory(electronics);
        camera.setTitle("Cámara mirrorless Sony A6400");
        camera.setDescription(
                "Cámara en excelente estado con dos lentes y accesorios originales. Ideal para fotografía urbana.");
        camera.setCondition("Excelente");
        camera.setEstimatedValue(new BigDecimal("850.00"));
        camera.setAvailable(true);
        camera.setService(false);
        camera.setLocation(maria.getLocation());
        camera.setImages(List.of(
                "https://images.unsplash.com/photo-1519183071298-a2962be90b8e",
                "https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f"));
        camera.setCreatedAt(now.minusDays(14));
        camera.setUpdatedAt(now.minusDays(3));

        Item bike = new Item();
        bike.setOwner(luis);
        bike.setCategory(sports);
        bike.setTitle("Bicicleta de montaña Orbea");
        bike.setDescription(
                "Bicicleta bien cuidada, usada en rutas ligeras. Incluye kit de reparación y casco.");
        bike.setCondition("Muy buena");
        bike.setEstimatedValue(new BigDecimal("650.00"));
        bike.setAvailable(true);
        bike.setService(false);
        bike.setLocation(luis.getLocation());
        bike.setImages(List.of(
                "https://images.unsplash.com/photo-1518655048521-f130df041f66",
                "https://images.unsplash.com/photo-1509395176047-4a66953fd231"));
        bike.setCreatedAt(now.minusDays(10));
        bike.setUpdatedAt(now.minusDays(2));

        Item designConsultancy = new Item();
        designConsultancy.setOwner(ana);
        designConsultancy.setCategory(services);
        designConsultancy.setTitle("Consultoría de diseño UX/UI");
        designConsultancy.setDescription(
                "Sesión de dos horas para revisar y mejorar la experiencia de usuario de tu producto digital.");
        designConsultancy.setCondition("Servicio");
        designConsultancy.setEstimatedValue(new BigDecimal("200.00"));
        designConsultancy.setAvailable(true);
        designConsultancy.setService(true);
        designConsultancy.setLocation("Remoto");
        designConsultancy.setImages(List.of("https://images.unsplash.com/photo-1553877522-43269d4ea984"));
        designConsultancy.setCreatedAt(now.minusDays(7));
        designConsultancy.setUpdatedAt(now.minusDays(1));

        itemRepository.saveAll(List.of(camera, bike, designConsultancy));

        Trade trade = new Trade();
        trade.setOwner(maria);
        trade.setRequester(luis);
        trade.setOwnerItem(camera);
        trade.setRequesterItem(bike);
        trade.setMessage("¿Te interesa intercambiar tu bicicleta por mi cámara? Podemos ajustar el trato.");
        trade.setStatus(TradeStatus.COMPLETED);
        trade.setCreatedAt(now.minusDays(5));
        trade.setUpdatedAt(now.minusDays(1));

        tradeRepository.save(trade);

        Message message = new Message();
        message.setTrade(trade);
        message.setSender(luis);
        message.setContent("Hola María, la cámara luce genial. ¿Aceptas un intercambio directo?");
        message.setCreatedAt(now.minusDays(5).plusHours(3));
        messageRepository.save(message);

        Review review = new Review();
        review.setTrade(trade);
        review.setReviewer(luis);
        review.setReviewed(maria);
        review.setRating(5);
        review.setComment("Intercambio impecable, la cámara está como nueva.");
        review.setCreatedAt(now.minusDays(1));
        reviewRepository.save(review);

        maria.setTotalTrades(maria.getTotalTrades() + 1);
        luis.setTotalTrades(luis.getTotalTrades() + 1);
        profileRepository.saveAll(List.of(maria, luis));
    }

    private void createAccountForProfile(Profile profile, String email, OffsetDateTime createdAt) {
        UserAccount account = new UserAccount();
        account.setEmail(email);
        account.setPassword(passwordEncoder.encode("Password123!"));
        account.setProfile(profile);
        account.setCreatedAt(createdAt);

        userAccountRepository.save(account);
        profile.setAccount(account);
        profileRepository.save(profile);
    }
}
