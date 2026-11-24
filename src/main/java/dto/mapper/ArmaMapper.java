package dto.mapper;

import entity.Arma;
import entity.TipoArma;
import dto.ArmaDTO;

/**
 * Mapper: Entidad Arma ↔ DTO ArmaDTO
 * SOLID: S (Responsabilidad única - solo mapea Arma).
 */
public class ArmaMapper {
    private ArmaMapper() {}

    public static ArmaDTO toDTO(Arma arma) {
        if (arma == null) return null;

        return new ArmaDTO(
            arma.getTipo().name(),
            arma.getNombre(),
            arma.getMultiplicadorDaño(),
            arma.getDañoBonus(),
            arma.getCooldownMs(),
            arma.getProyectilesPorDisparo(),
            arma.getDispersion(),
            arma.getVelocidadProyectil(),
            arma.getProbabilidadAparicion()
        );
    }
}
