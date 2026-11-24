package visual;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

public class CardImageLoader {
    private static final String CARDS_PATH = "resources/cards/";

    private static final Map<String, BufferedImage> cache = new HashMap<>();

    /**
     * Obtener imagen de una carta dado su código (ej. "1h", "13s").
     * Si no se encuentra el archivo en disco, retorna null.
     */
    public static BufferedImage obtenerImagen(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) return null;

        String cod = codigo.trim().toLowerCase();
        if (cache.containsKey(cod)) {
            return cache.get(cod);
        }

        String[] extensiones = { ".png", ".PNG", ".jpg", ".JPG" };
        for (String ext : extensiones) {
            File archivo = new File(CARDS_PATH + cod + ext);
            if (archivo.exists() && archivo.isFile()) {
                try {
                    BufferedImage img = ImageIO.read(archivo);
                    if (img != null) {
                        cache.put(cod, img);
                        System.out.println("[CARD IMAGES] Carta cargada: " + archivo.getPath() + " (" + img.getWidth() + "x" + img.getHeight() + ")");
                        return img;
                    }
                } catch (IOException e) {
                    System.err.println("[CARD IMAGES] Error al leer " + archivo.getPath() + ": " + e.getMessage());
                }
            }
        }

        return null;
    }

    public static void limpiarCache() {
        cache.clear();
    }
}
