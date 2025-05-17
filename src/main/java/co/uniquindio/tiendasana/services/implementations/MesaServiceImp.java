package co.uniquindio.tiendasana.services.implementations;

import co.uniquindio.tiendasana.dto.mesadtos.*;
import co.uniquindio.tiendasana.model.documents.Mesa;
import co.uniquindio.tiendasana.model.enums.EstadoMesa;
import co.uniquindio.tiendasana.model.enums.Localidad;
import co.uniquindio.tiendasana.repos.MesaRepo;
import co.uniquindio.tiendasana.services.interfaces.MesaService;

import org.springframework.stereotype.Service;
import co.uniquindio.tiendasana.utils.MesaConstantes;

import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class MesaServiceImp implements MesaService {

    private final MesaRepo mesaRepo;

    public MesaServiceImp(MesaRepo mesaRepo) {
        this.mesaRepo = mesaRepo;
    }

    @Override
    public MesaInfoDTO obtenerInfoMesa(String mesaId) throws Exception {
        try {
            Optional<Mesa> mesaObtenida= mesaRepo.obtenerPorId(mesaId);
            if (mesaObtenida.isEmpty()) {
                throw new Exception("Mesa no encontrada");
            }
            Mesa mesa=mesaObtenida.get();
            return new MesaInfoDTO(
                    mesa.getId(),
                    mesa.getNombre(),
                    mesa.getEstado(),
                    mesa.getLocalidad().getLocalidad(),
                    mesa.getPrecioReserva(),
                    mesa.getCapacidad(),
                    mesa.getImagen()
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ListaMesasDTO obtenerMesasCliente(int pagina) throws Exception {
        MesasTotalDTO paginaMesas = mesaRepo.obtenerMesas(pagina, MesaConstantes.ELEMENTOSPAGINA);
        System.out.println("Numero elem: "+MesaConstantes.ELEMENTOSPAGINA);
        List<Mesa> mesas=paginaMesas.mesas();
        List<MesaItemDTO> mesasItems = mesas.stream()
                .map(mesa -> new MesaItemDTO(
                        mesa.getId(),
                        mesa.getNombre(),
                        mesa.getEstado(),
                        mesa.getLocalidad().getLocalidad(),
                        mesa.getPrecioReserva(),
                        mesa.getCapacidad(),
                        mesa.getImagen()
                ))
                .collect(Collectors.toList());

        return new ListaMesasDTO(
                (int) Math.ceil((double) paginaMesas.totalMesas() / MesaConstantes.ELEMENTOSPAGINA),
                mesasItems
        );
    }

    @Override
    public Mesa obtenerMesa(String mesaId) throws Exception {
        Optional<Mesa> mesaObtenida= mesaRepo.obtenerPorId(mesaId);
        if (mesaObtenida.isEmpty()) {
            throw new Exception("Mesa no encontrada");
        }
        return mesaObtenida.get();
    }

    @Override
    public void cambiarEstadoMesa(String mesaId, String estado) throws Exception {
        Mesa mesa= obtenerMesa(mesaId);
        mesa.setEstado(EstadoMesa.fromEstado(estado));
        mesaRepo.actualizar(mesa);
        System.out.println("Se ha actualizado la mesa correctamente");

    }

    @Override
    public ListaMesasDTO filtrarMesas(FiltroMesaDTO filtroMesaDTO) throws Exception {
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

        return new ListaMesasDTO(totalPaginas, mesasItems);
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