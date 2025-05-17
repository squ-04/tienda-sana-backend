package co.uniquindio.tiendasana.services.implementations;

import co.uniquindio.tiendasana.dto.mesadtos.FiltroMesaDTO;
import co.uniquindio.tiendasana.dto.mesadtos.ListaMesas;
import co.uniquindio.tiendasana.dto.mesadtos.MesaInfoDTO;
import co.uniquindio.tiendasana.dto.mesadtos.MesaItemDTO;
import co.uniquindio.tiendasana.dto.productodtos.ListaProductos;
import co.uniquindio.tiendasana.model.documents.Mesa;
import co.uniquindio.tiendasana.model.enums.Localidad;
import co.uniquindio.tiendasana.repos.MesaRepo;
import co.uniquindio.tiendasana.services.interfaces.MesaService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class MesaServiceImp implements MesaService {

    private final MesaRepo mesaRepo;

    public MesaServiceImp(MesaRepo mesaRepo) {
        this.mesaRepo = mesaRepo;
    }

    @Override
    public MesaInfoDTO obtenerInfoMesa(String mesaId) throws Exception {
        return null;
    }

    @Override
    public ListaMesas obtenerMesasCliente(int pagina) throws Exception {
        return null;
    }

    @Override
    public Mesa obtenerMesa(String mesaId) throws Exception {
        return null;
    }

    @Override
    public void cambiarEstadoMesa(String mesaId, String estado) throws Exception {

    }

    @Override
    public ListaMesas filtrarMesas(FiltroMesaDTO filtroMesaDTO) throws Exception {
        boolean filtroVacio = (filtroMesaDTO.nombre() == null || filtroMesaDTO.nombre().isEmpty()) &&
                (filtroMesaDTO.localidad() == null || filtroMesaDTO.localidad().isEmpty()) &&
                filtroMesaDTO.capacidad() == 0;

        if (filtroVacio) {
            throw new Exception("Debe proporcionar al menos un criterio de filtro.");
        }

        Predicate<Mesa> filtro = mesa -> {
            boolean matches = true;

            if (filtroMesaDTO.nombre() != null && !filtroMesaDTO.nombre().isEmpty()) {
                matches &= (mesa.getNombre() != null &&
                        mesa.getNombre().toLowerCase().contains(filtroMesaDTO.nombre().toLowerCase()));
            }

            if (filtroMesaDTO.capacidad() != 0) {
                matches &= mesa.getCapacidad() >= filtroMesaDTO.capacidad();
            }

            if (filtroMesaDTO.localidad() != null && !filtroMesaDTO.localidad().isEmpty()) {
                matches &= (mesa.getLocalidad() != null &&
                        mesa.getLocalidad().getLocalidad() != null && // Chequeo para el String de localidad
                        mesa.getLocalidad().getLocalidad().toLowerCase().contains(filtroMesaDTO.localidad().toLowerCase()));
            }
            return matches;
        };

        List<Mesa> mesasFiltradas = mesaRepo.filtrar(filtro);

        int pageSize = 9;
        int totalItems = mesasFiltradas.size();
        int totalPaginas = (totalItems == 0) ? 0 : (int) Math.ceil((double) totalItems / pageSize);

        int pageNumber = filtroMesaDTO.pagina(); // Asumir 0-indexado.
        if (pageNumber < 0) {
            pageNumber = 0;
        }
        
        List<Mesa> paginatedList;
        if (totalItems == 0 || pageNumber * pageSize >= totalItems) {
            // Si no hay ítems o la página solicitada está fuera de rango (demasiado alta)
            paginatedList = Collections.emptyList();
        } else {
            int startItem = pageNumber * pageSize;
            int endItem = Math.min(startItem + pageSize, totalItems);
            paginatedList = mesasFiltradas.subList(startItem, endItem);
        }

        // Mapear a MesaItemDTO
        List<MesaItemDTO> mesasItems = paginatedList.stream()
                .map(mesa -> new MesaItemDTO(
                        mesa.getId(),
                        mesa.getNombre(),
                        mesa.getEstado(),
                        (mesa.getLocalidad() != null ? mesa.getLocalidad().getLocalidad() : null), // Manejo de nulidad también en el mapeo
                        mesa.getPrecioReserva(),
                        mesa.getCapacidad(),
                        mesa.getImagen()
                ))
                .collect(Collectors.toList());

        return new ListaMesas(totalPaginas, mesasItems);
    }

    @Override
    public List<Localidad> listarLocalidades() throws Exception {
        List<Localidad> localidades= Arrays.asList(Localidad.values());
        if (localidades.isEmpty()){
            throw new Exception("No existen localidades para las mesas");

        }
        return localidades;
    }
}