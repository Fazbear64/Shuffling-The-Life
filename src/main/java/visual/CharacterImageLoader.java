package visual;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

public class CharacterImageLoader {
    private static final String SPRITES_BASE_PATH = "resources/characters/";
    private static final Map<String, BufferedImage> imagenes = new HashMap<>();
    private static boolean cargado = false;

    // Personajes disponibles
    public static final String[] PERSONAJES = { "mapache", "gato", "rana", "collie" };

    /**
     * Cargar imagen de un personaje
     */
    public static void cargarImagenPersonaje(String personaje) {
        if (imagenes.containsKey(personaje)) return;

        String ruta = SPRITES_BASE_PATH + personaje + ".png";

        try {
            File file = new File(ruta);
            if (file.exists()) {
                BufferedImage img = ImageIO.read(file);
                imagenes.put(personaje, img);
                System.out.println("[CHARACTER IMAGES] Personaje cargado: " + personaje + " (" + img.getWidth() + "x" + img.getHeight() + ")");
            } else {
                System.out.println("[CHARACTER IMAGES] No se encontró: " + ruta);
            }
        } catch (IOException e) {
            System.out.println("[CHARACTER IMAGES] Error al cargar " + personaje + ": " + e.getMessage());
        }
    }

    /**
     * Cargar todas las imágenes de personajes
     */
    public static void cargarTodos() {
        if (cargado) return;

        for (String personaje : PERSONAJES) {
            cargarImagenPersonaje(personaje);
        }

        cargado = true;
        System.out.println("[CHARACTER IMAGES] Todas las imágenes cargadas");
    }

    /**
     * Obtener imagen de un personaje
     */
    public static BufferedImage obtenerImagen(String personaje) {
        if (!cargado) {
            cargarTodos();
        }

        return imagenes.getOrDefault(personaje, null);
    }

    /**
     * Verificar si existe imagen de personaje
     */
    public static boolean existeImagen(String personaje) {
        return obtenerImagen(personaje) != null;
    }

    /**
     * Obtener índice del personaje
     */
    public static int getIndicePersonaje(String personaje) {
        for (int i = 0; i < PERSONAJES.length; i++) {
            if (PERSONAJES[i].equals(personaje.toLowerCase())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Obtener nombre del personaje por índice
     */
    public static String getPersonajePorIndice(int indice) {
        if (indice >= 0 && indice < PERSONAJES.length) {
            return PERSONAJES[indice];
        }
        return null;
    }
}