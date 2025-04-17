package co.uniquindio.tiendasana.config;


import com.google.api.client.json.gson.GsonFactory;
import org.springframework.context.annotation.Configuration;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

/**
 * Clase de configuración para el servicio de GoogleSheets
 */
@Configuration
public class GoogleSheetsConfig {

    /**
     * Constante para la nombrar la aplicacion de Google a usar
     */
    private static final String APPLICATION_NAME = "Mi Aplicación Google Sheets";
    /**
     *  Constante para el definir el metodo de fabrica y gestion de Json a usar
     */
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    /**
     * Metoddo usado para obtener un servicio de sheets en las demás partes del proyecto
     * @return
     * @throws IOException
     * @throws GeneralSecurityException
     */
    @Bean  // Define un bean que puede ser inyectado en otros servicios
    public Sheets sheetsService() throws IOException, GeneralSecurityException {
        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream( new File("/etc/secrets/credentials.json") ))
                .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));

        Sheets sheetsService = new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, new HttpCredentialsAdapter(credentials))
                .setApplicationName(APPLICATION_NAME)
                .build();

        System.out.println("SheetsService bean creado correctamente");
        return sheetsService;
    }
}
