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

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Configuration
public class GoogleSheetsConfig {


    private static final String APPLICATION_NAME = "Mi Aplicación Google Sheets";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    @Bean  // Define un bean que puede ser inyectado en otros servicios
    public Sheets sheetsService() throws IOException, GeneralSecurityException {
        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream("src/main/resources/credentials.json"))
                .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));

        Sheets sheetsService = new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, new HttpCredentialsAdapter(credentials))
                .setApplicationName(APPLICATION_NAME)
                .build();

        System.out.println("SheetsService bean creado correctamente");
        return sheetsService;
    }
}
