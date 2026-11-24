package visual;

import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * Cargador centralizado de fonts desde resources/fonts/
 * Soporta .ttf (TrueType) y .otf (OpenType)
 * Uso: Font gameFont = FontLoader.loadFont("MiFont.otf", 24);
 */
public class FontLoader {
    private static final String FONTS_DIR = "resources/fonts/";

    private FontLoader() {}

    /**
     * Carga una font desde resources/fonts/ (.ttf o .otf)
     * @param fontName Nombre del archivo (ej: "MiFont.otf" o "Roboto-Bold.ttf")
     * @param size Tamaño en puntos
     * @return Font cargada, o null si falla
     */
    public static Font loadFont(String fontName, int size) {
        try {
            File fontFile = new File(FONTS_DIR + fontName);
            if (!fontFile.exists()) {
                System.err.println("[FontLoader] ⚠️ Font no encontrada: " + fontFile.getAbsolutePath());
                return getDefaultFont(size);
            }

            // Font.TRUETYPE_FONT soporta tanto .ttf como .otf
            Font font = Font.createFont(Font.TRUETYPE_FONT, fontFile);
            System.out.println("[FontLoader] ✓ Font cargada: " + fontName + " (" + size + "pt)");
            return font.deriveFont(Font.PLAIN, size);
        } catch (FontFormatException e) {
            System.err.println("[FontLoader] ❌ Formato de font inválido: " + fontName);
            System.err.println("   Debe ser .ttf (TrueType) o .otf (OpenType)");
            System.err.println("   Error: " + e.getMessage());
            return getDefaultFont(size);
        } catch (IOException e) {
            System.err.println("[FontLoader] ❌ Error al cargar font: " + fontName);
            System.err.println("   " + e.getMessage());
            return getDefaultFont(size);
        }
    }

    /**
     * Carga una font bold
     */
    public static Font loadFontBold(String fontName, int size) {
        Font font = loadFont(fontName, size);
        if (font != null) {
            return font.deriveFont(Font.BOLD);
        }
        return getDefaultFontBold(size);
    }

    /**
     * Font por defecto (Arial del sistema)
     */
    private static Font getDefaultFont(int size) {
        System.out.println("[FontLoader] ℹ️ Usando font del sistema (Arial)");
        return new Font("Arial", Font.PLAIN, size);
    }

    /**
     * Font por defecto bold
     */
    private static Font getDefaultFontBold(int size) {
        return new Font("Arial", Font.BOLD, size);
    }

    /**
     * Lista todas las fonts disponibles en resources/fonts/
     */
    public static void listAvailableFonts() {
        File fontsDir = new File(FONTS_DIR);
        if (!fontsDir.exists() || !fontsDir.isDirectory()) {
            System.out.println("[FontLoader] ⚠️ Carpeta de fonts no encontrada: " + FONTS_DIR);
            return;
        }

        File[] fontFiles = fontsDir.listFiles((dir, name) ->
            name.endsWith(".ttf") || name.endsWith(".otf") ||
            name.endsWith(".TTF") || name.endsWith(".OTF")
        );

        if (fontFiles == null || fontFiles.length == 0) {
            System.out.println("[FontLoader] ⚠️ No hay fonts en: " + FONTS_DIR);
            return;
        }

        System.out.println("[FontLoader] 📋 Fonts disponibles (" + fontFiles.length + "):");
        for (File font : fontFiles) {
            long sizeKB = font.length() / 1024;
            System.out.println("  ✓ " + font.getName() + " (" + sizeKB + " KB)");
        }
    }
}
