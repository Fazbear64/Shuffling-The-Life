package visual;

import entity.ID;
import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class SpriteLoader {
    // Rutas de sprites
    private static final String SPRITES_BASE_PATH = "resources/sprites/player/";

    // Cache de sprites cargados
    private static final Map<String, Map<String, Map<String, List<BufferedImage>>>> spriteCache = new HashMap<>();
    private static boolean cacheInicializada = false;

    // Direcciones
    public enum Direction { FRONT, BACK, LEFT, RIGHT }

    // Estados de animación
    public enum AnimationState { IDLE, WALK, SHOOT }

    /**
     * Cargar sprites para un personaje específico
     */
    public static void cargarSpritesPersonaje(String personaje) {
        if (spriteCache.containsKey(personaje)) return;

        Map<String, Map<String, List<BufferedImage>>> animaciones = new HashMap<>();

        // Cargar cada tipo de animación
        animaciones.put("idle", cargarAnimacion(personaje, "idle"));
        animaciones.put("walk", cargarAnimacion(personaje, "walk"));
        animaciones.put("shoot", cargarAnimacion(personaje, "shoot"));

        spriteCache.put(personaje, animaciones);
    }

    /**
     * Cargar una animación específica (idle, walk, shoot)
     */
    private static Map<String, List<BufferedImage>> cargarAnimacion(String personaje, String tipoAnim) {
        Map<String, List<BufferedImage>> direcciones = new HashMap<>();

        String[] dirs = { "front", "back", "left", "right" };

        for (String dir : dirs) {
            String ruta = SPRITES_BASE_PATH + personaje + "/" + tipoAnim + "/" + dir;
            List<BufferedImage> frames = new ArrayList<>();

            // Para walk: buscar _1.png, _2.png
            // Para idle: buscar _1.png, _2.png
            // Para shoot: solo .png (sin número)
            if (tipoAnim.equals("walk")) {
                // Walk: 2 frames (_1.png, _2.png)
                for (int i = 1; i <= 2; i++) {
                    BufferedImage img = cargarImagen(ruta + "_" + i + ".png");
                    if (img != null) {
                        frames.add(img);
                    }
                }
            } else if (tipoAnim.equals("idle")) {
                // Idle: 1 solo frame (sin número)
                BufferedImage img = cargarImagen(ruta + ".png");
                if (img != null) {
                    frames.add(img);
                }
            } else {
                // shoot solo tiene 1 frame por dirección
                BufferedImage img = cargarImagen(ruta + ".png");
                if (img != null) {
                    frames.add(img);
                }
            }

            if (!frames.isEmpty()) {
                direcciones.put(dir, frames);
            }
        }

        return direcciones;
    }

    /**
     * Cargar una imagen desde archivo
     */
    private static BufferedImage cargarImagen(String ruta) {
        try {
            File file = new File(ruta);
            if (file.exists()) {
                return ImageIO.read(file);
            }
        } catch (IOException e) {
            // Silencioso - sprite no encontrado
        }
        return null;
    }

    /**
     * Obtener frame de animación
     * @param personaje Nombre del personaje (mapache, gato, rana, collie)
     * @param estado Estado de animación (IDLE, WALK, SHOOT)
     * @param direccion Dirección (FRONT, BACK, LEFT, RIGHT)
     * @param frame Frame específico (0 para idle, 0-1 para walk)
     * @return Imagen del sprite o null si no existe
     */
    public static BufferedImage obtenerSprite(String personaje, AnimationState estado, Direction direccion, int frame) {
        // Inicializar cache si es necesario
        if (!cacheInicializada) {
            inicializarCache();
        }

        String nombrePersonaje = getNombrePersonaje(personaje);

        if (!spriteCache.containsKey(nombrePersonaje)) {
            return null;
        }

        String tipoAnim = estado.name().toLowerCase();
        String dirName = direccion.name().toLowerCase();

        Map<String, Map<String, List<BufferedImage>>> animaciones = spriteCache.get(nombrePersonaje);

        if (!animaciones.containsKey(tipoAnim)) {
            return null;
        }

        Map<String, List<BufferedImage>> direcciones = animaciones.get(tipoAnim);

        if (!direcciones.containsKey(dirName)) {
            return null;
        }

        List<BufferedImage> frames = direcciones.get(dirName);

        if (frames.isEmpty()) {
            return null;
        }

        // Para walk/idle usar frame específico, para shoot solo usar frame 0
        int frameIndex = (estado == AnimationState.SHOOT) ? 0 : frame;

        return frames.get(frameIndex % frames.size());
    }

    /**
     * Obtener la cantidad de frames de una animación
     */
    public static int getCantidadFrames(String personaje, AnimationState estado, Direction direccion) {
        String nombrePersonaje = getNombrePersonaje(personaje);

        if (!spriteCache.containsKey(nombrePersonaje)) {
            return (estado == AnimationState.SHOOT) ? 1 : 2;
        }

        String tipoAnim = estado.name().toLowerCase();
        String dirName = direccion.name().toLowerCase();

        Map<String, Map<String, List<BufferedImage>>> animaciones = spriteCache.get(nombrePersonaje);

        if (!animaciones.containsKey(tipoAnim)) {
            return (estado == AnimationState.SHOOT) ? 1 : 2;
        }

        Map<String, List<BufferedImage>> direcciones = animaciones.get(tipoAnim);

        if (!direcciones.containsKey(dirName)) {
            return (estado == AnimationState.SHOOT) ? 1 : 2;
        }

        return direcciones.get(dirName).size();
    }

    /**
     * Convertir nombre de personaje a nombre de carpeta
     */
    private static String getNombrePersonaje(String personaje) {
        switch (personaje.toLowerCase()) {
            case "mapache": return "mapache";
            case "gato": return "gato";
            case "rana": return "rana";
            case "collie": return "collie";
            default: return personaje.toLowerCase();
        }
    }

    /**
     * Inicializar cache con personajes por defecto
     */
    private static void inicializarCache() {
        cacheInicializada = true;

        // Cargar todos los personajes disponibles
        cargarSpritesPersonaje("mapache");
        cargarSpritesPersonaje("gato");
        cargarSpritesPersonaje("rana");
        cargarSpritesPersonaje("collie");

        System.out.println("[SPRITE LOADER] Sprites cargados correctamente");
    }

    /**
     * Precargar todos los sprites al iniciar el juego
     */
    public static void precargarTodosLosSprites() {
        inicializarCache();
    }

    /**
     * Verificar si un sprite existe
     */
    public static boolean existeSprite(String personaje, AnimationState estado, Direction direccion) {
        return obtenerSprite(personaje, estado, direccion, 0) != null;
    }
}