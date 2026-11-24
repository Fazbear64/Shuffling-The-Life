package dto;

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

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public float getMultiplicadorDaño() { return multiplicadorDaño; }
    public void setMultiplicadorDaño(float multiplicadorDaño) { this.multiplicadorDaño = multiplicadorDaño; }

    public int getDañoBonus() { return dañoBonus; }
    public void setDañoBonus(int dañoBonus) { this.dañoBonus = dañoBonus; }

    public long getCooldownMs() { return cooldownMs; }
    public void setCooldownMs(long cooldownMs) { this.cooldownMs = cooldownMs; }

    public int getProyectilesPorDisparo() { return proyectilesPorDisparo; }
    public void setProyectilesPorDisparo(int proyectilesPorDisparo) { this.proyectilesPorDisparo = proyectilesPorDisparo; }

    public double getDispersion() { return dispersion; }
    public void setDispersion(double dispersion) { this.dispersion = dispersion; }

    public float getVelocidadProyectil() { return velocidadProyectil; }
    public void setVelocidadProyectil(float velocidadProyectil) { this.velocidadProyectil = velocidadProyectil; }

    public double getProbabilidadAparicion() { return probabilidadAparicion; }
    public void setProbabilidadAparicion(double probabilidadAparicion) { this.probabilidadAparicion = probabilidadAparicion; }
}