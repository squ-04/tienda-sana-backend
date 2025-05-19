package co.uniquindio.tiendasana.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Duration;

@Configuration
public class RateLimitConfig implements WebMvcConfigurer {

    @Bean
    public RateLimitInterceptor rateLimitInterceptor() {
        return new RateLimitInterceptor(bucket());
    }

    @Bean
    public Bucket bucket() {
        // Permitir 100 peticiones por minuto
        Bandwidth limit = Bandwidth.classic(60, Refill.greedy(60, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor())
                .addPathPatterns("/**"); // Aplicar a todas las URLs
    }

    @Bean("sheetsApiRateLimitBucket") // Dale un nombre específico al bean
    public Bucket sheetsApiRateLimitBucket() {
        int limiteLlamadasSheetsPorMinuto = 55;
        Bandwidth limit = Bandwidth.classic(limiteLlamadasSheetsPorMinuto,
                Refill.greedy(limiteLlamadasSheetsPorMinuto, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}