package co.uniquindio.tiendasana.config;

import co.uniquindio.tiendasana.dto.jwtdtos.MessageDTO;
import co.uniquindio.tiendasana.model.enums.Rol;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // Importar
import org.springframework.security.core.GrantedAuthority;                         // Importar
import org.springframework.security.core.authority.SimpleGrantedAuthority;       // Importar
import org.springframework.security.core.context.SecurityContextHolder;          // Importar
import org.springframework.security.core.userdetails.User;                       // Importar (o tu propia clase UserDetails)
import org.springframework.security.core.userdetails.UserDetails;                // Importar
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.http.HttpMethod;
import java.io.IOException;
import java.util.Collections;                                                  // Importar
import java.util.List;                                                         // Importar

@Component
@RequiredArgsConstructor
public class TokenFilter extends OncePerRequestFilter {

    private final JWTUtils jwtUtils;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // ----- INICIO DE CAMBIO CRÍTICO -----
        // Manten SÓLO este bloque para OPTIONS y que esté AL PRINCIPIO del método.
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            filterChain.doFilter(request, response); // Pasa al siguiente filtro (debería ser el CorsFilter de Spring)
            return; // MUY IMPORTANTE: termina la ejecución de este filtro para OPTIONS.
        }
        // ----- FIN DE CAMBIO CRÍTICO -----


        // ELIMINA ESTE BLOQUE SI LO TENÍAS ANTES DE HttpMethod.OPTIONS.matches(...)
        // if (request.getMethod().equals("OPTIONS")) {
        //    response.setStatus(HttpServletResponse.SC_OK);
        //    return;
        // }

        String requestURI = request.getRequestURI();

        // Bypass token processing for public auth paths
        if (requestURI.startsWith("/api/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = getToken(request); // Mueve esto aquí, después del manejo de OPTIONS y /api/auth

        try {
            if (requiereAutenticacion(requestURI)) {
                if (token == null) {
                    crearRespuestaError("Token no proporcionado", HttpServletResponse.SC_UNAUTHORIZED, response);
                    return;
                }

                Jws<Claims> jws = jwtUtils.parseJwt(token); // Parsear una sola vez

                if (!tienePermisosParaUri(requestURI, jws)) { // Reutilizar jws
                    crearRespuestaError("No tiene permisos para acceder a este recurso",
                            HttpServletResponse.SC_FORBIDDEN, response);
                    return;
                }

                // ----- INICIO DE INTEGRACIÓN CON SecurityContextHolder -----
                String email = jws.getPayload().getSubject(); // Asume que el subject es el email
                String rolStr = jws.getPayload().get("rol", String.class); // Obtén el rol como String
                if (email != null && rolStr != null) {
                    Rol userRol = Rol.valueOf(rolStr);
                    List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + userRol.name()));
                    // La contraseña es "" porque la autenticación es por token, no por password aquí.
                    UserDetails userDetails = new User(email, "", authorities);
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    // Podrías loggear un error si el email o rol son nulos en un token que se esperaba válido
                    logger.warn("Token válido pero con email o rol nulos. Subject: " + email + ", Rol String: " + rolStr);
                    // Dependiendo de tu lógica, podrías denegar el acceso aquí.
                    // Por ahora, si tienePermisosParaUri pasó, podría continuar, pero es un estado anómalo.
                }
                // ----- FIN DE INTEGRACIÓN CON SecurityContextHolder -----
            }

            filterChain.doFilter(request, response);

        } catch (MalformedJwtException | SignatureException e) {
            SecurityContextHolder.clearContext(); // Limpia el contexto en caso de error
            crearRespuestaError("El token es incorrecto", HttpServletResponse.SC_UNAUTHORIZED, response);
        } catch (ExpiredJwtException e) {
            SecurityContextHolder.clearContext();
            crearRespuestaError("El token está vencido", HttpServletResponse.SC_UNAUTHORIZED, response);
        } catch (IllegalArgumentException e) { // Ej. si Rol.valueOf falla
            SecurityContextHolder.clearContext();
            logger.error("Argumento ilegal procesando token (ej. rol inválido): " + e.getMessage());
            crearRespuestaError("Error en los datos del token.", HttpServletResponse.SC_BAD_REQUEST, response);
        }
        catch (Exception e) {
            SecurityContextHolder.clearContext();
            logger.error("Error inesperado en TokenFilter", e); // Loguea la traza completa de la excepción
            crearRespuestaError("Error interno del servidor: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, response);
        }
    }

    private boolean requiereAutenticacion(String uri) {
        // Esta lógica está bien
        return uri.startsWith("/api/account") ||
                uri.startsWith("/api/admin") ||
                uri.startsWith("/api/cliente");
    }

    // Modificado para evitar parsear el token múltiples veces
    private boolean tienePermisosParaUri(String uri, Jws<Claims> jws) {
        // String token ya no es necesario como parámetro si pasas jws
        // if (token == null) return false; // Ya no es necesario aquí si el token se valida antes

        String rolStr = jws.getPayload().get("rol").toString();
        Rol userRol = Rol.valueOf(rolStr); // Esto podría lanzar IllegalArgumentException si el rol no existe

        if (uri.startsWith("/api/admin")) {
            return userRol == Rol.ADMIN;
        } else if (uri.startsWith("/api/cliente")) {
            return userRol == Rol.CLIENTE;
        } else if (uri.startsWith("/api/account")) {
            // Asumiendo que /api/account es para que el propio cliente o un admin vean/modifiquen datos.
            // La lógica específica de "puede X modificar Y" debería estar en el servicio/controlador
            // usando el email del SecurityContext y el email del recurso.
            // Este filtro solo verifica si el rol es permitido para la *ruta general*.
            return userRol == Rol.CLIENTE || userRol == Rol.ADMIN;
        }

        // Si la URI no coincide con ninguna de las anteriores pero requiereAutenticacion es true,
        // ¿qué debería pasar? Por defecto, aquí se permitiría. Deberías considerar esto.
        // Si es una ruta autenticada no listada, quizás deberías retornar false por defecto
        // a menos que tengas una lógica más general.
        return true; // O `false` si las rutas no listadas pero autenticadas deben ser denegadas por defecto.
    }

    // getToken y crearRespuestaError se mantienen como los tienes (con la pequeña corrección en get token)
    private String getToken(HttpServletRequest req) {
        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7); // Correcto: "Bearer ".length() es 7
        }
        return null;
    }

    private void crearRespuestaError(String mensaje, int codigoError, HttpServletResponse response)
            throws IOException {
        if (response.isCommitted()) {
            logger.warn("Respuesta ya enviada (committed), no se puede escribir error: " + mensaje);
            return;
        }
        MessageDTO<String> dto = new MessageDTO<>(true, mensaje);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8"); // Usa UTF-8, es más estándar que "UTF-F"
        response.setStatus(codigoError);
        // Usar objectMapper inyectado o el miembro de la clase
        response.getWriter().write(this.objectMapper.writeValueAsString(dto));
    }
    // ... (isSecurePath ya no es usado si usas requiereAutenticacion)
}