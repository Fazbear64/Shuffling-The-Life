package visual;

import DataBase.BaseDeDatos.RecordEntry;
import DataBase.RankingService.Estado;
import DataBase.RankingService.Snapshot;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

/**
 * Dibuja el panel "TOP récords" a partir de un Snapshot ya cargado.
 * Solo lee datos en memoria: es seguro llamarlo en cada frame.
 */
final class RankingRenderer {
    private static final Font FUENTE_TITULO = new Font("Arial", Font.BOLD, 18);
    private static final Font FUENTE_FILA = new Font("Arial", Font.PLAIN, 16);
    private static final Font FUENTE_NOTA = new Font("Arial", Font.ITALIC, 13);

    private static final int ALTO_FILA = 22;
    private static final int MITAD_ANCHO = 170; // el panel mide 2 * MITAD_ANCHO de ancho

    private RankingRenderer() {}

    /**
     * @param centroX        centro horizontal del panel
     * @param y              línea base del título
     * @param jugadorActual  nombre a resaltar en la lista (puede ser null)
     * @return la coordenada Y libre debajo del panel
     */
    static int dibujar(Graphics g, Snapshot s, String jugadorActual, int centroX, int y) {
        g.setFont(FUENTE_TITULO);
        g.setColor(Color.YELLOW);
        centrado(g, "TOP 5 - MEJORES SUPERVIVIENTES", centroX, y);

        int yFila = y + 26;
        boolean hayDatos = !s.top.isEmpty();

        if (hayDatos) {
            g.setFont(FUENTE_FILA);
            int izq = centroX - MITAD_ANCHO;
            int der = centroX + MITAD_ANCHO;
            FontMetrics fm = g.getFontMetrics();
            for (int i = 0; i < s.top.size(); i++) {
                RecordEntry e = s.top.get(i);
                boolean esJugador = jugadorActual != null && jugadorActual.equals(e.getNombre());
                g.setColor(esJugador ? Color.GREEN : Color.WHITE);

                g.drawString((i + 1) + ".", izq, yFila);
                g.drawString(e.getNombre(), izq + 28, yFila);
                String puntos = e.getPuntuacion() + "s";
                g.drawString(puntos, der - fm.stringWidth(puntos), yFila);
                yFila += ALTO_FILA;
            }
        } else {
            g.setFont(FUENTE_FILA);
            if (s.estado == Estado.LISTO) {
                g.setColor(Color.LIGHT_GRAY);
                centrado(g, "Aún no hay récords. ¡Sé el primero!", centroX, yFila);
            } else if (s.estado == Estado.ERROR) {
                g.setColor(Color.ORANGE);
                centrado(g, "Ranking no disponible", centroX, yFila);
            } else {
                g.setColor(Color.LIGHT_GRAY);
                centradoAnimado(g, "Cargando ranking", centroX, yFila);
            }
            yFila += ALTO_FILA;
        }

        // Línea de estado / marca personal
        g.setFont(FUENTE_NOTA);
        if (s.estado == Estado.ERROR) {
            g.setColor(Color.ORANGE);
            centrado(g, (s.mensaje != null ? s.mensaje : "Sin conexión") + (hayDatos ? " (datos antiguos)" : ""),
                    centroX, yFila + 4);
            yFila += ALTO_FILA;
        } else if (s.estado == Estado.CARGANDO && hayDatos) {
            g.setColor(Color.GRAY);
            centradoAnimado(g, "Actualizando", centroX, yFila + 4);
            yFila += ALTO_FILA;
        } else if (s.estado == Estado.LISTO && s.mejorPersonal >= 0
                && jugadorActual != null && jugadorActual.equals(s.jugador)) {
            g.setColor(Color.CYAN);
            centrado(g, "Tu mejor marca en línea: " + s.mejorPersonal + "s", centroX, yFila + 4);
            yFila += ALTO_FILA;
        }
        return yFila;
    }

    /** Texto con 0-3 puntos animados; se ancla al ancho máximo para que no "baile" al recentrarse. */
    private static void centradoAnimado(Graphics g, String base, int centroX, int y) {
        int n = (int) ((System.currentTimeMillis() / 400) % 4);
        int ancho = g.getFontMetrics().stringWidth(base + "...");
        g.drawString(base + ".".repeat(n), centroX - ancho / 2, y);
    }

    private static void centrado(Graphics g, String texto, int centroX, int y) {
        g.drawString(texto, centroX - g.getFontMetrics().stringWidth(texto) / 2, y);
    }
}
