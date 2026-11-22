package dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PartidaDTO {
    private int rondaActual;
    private int tiradasTragaperras;
    private long maxTiempoSobrevividoMs;
    private long tiempoActualOleadaMs;
    private String estadoActual;
    private PlayerDTO jugador;

    public PartidaDTO() {
    }

    public PartidaDTO(int rondaActual, int tiradasTragaperras, long maxTiempoSobrevividoMs,
                     long tiempoActualOleadaMs, String estadoActual, PlayerDTO jugador) {
        this.rondaActual = rondaActual;
        this.tiradasTragaperras = tiradasTragaperras;
        this.maxTiempoSobrevividoMs = maxTiempoSobrevividoMs;
        this.tiempoActualOleadaMs = tiempoActualOleadaMs;
        this.estadoActual = estadoActual;
        this.jugador = jugador;
    }
}