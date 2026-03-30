# Guia de iniciacion del backend (Spring Boot)

Esta guia resume todas las variables de configuracion y los comandos para levantar el backend de Tienda Sana en entorno local.

## 1) Requisitos previos

- Java 17 instalado
- MongoDB disponible (local o Atlas)
- Gradle Wrapper del proyecto (ya incluido)
- Opcional: ngrok (si vas a probar webhooks externos)

Version de Java esperada por el proyecto:
- sourceCompatibility=17
- targetCompatibility=17

Referencia:
- [build.gradle](build.gradle)

## 2) Variables de entorno a configurar

Variables leidas desde application.properties y usadas por Spring:

1. MONGODB_URI
- Uso: conexion MongoDB
- Default en proyecto: mongodb://localhost:27017/tienda_sana
- Obligatoria: no, si usas Mongo local en ese puerto

2. CLOUDINARY_CLOUD_NAME
- Uso: integracion Cloudinary
- Default en proyecto: dpv25pud4
- Obligatoria: recomendada en produccion

3. CLOUDINARY_API_KEY
- Uso: integracion Cloudinary
- Default en proyecto: 996674287767736
- Obligatoria: recomendada en produccion

4. CLOUDINARY_API_SECRET
- Uso: firma server-side Cloudinary
- Default en proyecto: vacio
- Obligatoria: si vas a subir/firmar recursos en Cloudinary

5. MERCADOPAGO_ACCESS_TOKEN
- Uso: pagos y reservas con Mercado Pago
- Default en proyecto: valor de prueba definido en application.properties
- Obligatoria: si vas a usar pagos reales

6. MERCADOPAGO_FRONTEND_BASE_URL
- Uso: callbacks y redirecciones a frontend
- Default en proyecto: http://localhost:4200
- Obligatoria: recomendada cuando cambie tu host de frontend

7. MERCADOPAGO_WEBHOOK_BASE_URL
- Uso: URL base para webhooks del backend
- Default en proyecto: http://localhost:8080
- Obligatoria: si pruebas webhooks externos (normalmente con ngrok)

Variables relevantes para herramientas auxiliares:

8. MONGODB_URI (tambien usada por seed script)
- Uso: task seedMongo
- Default interno del script: mongodb://localhost:27017/tienda_sana

Referencias de configuracion:
- [src/main/resources/application.properties](src/main/resources/application.properties)
- [src/main/java/co/uniquindio/tiendasana/services/implementations/ReservaServiceImp.java](src/main/java/co/uniquindio/tiendasana/services/implementations/ReservaServiceImp.java)
- [src/main/java/co/uniquindio/tiendasana/services/implementations/VentaProductoServiceImp.java](src/main/java/co/uniquindio/tiendasana/services/implementations/VentaProductoServiceImp.java)
- [src/main/java/co/uniquindio/tiendasana/scripts/MongoSeedCli.java](src/main/java/co/uniquindio/tiendasana/scripts/MongoSeedCli.java)

## 3) Configuracion SMTP (correo)

Actualmente el backend toma estas propiedades directamente de application.properties:
- simplejavamail.smtp.host
- simplejavamail.smtp.port
- simplejavamail.smtp.username
- simplejavamail.smtp.password

Referencia:
- [src/main/java/co/uniquindio/tiendasana/services/implementations/EmailServiceImp.java](src/main/java/co/uniquindio/tiendasana/services/implementations/EmailServiceImp.java)

Recomendacion:
- Para mayor seguridad, mueve credenciales SMTP a variables de entorno o a un archivo local no versionado.

## 4) Comandos para arrancar (Windows PowerShell)

Ubicate en la carpeta del backend:

  Set-Location "c:\Users\Miguel Angel\Documents\GitHub\tienda-sana-backend"

Configura variables minimas (ejemplo local):

  $env:MONGODB_URI="mongodb://localhost:27017/tienda_sana"
  $env:CLOUDINARY_API_SECRET="TU_CLOUDINARY_SECRET"
  $env:MERCADOPAGO_ACCESS_TOKEN="TU_MP_TOKEN"
  $env:MERCADOPAGO_FRONTEND_BASE_URL="http://localhost:4200"
  $env:MERCADOPAGO_WEBHOOK_BASE_URL="http://localhost:8080"

Arranca el backend:

  .\gradlew.bat bootRun

## 5) Comandos equivalentes (Linux/macOS)

  cd /ruta/a/tienda-sana-backend
  export MONGODB_URI="mongodb://localhost:27017/tienda_sana"
  export CLOUDINARY_API_SECRET="TU_CLOUDINARY_SECRET"
  export MERCADOPAGO_ACCESS_TOKEN="TU_MP_TOKEN"
  export MERCADOPAGO_FRONTEND_BASE_URL="http://localhost:4200"
  export MERCADOPAGO_WEBHOOK_BASE_URL="http://localhost:8080"
  ./gradlew bootRun

## 6) Opcional: sembrar datos demo en Mongo

Windows:

  .\gradlew.bat seedMongo

Linux/macOS:

  ./gradlew seedMongo

## 7) Verificacion rapida

- Backend API: http://localhost:8080
- Actuator management port: http://localhost:8082
- Swagger UI (si esta habilitado por configuracion actual)

## 8) Si necesitas webhooks de Mercado Pago desde internet

1. Levanta backend en 8080.
2. Expone 8080 con ngrok.
3. Ajusta MERCADOPAGO_WEBHOOK_BASE_URL a la URL publica de ngrok.
4. Reinicia backend.

Ejemplo:

  ngrok http 8080

## 9) Notas de seguridad

- No uses secretos reales en archivos versionados.
- Usa variables de entorno por maquina.
- Para produccion, separa perfiles (dev, prod) y secretos en un vault.
