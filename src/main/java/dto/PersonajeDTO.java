package dto;

public class PersonajeDTO {
    private String nombre;
    private boolean desbloqueado;
    private float bonificacionVida;
    private float bonificacionDanio;
    private float bonificacionVelocidad;

    public PersonajeDTO() {
    }

    public PersonajeDTO(String nombre, boolean desbloqueado, float bonificacionVida,
                       float bonificacionDanio, float bonificacionVelocidad) {
        this.nombre = nombre;
        this.desbloqueado = desbloqueado;
        this.bonificacionVida = bonificacionVida;
        this.bonificacionDanio = bonificacionDanio;
        this.bonificacionVelocidad = bonificacionVelocidad;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public boolean isDesbloqueado() { return desbloqueado; }
    public void setDesbloqueado(boolean desbloqueado) { this.desbloqueado = desbloqueado; }

    public float getBonificacionVida() { return bonificacionVida; }
    public void setBonificacionVida(float bonificacionVida) { this.bonificacionVida = bonificacionVida; }

    public float getBonificacionDanio() { return bonificacionDanio; }
    public void setBonificacionDanio(float bonificacionDanio) { this.bonificacionDanio = bonificacionDanio; }

    public float getBonificacionVelocidad() { return bonificacionVelocidad; }
    public void setBonificacionVelocidad(float bonificacionVelocidad) { this.bonificacionVelocidad = bonificacionVelocidad; }
}