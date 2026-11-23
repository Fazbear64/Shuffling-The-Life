package visual;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class BackgroundLoader {
    private static BufferedImage fondo = null;
    private static boolean cargado = false;
    private static String rutaFondo = "resources/fondo.png";

    /**
     * Cargar imagen de fondo
     */
    public static void cargarFondo() {
        if (cargado) return;

        try {
            File file = new File(rutaFondo);
            if (file.exists()) {
                fondo = ImageIO.read(file);
                System.out.println("[BACKGROUND] Fondo cargado: " + rutaFondo);
                System.out.println("[BACKGROUND] Tamaño: " + fondo.getWidth() + "x" + fondo.getHeight());
            } else {
                System.out.println("[BACKGROUND] No se encontró: " + rutaFondo);
            }
        } catch (IOException e) {
            System.out.println("[BACKGROUND] Error al cargar: " + e.getMessage());
        }

        cargado = true;
    }

    /**
     * Renderizar fondo
     */
    public static void render(Graphics2D g2d, Camera camera) {
        // Cargar si no se ha cargado
        if (!cargado) {
            cargarFondo();
        }

        // Si hay imagen, dibujarla
        if (fondo != null) {
            // Dibujar la imagen completa del mundo
            g2d.drawImage(fondo, 0, 0, null);

            // Agregar capa oscura semi-transparente para hacer el fondo más opaco
            g2d.setColor(new java.awt.Color(0, 0, 0, 100)); // Negro con 100 de alpha (opacidad)
            g2d.fillRect(0, 0, config.DefaultSettings.worldWidth, config.DefaultSettings.worldHeight);
        } else {
            // Fallback: fondo negro si no hay imagen
            g2d.setColor(java.awt.Color.BLACK);
            g2d.fillRect(0, 0, config.DefaultSettings.worldWidth, config.DefaultSettings.worldHeight);
        }
    }

    /**
     * Establecer ruta personalizada del fondo
     */
    public static void setRutaFondo(String ruta) {
        rutaFondo = ruta;
        cargado = false; // Forzar recarga
    }

    /**
     * Verificar si hay fondo cargado
     */
    public static boolean hayFondo() {
        return fondo != null;
    }
}