package config;

/**
 * Centraliza constantes del juego.
 * Facilita ajuste de dificultad, balance y tiempo sin tocar lógica.
 * SOLID: S (Responsabilidad única), O (Abierto/cerrado para extensión).
 */
public class GameConstants {
    // Oleadas
    public static final long DURACION_OLEADA_MS = 60_000L;
    public static final long DURACION_CINEMÁTICA_MS = 2_500L;

    // Spawn de enemigos
    public static final long INTERVALO_SPAWN_BASE_MS = 1_500L;
    public static final long REDUCCION_INTERVALO_POR_RONDA_MS = 100L;
    public static final long INTERVALO_SPAWN_MIN_MS = 800L;

    // Cooldowns
    public static final long COOLDOWN_DAÑO_ENEMIGO_MS = 500L;
    public static final long COOLDOWN_DAÑO_RECIBIDO_MS = 250L;
    public static final long INTERVALO_FRAME_ANIMACION_MS = 150L;

    // Tienda
    public static final int COSTO_REPETIR_TRAGAPERRAS = 10;
    public static final int COSTO_BASE_COMODIN = 70;
    public static final int INCREMENTO_COSTO_COMODIN = 5;

    // Probabilidades tragaperras
    public static final double PROB_BOTIQUIN_TRAGAPERRAS = 0.10;
    public static final double PROB_NADA_TRAGAPERRAS = 0.75;
    public static final double PROB_ARMA_TRAGAPERRAS = 0.15;

    // Spawn de botiquín por oleada
    public static final double PROB_BOTIQUIN_ENEMIGO = 0.05;

    // Spawn de gusanos en tienda
    public static final int GUSANOS_MAX_TIENDA = 4;
    public static final int DISTANCIA_SPAWN_GUSANO_MIN = 300;
    public static final int DISTANCIA_SPAWN_GUSANO_MAX = 450;

    // Distancias
    public static final int DISTANCIA_SPAWN_ENEMIGO_MIN = 500;
    public static final int DISTANCIA_SPAWN_ENEMIGO_MAX = 700;

    // Player
    public static final int VIDA_BASE = 100;
    public static final int VELOCIDAD_BASE = 4;
    public static final int DAÑO_BASE = 1;

    // Enemigos
    public static final double PROB_ENEMIGO_NORMAL = 0.40;
    public static final double PROB_ENEMIGO_VELOZ = 0.20;
    public static final double PROB_ENEMIGO_TANQUE = 0.20;
    public static final double PROB_ENEMIGO_EXPLOSIVO = 0.20;

    // Personajes - Desbloqueo por tienda
    public static final int TIENDA_DESBLOQUEA_GATO = 1;
    public static final int TIENDA_DESBLOQUEA_RANA = 2;
    public static final int TIENDA_DESBLOQUEA_COLLIE = 3;

    private GameConstants() {}
}
