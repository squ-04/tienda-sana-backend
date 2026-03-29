package co.uniquindio.tiendasana.scripts;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;

import java.util.Date;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

/**
 * Inserta datos demo (cuentas, productos, mesas) y comprueba la conexión a MongoDB.
 * <p>
 * Ejecutar: {@code ./gradlew seedMongo} (o {@code gradlew.bat seedMongo} en Windows).
 * URI: variable de entorno {@code MONGODB_URI} o {@code mongodb://localhost:27017/tienda_sana}.
 */
public final class MongoSeedCli {

    private static final String DEFAULT_URI = "mongodb://localhost:27017/tienda_sana";

    private static final String HASH_CLIENTE = "$2a$10$IvLX08tKkzPt7y0JUbL2xeO07vBhBjt9E2tVUzbAxdLZ2J9IydgkG"; // Cliente123!
    private static final String HASH_ADMIN = "$2a$10$eX7bf6igFrHrvpqnK4NOFuk6oNNDOLSwZXJt4i9ufWlv/tQydSu9K"; // Admin123!

    public static void main(String[] args) {
        String uri = System.getenv().getOrDefault("MONGODB_URI", DEFAULT_URI);
        ConnectionString cs = new ConnectionString(uri);
        String dbName = cs.getDatabase();
        if (dbName == null || dbName.isBlank()) {
            dbName = "tienda_sana";
        }

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(cs)
                .build();

        try (MongoClient client = MongoClients.create(settings)) {
            MongoDatabase db = client.getDatabase(dbName);
            db.runCommand(new Document("ping", 1));
            System.out.println("Conexión OK: " + uri.replaceAll("://[^@]+@", "://***@"));

            Date now = new Date();
            ReplaceOptions upsert = new ReplaceOptions().upsert(true);

            List<Document> accounts = List.of(
                    new Document("_id", "cliente.demo@tiendasana.local")
                            .append("dni", "1090123456")
                            .append("nombre", "Cliente Demo")
                            .append("telefono", "3001234567")
                            .append("direccion", "Calle 10 # 20-30")
                            .append("contrasenia", HASH_CLIENTE)
                            .append("rol", "CLIENTE")
                            .append("estado", "ACTIVA")
                            .append("fechaRegistro", now)
                            .append("codigoRegistro", "SEED01")
                            .append("fechaCodigoRegistro", now)
                            .append("codigoContrasenia", "SEED02")
                            .append("fechaCodigoContrasenia", now),
                    new Document("_id", "admin@tiendasana.local")
                            .append("dni", "8000123456")
                            .append("nombre", "Administrador Demo")
                            .append("telefono", "3009876543")
                            .append("direccion", "Tienda Sana HQ")
                            .append("contrasenia", HASH_ADMIN)
                            .append("rol", "ADMIN")
                            .append("estado", "ACTIVA")
                            .append("fechaRegistro", now)
                            .append("codigoRegistro", "SEED03")
                            .append("fechaCodigoRegistro", now)
                            .append("codigoContrasenia", "SEED04")
                            .append("fechaCodigoContrasenia", now));

            for (Document doc : accounts) {
                db.getCollection("accounts").replaceOne(eq("_id", doc.getString("_id")), doc, upsert);
            }

            List<Document> products = List.of(
                    product("seed-prod-ensalada-001", "Ensalada Mediterránea",
                            "Lechuga, tomate, pepino, aceitunas y queso feta.", "Ensaladas",
                            "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=800", 18900, 40),
                    product("seed-prod-jugo-002", "Jugo natural de naranja",
                            "500 ml, recién exprimido.", "Bebidas",
                            "https://images.unsplash.com/photo-1600271886742-f049cd451bba?w=800", 6500, 100),
                    product("seed-prod-wrap-003", "Wrap integral pollo",
                            "Pollo grill, vegetales y aderezo yogurt.", "Platos fuertes",
                            "https://images.unsplash.com/photo-1626700051175-6818013e1d4f?w=800", 22500, 25));

            for (Document doc : products) {
                db.getCollection("products").replaceOne(eq("_id", doc.getString("_id")), doc, upsert);
            }

            List<Document> reservationTables = List.of(
                    mesaCliente("seed-mesa-centro-01", "Mesa terraza Centro", "Disponible", "Centro", 45000, 4,
                            "https://images.unsplash.com/photo-1414235077428-338989a2e8c0?w=800"),
                    mesaCliente("seed-mesa-patio-02", "Mesa jardín", "Disponible", "Patio", 55000, 6,
                            "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=800"),
                    mesaCliente("seed-mesa-salon-03", "Mesa salón VIP", "Disponible", "Salon", 60000, 8,
                            "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=800"));

                        for (Document doc : reservationTables) {
                                db.getCollection("tables").replaceOne(eq("_id", doc.getString("_id")), doc, upsert);
            }

            System.out.println("\n=== Conteos ===");
            System.out.println("accounts:           " + db.getCollection("accounts").countDocuments());
            System.out.println("products:           " + db.getCollection("products").countDocuments());
            System.out.println("tables:             " + db.getCollection("tables").countDocuments());
            System.out.println("\nSeed completado.");
            System.out.println("Cliente: cliente.demo@tiendasana.local / Cliente123!");
            System.out.println("Admin:   admin@tiendasana.local / Admin123!");
        } catch (MongoException e) {
            System.err.println("Error MongoDB: " + e.getMessage());
            System.exit(1);
        }
    }

    private static Document product(String id, String nombre, String descripcion, String categoria,
                                    String imagen, double precio, int stock) {
        return new Document("_id", id)
                .append("nombre", nombre)
                .append("descripcion", descripcion)
                .append("categoria", categoria)
                .append("imagen", imagen)
                .append("precioUnitario", precio)
                .append("stockQuantity", stock)
                .append("active", true)
                .append("outOfStock", false)
                .append("calificacionPromedio", 0);
    }

    private static Document mesaCliente(String id, String nombre, String estado, String localidad,
                                      double precio, int capacidad, String imagen) {
        return new Document("_id", id)
                .append("nombre", nombre)
                .append("estado", estado)
                .append("localidad", localidad)
                .append("precioReserva", precio)
                .append("capacidad", capacidad)
                .append("imagen", imagen)
                .append("visibleToClient", true);
    }

    private MongoSeedCli() {
    }
}
