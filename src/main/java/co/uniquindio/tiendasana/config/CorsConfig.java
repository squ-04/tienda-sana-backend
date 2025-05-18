package co.uniquindio.tiendasana.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource; // Importa esta
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List; // Importa esta

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);

        config.setAllowedOrigins(Arrays.asList(
                "http://localhost:4200",                  // Para desarrollo local
                "https://tienda-sana-frontend.vercel.app" // Para tu frontend desplegado en Vercel
        ));

        config.setAllowedHeaders(Arrays.asList(
                "Authorization", "Content-Type", "X-Requested-With", "accept", "Origin",
                "Access-Control-Request-Method", "Access-Control-Request-Headers"
        ));
        config.setExposedHeaders(Arrays.asList(
                "Access-Control-Allow-Origin", "Access-Control-Allow-Credentials", "Authorization", "Content-Disposition"
        ));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setMaxAge(3600L);

        // Aplica esta configuración a todas las rutas (o ajusta si es necesario, ej. "/api/**")
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}