package entity;

import java.awt.*;

public class Proyectil extends GameObject implements Collidable {
    private float velX, velY;
    private int daño = 1;
    private Arma arma;
    private Color color = Color.YELLOW;
    private int radio = 10;
    private long tiempoCreacion;
    private int alcanceMs = 0;

    public Proyectil(int x, int y, ID id, double angulo, int daño) {
        this(x, y, id, angulo, daño, null);
    }

    public Proyectil(int x, int y, ID id, double angulo, int daño, Arma arma) {
        super(x, y, id);
        this.daño = daño;
        this.arma = arma;
        this.tiempoCreacion = System.currentTimeMillis();

        double velocidadDisparo = 10.0;
        if (arma != null) {
            velocidadDisparo = arma.getVelocidadProyectil();
            this.color = arma.getColorProyectil();
            this.radio = arma.getTamañoProyectil();
            this.alcanceMs = arma.getAlcanceMs();
        }

        this.velX = (float) (Math.cos(angulo) * velocidadDisparo);
        this.velY = (float) (Math.sin(angulo) * velocidadDisparo);
    }

    @Override
    public void tick() {
        x += velX;
        y += velY;
    }

    public boolean estaExpirado() {
        if (alcanceMs > 0) {
            return (System.currentTimeMillis() - tiempoCreacion) > alcanceMs;
        }
        return false;
    }

    @Override
    public void render(Graphics g) {
        if (arma != null && arma.getTipo() == TipoArma.LANZALLAMAS) {
            // Renderizado de llamarada de fuego con centro brillante
            g.setColor(new Color(255, 69, 0, 190));
            g.fillOval(x - radio / 2, y - radio / 2, radio, radio);
            g.setColor(new Color(255, 215, 0, 240));
            int inner = Math.max(4, radio / 2);
            g.fillOval(x - inner / 2, y - inner / 2, inner, inner);
        } else if (arma != null && arma.getTipo() == TipoArma.MINIGUN) {
            g.setColor(color != null ? color : Color.CYAN);
            g.fillOval(x - radio / 2, y - radio / 2, radio, radio);
        } else if (arma != null && arma.getTipo() == TipoArma.ESCOPETA) {
            g.setColor(color != null ? color : Color.LIGHT_GRAY);
            g.fillOval(x - radio / 2, y - radio / 2, radio, radio);
        } else {
            g.setColor(color != null ? color : Color.YELLOW);
            g.fillOval(x - radio / 2, y - radio / 2, radio, radio);
        }
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x - radio / 2, y - radio / 2, radio, radio);
    }

    public int getDaño() {
        return daño;
    }

    public Arma getArma() {
        return arma;
    }
}
