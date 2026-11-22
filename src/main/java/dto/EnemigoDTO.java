package dto;

import entity.ID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EnemigoDTO {
    private int x;
    private int y;
    private ID id;
    private int vida;
    private int vidaMax;
    private int daño;
    private float velocidad;
    private int ancho;
    private int alto;

    public EnemigoDTO() {
    }

    public EnemigoDTO(int x, int y, ID id, int vida, int vidaMax, int daño, float velocidad, int ancho, int alto) {
        this.x = x;
        this.y = y;
        this.id = id;
        this.vida = vida;
        this.vidaMax = vidaMax;
        this.daño = daño;
        this.velocidad = velocidad;
        this.ancho = ancho;
        this.alto = alto;
    }
}