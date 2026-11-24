package entity;

import java.awt.*;

public class Botiquin extends GameObject implements Collidable {
    private Color color = Color.MAGENTA;
    private int radio = 16;
    private long tiempoCreacion;

    public Botiquin(int x, int y, ID id) {
        super(x, y, id);
        this.tiempoCreacion = System.currentTimeMillis();
    }

    @Override
    public void tick() {
        // Botiquín desaparece después de 15 segundos en el suelo
        if (System.currentTimeMillis() - tiempoCreacion > 15000) {
            // Marcar para remoción - el Handler se hará cargo
        }
    }

    @Override
    public void render(Graphics g) {
        // Solo renderizar si aún no ha expirado (15 segundos)
        if (System.currentTimeMillis() - tiempoCreacion <= 15000) {
            g.setColor(color);
            g.fillRect(x - radio / 2, y - radio / 2, radio, radio);

            // Ícono cruz para indicar botiquín
            g.setColor(Color.WHITE);
            g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 10));
            String texto = "♥";
            int strW = g.getFontMetrics().stringWidth(texto);
            g.drawString(texto, x - strW / 2, y + 5);
        }
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x - radio / 2, y - radio / 2, radio, radio);
    }

    public boolean isExpirado() {
        return System.currentTimeMillis() - tiempoCreacion > 15000;
    }
}