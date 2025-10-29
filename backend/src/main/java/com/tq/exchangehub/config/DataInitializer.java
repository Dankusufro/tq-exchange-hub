package com.tq.exchangehub.config;

import com.tq.exchangehub.entity.Category;
import com.tq.exchangehub.entity.Item;
import com.tq.exchangehub.entity.Message;
import com.tq.exchangehub.entity.Profile;
import com.tq.exchangehub.entity.Review;
import com.tq.exchangehub.entity.Trade;
import com.tq.exchangehub.entity.TradeStatus;
import com.tq.exchangehub.entity.UserAccount;
import com.tq.exchangehub.entity.Role;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
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

        Category books = new Category();
        books.setName("Libros y Educación");
        books.setDescription("Material académico y lecturas inspiradoras para estudiantes.");
        books.setIcon("lucide:book-open");
        books.setCreatedAt(now.minusDays(40));

        Category clothing = new Category();
        clothing.setName("Ropa y Accesorios");
        clothing.setDescription("Prendas únicas y accesorios en excelente estado.");
        clothing.setIcon("lucide:shirt");
        clothing.setCreatedAt(now.minusDays(38));

        Category electronics = new Category();
        electronics.setName("Electrónicos");
        electronics.setDescription("Dispositivos y gadgets listos para un nuevo hogar.");
        electronics.setIcon("lucide:smartphone");
        electronics.setCreatedAt(now.minusDays(36));

        Category home = new Category();
        home.setName("Hogar y Jardín");
        home.setDescription("Decoración, muebles y plantas para renovar tus espacios.");
        home.setIcon("lucide:home");
        home.setCreatedAt(now.minusDays(34));

        Category vehicles = new Category();
        vehicles.setName("Vehículos");
        vehicles.setDescription("Movilidad urbana y opciones sostenibles para transportarte.");
        vehicles.setIcon("lucide:car");
        vehicles.setCreatedAt(now.minusDays(32));

        Category crafts = new Category();
        crafts.setName("Arte y Manualidades");
        crafts.setDescription("Creaciones hechas a mano y materiales creativos.");
        crafts.setIcon("lucide:palette");
        crafts.setCreatedAt(now.minusDays(30));

        Category sports = new Category();
        sports.setName("Deportes");
        sports.setDescription("Equipamiento y accesorios para mantenerte activo.");
        sports.setIcon("lucide:dumbbell");
        sports.setCreatedAt(now.minusDays(28));

        Category services = new Category();
        services.setName("Servicios");
        services.setDescription("Talentos y habilidades listos para ayudarte.");
        services.setIcon("lucide:wrench");
        services.setCreatedAt(now.minusDays(26));

        categoryRepository.saveAll(List.of(books, clothing, electronics, home, vehicles, crafts, sports, services));

        Profile maria = new Profile();
        maria.setDisplayName("María López");
        maria.setBio("Apasionada por la fotografía y los viajes en bici.");
        maria.setAvatarUrl("https://images.unsplash.com/photo-1524504388940-b1c1722653e1");
        maria.setLocation("Madrid, España");
        maria.setPhone("+34 600 123 456");
        maria.setRating(4.9);
        maria.setTotalTrades(22);
        maria.setCreatedAt(now.minusMonths(6));
        maria.setUpdatedAt(now.minusDays(2));

        Profile luis = new Profile();
        luis.setDisplayName("Luis García");
        luis.setBio("Coleccionista de tecnología vintage y lector empedernido.");
        luis.setAvatarUrl("https://images.unsplash.com/photo-1527980965255-d3b416303d12");
        luis.setLocation("Barcelona, España");
        luis.setPhone("+34 611 987 654");
        luis.setRating(4.7);
        luis.setTotalTrades(15);
        luis.setCreatedAt(now.minusMonths(5));
        luis.setUpdatedAt(now.minusDays(4));

        Profile ana = new Profile();
        ana.setDisplayName("Ana Ruiz");
        ana.setBio("Diseñadora freelance y amante de la jardinería.");
        ana.setAvatarUrl("https://images.unsplash.com/photo-1521572267360-ee0c2909d518");
        ana.setLocation("Valencia, España");
        ana.setPhone("+34 622 456 789");
        ana.setRating(4.8);
        ana.setTotalTrades(11);
        ana.setCreatedAt(now.minusMonths(4));
        ana.setUpdatedAt(now.minusDays(5));

        Profile carlos = new Profile();
        carlos.setDisplayName("Carlos Mendoza");
        carlos.setBio("Amante del deporte y las aventuras al aire libre.");
        carlos.setAvatarUrl("https://images.unsplash.com/photo-1544723795-3fb6469f5b39");
        carlos.setLocation("Ciudad de México, México");
        carlos.setPhone("+52 55 1234 5678");
        carlos.setRating(4.8);
        carlos.setTotalTrades(14);
        carlos.setCreatedAt(now.minusMonths(7));
        carlos.setUpdatedAt(now.minusDays(3));

        Profile sofia = new Profile();
        sofia.setDisplayName("Sofía Vega");
        sofia.setBio("Instructora certificada de yoga y bienestar integral.");
        sofia.setAvatarUrl("https://images.unsplash.com/photo-1524504388940-b1c1722653e1?ixid=sofia");
        sofia.setLocation("Puebla, México");
        sofia.setPhone("+52 22 9876 5432");
        sofia.setRating(5.0);
        sofia.setTotalTrades(19);
        sofia.setCreatedAt(now.minusMonths(8));
        sofia.setUpdatedAt(now.minusDays(1));

        profileRepository.saveAll(List.of(maria, luis, ana, carlos, sofia));

        createAccountForProfile(maria, "maria@example.com", now.minusMonths(6));
        createAccountForProfile(luis, "luis@example.com", now.minusMonths(5));
        createAccountForProfile(ana, "ana@example.com", now.minusMonths(4));
        createAccountForProfile(carlos, "carlos@example.com", now.minusMonths(7));
        createAccountForProfile(sofia, "sofia@example.com", now.minusMonths(8));

        Item mountainBike = new Item();
        mountainBike.setOwner(carlos);
        mountainBike.setCategory(sports);
        mountainBike.setTitle("Bicicleta de montaña Trek");
        mountainBike.setDescription(
                "Suspensión ajustable y frenos de disco. Lista para rutas en la sierra.");
        mountainBike.setCondition("Muy buena");
        mountainBike.setEstimatedValue(new BigDecimal("680.00"));
        mountainBike.setAvailable(true);
        mountainBike.setService(false);
        mountainBike.setLocation(carlos.getLocation());
        mountainBike.setImages(List.of(
                "https://images.unsplash.com/photo-1518655048521-f130df041f66",
                "https://images.unsplash.com/photo-1509395176047-4a66953fd231"));
        mountainBike.setWishlist(List.of("Laptop", "Cámara fotográfica", "Guitarra eléctrica"));
        mountainBike.setCreatedAt(now.minusDays(12));
        mountainBike.setUpdatedAt(now.minusDays(2));

        Item leatherJacket = new Item();
        leatherJacket.setOwner(ana);
        leatherJacket.setCategory(clothing);
        leatherJacket.setTitle("Chaqueta de cuero vintage");
        leatherJacket.setDescription(
                "Piel auténtica, talla M. Conserva el forro original y cierres funcionales.");
        leatherJacket.setCondition("Excelente");
        leatherJacket.setEstimatedValue(new BigDecimal("320.00"));
        leatherJacket.setAvailable(true);
        leatherJacket.setService(false);
        leatherJacket.setLocation(ana.getLocation());
        leatherJacket.setImages(List.of(
                "https://images.unsplash.com/photo-1542291026-7eec264c27ff",
                "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab"));
        leatherJacket.setWishlist(List.of("Botas de cuero", "Reloj clásico", "Perfume unisex"));
        leatherJacket.setCreatedAt(now.minusDays(9));
        leatherJacket.setUpdatedAt(now.minusDays(3));

        Item bookCollection = new Item();
        bookCollection.setOwner(luis);
        bookCollection.setCategory(books);
        bookCollection.setTitle("Colección de libros universitarios");
        bookCollection.setDescription(
                "Textos de ingeniería y matemáticas. Incluye marcadores y notas destacadas.");
        bookCollection.setCondition("Bueno");
        bookCollection.setEstimatedValue(new BigDecimal("150.00"));
        bookCollection.setAvailable(true);
        bookCollection.setService(false);
        bookCollection.setLocation(luis.getLocation());
        bookCollection.setImages(List.of(
                "https://images.unsplash.com/photo-1523475472560-d2df97ec485c",
                "https://images.unsplash.com/photo-1521587760476-6c12a4b040da"));
        bookCollection.setWishlist(List.of("Tablet", "Auriculares", "Mochila ergonómica"));
        bookCollection.setCreatedAt(now.minusDays(11));
        bookCollection.setUpdatedAt(now.minusDays(4));

        Item yogaClasses = new Item();
        yogaClasses.setOwner(sofia);
        yogaClasses.setCategory(services);
        yogaClasses.setTitle("Clases de yoga personalizadas");
        yogaClasses.setDescription(
                "Sesiones online y presenciales enfocadas en respiración y fuerza.");
        yogaClasses.setCondition("Servicio");
        yogaClasses.setEstimatedValue(new BigDecimal("180.00"));
        yogaClasses.setAvailable(true);
        yogaClasses.setService(true);
        yogaClasses.setLocation("Puebla y modalidad virtual");
        yogaClasses.setImages(List.of(
                "https://images.unsplash.com/photo-1554306274-f23873d9a26f",
                "https://images.unsplash.com/photo-1552196563-55cd4e45efb3"));
        yogaClasses.setWishlist(List.of("Masajes terapéuticos", "Clases de cocina", "Consultas nutricionales"));
        yogaClasses.setCreatedAt(now.minusDays(8));
        yogaClasses.setUpdatedAt(now.minusDays(1));

        Item iphone = new Item();
        iphone.setOwner(maria);
        iphone.setCategory(electronics);
        iphone.setTitle("iPhone 12 Pro 128GB");
        iphone.setDescription(
                "Pantalla impecable, batería al 89%. Incluye cargador original y funda protectora.");
        iphone.setCondition("Muy bueno");
        iphone.setEstimatedValue(new BigDecimal("720.00"));
        iphone.setAvailable(true);
        iphone.setService(false);
        iphone.setLocation(maria.getLocation());
        iphone.setImages(List.of(
                "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9",
                "https://images.unsplash.com/photo-1512496015851-a90fb38ba796"));
        iphone.setWishlist(List.of("Laptop ligera", "Consola portátil", "Bicicleta urbana"));
        iphone.setCreatedAt(now.minusDays(6));
        iphone.setUpdatedAt(now.minusDays(2));

        Item indoorPlants = new Item();
        indoorPlants.setOwner(ana);
        indoorPlants.setCategory(home);
        indoorPlants.setTitle("Conjunto de plantas de interior");
        indoorPlants.setDescription(
                "Incluye macetas de cerámica, guía de cuidados y fertilizante orgánico.");
        indoorPlants.setCondition("Excelente");
        indoorPlants.setEstimatedValue(new BigDecimal("95.00"));
        indoorPlants.setAvailable(true);
        indoorPlants.setService(false);
        indoorPlants.setLocation(ana.getLocation());
        indoorPlants.setImages(List.of(
                "https://images.unsplash.com/photo-1483794344563-d27a8d18014e",
                "https://images.unsplash.com/photo-1493663284031-b7e3aefcae8e"));
        indoorPlants.setWishlist(List.of("Libros de botánica", "Herramientas de jardinería", "Decoración artesanal"));
        indoorPlants.setCreatedAt(now.minusDays(5));
        indoorPlants.setUpdatedAt(now.minusDays(1));

        Item watercolorKit = new Item();
        watercolorKit.setOwner(maria);
        watercolorKit.setCategory(crafts);
        watercolorKit.setTitle("Kit de acuarela profesional");
        watercolorKit.setDescription(
                "Incluye 24 colores, pinceles premium y papel de algodón prensado en frío.");
        watercolorKit.setCondition("Nuevo");
        watercolorKit.setEstimatedValue(new BigDecimal("120.00"));
        watercolorKit.setAvailable(true);
        watercolorKit.setService(false);
        watercolorKit.setLocation(maria.getLocation());
        watercolorKit.setImages(List.of(
                "https://images.unsplash.com/photo-1526498460520-4c246339dccb",
                "https://images.unsplash.com/photo-1513364776144-60967b0f800f"));
        watercolorKit.setWishlist(List.of("Clases de cerámica", "Marco de madera", "Impresora fotográfica"));
        watercolorKit.setCreatedAt(now.minusDays(4));
        watercolorKit.setUpdatedAt(now.minusDays(1));

        Item cityScooter = new Item();
        cityScooter.setOwner(carlos);
        cityScooter.setCategory(vehicles);
        cityScooter.setTitle("Scooter eléctrico urbano");
        cityScooter.setDescription(
                "Autonomía de 25 km, plegable y ligero. Incluye cargador y bolsa de transporte.");
        cityScooter.setCondition("Muy bueno");
        cityScooter.setEstimatedValue(new BigDecimal("540.00"));
        cityScooter.setAvailable(true);
        cityScooter.setService(false);
        cityScooter.setLocation(carlos.getLocation());
        cityScooter.setImages(List.of(
                "https://images.unsplash.com/photo-1519750157634-b6d493a0f77d",
                "https://images.unsplash.com/photo-1502877338535-766e1452684a"));
        cityScooter.setWishlist(List.of("Cámara deportiva", "Curso de fotografía", "Mochila impermeable"));
        cityScooter.setCreatedAt(now.minusDays(3));
        cityScooter.setUpdatedAt(now.minusHours(12));

        itemRepository.saveAll(
                List.of(
                        mountainBike,
                        leatherJacket,
                        bookCollection,
                        yogaClasses,
                        iphone,
                        indoorPlants,
                        watercolorKit,
                        cityScooter));

        Trade trade = new Trade();
        trade.setOwner(maria);
        trade.setRequester(carlos);
        trade.setOwnerItem(iphone);
        trade.setRequesterItem(mountainBike);
        trade.setMessage("¿Te interesa cambiar tu bicicleta por mi iPhone? Podemos ajustar accesorios.");
        trade.setStatus(TradeStatus.COMPLETED);
        trade.setCreatedAt(now.minusDays(5));
        trade.setUpdatedAt(now.minusDays(1));

        tradeRepository.save(trade);

        Message message = new Message();
        message.setTrade(trade);
        message.setSender(carlos);
        message.setContent("Hola María, el iPhone se ve impecable. ¿Incluyes la funda en el intercambio?");
        message.setCreatedAt(now.minusDays(5).plusHours(3));
        messageRepository.save(message);

        Review review = new Review();
        review.setTrade(trade);
        review.setReviewer(carlos);
        review.setReviewed(maria);
        review.setRating(5);
        review.setComment("Intercambio impecable, el dispositivo llegó como se prometió.");
        review.setCreatedAt(now.minusDays(1));
        reviewRepository.save(review);

        maria.setTotalTrades(maria.getTotalTrades() + 1);
        carlos.setTotalTrades(carlos.getTotalTrades() + 1);
        profileRepository.saveAll(List.of(maria, carlos));
    }

    private void createAccountForProfile(Profile profile, String email, OffsetDateTime createdAt) {
        UserAccount account = new UserAccount();
        account.setEmail(email);
        account.setPassword(passwordEncoder.encode("Password123!"));
        account.setProfile(profile);
        account.setCreatedAt(createdAt);
        if ("maria@example.com".equalsIgnoreCase(email)) {
            account.setRole(Role.ADMIN);
        } else {
            account.setRole(Role.USER);
        }

        userAccountRepository.save(account);
        profile.setAccount(account);
        profileRepository.save(profile);
    }
}
