package dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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
}