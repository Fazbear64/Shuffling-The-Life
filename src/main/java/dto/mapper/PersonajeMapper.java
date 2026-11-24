package dto.mapper;

import entity.Personaje;
import dto.PersonajeDTO;

/**
 * Mapper: Entidad Personaje ↔ DTO PersonajeDTO
 * SOLID: I (Segregación de interfaces - solo mapea lo necesario).
 */
public class PersonajeMapper {
    private PersonajeMapper() {}

    public static PersonajeDTO toDTO(Personaje personaje) {
        if (personaje == null) return null;

        return new PersonajeDTO(
            personaje.toString(),
            personaje.isDesbloqueado(),
            personaje.getBonificacionVida(),
            personaje.getBonificacionDanio(),
            personaje.getBonificacionVelocidad()
        );
    }

    public static Personaje fromDTO(PersonajeDTO dto) {
        if (dto == null) return null;

        Personaje personaje = new Personaje(dto.getNombre());
        personaje.setDesbloqueado(dto.isDesbloqueado());
        return personaje;
    }
}
