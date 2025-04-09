package co.uniquindio.tiendasana.services.implementations;

import co.uniquindio.tiendasana.config.JWTUtils;
import co.uniquindio.tiendasana.dto.EmailDTO;
import co.uniquindio.tiendasana.repos.CuentaRepo;
import com.google.api.services.sheets.v4.Sheets;
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
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CuentaServiceImp implements CuentaService {

    private final CuentaRepo cuentaRepo;
    private final EmailService emailService;

    @Override
    public String crearCuenta(CrearCuentaDTO cuentaDTO) throws Exception {

        List<Cuenta> cuentas = cuentaRepo.obtenerCuentas();

        // Verifica si ya existe una cuenta con ese correo
        for (Cuenta c : cuentas) {
            if (c.getEmail().equalsIgnoreCase(cuentaDTO.email()) ||
                    c.getUsuario().getDni().equals(cuentaDTO.dni())) {
                throw new Exception("Ya existe una cuenta con ese correo o dni");
            }
        }

        // Encripta la contraseña
        String contraseniaEncriptada = new BCryptPasswordEncoder().encode(cuentaDTO.contrasenia());

        // Genera un código de validación
        String codigoValidacion = generarCodigoValidacion();

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
                .codigoValidacionContrasenia(new CodigoValidacion(LocalDateTime.now(), codigoValidacion))
                .build();

        // TODO me falta crear el metodo de guardado/escritura dentro del Repo
        // TODO: guardarCuenta(cuenta);

        // Envía el correo de validación
        String subject = "Bienvenido a Tienda Sana: activa tu cuenta";
        String body = "Tu código de verificación es: " + codigoValidacion + ". Tienes 15 minutos para activarla.";

        emailService.sendEmail(new EmailDTO(subject, body, cuenta.getEmail()));

        // Retorna un identificador temporal
        return UUID.randomUUID().toString();
    }

    @Override
    public String actualizarCuenta(ActualizarCuentaDTO cuentaDTO) throws Exception {
        return "";
    }

    @Override
    public String eliminarCuenta(String id) throws Exception {
        return "";
    }

    @Override
    public InfoCuentaDTO obtenerInfoCuenta(String id) throws Exception {
        return null;
    }

    @Override
    public Cuenta obtenerCuenta(String id) throws Exception {
        return null;
    }

    @Override
    public Cuenta obtenerCuentaPorEmail(String email) throws Exception {
        return null;
    }

    @Override
    public String enviarCodigoRecuperacion(String email) throws Exception {
        return "";
    }

    @Override
    public String cambiarContrasenia(CambiarContraseniaDTO dto) throws Exception {
        return "";
    }

    @Override
    public String validarCodigoRegistro(ActivarCuentaDTO activarCuentaDTO) throws Exception {
        return "";
    }

    @Override
    public String reenviarCodigoRegistro(String email) throws Exception {
        return "";
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
}
