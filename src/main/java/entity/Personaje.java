package entity;

public class Personaje {
    // IDs/nombres de los personajes
    public static final String PERSONAJE_MAPACHE = "mapache";
    public static final String PERSONAJE_GATO = "gato";
    public static final String PERSONAJE_RANA = "rana";
    public static final String PERSONAJE_COLLIE = "collie";

    // Estado de desbloqueo (persistente entre partidas)
    private boolean desbloqueado;

    // Bonificaciones permanentes (se aplican al iniciar cada nueva partida)
    private float bonificacionVida;
    private float bonificacionDanio;
    private float bonificacionVelocidad;

    // Constructor
    public Personaje(String nombre) {
        this.desbloqueado = false;
        this.bonificacionVida = 0.0f;
        this.bonificacionDanio = 0.0f;
        this.bonificacionVelocidad = 0.0f;

        // Asignar bonificaciones basadas en el tipo de personaje
        asignarBonificaciones(nombre);
    }

    // Método para asignar bonificaciones según el personaje
    private void asignarBonificaciones(String nombre) {
        switch (nombre.toLowerCase()) {
            case PERSONAJE_MAPACHE:
                // Confiado, callejero o ágil; ideal para buen daño o ritmo equilibrado
                this.bonificacionVida = 0.10f;      // +10% vida
                this.bonificacionDanio = 0.15f;     // +15% daño
                this.bonificacionVelocidad = 0.10f; // +10% velocidad
                this.desbloqueado = true;            // Empieza desbloqueado por defecto
                break;

            case PERSONAJE_GATO:
                // Sigiloso, rápido o de estilo ágil/preciso
                this.bonificacionVida = 0.05f;      // +5% vida
                this.bonificacionDanio = 0.05f;     // +5% daño
                this.bonificacionVelocidad = 0.20f; // +20% velocidad
                this.desbloqueado = false;           // Debe desbloquearse
                break;

            case PERSONAJE_RANA:
                // Golpeador o de combate cercano/contundente, muy expresivo y caótico
                this.bonificacionVida = 0.30f;      // +30% vida (tanque)
                this.bonificacionDanio = 0.20f;     // +20% daño cuerpo a cuerpo
                this.bonificacionVelocidad = -0.10f;// -10% velocidad (más pesado)
                this.desbloqueado = false;           // Debe desbloquearse
                break;

            case PERSONAJE_COLLIE:
                // Border Collie: perro pastor inteligente, enérgico y ágil
                // Vibe: Rápido, inteligente, equilibrado - buen daño y velocidad
                this.bonificacionVida = 0.10f;      // +10% vida (moderado)
                this.bonificacionDanio = 0.15f;     // +15% daño (atletismo)
                this.bonificacionVelocidad = 0.20f; // +20% velocidad (muy ágil/inteligente)
                this.desbloqueado = false;           // Debe desbloquearse (como el gato)
                break;

            default:
                // Personaje por defecto si no coincide ninguno
                this.bonificacionVida = 0.0f;
                this.bonificacionDanio = 0.0f;
                this.bonificacionVelocidad = 0.0f;
                break;
        }
    }

    // Getters y Setters
    public boolean isDesbloqueado() {
        return desbloqueado;
    }

    public void setDesbloqueado(boolean desbloqueado) {
        this.desbloqueado = desbloqueado;
    }

    public float getBonificacionVida() {
        return bonificacionVida;
    }

    public void setBonificacionVida(float bonificacionVida) {
        this.bonificacionVida = bonificacionVida;
    }

    public float getBonificacionDanio() {
        return bonificacionDanio;
    }

    public void setBonificacionDanio(float bonificacionDanio) {
        this.bonificacionDanio = bonificacionDanio;
    }

    public float getBonificacionVelocidad() {
        return bonificacionVelocidad;
    }

    public void setBonificacionVelocidad(float bonificacionVelocidad) {
        this.bonificacionVelocidad = bonificacionVelocidad;
    }

    // Método estático para obtener un personaje por nombre (útil para menú)
    public static Personaje crearPersonaje(String nombre) {
        return new Personaje(nombre);
    }

    // Método para verificar si el personaje está disponible para selección
    public static boolean estaDisponible(String nombre) {
        Personaje p = new Personaje(nombre);
        return p.isDesbloqueado();
    }

    public dto.PersonajeDTO toDTO() {
        return new dto.PersonajeDTO(
            toString(),
            desbloqueado,
            bonificacionVida,
            bonificacionDanio,
            bonificacionVelocidad
        );
    }
}