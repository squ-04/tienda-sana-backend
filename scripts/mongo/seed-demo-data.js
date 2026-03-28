/**
 * Datos de demostración para Tienda Sana (MongoDB).
 *
 * Uso (PowerShell, desde la raíz del backend):
 *   mongosh "mongodb://localhost:27017/tienda_sana" scripts/mongo/seed-demo-data.js
 *
 * Sin mongosh instalado, alternativa (misma URI que application.properties):
 *   .\gradlew.bat seedMongo
 *
 * Credenciales (BCrypt, Spring Security):
 *   Cliente: cliente.demo@tiendasana.local / Cliente123!
 *   Admin:   admin@tiendasana.local       / Admin123!
 *
 * Las cuentas tienen estado ACTIVA para poder hacer login sin activación por correo.
 */

const dbName = 'tienda_sana';
const db = db.getSiblingDB(dbName);

const now = new Date();

// --- Cuentas (colección accounts, _id = email) ---
// Hashes generados con BCryptPasswordEncoder (strength 10), mismos que Spring Boot.
const hashCliente = '$2a$10$IvLX08tKkzPt7y0JUbL2xeO07vBhBjt9E2tVUzbAxdLZ2J9IydgkG'; // Cliente123!
const hashAdmin = '$2a$10$eX7bf6igFrHrvpqnK4NOFuk6oNNDOLSwZXJt4i9ufWlv/tQydSu9K'; // Admin123!

const accounts = [
  {
    _id: 'cliente.demo@tiendasana.local',
    dni: '1090123456',
    nombre: 'Cliente Demo',
    telefono: '3001234567',
    direccion: 'Calle 10 # 20-30',
    contrasenia: hashCliente,
    rol: 'CLIENTE',
    estado: 'ACTIVA',
    fechaRegistro: now,
    codigoRegistro: 'SEED01',
    fechaCodigoRegistro: now,
    codigoContrasenia: 'SEED02',
    fechaCodigoContrasenia: now,
  },
  {
    _id: 'admin@tiendasana.local',
    dni: '8000123456',
    nombre: 'Administrador Demo',
    telefono: '3009876543',
    direccion: 'Tienda Sana HQ',
    contrasenia: hashAdmin,
    rol: 'ADMIN',
    estado: 'ACTIVA',
    fechaRegistro: now,
    codigoRegistro: 'SEED03',
    fechaCodigoRegistro: now,
    codigoContrasenia: 'SEED04',
    fechaCodigoContrasenia: now,
  },
];

// --- Productos (products) ---
const products = [
  {
    _id: 'seed-prod-ensalada-001',
    nombre: 'Ensalada Mediterránea',
    descripcion: 'Lechuga, tomate, pepino, aceitunas y queso feta.',
    categoria: 'Ensaladas',
    imagen: 'https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=800',
    precioUnitario: 18900,
    stockQuantity: 40,
    active: true,
    outOfStock: false,
    calificacionPromedio: 0,
  },
  {
    _id: 'seed-prod-jugo-002',
    nombre: 'Jugo natural de naranja',
    descripcion: '500 ml, recién exprimido.',
    categoria: 'Bebidas',
    imagen: 'https://images.unsplash.com/photo-1600271886742-f049cd451bba?w=800',
    precioUnitario: 6500,
    stockQuantity: 100,
    active: true,
    outOfStock: false,
    calificacionPromedio: 0,
  },
  {
    _id: 'seed-prod-wrap-003',
    nombre: 'Wrap integral pollo',
    descripcion: 'Pollo grill, vegetales y aderezo yogurt.',
    categoria: 'Platos fuertes',
    imagen: 'https://images.unsplash.com/photo-1626700051175-6818013e1d4f?w=800',
    precioUnitario: 22500,
    stockQuantity: 25,
    active: true,
    outOfStock: false,
    calificacionPromedio: 0,
  },
];

// --- Mesas reservas cliente (reservation_tables) ---
const reservationTables = [
  {
    _id: 'seed-mesa-centro-01',
    nombre: 'Mesa terraza Centro',
    estado: 'Disponible',
    localidad: 'Centro',
    precioReserva: 45000,
    capacidad: 4,
    imagen: 'https://images.unsplash.com/photo-1414235077428-338989a2e8c0?w=800',
    visibleToClient: true,
  },
  {
    _id: 'seed-mesa-patio-02',
    nombre: 'Mesa jardín',
    estado: 'Disponible',
    localidad: 'Patio',
    precioReserva: 55000,
    capacidad: 6,
    imagen: 'https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=800',
    visibleToClient: true,
  },
  {
    _id: 'seed-mesa-salon-03',
    nombre: 'Mesa salón VIP',
    estado: 'Disponible',
    localidad: 'Salon',
    precioReserva: 60000,
    capacidad: 8,
    imagen: 'https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=800',
    visibleToClient: true,
  },
];

// --- Mesas admin operativas (restaurant_tables) ---
const restaurantTables = [
  { _id: 'seed-rt-ventana-1', capacity: 2, location: 'Ventana', active: true, status: 'AVAILABLE' },
  { _id: 'seed-rt-salon-1', capacity: 4, location: 'Salón principal', active: true, status: 'AVAILABLE' },
  { _id: 'seed-rt-salon-2', capacity: 4, location: 'Salón principal', active: true, status: 'RESERVED' },
];

function upsertById(collection, docs) {
  docs.forEach((doc) => {
    collection.replaceOne({ _id: doc._id }, doc, { upsert: true });
  });
}

print('=== Tienda Sana — seed demo ===');
print('Base de datos: ' + dbName);

upsertById(db.accounts, accounts);
print('Cuentas (upsert por _id): ' + accounts.length);

upsertById(db.products, products);
print('Productos: ' + products.length);

upsertById(db.reservation_tables, reservationTables);
print('Mesas reservas (cliente): ' + reservationTables.length);

upsertById(db.restaurant_tables, restaurantTables);
print('Mesas restaurante (admin): ' + restaurantTables.length);

print('\n--- Conteos ---');
print('accounts:           ' + db.accounts.countDocuments());
print('products:            ' + db.products.countDocuments());
print('reservation_tables: ' + db.reservation_tables.countDocuments());
print('restaurant_tables:  ' + db.restaurant_tables.countDocuments());

print('\n✅ Seed completado.');
print('Login cliente: cliente.demo@tiendasana.local / Cliente123!');
print('Login admin:   admin@tiendasana.local / Admin123!');
