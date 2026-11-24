package entity;

import dto.ArmaDTO;
import java.awt.Color;

public class Arma {
    private TipoArma tipo;
    private String nombre;
    private float multiplicadorDaño;
    private int dañoBonus;
    private long cooldownMs;
    private int proyectilesPorDisparo;
    private double dispersion;
    private float velocidadProyectil;
    private int alcanceMs;
    private Color colorProyectil;
    private int tamañoProyectil;
    private double probabilidadAparicion;
    private boolean disparoSostenido;
    private long duracionDisparoSostenidoMs;
    private long enfriamientoSostenidoMs;

    public Arma(TipoArma tipo, String nombre, float multiplicadorDaño, int dañoBonus,
                long cooldownMs, int proyectilesPorDisparo, double dispersion,
                float velocidadProyectil, int alcanceMs, Color colorProyectil,
                int tamañoProyectil, double probabilidadAparicion) {
        this(tipo, nombre, multiplicadorDaño, dañoBonus, cooldownMs, proyectilesPorDisparo,
             dispersion, velocidadProyectil, alcanceMs, colorProyectil, tamañoProyectil,
             probabilidadAparicion, false, 0, 0);
    }

    public Arma(TipoArma tipo, String nombre, float multiplicadorDaño, int dañoBonus,
                long cooldownMs, int proyectilesPorDisparo, double dispersion,
                float velocidadProyectil, int alcanceMs, Color colorProyectil,
                int tamañoProyectil, double probabilidadAparicion, boolean disparoSostenido,
                long duracionDisparoSostenidoMs, long enfriamientoSostenidoMs) {
        this.tipo = tipo;
        this.nombre = nombre;
        this.multiplicadorDaño = multiplicadorDaño;
        this.dañoBonus = dañoBonus;
        this.cooldownMs = cooldownMs;
        this.proyectilesPorDisparo = proyectilesPorDisparo;
        this.dispersion = dispersion;
        this.velocidadProyectil = velocidadProyectil;
        this.alcanceMs = alcanceMs;
        this.colorProyectil = colorProyectil;
        this.tamañoProyectil = tamañoProyectil;
        this.probabilidadAparicion = probabilidadAparicion;
        this.disparoSostenido = disparoSostenido;
        this.duracionDisparoSostenidoMs = duracionDisparoSostenidoMs;
        this.enfriamientoSostenidoMs = enfriamientoSostenidoMs;
    }

    public static Arma crearRevolver() {
        return new Arma(
            TipoArma.REVOLVER,
            "Revolver",
            1.0f,
            0,
            380L,
            1,
            0.04,
            12.0f,
            0,
            Color.YELLOW,
            8,
            0.40
        );
    }

    public static Arma crearEscopeta() {
        return new Arma(
            TipoArma.ESCOPETA,
            "Escopeta",
            1.0f,
            0,
            700L,
            5,
            0.15,
            11.0f,
            0,
            Color.LIGHT_GRAY,
            6,
            0.30
        );
    }

    public static Arma crearMinigun() {
        return new Arma(
            TipoArma.MINIGUN,
            "Minigun",
            1.0f,
            0,
            100L,
            1,
            0.08,
            14.0f,
            0,
            new Color(0, 220, 255),
            6,
            0.20,
            true,
            7000L,
            2000L
        );
    }

    public static Arma crearLanzallamas() {
        return new Arma(
            TipoArma.LANZALLAMAS,
            "Lanzallamas",
            1.5f,
            0,
            80L,
            2,
            0.30,
            6.5f,
            450,
            Color.ORANGE,
            14,
            0.10,
            true,
            0L,
            0L
        );
    }

    public static Arma crearArmaPorTipo(TipoArma tipo) {
        if (tipo == null) return crearRevolver();
        switch (tipo) {
            case ESCOPETA: return crearEscopeta();
            case MINIGUN: return crearMinigun();
            case LANZALLAMAS: return crearLanzallamas();
            case REVOLVER:
            default: return crearRevolver();
        }
    }

    public static Arma sortearArmaAleatoria() {
        double rand = Math.random();
        if (rand < 0.10) {
            return crearLanzallamas();
        } else if (rand < 0.30) {
            return crearMinigun();
        } else if (rand < 0.60) {
            return crearEscopeta();
        } else {
            return crearRevolver();
        }
    }

    public ArmaDTO toDTO() {
        return new ArmaDTO(
            tipo != null ? tipo.name() : TipoArma.REVOLVER.name(),
            nombre,
            multiplicadorDaño,
            dañoBonus,
            cooldownMs,
            proyectilesPorDisparo,
            dispersion,
            velocidadProyectil,
            probabilidadAparicion
        );
    }

    public static Arma fromDTO(ArmaDTO dto) {
        if (dto == null) return crearRevolver();
        TipoArma t;
        try {
            t = TipoArma.valueOf(dto.getTipo());
        } catch (Exception e) {
            t = TipoArma.REVOLVER;
        }
        Arma a = crearArmaPorTipo(t);
        if (dto.getNombre() != null) a.nombre = dto.getNombre();
        if (dto.getMultiplicadorDaño() > 0) a.multiplicadorDaño = dto.getMultiplicadorDaño();
        a.dañoBonus = dto.getDañoBonus();
        if (dto.getCooldownMs() > 0) a.cooldownMs = dto.getCooldownMs();
        if (dto.getProyectilesPorDisparo() > 0) a.proyectilesPorDisparo = dto.getProyectilesPorDisparo();
        a.dispersion = dto.getDispersion();
        if (dto.getVelocidadProyectil() > 0) a.velocidadProyectil = dto.getVelocidadProyectil();
        if (dto.getProbabilidadAparicion() > 0) a.probabilidadAparicion = dto.getProbabilidadAparicion();
        return a;
    }

    public TipoArma getTipo() { return tipo; }
    public String getNombre() { return nombre; }
    public float getMultiplicadorDaño() { return multiplicadorDaño; }
    public void setMultiplicadorDaño(float multiplicadorDaño) { this.multiplicadorDaño = multiplicadorDaño; }
    public int getDañoBonus() { return dañoBonus; }
    public void setDañoBonus(int dañoBonus) { this.dañoBonus = dañoBonus; }
    public long getCooldownMs() { return cooldownMs; }
    public void setCooldownMs(long cooldownMs) { this.cooldownMs = cooldownMs; }
    public int getProyectilesPorDisparo() { return proyectilesPorDisparo; }
    public double getDispersion() { return dispersion; }
    public float getVelocidadProyectil() { return velocidadProyectil; }
    public int getAlcanceMs() { return alcanceMs; }
    public Color getColorProyectil() { return colorProyectil; }
    public int getTamañoProyectil() { return tamañoProyectil; }
    public double getProbabilidadAparicion() { return probabilidadAparicion; }
    public boolean isDisparoSostenido() { return disparoSostenido; }
    public long getDuracionDisparoSostenidoMs() { return duracionDisparoSostenidoMs; }
    public long getEnfriamientoSostenidoMs() { return enfriamientoSostenidoMs; }
}
