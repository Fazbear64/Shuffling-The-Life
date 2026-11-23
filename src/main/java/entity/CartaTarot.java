package entity;

import java.util.*;

/**
 * Representa una carta de tarot o comodín de poker que se ofrece en la tienda.
 * Cada carta tiene un efecto que puede ser positivo, negativo o 50/50.
 * El jugador elige una de 3 opciones aleatorias antes de salir de la tienda.
 */
public class CartaTarot {
    // Efectos posibles de la carta
    public static final String EFECTO_POSITIVO = "positivo";
    public static final String EFECTO_NEGATIVO = "negativo";
    public static final String EFECTO_5050 = "50-50";
    public static final String EFECTO_LEGENDARIO = "legendario";

    // Atributos de la carta
    private String nombre;
    private String efecto; // positivo, negativo, 50-50, legendario
    private float valor; // magnitud del efecto (puede ser porcentaje o valor absoluto)
    private String tipoEfecto; // descripción textual del efecto
    private boolean esComodin; // true si es comodín, false si es tarot

    // 26 comodines de poker (As a Rey de Corazones y Espadas)
    private static final String[] COMODIN_NOMBRES = {
        "As de Corazones", "2 de Corazones", "3 de Corazones", "4 de Corazones",
        "5 de Corazones", "6 de Corazones", "7 de Corazones", "8 de Corazones",
        "9 de Corazones", "10 de Corazones", "J de Corazones", "Q de Corazones", "K de Corazones",
        "As de Espadas", "2 de Espadas", "3 de Espadas", "4 de Espadas",
        "5 de Espadas", "6 de Espadas", "7 de Espadas", "8 de Espadas",
        "9 de Espadas", "10 de Espadas", "J de Espadas", "Q de Espadas", "K de Espadas"
    };

    private static final String[] COMODIN_EFECTOS = {
        "+30% Vida Max",           // As de Corazones
        "+8% Vida Max",            // 2 de Corazones
        "+12% Vida Max",           // 3 de Corazones
        "+15% Vida Max",           // 4 de Corazones
        "Te curas +40% más rápido", // 5 de Corazones
        "Los enemigos reciben -20% daño", // 6 de Corazones
        "+10% Velocidad",          // 7 de Corazones
        "+18% Daño general",       // 8 de Corazones
        "+25% Daño general",       // 9 de Corazones
        "+35% Daño general",       // 10 de Corazones
        "+50% Daño con Revolver",  // J de Corazones
        "+50% Daño con Escopeta",  // Q de Corazones
        "+50% Daño con Minigun",   // K de Corazones
        "-15% Vida Max",           // As de Espadas
        "-5% Vida Max",            // 2 de Espadas
        "-8% Vida Max",            // 3 de Espadas
        "-10% Vida Max",           // 4 de Espadas
        "Los enemigos causan +30% daño", // 5 de Espadas
        "Los enemigos son más fuertes", // 6 de Espadas
        "-8% Velocidad",           // 7 de Espadas
        "-12% Daño general",       // 8 de Espadas
        "-18% Daño general",       // 9 de Espadas
        "-25% Daño general",       // 10 de Espadas
        "-30% Velocidad",          // J de Espadas
        "-40% Vida al disparar",   // Q de Espadas
        "Cambias de arma aleatoriamente" // K de Espadas
    };

    // Constructor
    public CartaTarot(String nombre, String efecto, float valor, String tipoEfecto) {
        this(nombre, efecto, valor, tipoEfecto, false);
    }

    public CartaTarot(String nombre, String efecto, float valor, String tipoEfecto, boolean esComodin) {
        this.nombre = nombre;
        this.efecto = efecto;
        this.valor = valor;
        this.tipoEfecto = tipoEfecto;
        this.esComodin = esComodin;
    }

    // Getters
    public String getNombre() {
        return nombre;
    }

    public String getEfecto() {
        return efecto;
    }

    public float getValor() {
        return valor;
    }

    public String getTipoEfecto() {
        return tipoEfecto;
    }

    public boolean isComodin() {
        return esComodin;
    }

    public dto.CartaTarotDTO toDTO() {
        return new dto.CartaTarotDTO(nombre, efecto, valor, tipoEfecto);
    }

    public static CartaTarot fromDTO(dto.CartaTarotDTO dto) {
        if (dto == null) return null;
        return new CartaTarot(dto.getNombre(), dto.getEfecto(), dto.getValor(), dto.getTipoEfecto());
    }

    // Generar comodín poker por índice (0-25)
    public static CartaTarot generarComodinPorIndice(int indice) {
        if (indice < 0 || indice >= COMODIN_NOMBRES.length) return null;

        String nombre = COMODIN_NOMBRES[indice];
        String efecto_txt = COMODIN_EFECTOS[indice];
        boolean esCorazon = indice < 13;

        CartaTarot carta = null;
        switch (indice) {
            case 0: // As de Corazones: +30% Vida
                carta = new CartaTarot(nombre, EFECTO_POSITIVO, 0.30f, "vida", true);
                break;
            case 1: // 2 de Corazones: +8% Vida
                carta = new CartaTarot(nombre, EFECTO_POSITIVO, 0.08f, "vida", true);
                break;
            case 2: // 3 de Corazones: +12% Vida
                carta = new CartaTarot(nombre, EFECTO_POSITIVO, 0.12f, "vida", true);
                break;
            case 3: // 4 de Corazones: +15% Vida
                carta = new CartaTarot(nombre, EFECTO_POSITIVO, 0.15f, "vida", true);
                break;
            case 4: // 5 de Corazones: +40% Curación
                carta = new CartaTarot(nombre, EFECTO_POSITIVO, 0.40f, "curacion", true);
                break;
            case 5: // 6 de Corazones: -20% daño enemigos
                carta = new CartaTarot(nombre, EFECTO_POSITIVO, 0.20f, "defensa-enemigos", true);
                break;
            case 6: // 7 de Corazones: +10% Velocidad
                carta = new CartaTarot(nombre, EFECTO_POSITIVO, 0.10f, "velocidad", true);
                break;
            case 7: // 8 de Corazones: +18% Daño
                carta = new CartaTarot(nombre, EFECTO_POSITIVO, 0.18f, "daño", true);
                break;
            case 8: // 9 de Corazones: +25% Daño
                carta = new CartaTarot(nombre, EFECTO_POSITIVO, 0.25f, "daño", true);
                break;
            case 9: // 10 de Corazones: +35% Daño
                carta = new CartaTarot(nombre, EFECTO_POSITIVO, 0.35f, "daño", true);
                break;
            case 10: // J de Corazones: +50% Daño Revolver
                carta = new CartaTarot(nombre, EFECTO_POSITIVO, 0.50f, "daño-revolver", true);
                break;
            case 11: // Q de Corazones: +50% Daño Escopeta
                carta = new CartaTarot(nombre, EFECTO_POSITIVO, 0.50f, "daño-escopeta", true);
                break;
            case 12: // K de Corazones: +50% Daño Minigun
                carta = new CartaTarot(nombre, EFECTO_POSITIVO, 0.50f, "daño-minigun", true);
                break;
            case 13: // As de Espadas: -15% Vida
                carta = new CartaTarot(nombre, EFECTO_NEGATIVO, -0.15f, "vida", true);
                break;
            case 14: // 2 de Espadas: -5% Vida
                carta = new CartaTarot(nombre, EFECTO_NEGATIVO, -0.05f, "vida", true);
                break;
            case 15: // 3 de Espadas: -8% Vida
                carta = new CartaTarot(nombre, EFECTO_NEGATIVO, -0.08f, "vida", true);
                break;
            case 16: // 4 de Espadas: -10% Vida
                carta = new CartaTarot(nombre, EFECTO_NEGATIVO, -0.10f, "vida", true);
                break;
            case 17: // 5 de Espadas: +30% daño enemigos
                carta = new CartaTarot(nombre, EFECTO_NEGATIVO, 0.30f, "debilidad-enemigos", true);
                break;
            case 18: // 6 de Espadas: enemigos más fuertes
                carta = new CartaTarot(nombre, EFECTO_NEGATIVO, 0.25f, "enemigos-fuertes", true);
                break;
            case 19: // 7 de Espadas: -8% Velocidad
                carta = new CartaTarot(nombre, EFECTO_NEGATIVO, -0.08f, "velocidad", true);
                break;
            case 20: // 8 de Espadas: -12% Daño
                carta = new CartaTarot(nombre, EFECTO_NEGATIVO, -0.12f, "daño", true);
                break;
            case 21: // 9 de Espadas: -18% Daño
                carta = new CartaTarot(nombre, EFECTO_NEGATIVO, -0.18f, "daño", true);
                break;
            case 22: // 10 de Espadas: -25% Daño
                carta = new CartaTarot(nombre, EFECTO_NEGATIVO, -0.25f, "daño", true);
                break;
            case 23: // J de Espadas: -30% Velocidad
                carta = new CartaTarot(nombre, EFECTO_NEGATIVO, -0.30f, "velocidad", true);
                break;
            case 24: // Q de Espadas: -40% Vida al disparar
                carta = new CartaTarot(nombre, EFECTO_5050, -0.40f, "vida-disparo", true);
                break;
            case 25: // K de Espadas: cambio arma aleatorio
                carta = new CartaTarot(nombre, EFECTO_NEGATIVO, 0f, "cambio-arma", true);
                break;
        }

        return carta;
    }

    // Generar 6 comodines aleatorios sin repetir
    public static CartaTarot[] generar6ComidinesAleatorios(Set<Integer> usados) {
        CartaTarot[] comodines = new CartaTarot[6];
        List<Integer> disponibles = new ArrayList<>();

        for (int i = 0; i < 26; i++) {
            if (!usados.contains(i)) {
                disponibles.add(i);
            }
        }

        // Si no hay 6 disponibles, resetear y usar todos
        if (disponibles.size() < 6) {
            usados.clear();
            for (int i = 0; i < 26; i++) {
                disponibles.add(i);
            }
        }

        // Mezclar y tomar 6
        Collections.shuffle(disponibles);
        for (int i = 0; i < 6; i++) {
            int idx = disponibles.get(i);
            comodines[i] = generarComodinPorIndice(idx);
            usados.add(idx);
        }

        return comodines;
    }

    // Efectos positivos: vida, daño, velocidad
    public static CartaTarot generarPositivoAleatorio() {
        int tipo = (int) (Math.random() * 3);
        String nombre;
        float valor = 0.1f + (float) (Math.random() * 0.15);

        switch (tipo) {
            case 0:
                nombre = "+" + String.format("%.0f", valor * 100) + "% Vida";
                return new CartaTarot(nombre, EFECTO_POSITIVO, valor, "vida");
            case 1:
                nombre = "+" + String.format("%.0f", valor * 100) + "% Daño";
                return new CartaTarot(nombre, EFECTO_POSITIVO, valor, "daño");
            case 2:
                nombre = "+" + String.format("%.0f", valor * 100) + "% Velocidad";
                return new CartaTarot(nombre, EFECTO_POSITIVO, valor, "velocidad");
            default:
                return null;
        }
    }

    // Efectos negativos: menos vida, menos daño, más lento
    public static CartaTarot generarNegativoAleatorio() {
        int tipo = (int) (Math.random() * 3);
        String nombre;
        float valor = 0.1f + (float) (Math.random() * 0.15);

        switch (tipo) {
            case 0:
                nombre = "-" + String.format("%.0f", valor * 100) + "% Vida";
                return new CartaTarot(nombre, EFECTO_NEGATIVO, -valor, "vida");
            case 1:
                nombre = "-" + String.format("%.0f", valor * 100) + "% Daño";
                return new CartaTarot(nombre, EFECTO_NEGATIVO, -valor, "daño");
            case 2:
                nombre = "-" + String.format("%.0f", valor * 100) + "% Velocidad";
                return new CartaTarot(nombre, EFECTO_NEGATIVO, -valor, "velocidad");
            default:
                return null;
        }
    }

    // Efectos 50/50: mezclados (ej. +daño pero -velocidad) - REBALANCEADOS para que valgan la pena
    public static CartaTarot generar5050Aleatorio() {
        int tipo = (int) (Math.random() * 3);
        String nombre;

        switch (tipo) {
            case 0:
                // +50% daño, -20% velocidad (antes era +10-25% daño con -10-25% vel)
                nombre = "+50% Daño, -20% Velocidad";
                return new CartaTarot(nombre, EFECTO_5050, 0.50f, "daño-velocidad");
            case 1:
                // +60% daño, te quedas quieto (velocidad = 0) - alto riesgo, alta recompensa
                nombre = "+60% Daño, te quedas QUIETO";
                return new CartaTarot(nombre, EFECTO_5050, 0.60f, "daño-quieto");
            case 2:
                // -25% vida, +55% daño (antes era -10-25% vida con +10-25% daño)
                nombre = "-25% Vida, +55% Daño";
                return new CartaTarot(nombre, EFECTO_5050, 0.55f, "vida-daño");
            default:
                return null;
        }
    }

    // Método estático para generar una de 3 opciones aleatorias de TAROT (no comodines)
    public static CartaTarot[] generarOpcionesAleatorias() {
        CartaTarot[] opciones = new CartaTarot[3];

        // Mezclar tipos: puede ser 3 positivos, 3 negativos, o mezcla
        int tipoMezcla = (int) (Math.random() * 6);

        switch (tipoMezcla) {
            case 0: // 3 positivas
                for (int i = 0; i < 3; i++) {
                    opciones[i] = generarPositivoAleatorio();
                }
                break;
            case 1: // 3 negativas
                for (int i = 0; i < 3; i++) {
                    opciones[i] = generarNegativoAleatorio();
                }
                break;
            case 2: // 2 positivas, 1 negativa
                opciones[0] = generarPositivoAleatorio();
                opciones[1] = generarPositivoAleatorio();
                opciones[2] = generarNegativoAleatorio();
                break;
            case 3: // 1 positiva, 2 negativas
                opciones[0] = generarPositivoAleatorio();
                opciones[1] = generarNegativoAleatorio();
                opciones[2] = generarNegativoAleatorio();
                break;
            case 4: // 3 cartas 50/50
                for (int i = 0; i < 3; i++) {
                    opciones[i] = generar5050Aleatorio();
                }
                break;
            case 5: // Mezcla total (1 pos, 1 neg, 1 50/50)
                opciones[0] = generarPositivoAleatorio();
                opciones[1] = generarNegativoAleatorio();
                opciones[2] = generar5050Aleatorio();
                break;
            default:
                for (int i = 0; i < 3; i++) {
                    opciones[i] = generarPositivoAleatorio();
                }
                break;
        }

        return opciones;
    }

    @Override
    public String toString() {
        return nombre + " [" + efecto + ": " + String.format("%.1f", valor * 100) + "%]";
    }
}