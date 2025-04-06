package co.uniquindio.tiendasana.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

/**
 * Clase de servicios de utilidad para la gestion de Json Web Token (JWT)
 */
@Component
public class JWTUtils {
    /**
     * Metodo usado para generar token de inicio de sesión al usuario que entra
     * @param email
     * @param claims
     * @return
     */
    public String generarToken(String email, Map<String, Object> claims){
        Instant now = Instant.now();
        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(8L, ChronoUnit.HOURS)))
                .signWith( getKey() )
                .compact();
    }

    /**
     * Metodo para analizar la validez del contenido de un Json Web Token (JWT)
     * @param jwtString
     * @return
     * @throws ExpiredJwtException
     * @throws UnsupportedJwtException
     * @throws MalformedJwtException
     * @throws IllegalArgumentException
     */
    public Jws<Claims> parseJwt(String jwtString) throws ExpiredJwtException,
            UnsupportedJwtException, MalformedJwtException, IllegalArgumentException {
        JwtParser jwtParser = Jwts.parser().verifyWith( getKey() ).build();
        return jwtParser.parseSignedClaims(jwtString);
    }

    /**
     *  Metodo para generar clave secreta
     * @return
     */
    private SecretKey getKey(){
        String secretKey = "secretsecretsecretsecretsecretsecretsecretsecret";

        byte[] secretKeyBytes = secretKey.getBytes();
        return Keys.hmacShaKeyFor(secretKeyBytes);
    }
}
