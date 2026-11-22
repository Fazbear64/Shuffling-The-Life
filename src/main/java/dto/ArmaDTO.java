package dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ArmaDTO {
    private String tipo;
    private String nombre;
    private float multiplicadorDaño;
    private int dañoBonus;
    private long cooldownMs;
    private int proyectilesPorDisparo;
    private double dispersion;
    private float velocidadProyectil;
    private double probabilidadAparicion;

    public ArmaDTO() {
    }

    public ArmaDTO(String tipo, String nombre, float multiplicadorDaño, int dañoBonus,
                   long cooldownMs, int proyectilesPorDisparo, double dispersion,
                   float velocidadProyectil, double probabilidadAparicion) {
        this.tipo = tipo;
        this.nombre = nombre;
        this.multiplicadorDaño = multiplicadorDaño;
        this.dañoBonus = dañoBonus;
        this.cooldownMs = cooldownMs;
        this.proyectilesPorDisparo = proyectilesPorDisparo;
        this.dispersion = dispersion;
        this.velocidadProyectil = velocidadProyectil;
        this.probabilidadAparicion = probabilidadAparicion;
    }
}