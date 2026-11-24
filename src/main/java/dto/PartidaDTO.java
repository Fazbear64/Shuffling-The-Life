package dto;

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

    public int getRondaActual() { return rondaActual; }
    public void setRondaActual(int rondaActual) { this.rondaActual = rondaActual; }

    public int getTiradasTragaperras() { return tiradasTragaperras; }
    public void setTiradasTragaperras(int tiradasTragaperras) { this.tiradasTragaperras = tiradasTragaperras; }

    public long getMaxTiempoSobrevividoMs() { return maxTiempoSobrevividoMs; }
    public void setMaxTiempoSobrevividoMs(long maxTiempoSobrevividoMs) { this.maxTiempoSobrevividoMs = maxTiempoSobrevividoMs; }

    public long getTiempoActualOleadaMs() { return tiempoActualOleadaMs; }
    public void setTiempoActualOleadaMs(long tiempoActualOleadaMs) { this.tiempoActualOleadaMs = tiempoActualOleadaMs; }

    public String getEstadoActual() { return estadoActual; }
    public void setEstadoActual(String estadoActual) { this.estadoActual = estadoActual; }

    public PlayerDTO getJugador() { return jugador; }
    public void setJugador(PlayerDTO jugador) { this.jugador = jugador; }
}