package entity;

import config.DefaultSettings;
import java.awt.*;

public class Moneda extends GameObject {
    private int valor = 1;
    private long tiempoCreacion;
    private static final int RANGO_IMAN = 50; // Rango en píxeles donde la moneda se acerca al jugador
    private static final double VELOCIDAD_IMAN = 5.0; // Velocidad de acercamiento al jugador

    public Moneda(int x, int y, ID id, int valor) {
        super(x, y, id);
        this.valor = valor;
        this.tiempoCreacion = System.currentTimeMillis();
    }

    @Override
    public void tick() {
        // Las monedas desaparecen después de 15 segundos en el suelo
        if (System.currentTimeMillis() - tiempoCreacion > 15000) {
            // Marcar para remoción - el Handler se hará cargo
            // Usaremos un flag o el Handler removerá basándose en el tiempo
        }
    }

    public void moverHaciaJugador(int playerX, int playerY) {
        // Calcular distancia al jugador
        int dx = playerX - x;
        int dy = playerY - y;
        double distancia = Math.sqrt(dx * dx + dy * dy);

        // Si está dentro del rango magnético, moverse hacia el jugador
        if (distancia < RANGO_IMAN && distancia > 0) {
            // Normalizar y multiplicar por velocidad
            double vx = (dx / distancia) * VELOCIDAD_IMAN;
            double vy = (dy / distancia) * VELOCIDAD_IMAN;

            x += (int) vx;
            y += (int) vy;
        }
    }

    @Override
    public void render(Graphics g) {
        // Representación visual temporal (moneda dorada brillante)
        // Solo renderizar si aún no ha expirado
        if (System.currentTimeMillis() - tiempoCreacion <= 15000) {
            g.setColor(Color.YELLOW);
            g.fillOval(x, y, 16, 16);
            g.setColor(Color.ORANGE);
            g.drawOval(x, y, 16, 16);
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, 16, 16);
    }

    public int getValor() {
        return valor;
    }

    public boolean isExpirado() {
        return System.currentTimeMillis() - tiempoCreacion > 15000;
    }
}
