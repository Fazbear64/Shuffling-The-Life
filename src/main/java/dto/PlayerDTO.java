package dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlayerDTO {
    private int x;
    private int y;
    private int vidaActual;
    private int vidaMax;
    private int dañoBase;
    private int velocidadMovimiento;
    private int monedas;
    private String nombrePersonaje;
    private ArmaDTO arma;

    public PlayerDTO() {
    }

    public PlayerDTO(int x, int y, int vidaActual, int vidaMax, int dañoBase,
                     int velocidadMovimiento, int monedas, String nombrePersonaje) {
        this(x, y, vidaActual, vidaMax, dañoBase, velocidadMovimiento, monedas, nombrePersonaje, null);
    }

    public PlayerDTO(int x, int y, int vidaActual, int vidaMax, int dañoBase,
                     int velocidadMovimiento, int monedas, String nombrePersonaje, ArmaDTO arma) {
        this.x = x;
        this.y = y;
        this.vidaActual = vidaActual;
        this.vidaMax = vidaMax;
        this.dañoBase = dañoBase;
        this.velocidadMovimiento = velocidadMovimiento;
        this.monedas = monedas;
        this.nombrePersonaje = nombrePersonaje;
        this.arma = arma;
    }
}