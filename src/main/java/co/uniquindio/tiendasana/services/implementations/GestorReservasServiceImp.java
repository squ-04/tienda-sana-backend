package co.uniquindio.tiendasana.services.implementations;

import co.uniquindio.tiendasana.dto.MesaDTO;
import co.uniquindio.tiendasana.dto.gestorReservasdtos.BorrarMesaGestorDTO;
import co.uniquindio.tiendasana.model.documents.GestorReservas;
import co.uniquindio.tiendasana.model.documents.Mesa;
import co.uniquindio.tiendasana.model.enums.EstadoMesa;
import co.uniquindio.tiendasana.repos.GestorReservasRepo;
import co.uniquindio.tiendasana.repos.MesaRepo;
import co.uniquindio.tiendasana.services.interfaces.GestorReservasService;
import co.uniquindio.tiendasana.services.interfaces.MesaService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class GestorReservasServiceImp implements GestorReservasService {

    private final GestorReservasRepo gestorReservasRepo;
    private final MesaRepo mesaRepo;

    /**
     * Metodo para crear el gestor de reservas
     * @param emailUsuario Email del usuario dueño del gestor de reservas
     * @return Id del gestor de reservas
     * @throws IOException Error al acceder a la base de datos
     */
    @Override
    public GestorReservas crearGestorReservas(String emailUsuario) throws IOException {
        Optional<GestorReservas> gestorReservasObtenido=
                gestorReservasRepo.obtenerPorEmail(emailUsuario);
        if (gestorReservasObtenido.isPresent()){
            return gestorReservasObtenido.get();
        } else {
            GestorReservas gestorReservas=GestorReservas.builder()
                    .id(UUID.randomUUID().toString())
                    .emailUsuario(emailUsuario)
                    .fecha(LocalDateTime.now())
                    .build();
            gestorReservasRepo.guardar(gestorReservas);
            System.out.println(gestorReservas);
            return gestorReservas;
        }
    }

    /**
     * Metodo que borra TODOS los elementos del gestor de reservas
     * @param emailUsuario Email del usuario dueño del gestor de reservas
     * @return Id del gestor de reservas
     * @throws IOException Error al acceder a la base de datos
     */
    @Override
    public String borrarGestorReservas(String emailUsuario) throws IOException {
        GestorReservas gestorReservas=obtenerGestorReservas(emailUsuario);
        List<Mesa> mesas=
            mesaRepo.obtenerPorGestorReserva(gestorReservas.getEmailUsuario());
        for (Mesa mesa : mesas) {
            mesa.setIdGestorReserva("-");
            mesaRepo.actualizar(mesa);
        }
        return gestorReservas.getId();
    }

    /**
     * Obtiene el gestor de reservas de usuario, si existe
     * @param emailUsuario Email del usuario de la cuenta
     * @return Gestor de reservas encontrado
     * @throws IOException Error al acceder a la base de datos
     */
    @Override
    public GestorReservas obtenerGestorReservas(String emailUsuario) throws IOException {
        Optional<GestorReservas> gestorReservas=gestorReservasRepo.obtenerPorEmail(emailUsuario);
        if(gestorReservas.isPresent()){
            return gestorReservas.get();
        }
        return null;
    }

    /**
     * Agrega una mesa al gestor de reservas, si el gestor de reservas no existe se crea uno
     * @param mesaDTO Datos de la mesa
     * @return Id de la mesa agregada
     * @throws IOException Error al acceder a la base de datos
     */
    @Override
    public String agregarMesaGestorReservas(MesaDTO mesaDTO) throws IOException {
        Mesa mesa=Mesa.builder()
                .id(mesaDTO.id())
                .nombre(mesaDTO.nombre())
                .estado(EstadoMesa.fromEstado(mesaDTO.estado()))
                .localidad(mesaDTO.localidad())
                .precioReserva(mesaDTO.precioReserva())
                .capacidad(mesaDTO.capacidad())
                .imagen(mesaDTO.imagen())
                .idReserva(mesaDTO.idReserva())
                .idGestorReserva(mesaDTO.idGestorReserva())
                .build();
        mesaRepo.actualizar(mesa);
        return mesa.getId();
    }

    /**
     * Elimina una mesa específica del gestor de reservas del usuario
     * @param mesaBorrarDTO Datos de la mesa a eliminar
     * @return Id de la mesa
     * @throws IOException Error al acceder a la base de datos
     */
    @Override
    public String borrarMesaGestorReservas(BorrarMesaGestorDTO mesaBorrarDTO) throws IOException {
        GestorReservas gestorReservas=obtenerGestorReservas(mesaBorrarDTO.emailUsuario());
        List<Mesa> mesas=
                mesaRepo.obtenerPorGestorReserva(gestorReservas.getEmailUsuario());
        Mesa mesaActualizada=null;
        for (Mesa mesa : mesas) {
            if (mesa.getId().equals(mesaBorrarDTO.mesaId())) {
                mesa.setIdGestorReserva("-");
                mesaRepo.actualizar(mesa);
                mesaActualizada=mesa;
                break;
            }
        }
        return mesaActualizada!=null?mesaActualizada.getId():"No se encontro la mesa";
    }

    /**
     * Lista todas las mesas dentro del gestor de reservas de usuario
     * @param emailUsuario Email del usuario al que pertenece el gestor de reservas
     * @return Mesas respectivas del gestor de reservas del usuario
     * @throws IOException Error al acceder a la base de datos
     */
    @Override
    public List<MesaDTO> obtenerMesasGestorReservas(String emailUsuario) throws IOException {
        GestorReservas gestorReservas=obtenerGestorReservas(emailUsuario);
        List<MesaDTO> datosMesas=new ArrayList<>();
        List<Mesa> mesas=
                mesaRepo.obtenerPorGestorReserva(gestorReservas.getEmailUsuario());
        for (Mesa mesa : mesas) {
            datosMesas.add(new MesaDTO(
                    mesa.getId(),
                    mesa.getNombre(),
                    mesa.getEstado(),
                    mesa.getLocalidad().toString(),
                    mesa.getPrecioReserva(),
                    mesa.getCapacidad(),
                    mesa.getImagen(),
                    mesa.getIdReserva(),
                    mesa.getIdGestorReserva()
                    )
            );
        }
        return datosMesas;
    }

}
