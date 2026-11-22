package entity;

import config.DefaultSettings;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Jefe extends GameObject implements Damageable, Collidable {
    private int vidaMax = 150;
    private int vida = 150;
    private float velocidad = 2.5f;
    private float exactX;
    private float exactY;
    private float velocidadX = 1.5f;
    private float velocidadY = 1.5f;

    // Segmentos del gusano (cabeza + cola)
    private List<Segmento> segmentos = new ArrayList<>();
    private static final int TAMAÑO_SEGMENTO = 64;
    private static final int NUM_SEGMENTOS = 25;

    // Para seguir al jugador
    private int targetX = 0;
    private int targetY = 0;
    private float giroVelocidad = 0.02f; // Giro muy lento

    public static class Segmento {
        public float x, y;
        public int vida;
        public int vidaMax;
        public boolean esCabeza;

        public Segmento(float x, float y, int vida, boolean esCabeza) {
            this.x = x;
            this.y = y;
            this.vida = vida;
            this.vidaMax = vida;
            this.esCabeza = esCabeza;
        }
    }

    public Jefe(int x, int y) {
        super(x, y, ID.JEFE);
        this.exactX = x;
        this.exactY = y;
        this.targetX = x;
        this.targetY = y;

        // Crear segmentos: cabeza + cuerpo
        for (int i = 0; i < NUM_SEGMENTOS; i++) {
            boolean esCabeza = (i == 0);
            int vidaSegmento = esCabeza ? 25 : 12;
            Segmento seg = new Segmento(x - (i * TAMAÑO_SEGMENTO), y, vidaSegmento, esCabeza);
            segmentos.add(seg);
        }
    }

    public void setTarget(int px, int py) {
        this.targetX = px;
        this.targetY = py;
    }

    @Override
    public void tick() {
        if (segmentos.isEmpty()) return;

        // Seguir al jugador con giro LENTO
        float diffX = targetX - exactX;
        float diffY = targetY - exactY;
        float distancia = (float) Math.sqrt(diffX * diffX + diffY * diffY);

        if (distancia > 1) {
            float targetAngle = (float) Math.atan2(diffY, diffX);
            float currentAngle = (float) Math.atan2(velocidadY, velocidadX);

            // Giro lento y suave
            float angleDiff = targetAngle - currentAngle;
            // Normalizar diferencia de ángulo
            while (angleDiff > Math.PI) angleDiff -= 2 * Math.PI;
            while (angleDiff < -Math.PI) angleDiff += 2 * Math.PI;

            // Aplicar giro gradual
            float newAngle = currentAngle + (angleDiff * giroVelocidad);
            float speed = 2.0f;
            velocidadX = (float) Math.cos(newAngle) * speed;
            velocidadY = (float) Math.sin(newAngle) * speed;
        }

        // Moverse
        exactX += velocidadX;
        exactY += velocidadY;

        // Rebote suave en bordes
        if (exactX <= 0 || exactX >= DefaultSettings.worldWidth - TAMAÑO_SEGMENTO) {
            velocidadX *= -0.8f;
            exactX = Math.max(0, Math.min(DefaultSettings.worldWidth - TAMAÑO_SEGMENTO, exactX));
        }
        if (exactY <= 0 || exactY >= DefaultSettings.worldHeight - TAMAÑO_SEGMENTO) {
            velocidadY *= -0.8f;
            exactY = Math.max(0, Math.min(DefaultSettings.worldHeight - TAMAÑO_SEGMENTO, exactY));
        }

        // Actualizar posición de la cabeza
        Segmento cabeza = segmentos.get(0);
        cabeza.x = exactX;
        cabeza.y = exactY;
        this.x = (int) exactX;
        this.y = (int) exactY;

        // La cola sigue a la cabeza (efecto de movimiento suave)
        for (int i = 1; i < segmentos.size(); i++) {
            Segmento anterior = segmentos.get(i - 1);
            Segmento actual = segmentos.get(i);

            float dx = anterior.x - actual.x;
            float dy = anterior.y - actual.y;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);

            if (dist > TAMAÑO_SEGMENTO * 0.8f) {
                float factor = (dist - TAMAÑO_SEGMENTO * 0.8f) / dist;
                actual.x += dx * factor * 0.5f;
                actual.y += dy * factor * 0.5f;
            }
        }
    }

    @Override
    public void render(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // Dibujar segmentos de atrás a adelante
        for (int i = segmentos.size() - 1; i >= 0; i--) {
            Segmento seg = segmentos.get(i);
            if (seg.vida <= 0) continue;

            if (seg.esCabeza) {
                // Cabeza: color rojo brillante
                g.setColor(new Color(255, 50, 50));
                g.fillRect((int) seg.x, (int) seg.y, TAMAÑO_SEGMENTO, TAMAÑO_SEGMENTO);
                g.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRect((int) seg.x, (int) seg.y, TAMAÑO_SEGMENTO, TAMAÑO_SEGMENTO);

                // Ojos de la cabeza
                g.setColor(Color.WHITE);
                g.fillOval((int) seg.x + 10, (int) seg.y + 10, 12, 12);
                g.fillOval((int) seg.x + 42, (int) seg.y + 10, 12, 12);
                g.setColor(Color.BLACK);
                g.fillOval((int) seg.x + 13, (int) seg.y + 13, 6, 6);
                g.fillOval((int) seg.x + 45, (int) seg.y + 13, 6, 6);
            } else {
                // Cuerpo: degradado de rojo a naranja
                int colorR = 255 - (i * 8);
                int colorG = 50 + (i * 3);
                g.setColor(new Color(Math.max(100, colorR), colorG, 50));
                g.fillRect((int) seg.x, (int) seg.y, TAMAÑO_SEGMENTO, TAMAÑO_SEGMENTO);
                g.setColor(new Color(200, 100, 0));
                g.drawRect((int) seg.x, (int) seg.y, TAMAÑO_SEGMENTO, TAMAÑO_SEGMENTO);
            }

            // Barra de vida
            int barraAncho = TAMAÑO_SEGMENTO;
            int barraAlto = 4;
            g.setColor(Color.RED);
            g.fillRect((int) seg.x, (int) seg.y - 8, barraAncho, barraAlto);
            g.setColor(Color.GREEN);
            int vidaVisual = (int) (barraAncho * ((double) seg.vida / seg.vidaMax));
            g.fillRect((int) seg.x, (int) seg.y - 8, vidaVisual, barraAlto);
        }
    }

    @Override
    public Rectangle getBounds() {
        Segmento cabeza = segmentos.get(0);
        return new Rectangle((int) cabeza.x, (int) cabeza.y, TAMAÑO_SEGMENTO, TAMAÑO_SEGMENTO);
    }

    @Override
    public void recibirDaño(int cantidad) {
        // Solo la cabeza recibe daño
        if (segmentos.isEmpty()) return;
        Segmento cabeza = segmentos.get(0);
        cabeza.vida -= cantidad;
        this.vida = cabeza.vida;

        if (cabeza.vida <= 0) {
            cabeza.vida = 0;
            System.out.println("¡JEFE DERROTADO!");
        }
    }

    @Override
    public boolean estaMuerto() {
        if (segmentos.isEmpty()) return true;
        return segmentos.get(0).vida <= 0;
    }

    public void recibirDañoEnSegmento(int x, int y, int cantidad) {
        // Buscar qué segmento fue golpeado
        for (int i = 0; i < segmentos.size(); i++) {
            Segmento seg = segmentos.get(i);
            Rectangle rect = new Rectangle((int) seg.x, (int) seg.y, TAMAÑO_SEGMENTO, TAMAÑO_SEGMENTO);

            if (rect.contains(x, y)) {
                if (seg.esCabeza) {
                    // Golpe a la cabeza = daño doble y muerte
                    seg.vida -= (cantidad * 2);
                    if (seg.vida <= 0) {
                        seg.vida = 0;
                        System.out.println("¡CABEZA DESTRUIDA! ¡JEFE MUERTO!");
                    }
                    System.out.println("¡GOLPE A LA CABEZA! Daño: " + (cantidad * 2));
                } else {
                    // Golpe al cuerpo = NO HACE NADA (solo la cabeza recibe daño)
                    System.out.println("¡Golpe al cuerpo! Pero nada pasa, ¡solo mata a la cabeza!");
                }
                return;
            }
        }
    }

    public List<Segmento> getSegmentos() {
        return segmentos;
    }

    public int getVida() {
        return vida;
    }

    public int getVidaMax() {
        return vidaMax;
    }
}
