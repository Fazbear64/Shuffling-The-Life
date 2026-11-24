package dto;

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
    private int botiquines = 0;

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

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }

    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

    public int getVidaActual() { return vidaActual; }
    public void setVidaActual(int vidaActual) { this.vidaActual = vidaActual; }

    public int getVidaMax() { return vidaMax; }
    public void setVidaMax(int vidaMax) { this.vidaMax = vidaMax; }

    public int getDañoBase() { return dañoBase; }
    public void setDañoBase(int dañoBase) { this.dañoBase = dañoBase; }

    public int getVelocidadMovimiento() { return velocidadMovimiento; }
    public void setVelocidadMovimiento(int velocidadMovimiento) { this.velocidadMovimiento = velocidadMovimiento; }

    public int getMonedas() { return monedas; }
    public void setMonedas(int monedas) { this.monedas = monedas; }

    public String getNombrePersonaje() { return nombrePersonaje; }
    public void setNombrePersonaje(String nombrePersonaje) { this.nombrePersonaje = nombrePersonaje; }

    public ArmaDTO getArma() { return arma; }
    public void setArma(ArmaDTO arma) { this.arma = arma; }

    public int getBotiquines() { return botiquines; }
    public void setBotiquines(int botiquines) { this.botiquines = botiquines; }
}