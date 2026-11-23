package entity;

public enum ID {
    PLAYER,          // Jugador principal
    ENEMIGO,         // Enemigo normal
    ENEMIGO_TANQUE,  // Enemigo tanque
    ENEMIGO_VELOZ,   // Enemigo veloz
    ENEMIGO_EXPLOSIVO, // Enemigo explosivo (explota al morir)
    EXPLOSION,       // Explosión de enemigo explosivo
    JEFE,            // Jefe gusano
    ARMA,            // Arma obtenida
    TAROT,           // Carta de tarot
    MEJORA,          // Carta de mejora
    TRAGAPERAS,      // Estado de tragaperras
    PARTIDA,         // Estado de partida
    MONEDA           // Divisa / moneda dejada por enemigos
}