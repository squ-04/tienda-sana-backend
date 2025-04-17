package co.uniquindio.tiendasana.services.implementations;

import co.uniquindio.tiendasana.config.JWTUtils;
import co.uniquindio.tiendasana.dto.EmailDTO;
import co.uniquindio.tiendasana.dto.TokenDTO;
import co.uniquindio.tiendasana.repos.CuentaRepo;
import com.google.api.services.sheets.v4.Sheets;
import lombok.AllArgsConstructor;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import co.uniquindio.tiendasana.model.documents.Cuenta;
import co.uniquindio.tiendasana.model.enums.EstadoCuenta;
import co.uniquindio.tiendasana.model.enums.Rol;
import co.uniquindio.tiendasana.model.vo.Usuario;
import co.uniquindio.tiendasana.model.vo.CodigoValidacion;
import co.uniquindio.tiendasana.dto.cuentadtos.*;

import co.uniquindio.tiendasana.exceptions.*;
import co.uniquindio.tiendasana.services.interfaces.CuentaService;
import co.uniquindio.tiendasana.services.interfaces.EmailService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
@AllArgsConstructor
public class CuentaServiceImp implements CuentaService {

    private final CuentaRepo cuentaRepo;
    private final EmailService emailService;
    private JWTUtils jwtUtils;

    @Override
    public String crearCuenta(CrearCuentaDTO cuentaDTO) throws Exception {

        List<Cuenta> cuentasObtenidas =
                cuentaRepo.obtenerPorDniOEmail(cuentaDTO.dni(), cuentaDTO.email());

        if (!cuentasObtenidas.isEmpty()) {
            throw new Exception("Ya existe una cuenta con ese correo o dni");
        }

        // Encripta la contraseña
        String contraseniaEncriptada = encriptarContrasenia(cuentaDTO.contrasenia());

        // Genera un código de validación
        String codigoValidacion = generarCodigoValidacion();
        String codigoValidacionContrasenia = generarCodigoValidacion();

        // Crea la cuenta
        Cuenta cuenta = Cuenta.builder()
                .email(cuentaDTO.email())
                .contrasenia(contraseniaEncriptada)
                .rol(Rol.CLIENTE) // Asignación por defecto
                .estado(EstadoCuenta.INACTIVA)
                .fechaRegistro(LocalDateTime.now())
                .usuario(Usuario.builder()
                        .dni(cuentaDTO.dni())
                        .nombre(cuentaDTO.nombre())
                        .telefono(cuentaDTO.telefono())
                        .direccion(cuentaDTO.direccion())
                        .build())
                .build();
        //Se cambio el codigo de contraseña por el del registro que es el que se tiene que colocar
        cuenta.setCodigoValidacionRegistro(new CodigoValidacion(LocalDateTime.now(), codigoValidacion));
        cuenta.setCodigoValidacionContrasenia(new CodigoValidacion(LocalDateTime.now(), codigoValidacionContrasenia));


        // Envía el correo de validación
        String subject = "Bienvenido a Tienda Sana: activa tu cuenta";
        String body = "Tu código de verificación es: " + codigoValidacion + ". Tienes 15 minutos para activarla.";

        emailService.sendEmail(new EmailDTO(subject, body, cuenta.getEmail()));
        cuentaRepo.guardar(cuenta);
        // Retorna un identificador temporal
        return cuenta.getEmail();
    }

    @Override
    public String actualizarCuenta(ActualizarCuentaDTO cuentaDTO) throws Exception {
        Cuenta cuenta = obtenerCuentaPorEmail(cuentaDTO.email());
        cuenta.getUsuario().setNombre(cuentaDTO.nombre());
        cuenta.getUsuario().setTelefono(cuentaDTO.telefono());
        cuenta.getUsuario().setDireccion(cuentaDTO.direccion());
        cuenta.setContrasenia(new BCryptPasswordEncoder().encode(cuentaDTO.contrasenia()));
        cuentaRepo.actualizar(cuenta);
        return "Actualizacion realizada con exito";
    }

    @Override
    public String eliminarCuenta(String email) throws Exception {
        Cuenta cuenta = obtenerCuentaPorEmail(email);
        cuenta.setEstado(EstadoCuenta.ELIMINADA);
        cuentaRepo.actualizar(cuenta);
        return "La cuenta ha sido eliminada " + email;
    }

    @Override
    public InfoCuentaDTO obtenerInfoCuenta(String email) throws Exception {
        Cuenta cuenta = cuentaRepo.obtenerPorEmail(email).get();
        return new InfoCuentaDTO(
                cuenta.getEmail(),
                cuenta.getUsuario().getDni(),
                cuenta.getUsuario().getNombre(),
                cuenta.getUsuario().getTelefono(),
                cuenta.getUsuario().getTelefono()
        );
    }


    @Override
    public Cuenta obtenerCuentaPorEmail(String email) throws Exception {
        Optional<Cuenta> cuentaObtenida = cuentaRepo.obtenerPorEmail(email);
        if (cuentaObtenida.isEmpty()) {
            throw new Exception("Cuenta no encontrada");
        }
        return cuentaObtenida.get();
    }

    @Override
    public String enviarCodigoRecuperacion(String email) throws Exception {
        Cuenta cuenta = cuentaRepo.obtenerPorEmail(email).get();
        String codigoRecuperacion = generarCodigoValidacion();
        String asunto = "Codigo de recuperacion";
        String cuerpo = "Hey! has pedido el codigo de recuperacion de tu contrasenia por tu cuenta de TiendaSana\n" +
                "este es tu codigo de recuperacion: " + codigoRecuperacion + "\nEste codigo durara 15 minutos.";
        cuenta.setCodigoValidacionContrasenia(new CodigoValidacion(LocalDateTime.now(), codigoRecuperacion));
        cuentaRepo.actualizar(cuenta);
        emailService.sendEmail(new EmailDTO(asunto, cuerpo, cuenta.getEmail()));
        return cuenta.getEmail();
    }

    @Override
    public String cambiarContrasenia(CambiarContraseniaDTO dto) throws Exception {
        Optional<Cuenta> cuentaOptional = cuentaRepo.obtenerPorEmail(dto.email());
        if (cuentaOptional.isEmpty()) {
            throw new ResourceNotFoundException("This email is not registered");
        }
        Cuenta cuenta = cuentaOptional.get();

        CodigoValidacion codigoValidacionContrasenia = cuenta.getCodigoValidacionContrasenia();
        if (codigoValidacionContrasenia != null) {
            if (codigoValidacionContrasenia.getCodigo().equals(dto.codigoVerificacion())) {
                if (codigoValidacionContrasenia.getFechaCreacion().plusMinutes(15).isAfter(LocalDateTime.now())) {
                    cuenta.setContrasenia(encriptarContrasenia(dto.nuevaContrasenia()));
                    cuentaRepo.actualizar(cuenta);
                } else {
                    cuenta.setCodigoValidacionContrasenia(null);
                    cuentaRepo.actualizar(cuenta);
                    throw new Exception("Este codigo de verificacion ha experido");
                }
            } else {
                throw new Exception("Este codigo de verififcacion es incorrecto");
            }
        }
        return cuenta.getEmail();
    }

    @Override
    public String validarCodigoRegistro(ActivarCuentaDTO activarCuentaDTO) throws Exception {
        Cuenta cuenta = cuentaRepo.obtenerPorEmail(activarCuentaDTO.email()).get();
        CodigoValidacion codigoValidacionRegistro = cuenta.getCodigoValidacionRegistro();

        if (codigoValidacionRegistro != null) {
            if (codigoValidacionRegistro.getCodigo().equals(activarCuentaDTO.codigoVerificacionRegistro())) {
                if (codigoValidacionRegistro.getFechaCreacion().plusMinutes(15).isAfter(LocalDateTime.now())) {
                    cuenta.setEstado(EstadoCuenta.ACTIVA);
                    cuentaRepo.actualizar(cuenta);
                    //TODO tener en cuenta que antes aqui habia el codigo del cupon
                } else {
                    throw new Exception("Registration validation code has expired");
                }
            } else {
                throw new Exception("This registration validation code is incorrect");
            }
        }
        return "La cuenta ha sido verificada exitosamente";
    }

    @Override
    public String reenviarCodigoRegistro(String email) throws Exception {
        Cuenta cuenta = cuentaRepo.obtenerPorEmail(email).get();
        CodigoValidacion codigoValidacionReenviado = new CodigoValidacion(LocalDateTime.now(), generarCodigoValidacion());
        cuenta.setCodigoValidacionRegistro(codigoValidacionReenviado);
        String asunto = "Hey! este es tu NUEVO codigo de activacion para tu cuenta de Tienda Sana";
        String cuerpo = "Tu codigo de activacion es " + codigoValidacionReenviado.getCodigo() + " tienes 15 minutos para realizar la activacion " +
                "de tu cuenta de Tienda Sana.";
        emailService.sendEmail(new EmailDTO(asunto, cuerpo, cuenta.getEmail()));
        cuentaRepo.actualizar(cuenta);
        return cuenta.getEmail();
    }

    @Override
    public TokenDTO login(LoginDTO loginDTO) throws Exception {
        Cuenta cuenta = obtenerCuentaPorEmail(loginDTO.email());
        if (cuenta.getEstado() == EstadoCuenta.ELIMINADA) {
            throw new Exception("No existe una cuenta con este correo");
        }
        if (cuenta.getEstado() == EstadoCuenta.INACTIVA) {
            throw new Exception("La cuenta con este correo no ha sido activada ");
        }
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        if (!passwordEncoder.matches(loginDTO.contrasenia(),cuenta.getContrasenia())) {
            throw new Exception("Contraseña incorrecta");
        }
        Map<String, Object> map = buildClaims(cuenta);

        return new TokenDTO(jwtUtils.generarToken(cuenta.getEmail(), map));
    }

    private Map<String, Object> buildClaims(Cuenta cuenta) {
        return Map.of(
                "rol", cuenta.getRol(),
                "nombre", cuenta.getUsuario().getNombre(),
                "estado", cuenta.getEstado(),
                "email", cuenta.getEmail()
        );
    }

    @Override
    public TokenDTO refresh(Map<String, Object> claims) {
        return null;
    }

    private String generarCodigoValidacion() {
        String alfabeto = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder codigo = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int idx = (int) (Math.random() * alfabeto.length());
            codigo.append(alfabeto.charAt(idx));
        }
        return codigo.toString();
    }

    private String encriptarContrasenia(String Contrasenia) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.encode(Contrasenia);
    }
}
