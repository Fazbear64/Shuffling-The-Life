package dto.mapper;

import entity.Player;
import entity.Arma;
import dto.PlayerDTO;
import dto.ArmaDTO;

/**
 * Mapper: Entidad Player ↔ DTO PlayerDTO
 * Separa representación de dominio de transferencia de datos.
 * SOLID: D (Inversión de dependencias - capas no dependen de DTOs).
 */
public class PlayerMapper {
    private PlayerMapper() {}

    public static PlayerDTO toDTO(Player player) {
        if (player == null) return null;

        // Usar el método toDTO() que ya existe en Player
        return player.toDTO();
    }

    public static PlayerDTO toDTO(Player player, String nombrePersonaje) {
        if (player == null) return null;

        Arma arma = player.getArmaEquipada();
        ArmaDTO armaDTO = arma != null ? ArmaMapper.toDTO(arma) : null;

        return new PlayerDTO(
            player.getX(),
            player.getY(),
            player.getVidaActual(),
            player.getVidaMax(),
            player.getDañoBase(),
            player.getVelocidadMovimiento(),
            player.getMonedas(),
            nombrePersonaje,
            armaDTO
        );
    }
}

