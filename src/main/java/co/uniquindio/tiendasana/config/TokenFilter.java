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
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Servicio para establecer metodos que ayuden a filtrar y gestionar acceso los controladores o metodos
 */
@Component
@RequiredArgsConstructor
public class TokenFilter extends OncePerRequestFilter {

    /**
     *  Constante que hace referencia la clase de JWTUtils
     */
    private final JWTUtils jwtUtils;

    /**
     * Metodo para hacr el filtrado de una solicitud HTTP dependiendo del metodo de encabezado y tol en el token
     * @param request
     * @param response
     * @param filterChain
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        //Configuracion de cabeceras para CORS
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.addHeader("Access-Control-Allow-Headers", "Origin, Accept, Content-Type, Authorization");

        if (request.getMethod().equals("OPTIONS")) {
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            //Obtener la URI de la petición que se está realizando
            String requestURI = request.getRequestURI();
            //Se obtiene el token de la petición del encabezado del mensaje HTTP
            String token = getToken(request);
            boolean error = false;
            try {
                if (requestURI.startsWith("/api/account")) {
                    error = validarToken(token, Rol.CLIENTE) && validarToken(token, Rol.ADMIN);
                } else if (requestURI.startsWith("/api/admin")) {
                    error = validarToken(token, Rol.ADMIN);
                } else if (requestURI.startsWith("/api/client")) {
                    error = validarToken(token, Rol.CLIENTE);
                } else {
                    error = false;
                }
                //Agregar la validación para las peticiones que sean de los administradores

                if (error) {
                    crearRespuestaError("No tiene permisos para acceder a este recurso",
                            HttpServletResponse.SC_FORBIDDEN, response);
                }
            } catch (MalformedJwtException | SignatureException e) {
                crearRespuestaError("El token es incorrecto",
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR, response);
            } catch (ExpiredJwtException e) {
                crearRespuestaError("El token está vencido",
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR, response);
            } catch (Exception e) {
                crearRespuestaError(e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        response);
            }
            //Si no hay errores se continúa con la petición
            if (!error) {
                filterChain.doFilter(request, response);
            }
        }
    }

    /**
     *  Método para obetener el token en una solicitud HTTP
     * @param req
     * @return
     */
    private String getToken(HttpServletRequest req) {
        String header = req.getHeader("Authorization");
        return header != null && header.startsWith("Bearer ") ? header.replace("Bearer ", "") :
                null;
    }

    /**
     *  Método para crear un mensaje de respuesta en caso de existir algun error correspondiente al token
     * @param mensaje
     * @param codigoError
     * @param response
     * @throws IOException
     */
    private void crearRespuestaError(String mensaje, int codigoError, HttpServletResponse response)
            throws IOException {
        MessageDTO<String> dto = new MessageDTO<>(true, mensaje);
        response.setContentType("application/json");
        response.setStatus(codigoError);
        response.getWriter().write(new ObjectMapper().writeValueAsString(dto));
        response.getWriter().flush();
        response.getWriter().close();
    }

    /**
     * Método usado para validar que el rol del token sea el mismo que el rol de parametro
     * @param token
     * @param role
     * @return
     */
    private boolean validarToken(String token, Rol role) {
        boolean error = true;
        if (token != null) {
            Jws<Claims> jws = jwtUtils.parseJwt(token);
            if (Rol.valueOf(jws.getPayload().get("rol").toString()) == role) {
                error = false;
            }
        }
        return error;
    }
}
