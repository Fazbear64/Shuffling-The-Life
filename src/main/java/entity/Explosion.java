package entity;

import java.awt.*;

public class Explosion extends GameObject {
    private long tiempoCreacion;
    private static final long DURACION_MS = 500; // La explosión dura 0.5 segundos
    private int radioExplosion = 150; // Radio de daño
    private int dañoExplosion = 30;
    private boolean dañoAplicado = false; // Para aplicar daño solo una vez

    public Explosion(int x, int y, ID id) {
        super(x, y, id);
        this.tiempoCreacion = System.currentTimeMillis();
        System.out.println("[DEBUG EXPLOSION] Explosión creada en (" + x + ", " + y + ")");
    }

    @Override
    public void tick() {
        // La explosión no se mueve
    }

    @Override
    public void render(Graphics g) {
        long tiempoTranscurrido = System.currentTimeMillis() - tiempoCreacion;
        float progreso = Math.min(1.0f, tiempoTranscurrido / (float) DURACION_MS);

        // Efecto visual: círculo que crece y se desvanece
        int radioActual = (int) (radioExplosion * progreso);
        int alpha = (int) (255 * (1.0f - progreso)); // Se desvanece

        // Círculo exterior (amarillo)
        g.setColor(new Color(255, 200, 0, Math.max(0, alpha)));
        g.fillOval(x - radioActual, y - radioActual, radioActual * 2, radioActual * 2);

        // Círculo interior (rojo)
        int radioInterior = radioActual / 2;
        g.setColor(new Color(255, 50, 0, Math.max(0, alpha)));
        g.fillOval(x - radioInterior, y - radioInterior, radioInterior * 2, radioInterior * 2);

        // Centro brillante (blanco)
        int radioNucleo = radioActual / 4;
        g.setColor(new Color(255, 255, 255, Math.max(0, alpha)));
        g.fillOval(x - radioNucleo, y - radioNucleo, radioNucleo * 2, radioNucleo * 2);
    }

    public boolean haExpirado() {
        return (System.currentTimeMillis() - tiempoCreacion) > DURACION_MS;
    }

    public int getRadioExplosion() {
        return radioExplosion;
    }

    public int getDañoExplosion() {
        return dañoExplosion;
    }

    public boolean getDañoAplicado() {
        return dañoAplicado;
    }

    public void setDañoAplicado(boolean aplicado) {
        this.dañoAplicado = aplicado;
    }

    // Verifica si una posición está dentro del radio de la explosión
    public boolean afectaA(int px, int py) {
        float distancia = (float) Math.sqrt(Math.pow(x - px, 2) + Math.pow(y - py, 2));
        return distancia <= radioExplosion;
    }
}
