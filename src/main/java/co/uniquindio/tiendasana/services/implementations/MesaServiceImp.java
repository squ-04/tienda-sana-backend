package co.uniquindio.tiendasana.services.implementations;

import co.uniquindio.tiendasana.dto.mesadtos.FiltroMesaDTO;
import co.uniquindio.tiendasana.dto.mesadtos.ListaMesas;
import co.uniquindio.tiendasana.dto.mesadtos.MesaInfoDTO;
import co.uniquindio.tiendasana.dto.mesadtos.MesaItemDTO;
import co.uniquindio.tiendasana.model.documents.Mesa;
import co.uniquindio.tiendasana.repos.MesaRepo;
import co.uniquindio.tiendasana.services.interfaces.MesaService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public List<MesaItemDTO> filtrarMesas(FiltroMesaDTO filtroMesaDTO) throws Exception {

        Predicate<Mesa> filtro = mesa -> {
            boolean matches = true;

            if (filtroMesaDTO.nombre() != null) {
                matches &= mesa.getNombre().toLowerCase().contains(filtroMesaDTO.nombre().toLowerCase());
            }
            if (filtroMesaDTO.capacidad() != 0) {
                matches &= mesa.getCapacidad() >= filtroMesaDTO.capacidad();
            }
            if (filtroMesaDTO.localidad() != null) {
                matches &= mesa.getLocalidad().toLowerCase().contains(filtroMesaDTO.localidad().toLowerCase());
            }

            return matches;
        };

        List<Mesa> mesasFiltradas = mesaRepo.filtrar(filtro);

        int pageSize = 9;
        int pageNumber = filtroMesaDTO.pagina();
        int startItem = pageNumber * pageSize;
        int endItem = Math.min(startItem + pageSize, mesasFiltradas.size());

        List<Mesa> paginatedList = mesasFiltradas.subList(startItem, endItem);

        return paginatedList.stream()
                .map(mesa -> new MesaItemDTO(
                        mesa.getId(),
                        mesa.getNombre(),
                        mesa.getEstado(),
                        mesa.getLocalidad(),
                        mesa.getPrecioReserva(),
                        mesa.getCapacidad(),
                        mesa.getImagen()
                ))
                .collect(Collectors.toList());
    }
}