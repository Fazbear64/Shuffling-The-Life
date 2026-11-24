package dto;

import entity.ID;

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

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }

    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

    public ID getId() { return id; }
    public void setId(ID id) { this.id = id; }

    public int getVida() { return vida; }
    public void setVida(int vida) { this.vida = vida; }

    public int getVidaMax() { return vidaMax; }
    public void setVidaMax(int vidaMax) { this.vidaMax = vidaMax; }

    public int getDaño() { return daño; }
    public void setDaño(int daño) { this.daño = daño; }

    public float getVelocidad() { return velocidad; }
    public void setVelocidad(float velocidad) { this.velocidad = velocidad; }

    public int getAncho() { return ancho; }
    public void setAncho(int ancho) { this.ancho = ancho; }

    public int getAlto() { return alto; }
    public void setAlto(int alto) { this.alto = alto; }
}