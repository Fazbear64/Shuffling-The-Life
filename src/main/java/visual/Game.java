package visual;

import DataBase.RankingService;
import config.DefaultSettings;
import entity.Arma;
import entity.Botiquin;
import entity.CartaTarot;
import entity.Enemigo;
import entity.GameObject;
import entity.ID;
import entity.Jefe;
import entity.Moneda;
import entity.Personaje;
import entity.Player;
import entity.Proyectil;
import entity.TipoArma;
import logic.Progreso;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

public class Game extends Canvas implements Runnable {
    private Thread thread;
    private boolean running = false;
    private Handler handler;
    private Camera camera;

    // Sistema de progreso
    private Progreso progreso = new Progreso();

    // Ranking online (Neon DB): todo el acceso a red ocurre en un hilo aparte
    private final RankingService ranking = new RankingService(5);

    // Entrada de nombre
    private String nombreEntrando = "";
    private boolean nombreIngresado = false;

    private int rondaActual = 1;
    private int tiradasTragaperras = 0;
    private int tiendasVisitadasEnPartida = 0;
    private long maxTiempoSobrevividoMs = 0;
    private long partidaStartTime = 0;
    private long tiempoPausadoMs = 0;  // Tiempo acumulado pausado en tiendas

    private static final long DURACION_OLEADA_MS = 60_000L;
    private long oleadaStartTime = 0;
    private long ultimoSpawnEnemigo = 0;

    // Constantes de selección de personaje
    public static final int SEL_ANCHO_PERSONAJE = 160;
    public static final int SEL_ALTO_PERSONAJE = 220;
    public static final int SEL_ESPACIO_ENTRE = 30;
    public static final int SEL_POS_Y = 140;

    // Comodines dinámicos de poker
    private CartaTarot[] comodinesActuales = new CartaTarot[6];
    private Set<Integer> comodinesUsados = new HashSet<>();
    private boolean[] comodinesComprados = new boolean[6];
    private int[] costoComodines = new int[6]; // Costos fijos por tienda

    // Botiquín del tragaperras (efecto especial)
    private Botiquin botiquinTragaperras = null;
    private boolean tieneBotiquinTragaperras = false;

    // Cartas tarot temporales (efectos de tienda a tienda)
    private CartaTarot cartaTarotActual = null;
    private long tiempoAplicacionTarot = 0;

    public enum EstadoJuego {
        CINEMÁTICA,
        ENTRADA_NOMBRE,
        SELECCION_PERSONAJE,
        JUGANDO,
        TRAGAPERAS,
        TIENDA_NORMAL,
        TIENDA_TAROT,
        GAME_OVER
    }
    private EstadoJuego estadoActual = EstadoJuego.CINEMÁTICA;

    private Personaje personajeSeleccionado;
    private CartaTarot[] cartasTarotActuales;
    private Arma armaObtenidaTragaperras;
    private boolean puedeRepetirTragaperras = true;
    private boolean ruletaGirada = false;

    // Disparo sostenido
    private boolean clickPulsado = false;
    public int ultimoMouseWorldX = 0;
    public int ultimoMouseWorldY = 0;

    // Jefe
    private Jefe jefe = null;
    private boolean jefeSpawneado = false;
    private boolean debeSpawnearJefe = false;

    // Control de cinemática
    private long cinemáticaStartTime = 0;
    private static final long DURACION_CINEMÁTICA_MS = 2500;
    private VideoPlayer videoPlayer;

    // Fonts personalizadas
    private Font gameFont;
    private Font titleFont;
    private Font hudFont;

    public Game() {
        handler = new Handler();
        camera = new Camera(0, 0);

        // Cargar fonts personalizadas desde resources/fonts/
        this.gameFont = FontLoader.loadFont("MiFont.otf", 20);
        this.titleFont = FontLoader.loadFont("MiFont.otf", 48);
        this.hudFont = FontLoader.loadFont("MiFont.otf", 14);

        // Listar fonts disponibles (debug)
        FontLoader.listAvailableFonts();

        // Verificar si la cinemática ya fue mostrada
        if (progreso.isCinematicaMostrada()) {
            // Ya se mostró antes, ir directo a selección de personaje o entrada de nombre
            String nombreGuardado = progreso.getNombreJugador();
            if (nombreGuardado != null && !nombreGuardado.equals("Jugador")) {
                nombreEntrando = nombreGuardado;
                nombreIngresado = true;
                estadoActual = EstadoJuego.SELECCION_PERSONAJE;
                System.out.println("[PROGRESO] Nombre cargado: " + nombreGuardado);
            } else {
                estadoActual = EstadoJuego.ENTRADA_NOMBRE;
            }
        } else {
            // Primera vez: mostrar cinemática
            cinemáticaStartTime = System.currentTimeMillis();
            videoPlayer = new VideoPlayer();
            videoPlayer.playVideo();
            estadoActual = EstadoJuego.CINEMÁTICA;
            progreso.marcarCinematicaMostrada();
        }

        // Cargar ranking en hilo separado (asíncrono)
        ranking.refrescar(null);

        new Window(this);
    }

    public synchronized void start(){
        thread = new Thread(this);
        thread.start();
        running = true;
    }

    public synchronized void stop(){
        try{
            thread.join();
            running = false;
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void run() {
        long lastTime = System.nanoTime();
        double amountOfTicks = 60.0;
        double ns = 1000000000 / amountOfTicks;
        double delta = 0;
        long timer = System.currentTimeMillis();
        int frames = 0;

        while (running){
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;

            while (delta >= 1) {
                tick();
                delta--;
                actualizarEstadoJuego();
            }

            if (running)
                render();

            frames++;

            if (System.currentTimeMillis() - timer > 1000){
                timer += 1000;
                frames = 0;
            }
        }
        stop();
    }

    private void tick(){
        if (estadoActual != EstadoJuego.JUGANDO) {
            return;
        }

        handler.tick();

        Player player = handler.getPlayer();
        if (player != null) {
            camera.tick(player);

            if (player.estaMuerto()) {
                estadoActual = EstadoJuego.GAME_OVER;
                registrarRecordFinPartida();
                return;
            }

            // Hacer que el jefe siga al jugador
            if (jefe != null) {
                if (!jefe.estaMuerto()) {
                    jefe.setTarget(player.getX() + 16, player.getY() + 16);
                } else {
                    jefe = null;
                }
            }
        }

        if (partidaStartTime > 0) {
            long tiempoVivoActual = System.currentTimeMillis() - partidaStartTime;
            if (tiempoVivoActual > maxTiempoSobrevividoMs) {
                maxTiempoSobrevividoMs = tiempoVivoActual;
            }
        }

        // Disparo sostenido para lanzallamas y minigun
        if (clickPulsado && player != null) {
            Arma arma = player.getArmaEquipada();
            if (arma != null && (arma.getTipo() == TipoArma.LANZALLAMAS || arma.getTipo() == TipoArma.MINIGUN)) {
                dispararHacia(ultimoMouseWorldX, ultimoMouseWorldY);
            }
        }

        long tiempoActual = System.currentTimeMillis();
        // Spawn más frecuente: cada 1.5 segundos en lugar de 2.5
        long intervaloSpawn = Math.max(800L, 1500L - (rondaActual - 1) * 100L);
        if (tiempoActual - ultimoSpawnEnemigo > intervaloSpawn && player != null) {
            spawnEnemigoCercanoAlJugador(player.getX(), player.getY());
            ultimoSpawnEnemigo = tiempoActual;
        }
    }

    private void spawnEnemigoCercanoAlJugador(int px, int py) {
        double angulo = Math.random() * Math.PI * 2;
        int distancia = 500 + (int)(Math.random() * 200);

        int ex = px + (int)(Math.cos(angulo) * distancia);
        int ey = py + (int)(Math.sin(angulo) * distancia);
        ex = Math.max(50, Math.min(DefaultSettings.worldWidth - 50, ex));
        ey = Math.max(50, Math.min(DefaultSettings.worldHeight - 50, ey));

        double rand = Math.random();
        ID tipoEnemigo;

        // 40% Normal, 20% Veloz, 20% Tanque, 20% Explosivo
        if (rand < 0.40) {
            tipoEnemigo = ID.ENEMIGO;         // Normal (40%)
        } else if (rand < 0.60) {
            tipoEnemigo = ID.ENEMIGO_VELOZ;  // Veloz (20%)
        } else if (rand < 0.80) {
            tipoEnemigo = ID.ENEMIGO_TANQUE; // Tanque (20%)
        } else {
            tipoEnemigo = ID.ENEMIGO_EXPLOSIVO; // Explosivo (20%)
        }

        handler.addObject(new Enemigo(ex, ey, tipoEnemigo));
    }

    public void dispararHacia(int mouseWorldX, int mouseWorldY) {
        Player player = handler.getPlayer();
        if (player != null && estadoActual == EstadoJuego.JUGANDO) {
            if (!player.puedeDisparar()) {
                return;
            }
            player.registrarDisparo();

            Arma arma = player.getArmaEquipada();
            if (arma == null) {
                arma = Arma.crearRevolver();
                player.equiparArma(arma);
            }

            int px = player.getX() + 16;
            int py = player.getY() + 16;
            double anguloBase = Math.atan2(mouseWorldY - py, mouseWorldX - px);

            int dañoPorProyectil = player.calcularDañoParaArma(arma);
            int cantidad = arma.getProyectilesPorDisparo();
            double dispersion = arma.getDispersion();

            if (cantidad <= 1) {
                double spread = (Math.random() - 0.5) * dispersion;
                handler.addObject(new Proyectil(px - 5, py - 5, ID.ARMA, anguloBase + spread, dañoPorProyectil, arma));
            } else {
                double step = dispersion / (cantidad - 1);
                double startAngle = anguloBase - (dispersion / 2.0);
                for (int i = 0; i < cantidad; i++) {
                    double angle = startAngle + (i * step) + ((Math.random() - 0.5) * 0.05);
                    handler.addObject(new Proyectil(px - 5, py - 5, ID.ARMA, angle, dañoPorProyectil, arma));
                }
            }
        }
    }

    public void registrarClickPulsado(int mouseWorldX, int mouseWorldY) {
        this.clickPulsado = true;
        this.ultimoMouseWorldX = mouseWorldX;
        this.ultimoMouseWorldY = mouseWorldY;
    }

    public void registrarClickSoltado() {
        this.clickPulsado = false;
    }

    private void render(){
        BufferStrategy bs = this.getBufferStrategy();
        if (bs == null){
            this.createBufferStrategy(3);
            return;
        }

        Graphics g = bs.getDrawGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, DefaultSettings.screenWidth, DefaultSettings.screenHeight);

        if (estadoActual == EstadoJuego.CINEMÁTICA) {
            renderCinemática(g);
        } else if (estadoActual == EstadoJuego.ENTRADA_NOMBRE) {
            renderEntradaNombre(g);
        } else if (estadoActual == EstadoJuego.SELECCION_PERSONAJE) {
            renderSeleccionPersonaje(g);
        } else if (estadoActual == EstadoJuego.TRAGAPERAS) {
            renderTragaperras(g);
        } else if (estadoActual == EstadoJuego.TIENDA_NORMAL) {
            renderTiendaNormal(g);
        } else if (estadoActual == EstadoJuego.TIENDA_TAROT) {
            renderTiendaTarot(g);
        } else if (estadoActual == EstadoJuego.GAME_OVER) {
            renderGameOver(g);
        } else if (estadoActual == EstadoJuego.JUGANDO) {
            Graphics2D g2d = (Graphics2D) g;

            // Primero mover la cámara
            g2d.translate(-camera.getX(), -camera.getY());

            // Luego dibujar el fondo (ahora sí en la posición correcta del mundo)
            BackgroundLoader.render(g2d, camera);

            // Dibujar objetos del juego
            handler.render(g2d);

            // Restaurar cámara
            g2d.translate(camera.getX(), camera.getY());

            // HUD Limpio: Solo RONDA y Tiempo Vivo
            // RONDA - Arriba, grande, centrado, rojo
            g.setColor(Color.RED);
            g.setFont(titleFont);
            String roundText = "RONDA " + rondaActual;
            int roundWidth = g.getFontMetrics().stringWidth(roundText);
            g.drawString(roundText, DefaultSettings.screenWidth / 2 - roundWidth / 2, 60);

            // Tiempo vivo - Abajo izquierda, pequeño
            long tiempoTranscurridoMs = System.currentTimeMillis() - partidaStartTime;
            long segundosSobrevividos = tiempoTranscurridoMs / 1000;
            long minutos = segundosSobrevividos / 60;
            long segundos = segundosSobrevividos % 60;

            g.setColor(Color.WHITE);
            g.setFont(hudFont);
            g.drawString(String.format("%02d:%02d", minutos, segundos), 20, DefaultSettings.screenHeight - 20);

            // Inventario de botiquines
            Player playerHUD = handler.getPlayer();
            if (playerHUD != null) {
                int cantBotiquines = playerHUD.getBotiquines();
                if (cantBotiquines > 0) {
                    g.setColor(Color.MAGENTA);
                    g.drawString("♥ Botiquines: " + cantBotiquines + " [B]", 90, DefaultSettings.screenHeight - 20);
                } else {
                    g.setColor(Color.GRAY);
                    g.drawString("♥ Botiquines: 0", 90, DefaultSettings.screenHeight - 20);
                }
            }
        }

        g.dispose();
        bs.show();
    }

    private void renderCinemática(Graphics g) {
        int centerX = DefaultSettings.screenWidth / 2;
        int centerY = DefaultSettings.screenHeight / 2;

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, DefaultSettings.screenWidth, DefaultSettings.screenHeight);

        g.setColor(Color.WHITE);
        g.setFont(titleFont);
        g.drawString("🎬 SHUFFLING THE LIFE 🎬", centerX - 300, centerY - 100);

        g.setFont(gameFont);
        g.drawString("Reproduciendo cinemática...", centerX - 200, centerY);

        // Animación de puntos
        long tiempoTranscurrido = System.currentTimeMillis() - cinemáticaStartTime;
        int puntos = (int) ((tiempoTranscurrido / 500) % 4);
        String textoPuntos = ".".repeat(puntos);

        g.setFont(gameFont);
        g.drawString("Por favor espera" + textoPuntos, centerX - 150, centerY + 80);

        // Cambiar a ENTRADA_NOMBRE después de 8 segundos (tiempo aproximado del video)
        if (tiempoTranscurrido >= 8000) {
            if (videoPlayer != null) {
                videoPlayer.stopVideo();
            }
            estadoActual = EstadoJuego.ENTRADA_NOMBRE;
        }
    }

    private void actualizarEstadoJuego() {
        if (estadoActual == EstadoJuego.JUGANDO && oleadaStartTime > 0) {
            if (System.currentTimeMillis() - oleadaStartTime >= DURACION_OLEADA_MS) {
                completarOleada();
            }
        }
    }

    public void completarOleada() {
        rondaActual++;
        tiradasTragaperras++;

        // Cada oleada hay una muy baja probabilidad de que un enemigo suelte botiquín
        double rand = Math.random();
        if (rand < 0.05) { // ~5% por oleada = aprox 1 cada 20 oleadas
            Player player = handler.getPlayer();
            if (player != null) {
                int x = player.getX() + 16;
                int y = player.getY() + 16;
                handler.addObject(new Botiquin(x, y, ID.MONEDA));
                System.out.println("¡Un enemigo dejó caer un botiquín!");
            }
        }

        // Si el jefe está vivo, no abrir tienda/tragaperras
        if (jefe != null && !jefe.estaMuerto()) {
            oleadaStartTime = System.currentTimeMillis();
            return;
        }

        // Resetear bandera de jefe si murió
        if (jefe != null && jefe.estaMuerto()) {
            jefe = null;
        }

        // Cada 3 oleadas hay un evento: tragaperras 1, tragaperras 2, tienda
        if (tiradasTragaperras == 1 || tiradasTragaperras == 2) {
            abrirTragaperras();
        } else {
            abrirTiendaNormal();
        }

        oleadaStartTime = System.currentTimeMillis();
    }

    private void abrirTragaperras() {
        ruletaGirada = false;
        armaObtenidaTragaperras = null;
        tieneBotiquinTragaperras = false;
        tiempoPausadoMs = System.currentTimeMillis();  // Pausar tiempo
        estadoActual = EstadoJuego.TRAGAPERAS;
    }

    private void girarRuletaTragaperras() {
        double rand = Math.random();
        Player player = handler.getPlayer();

        // 10% botiquín, 75% nada (sigues con tu arma), 15% arma nueva PERMANENTE
        if (rand < 0.10) {
            // BOTIQUÍN (10%)
            if (player != null) {
                int x = player.getX() + 16;
                int y = player.getY() + 16;
                botiquinTragaperras = new Botiquin(x, y, ID.MONEDA);
                handler.addObject(botiquinTragaperras);
                tieneBotiquinTragaperras = true;
                armaObtenidaTragaperras = null; // NO hay arma
                System.out.println("[DEBUG TRAGAPERRAS] ¡Botiquín! (sin arma)");
            }
        } else if (rand < 0.85) {
            // NADA (75%) - sigues con tu arma actual
            tieneBotiquinTragaperras = false;
            armaObtenidaTragaperras = null; // NULL significa "no cambiar arma"
            System.out.println("[DEBUG TRAGAPERRAS] ¡Nada! Sigues con tu arma actual");
        } else {
            // ARMA NUEVA PERMANENTE (15%)
            this.armaObtenidaTragaperras = Arma.sortearArmaAleatoria();
            if (player != null) {
                progreso.desbloquearArma(armaObtenidaTragaperras.getTipo());
                player.equiparArma(armaObtenidaTragaperras);
            }
            tieneBotiquinTragaperras = false;
            System.out.println("[DEBUG TRAGAPERRAS] ¡Arma nueva DESBLOQUEADA! " + armaObtenidaTragaperras.getNombre());
        }
        ruletaGirada = true;
    }

    private void abrirTiendaNormal() {
        comodinesActuales = CartaTarot.generar6ComidinesAleatorios(comodinesUsados);
        comodinesComprados = new boolean[6];
        cartaTarotActual = null;
        debeSpawnearJefe = true;
        tiempoPausadoMs = System.currentTimeMillis();  // Pausar tiempo

        // Generar costos FIJOS para esta tienda
        for (int i = 0; i < 6; i++) {
            costoComodines[i] = 70 + (i * 5);
        }

        // Desbloquear personaje según tiendas visitadas en esta partida
        tiendasVisitadasEnPartida++;
        int tiendaActual = tiendasVisitadasEnPartida;
        progreso.desbloquearPorTienda(tiendaActual);
        System.out.println("Tienda " + tiendaActual + " visitada. Personajes desbloqueados: " + progreso.getPersonajesDesbloqueados());

        // Spawn de gusanos escalado según tiendas visitadas (1 tras tienda 1, 2 tras tienda 2, etc., máximo 4)
        int numGusanos = Math.min(4, tiendaActual);
        if (numGusanos > 0) {
            Player player = handler.getPlayer();
            if (player != null) {
                for (int i = 0; i < numGusanos; i++) {
                    spawnGusanoCercaDelJugador(player.getX(), player.getY());
                }
                System.out.println("[DEBUG] Spawneados " + numGusanos + " gusanos tras tienda " + tiendaActual);
            }
        }

        estadoActual = EstadoJuego.TIENDA_NORMAL;
    }

    private void spawnGusanoCercaDelJugador(int px, int py) {
        double angulo = Math.random() * Math.PI * 2;
        int distancia = 300 + (int)(Math.random() * 150);

        int ex = px + (int)(Math.cos(angulo) * distancia);
        int ey = py + (int)(Math.sin(angulo) * distancia);
        ex = Math.max(50, Math.min(DefaultSettings.worldWidth - 50, ex));
        ey = Math.max(50, Math.min(DefaultSettings.worldHeight - 50, ey));

        handler.addObject(new Enemigo(ex, ey, ID.ENEMIGO_VELOZ, true)); // Gusanos de tienda con despawn de 15s
    }

    private void abrirTiendaTarot() {
        cartasTarotActuales = CartaTarot.generarOpcionesAleatorias();
        tiempoPausadoMs = System.currentTimeMillis();  // Pausar tiempo
        estadoActual = EstadoJuego.TIENDA_TAROT;
    }

    private void spawnearJefe() {
        Player player = handler.getPlayer();
        if (player != null) {
            jefe = new Jefe(
                DefaultSettings.worldWidth / 2,
                DefaultSettings.worldHeight / 2
            );
            handler.addObject(jefe);
            System.out.println("¡¡¡JEFE APARECIÓ!!! Ronda: " + rondaActual);
        }
    }

    public void volverAJugar() {
        Player player = handler.getPlayer();
        if (player != null) {
            player.resetMovement();
        }

        // RESETEAR el estado de click sostenido al volver al juego
        clickPulsado = false;

        // Reanudar tiempo: agregar el tiempo pausado al partidaStartTime
        if (tiempoPausadoMs > 0) {
            long tiempoPausaTotal = System.currentTimeMillis() - tiempoPausadoMs;
            partidaStartTime += tiempoPausaTotal;
            tiempoPausadoMs = 0;
        }

        // Si venimos de tienda tarot, spawneamos el jefe
        if (debeSpawnearJefe) {
            spawnearJefe();
            debeSpawnearJefe = false;
        }

        long ahora = System.currentTimeMillis();
        oleadaStartTime = ahora;
        ultimoSpawnEnemigo = ahora;
        estadoActual = EstadoJuego.JUGANDO;
        this.requestFocus();
        this.requestFocusInWindow();
    }

    public void manejarTeclaEspecial(int keyCode) {
        if (estadoActual == EstadoJuego.TRAGAPERAS) {
            Player player = handler.getPlayer();

            if (!ruletaGirada) {
                if (keyCode == KeyEvent.VK_SPACE || keyCode == KeyEvent.VK_ENTER) {
                    girarRuletaTragaperras();
                }
                return;
            }

            // R para repetir tirada (10 monedas)
            if (keyCode == KeyEvent.VK_R && player != null && puedeRepetirTragaperras) {
                if (player.gastarMonedas(10)) {
                    // Limpiar botiquín anterior si existía
                    if (botiquinTragaperras != null) {
                        handler.deleteObject(botiquinTragaperras);
                        botiquinTragaperras = null;
                    }

                    girarRuletaTragaperras();
                    puedeRepetirTragaperras = true;
                } else {
                    System.out.println("Monedas insuficientes para repetir (necesitas 10)");
                }
                return;
            }

            // SPACE o ENTER para confirmar y volver al juego
            if (keyCode == KeyEvent.VK_SPACE || keyCode == KeyEvent.VK_ENTER) {
                puedeRepetirTragaperras = true;
                volverAJugar();
            }

        } else if (estadoActual == EstadoJuego.TIENDA_NORMAL) {
            Player player = handler.getPlayer();

            // Teclas 1-6: comprar comodín correspondiente
            if (keyCode >= KeyEvent.VK_1 && keyCode <= KeyEvent.VK_6) {
                int idx = keyCode - KeyEvent.VK_1;
                if (!comodinesComprados[idx] && player != null && comodinesActuales[idx] != null) {
                    CartaTarot comodin = comodinesActuales[idx];
                    int costo = costoComodines[idx];

                    if (player.gastarMonedas(costo)) {
                        comodinesComprados[idx] = true;
                        aplicarComodin(player, comodin);
                        System.out.println("Comprado: " + comodin.getNombre());
                    } else {
                        System.out.println("Monedas insuficientes para: " + comodin.getNombre());
                    }
                } else if (comodinesComprados[idx]) {
                    System.out.println("Ya compraste este comodín");
                }
            }
            // ENTER/ESPACIO: avanzar a la tienda de tarot (OBLIGATORIO)
            if (keyCode == KeyEvent.VK_SPACE || keyCode == KeyEvent.VK_ENTER) {
                abrirTiendaTarot();
            }
            // ESC ya no permite salir

        } else if (estadoActual == EstadoJuego.TIENDA_TAROT) {
            // ESC, ENTER o ESPACIO: NO permitir salir sin elegir
            if (keyCode == KeyEvent.VK_ESCAPE || keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_SPACE) {
                System.out.println("¡Debes elegir una carta! Presiona 1, 2 o 3");
                return;
            }

            // Teclas 1-3: elegir carta (OBLIGATORIO)
            if (keyCode >= KeyEvent.VK_1 && keyCode <= KeyEvent.VK_3) {
                int idx = keyCode - KeyEvent.VK_1;
                Player player = handler.getPlayer();
                if (cartasTarotActuales != null && cartasTarotActuales[idx] != null && player != null) {
                    CartaTarot carta = cartasTarotActuales[idx];
                    cartaTarotActual = carta;
                    tiempoAplicacionTarot = System.currentTimeMillis();
                    aplicarComodin(player, carta);
                    System.out.println("Carta elegida: " + carta.getNombre());

                    // Reiniciar ciclo y volver a jugar
                    tiradasTragaperras = 0;
                    volverAJugar();
                }
            }

        } else if (estadoActual == EstadoJuego.JUGANDO) {
            Player player = handler.getPlayer();
            if (player != null) {
                // B para usar botiquín del inventario (recuperar vida completa)
                if (keyCode == KeyEvent.VK_B) {
                    if (player.getBotiquines() <= 0) {
                        System.out.println("No tienes botiquines en el inventario.");
                    } else if (player.getVidaActual() >= player.getVidaMax()) {
                        System.out.println("Tu vida ya está al máximo.");
                    } else {
                        if (player.usarBotiquin()) {
                            System.out.println("¡Botiquín usado! Vida completa recuperada. Te quedan: " + player.getBotiquines());
                        }
                    }
                }
            }
        } else if (estadoActual == EstadoJuego.GAME_OVER) {
            if (keyCode == KeyEvent.VK_R || keyCode == KeyEvent.VK_ENTER) {
                reiniciarPartida();
            }
        }
    }

    private void aplicarComodin(Player player, CartaTarot comodin) {
        if (comodin == null) return;

        String tipo = comodin.getTipoEfecto();
        float valor = comodin.getValor();

        switch (tipo) {
            case "vida":
                player.aplicarCartaTarot(new CartaTarot(comodin.getNombre(), comodin.getEfecto(), valor, "vida"));
                break;
            case "velocidad":
                player.aplicarCartaTarot(new CartaTarot(comodin.getNombre(), comodin.getEfecto(), valor, "velocidad"));
                break;
            case "daño":
                player.aplicarCartaTarot(new CartaTarot(comodin.getNombre(), comodin.getEfecto(), valor, "daño"));
                break;
            case "curacion":
                player.aplicarCartaTarot(new CartaTarot(comodin.getNombre(), comodin.getEfecto(), valor, "curacion"));
                break;
            case "defensa-enemigos":
                player.aplicarCartaTarot(new CartaTarot(comodin.getNombre(), comodin.getEfecto(), valor, "defensa-enemigos"));
                break;
            case "debilidad-enemigos":
                player.aplicarCartaTarot(new CartaTarot(comodin.getNombre(), comodin.getEfecto(), valor, "debilidad-enemigos"));
                break;
            case "daño-revolver":
                player.agregarBonusDañoArma(TipoArma.REVOLVER, valor);
                break;
            case "daño-escopeta":
                player.agregarBonusDañoArma(TipoArma.ESCOPETA, valor);
                break;
            case "daño-minigun":
                player.agregarBonusDañoArma(TipoArma.MINIGUN, valor);
                break;
            case "enemigos-fuertes":
                player.aplicarCartaTarot(new CartaTarot(comodin.getNombre(), comodin.getEfecto(), valor, "enemigos-fuertes"));
                break;
            case "vida-disparo":
                player.aplicarCartaTarot(new CartaTarot(comodin.getNombre(), comodin.getEfecto(), valor, "vida-disparo"));
                break;
            case "cambio-arma":
                player.aplicarCartaTarot(new CartaTarot(comodin.getNombre(), comodin.getEfecto(), valor, "cambio-arma"));
                break;
            case "daño-velocidad":
            case "daño-quieto":
            case "vida-daño":
                player.aplicarCartaTarot(new CartaTarot(comodin.getNombre(), comodin.getEfecto(), valor, tipo));
                break;
        }
    }

    /** Nombre con el que se guarda y se resalta al jugador en el ranking. */
    private String nombreJugadorActual() {
        return nombreIngresado ? nombreEntrando.trim() : progreso.getNombreJugador();
    }

    /**
     * Se llama UNA sola vez por partida, en el instante en que el jugador muere
     * (tick() deja de ejecutarse al pasar a GAME_OVER). Envía los segundos de ESTA
     * partida a Neon DB en segundo plano y luego refresca el ranking mostrado.
     */
    private void registrarRecordFinPartida() {
        long duracionMs = partidaStartTime > 0 ? System.currentTimeMillis() - partidaStartTime : 0;
        int segundos = (int) Math.min(Integer.MAX_VALUE, duracionMs / 1000);
        ranking.guardarYRefrescar(nombreJugadorActual(), segundos);
    }

    private void reiniciarPartida() {
        // Actualizar récord personal antes de reiniciar
        progreso.actualizarPuntuacionMaxima(maxTiempoSobrevividoMs);

        // Resetear variables de juego
        rondaActual = 1;
        tiradasTragaperras = 0;
        tiendasVisitadasEnPartida = 0;
        comodinesUsados.clear();
        comodinesComprados = new boolean[6];
        cartaTarotActual = null;
        armaObtenidaTragaperras = null;
        puedeRepetirTragaperras = true;
        ruletaGirada = false;
        botiquinTragaperras = null;
        tieneBotiquinTragaperras = false;
        clickPulsado = false;
        jefe = null;
        jefeSpawneado = false;
        debeSpawnearJefe = false;
        tiempoPausadoMs = 0;
        tiempoAplicacionTarot = 0;
        cartasTarotActuales = null;
        comodinesActuales = new CartaTarot[6];
        costoComodines = new int[6];
        maxTiempoSobrevividoMs = 0;
        partidaStartTime = 0;
        oleadaStartTime = 0;
        ultimoSpawnEnemigo = 0;

        // IMPORTANTE: Resetear personaje para permitir nueva selección
        personajeSeleccionado = null;

        // Limpiar objetos del handler
        handler.limpiarTodo();

        // Volver a la selección de personaje en cada reinicio
        estadoActual = EstadoJuego.SELECCION_PERSONAJE;
        System.out.println("[DEBUG] Reinicio - Volviendo a selección de personaje");
    }

    private void renderEntradaNombre(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(titleFont);
        g.drawString("BIENVENIDO A SHUFFLING THE LIFE", DefaultSettings.screenWidth/2 - 320, 100);

        g.setFont(gameFont);
        g.drawString("¿Cuál es tu nombre?", DefaultSettings.screenWidth/2 - 140, 180);

        // Caja de entrada
        g.setColor(Color.DARK_GRAY);
        g.fillRect(DefaultSettings.screenWidth/2 - 200, 220, 400, 60);
        g.setColor(Color.WHITE);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRect(DefaultSettings.screenWidth/2 - 200, 220, 400, 60);

        // Texto ingresado
        g.setFont(gameFont);
        g.setColor(Color.YELLOW);
        String textoMostrado = nombreEntrando.isEmpty() ? "Escribe tu nombre..." : nombreEntrando;
        g.drawString(textoMostrado, DefaultSettings.screenWidth/2 - 190, 265);

        // Instrucciones
        g.setFont(hudFont);
        g.setColor(Color.LIGHT_GRAY);
        g.drawString("Presiona [ENTER] para confirmar", DefaultSettings.screenWidth/2 - 160, 350);
        g.drawString("(Tu nombre será guardado en tu perfil)", DefaultSettings.screenWidth/2 - 190, 380);

        RankingRenderer.dibujar(g, ranking.getSnapshot(), null, DefaultSettings.screenWidth / 2, 430);
    }

    // Métodos para manejar la entrada del nombre
    public void agregarCaracterNombre(char c) {
        if (nombreEntrando.length() < 20) { // Límite de 20 caracteres
            nombreEntrando += c;
        }
    }

    public void borrarCaracterNombre() {
        if (!nombreEntrando.isEmpty()) {
            nombreEntrando = nombreEntrando.substring(0, nombreEntrando.length() - 1);
        }
    }

    public void confirmarNombre() {
        if (!nombreEntrando.trim().isEmpty()) {
            nombreIngresado = true;
            // Guardar el nombre en el progreso
            progreso.setNombreJugador(nombreEntrando.trim());
            ranking.refrescar(nombreJugadorActual()); // trae también la marca personal en línea
            // Pasar a la pantalla de selección de personaje
            estadoActual = EstadoJuego.SELECCION_PERSONAJE;
            System.out.println("Nombre ingresado: " + nombreEntrando.trim());
        }
    }

    private void renderSeleccionPersonaje(Graphics g) {
        // Fondo negro
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, DefaultSettings.screenWidth, DefaultSettings.screenHeight);

        // Título "Shuffling-The-Life" en grande (fuente especial)
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(new Color(255, 100, 200)); // Color cirquense/llamativo
        g.setFont(titleFont);
        String titulo = "Shuffling-The-Life";
        int tituloWidth = g.getFontMetrics().stringWidth(titulo);
        g.drawString(titulo, DefaultSettings.screenWidth/2 - tituloWidth/2, 80);

        // Cargar imágenes de personajes
        CharacterImageLoader.cargarTodos();

        // Dibujar personajes
        for (int i = 0; i < 4; i++) {
            java.awt.Rectangle rect = getRectanglePersonaje(i);
            String personaje = CharacterImageLoader.getPersonajePorIndice(i);
            boolean desbloqueado = estaPersonajeDesbloqueado(personaje);

            // Cargar imagen del personaje
            java.awt.image.BufferedImage img = CharacterImageLoader.obtenerImagen(personaje);

            if (desbloqueado && img != null) {
                // DESBLOQUEADO: imagen normal con marco blanco
                g.setColor(Color.WHITE);
                g2d.setStroke(new java.awt.BasicStroke(3));
                g2d.drawRect(rect.x, rect.y, rect.width, rect.height);

                g.drawImage(img, rect.x, rect.y, rect.width, rect.height, null);
            } else if (img != null) {
                // BLOQUEADO: silueta oscura
                g.setColor(new Color(30, 30, 30, 200)); // Gris muy oscuro semitransparente
                g.fillRect(rect.x, rect.y, rect.width, rect.height);

                // Marco gris oscuro
                g.setColor(Color.DARK_GRAY);
                g2d.setStroke(new java.awt.BasicStroke(2));
                g2d.drawRect(rect.x, rect.y, rect.width, rect.height);

                // Dibujar imagen con mucha opacidad reducida para silueta
                g2d.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.3f));
                g.drawImage(img, rect.x, rect.y, rect.width, rect.height, null);
                g2d.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 1.0f));
            } else {
                // Fallback: color de fondo
                g.setColor(new Color(50, 50, 50));
                g.fillRect(rect.x, rect.y, rect.width, rect.height);

                g.setColor(Color.WHITE);
                g.setFont(hudFont);
                String nombre = getNombrePersonajeCapitalizado(personaje);
                int nombreWidth = g.getFontMetrics().stringWidth(nombre);
                g.drawString(nombre, rect.x + (rect.width - nombreWidth)/2, rect.y + rect.height/2);
            }

            // Texto debajo del personaje (nombre)
            g.setColor(desbloqueado ? Color.GREEN : Color.GRAY);
            g.setFont(gameFont);
            String nombre = getNombrePersonajeCapitalizado(personaje);
            int nombreWidth = g.getFontMetrics().stringWidth(nombre);
            g.drawString(nombre, rect.x + (rect.width - nombreWidth)/2, rect.y + rect.height + 25);

            // Texto de bloqueo si está bloqueado
            if (!desbloqueado) {
                g.setColor(Color.RED);
                g.setFont(hudFont);
                String bloqueado = "[BLOQUEADO]";
                int bloqueadoWidth = g.getFontMetrics().stringWidth(bloqueado);
                g.drawString(bloqueado, rect.x + (rect.width - bloqueadoWidth)/2, rect.y + rect.height + 45);
            }
        }

        // Texto "Selecciona el personaje..." debajo
        g.setColor(Color.WHITE);
        g.setFont(gameFont);
        String instruccion = "Haz click en el personaje o presiona [1-4]";
        int instruccionWidth = g.getFontMetrics().stringWidth(instruccion);
        g.drawString(instruccion, DefaultSettings.screenWidth/2 - instruccionWidth/2, 450);
    }

    /**
     * Obtener nombre capitalizado del personaje
     */
    private String getNombrePersonajeCapitalizado(String personaje) {
        if (personaje == null) return "";
        return personaje.substring(0, 1).toUpperCase() + personaje.substring(1);
    }

    /**
     * Verificar si un personaje está desbloqueado
     */
    private boolean estaPersonajeDesbloqueado(String personaje) {
        switch (personaje.toLowerCase()) {
            case "mapache": return true;
            case "gato": return progreso.isGatoDesbloqueado();
            case "rana": return progreso.isRanaDesbloqueada();
            case "collie": return progreso.isColieDesbloqueado();
            default: return false;
        }
    }

    /**
     * Obtener rectangulo de un personaje en la pantalla de selección
     */
    public static java.awt.Rectangle getRectanglePersonaje(int indice) {
        int anchoTotal = (4 * SEL_ANCHO_PERSONAJE) + (3 * SEL_ESPACIO_ENTRE);
        int inicioPosX = (DefaultSettings.screenWidth - anchoTotal) / 2;
        int posX = inicioPosX + (indice * (SEL_ANCHO_PERSONAJE + SEL_ESPACIO_ENTRE));
        return new java.awt.Rectangle(posX, SEL_POS_Y, SEL_ANCHO_PERSONAJE, SEL_ALTO_PERSONAJE);
    }

    private void renderTragaperras(Graphics g) {
        g.setColor(new Color(15, 15, 28));
        g.fillRect(0, 0, DefaultSettings.screenWidth, DefaultSettings.screenHeight);

        g.setColor(Color.WHITE);
        g.setFont(gameFont);
        g.drawString("🎰 MÁQUINA TRAGAPERRAS 🎰", DefaultSettings.screenWidth / 2 - 210, 75);

        Player player = handler.getPlayer();
        int monedasActuales = player != null ? player.getMonedas() : 0;

        g.setFont(gameFont);
        g.setColor(Color.YELLOW);
        g.drawString("Monedas: " + monedasActuales, DefaultSettings.screenWidth / 2 - 50, 105);

        int boxW = 560;
        int boxH = 260;
        int boxX = DefaultSettings.screenWidth / 2 - boxW / 2;
        int boxY = 130;

        g.setColor(new Color(28, 28, 48));
        g.fillRect(boxX, boxY, boxW, boxH);
        g.setColor(new Color(255, 215, 0));
        g.drawRect(boxX, boxY, boxW, boxH);
        g.drawRect(boxX - 2, boxY - 2, boxW + 4, boxH + 4);

        if (!ruletaGirada) {
            g.setFont(gameFont);
            g.setColor(Color.YELLOW);
            g.drawString("🎰 RULETA LISTA PARA GIRAR 🎰", boxX + 85, boxY + 70);

            g.setColor(Color.WHITE);
            g.setFont(gameFont);
            g.drawString("Premios: Botiquín, Armas Nuevas o Nada", boxX + 115, boxY + 120);

            g.setColor(Color.CYAN);
            g.setFont(gameFont);
            g.drawString("Presiona [ESPACIO] o [ENTER] para GIRAR", boxX + 85, boxY + 185);
        } else {
            g.setFont(gameFont);
            g.setColor(Color.YELLOW);
            g.drawString("¡LA RULETA SE HA DETENIDO!", boxX + 150, boxY + 40);

            // CASO 1: BOTIQUÍN (sin arma)
            if (tieneBotiquinTragaperras && armaObtenidaTragaperras == null) {
                g.setColor(Color.MAGENTA);
                g.setFont(titleFont);
                String botiquinTxt = "★ BOTIQUÍN ★";
                int strW = g.getFontMetrics().stringWidth(botiquinTxt);
                g.drawString(botiquinTxt, DefaultSettings.screenWidth / 2 - strW / 2, boxY + 100);

                g.setColor(Color.WHITE);
                g.setFont(gameFont);
                g.drawString("¡Recógelo para guardarlo en tu inventario [B]!", boxX + 90, boxY + 145);

                g.setColor(Color.LIGHT_GRAY);
                g.setFont(hudFont);
                g.drawString("Tu arma actual se mantiene sin cambios", boxX + 140, boxY + 175);

            // CASO 2: NADA (sigues con tu arma actual)
            } else if (!tieneBotiquinTragaperras && armaObtenidaTragaperras == null) {
                g.setColor(Color.GRAY);
                g.setFont(titleFont);
                String nadaTxt = "¡NADA!";
                int strW = g.getFontMetrics().stringWidth(nadaTxt);
                g.drawString(nadaTxt, DefaultSettings.screenWidth / 2 - strW / 2, boxY + 100);

                g.setColor(Color.WHITE);
                g.setFont(gameFont);
                g.drawString("La ruleta no te dio nada esta vez", boxX + 150, boxY + 145);

                g.setColor(Color.CYAN);
                g.setFont(gameFont);
                String armaActual = (player != null && player.getArmaEquipada() != null)
                    ? player.getArmaEquipada().getNombre()
                    : "ninguna";
                g.drawString("Sigues con: " + armaActual, boxX + 180, boxY + 175);

            // CASO 3: ARMA NUEVA
            } else if (armaObtenidaTragaperras != null) {
                Color colArma;
                if (armaObtenidaTragaperras.getTipo() == TipoArma.LANZALLAMAS) {
                    colArma = new Color(255, 100, 0);
                } else if (armaObtenidaTragaperras.getTipo() == TipoArma.MINIGUN) {
                    colArma = new Color(0, 220, 255);
                } else if (armaObtenidaTragaperras.getTipo() == TipoArma.ESCOPETA) {
                    colArma = new Color(220, 220, 255);
                } else {
                    colArma = Color.YELLOW;
                }

                g.setColor(colArma);
                g.setFont(titleFont);
                String armaTxt = "★ " + armaObtenidaTragaperras.getNombre().toUpperCase() + " ★";
                int strW = g.getFontMetrics().stringWidth(armaTxt);
                g.drawString(armaTxt, DefaultSettings.screenWidth / 2 - strW / 2, boxY + 85);

                g.setColor(Color.WHITE);
                g.setFont(gameFont);
                g.drawString(armaObtenidaTragaperras.getTipo().getDescripcion(), boxX + 35, boxY + 125);

                g.setFont(hudFont);
                g.setColor(Color.LIGHT_GRAY);
                g.drawString("• Proyectiles: " + armaObtenidaTragaperras.getProyectilesPorDisparo() +
                             "  |  Cadencia: " + armaObtenidaTragaperras.getCooldownMs() + "ms" +
                             "  |  Multiplicador: x" + String.format("%.1f", armaObtenidaTragaperras.getMultiplicadorDaño()),
                             boxX + 35, boxY + 158);

                String dispStr = armaObtenidaTragaperras.getDispersion() <= 0.05 ? "Precisa" : "Abanico en cono";
                g.drawString("• Dispersión: " + dispStr +
                             "  |  Probabilidad: " + String.format("%.0f%%", armaObtenidaTragaperras.getProbabilidadAparicion() * 100),
                             boxX + 35, boxY + 185);

                g.setColor(Color.GREEN);
                g.setFont(gameFont);
                g.drawString("¡EQUIPADA AUTOMÁTICAMENTE AL JUGADOR!", boxX + 115, boxY + 225);
            }

            g.setColor(Color.WHITE);
            g.setFont(gameFont);
            g.drawString("Presiona [ESPACIO] o [ENTER] para volver al combate",
                         DefaultSettings.screenWidth / 2 - 240, boxY + boxH + 60);

            g.setColor(Color.YELLOW);
            g.setFont(gameFont);
            g.drawString("[R] Repetir tirada (10 monedas)",
                         DefaultSettings.screenWidth / 2 - 160, boxY + boxH + 85);
        }
    }

    // Constantes de diseño para la Tienda de Fortune (Comodines)
    public static final int TIENDA_CARD_ANCHO = 130;
    public static final int TIENDA_CARD_ALTO = 184;
    public static final int TIENDA_ESPACIO_X = 55;
    public static final int TIENDA_ESPACIO_Y = 46;
    public static final int TIENDA_INICIO_Y = 118;

    public static java.awt.Rectangle getRectangleCartaTienda(int indice) {
        int col = indice % 3;
        int fila = indice / 3;
        int anchoTotal = (3 * TIENDA_CARD_ANCHO) + (2 * TIENDA_ESPACIO_X);
        int inicioX = (DefaultSettings.screenWidth - anchoTotal) / 2;
        int x = inicioX + col * (TIENDA_CARD_ANCHO + TIENDA_ESPACIO_X);
        int y = TIENDA_INICIO_Y + fila * (TIENDA_CARD_ALTO + TIENDA_ESPACIO_Y);
        return new java.awt.Rectangle(x, y, TIENDA_CARD_ANCHO, TIENDA_CARD_ALTO);
    }

    private void renderTiendaNormal(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

        // Fondo oscuro temático
        g.setColor(new Color(12, 14, 22));
        g.fillRect(0, 0, DefaultSettings.screenWidth, DefaultSettings.screenHeight);

        // Título "TIENDA DE FORTUNE"
        g.setColor(new Color(255, 215, 0)); // Dorado
        g.setFont(titleFont);
        String titulo = "TIENDA DE FORTUNE";
        int tituloWidth = g.getFontMetrics().stringWidth(titulo);
        g.drawString(titulo, DefaultSettings.screenWidth / 2 - tituloWidth / 2, 48);

        // Subtítulo "Seleccione un comodín"
        g.setColor(Color.WHITE);
        g.setFont(gameFont);
        String subtitulo = "Seleccione un comodín";
        int subWidth = g.getFontMetrics().stringWidth(subtitulo);
        g.drawString(subtitulo, DefaultSettings.screenWidth / 2 - subWidth / 2, 74);

        // Monedas disponibles en la esquina superior derecha
        Player player = handler.getPlayer();
        int monedasActuales = player != null ? player.getMonedas() : 0;
        g.setColor(Color.YELLOW);
        g.setFont(gameFont);
        String txtMonedas = "Monedas: " + monedasActuales + " 🪙";
        g.drawString(txtMonedas, DefaultSettings.screenWidth - g.getFontMetrics().stringWidth(txtMonedas) - 30, 48);

        // Renderizar las 6 cartas en cuadrícula 2 filas x 3 columnas
        for (int i = 0; i < 6; i++) {
            if (comodinesActuales[i] == null) continue;

            java.awt.Rectangle rect = getRectangleCartaTienda(i);
            CartaTarot comodin = comodinesActuales[i];
            boolean comprado = comodinesComprados[i];
            int costo = costoComodines[i];
            boolean puedePagar = player != null && player.getMonedas() >= costo;

            java.awt.image.BufferedImage img = CardImageLoader.obtenerImagen(comodin.getCodigoImagen());

            if (comprado) {
                // Ya comprado: tono grisáceo con transparencia
                if (img != null) {
                    g2d.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.25f));
                    g.drawImage(img, rect.x, rect.y, rect.width, rect.height, null);
                    g2d.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 1.0f));
                } else {
                    g.setColor(new Color(35, 35, 35));
                    g.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 10, 10);
                }
                g.setColor(Color.DARK_GRAY);
                g2d.setStroke(new java.awt.BasicStroke(2));
                g2d.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 10, 10);

                // Texto COMPRADO sobre la carta
                g.setColor(Color.LIGHT_GRAY);
                g.setFont(gameFont);
                String strComp = "COMPRADO";
                int compW = g.getFontMetrics().stringWidth(strComp);
                g.drawString(strComp, rect.x + (rect.width - compW) / 2, rect.y + rect.height / 2);

            } else if (!puedePagar) {
                // NO ALCANZA EL DINERO: se muestra en gris (silueta / desaturado como personajes bloqueados)
                g.setColor(new Color(25, 25, 30, 210));
                g.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 10, 10);

                if (img != null) {
                    g2d.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.35f));
                    g.drawImage(img, rect.x, rect.y, rect.width, rect.height, null);
                    g2d.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 1.0f));
                }

                // Marco gris/rojo oscuro
                g.setColor(new Color(110, 50, 50));
                g2d.setStroke(new java.awt.BasicStroke(2));
                g2d.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 10, 10);

                // Costo en rojo indicando que falta oro
                g.setFont(hudFont);
                g.setColor(new Color(230, 90, 90));
                String tagCosto = costo + " monedas [Bloqueado]";
                int tagW = g.getFontMetrics().stringWidth(tagCosto);
                g.drawString(tagCosto, rect.x + (rect.width - tagW) / 2, rect.y + rect.height / 2);

            } else {
                // PUEDE COMPRAR (DISPONIBLE): Imagen nítida con marco dorado brillante
                if (img != null) {
                    g.drawImage(img, rect.x, rect.y, rect.width, rect.height, null);
                } else {
                    g.setColor(new Color(40, 48, 65));
                    g.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 10, 10);
                }

                // Marco dorado iluminado
                g.setColor(new Color(255, 215, 0));
                g2d.setStroke(new java.awt.BasicStroke(2.5f));
                g2d.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 10, 10);

                // Badge de número (1-6) y costo arriba
                g.setColor(new Color(0, 0, 0, 180));
                g.fillRoundRect(rect.x + 4, rect.y + 4, 38, 20, 6, 6);
                g.setColor(Color.WHITE);
                g.setFont(hudFont);
                g.drawString("[" + (i + 1) + "]", rect.x + 8, rect.y + 18);
            }

            // TEXTO DEBAJO DE LA CARTA: Solo lo que te da (ej: "20% más de vida") y el costo
            String descEfecto = comodin.getDescripcionEfecto();
            g.setFont(hudFont);

            if (comprado) {
                g.setColor(Color.GRAY);
            } else if (puedePagar) {
                g.setColor(new Color(140, 255, 140)); // Verde claro
            } else {
                g.setColor(new Color(160, 160, 160)); // Gris
            }

            int descW = g.getFontMetrics().stringWidth(descEfecto);
            g.drawString(descEfecto, rect.x + (rect.width - descW) / 2, rect.y + rect.height + 17);

            // Costo debajo de la descripción
            if (!comprado) {
                g.setColor(puedePagar ? Color.YELLOW : Color.RED);
                String txtCosto = costo + " monedas";
                int costW = g.getFontMetrics().stringWidth(txtCosto);
                g.drawString(txtCosto, rect.x + (rect.width - costW) / 2, rect.y + rect.height + 31);
            }
        }

        // Barra inferior: Teclas de control y botón para continuar
        g.setColor(Color.WHITE);
        g.setFont(hudFont);
        String instrucciones = "Haz click en la carta o presiona [1-6] para comprar  |  [ENTER/ESPACIO] Continuar hacia el Tarot ->";
        int instW = g.getFontMetrics().stringWidth(instrucciones);
        g.drawString(instrucciones, DefaultSettings.screenWidth / 2 - instW / 2, DefaultSettings.screenHeight - 16);
    }

    private void renderTiendaTarot(Graphics g) {
        g.setColor(new Color(180, 100, 255));
        g.setFont(gameFont);
        g.drawString("SELECCION DE CARTA TAROT", DefaultSettings.screenWidth/2 - 180, 80);

        g.setColor(Color.WHITE);
        g.setFont(gameFont);
        g.drawString("Presiona 1, 2 o 3 para elegir carta. ¡OBLIGATORIO!", DefaultSettings.screenWidth/2 - 240, 115);

        if (cartasTarotActuales == null) {
            cartasTarotActuales = CartaTarot.generarOpcionesAleatorias();
        }

        int startY = 170;
        int lineH = 65;
        Color[] coloresTipo = { Color.GREEN, new Color(255, 80, 80), Color.ORANGE };
        for (int i = 0; i < cartasTarotActuales.length; i++) {
            CartaTarot carta = cartasTarotActuales[i];
            if (carta == null) continue;
            String tipo = carta.getEfecto();
            if ("positivo".equals(tipo)) g.setColor(coloresTipo[0]);
            else if ("negativo".equals(tipo)) g.setColor(coloresTipo[1]);
            else g.setColor(coloresTipo[2]);

            g.setFont(gameFont);
            g.drawString((i + 1) + ". " + carta.getNombre(), 80, startY + i * lineH);
            g.setFont(hudFont);
            g.setColor(Color.LIGHT_GRAY);
            g.drawString("   Efecto: " + carta.getEfecto() + "  |  " + carta.getTipoEfecto()
                         + "  |  " + String.format("%.0f%%", Math.abs(carta.getValor()) * 100),
                         80, startY + i * lineH + 20);
        }
    }

    private void renderGameOver(Graphics g) {
        g.setColor(Color.RED);
        g.setFont(titleFont);
        g.drawString("HAS MUERTO - GAME OVER!", DefaultSettings.screenWidth/2 - 210, 150);

        g.setColor(Color.WHITE);
        g.setFont(gameFont);
        String nombreJugador = nombreIngresado ? nombreEntrando : progreso.getNombreJugador();
        g.drawString(nombreJugador, DefaultSettings.screenWidth/2 - (g.getFontMetrics().stringWidth(nombreJugador) / 2), 200);

        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        long segundosSobrevividos = maxTiempoSobrevividoMs / 1000;
        long recordPersonal = progreso.getPuntuacionMaxima() / 1000;
        String recordTexto;

        if (segundosSobrevividos > recordPersonal) {
            recordTexto = "NUEVO RÉCORD PERSONAL: " + segundosSobrevividos + " segundos!";
            g.setColor(Color.GREEN);
        } else {
            recordTexto = "Supervivencia: " + segundosSobrevividos + "s | Récord: " + recordPersonal + "s";
            g.setColor(Color.ORANGE);
        }

        g.drawString(recordTexto, DefaultSettings.screenWidth/2 - (g.getFontMetrics().stringWidth(recordTexto) / 2), 240);

        g.setColor(Color.WHITE);
        g.setFont(gameFont);
        g.drawString("Presiona [R] o [ENTER] para Reintentar", DefaultSettings.screenWidth/2 - 160, 300);

        RankingRenderer.dibujar(g, ranking.getSnapshot(), nombreJugadorActual(), DefaultSettings.screenWidth / 2, 350);
    }

    /**
     * Manejar clicks en la tienda de comodines (Fortune)
     */
    public void manejarClickTiendaNormal(int mouseX, int mouseY) {
        Player player = handler.getPlayer();

        for (int i = 0; i < 6; i++) {
            java.awt.Rectangle rect = getRectangleCartaTienda(i);
            // Incluir el área de la carta y el texto de abajo
            java.awt.Rectangle clickArea = new java.awt.Rectangle(rect.x, rect.y, rect.width, rect.height + 35);

            if (clickArea.contains(mouseX, mouseY)) {
                if (!comodinesComprados[i] && player != null && comodinesActuales[i] != null) {
                    CartaTarot comodin = comodinesActuales[i];
                    int costo = costoComodines[i];

                    if (player.gastarMonedas(costo)) {
                        comodinesComprados[i] = true;
                        aplicarComodin(player, comodin);
                        System.out.println("[TIENDA FORTUNE] Comprado: " + comodin.getNombre() + " (" + comodin.getDescripcionEfecto() + ")");
                    } else {
                        System.out.println("[TIENDA FORTUNE] Monedas insuficientes para: " + comodin.getNombre() + " (Tienes: " + player.getMonedas() + ", Necesitas: " + costo + ")");
                    }
                } else if (comodinesComprados[i]) {
                    System.out.println("[TIENDA FORTUNE] Ya compraste este comodín.");
                }
                return;
            }
        }
    }

    /**
     * Manejar clicks en la pantalla de selección de personaje
     */
    public void manejarClickSeleccionPersonaje(int mouseX, int mouseY) {
        for (int i = 0; i < 4; i++) {
            java.awt.Rectangle rect = getRectanglePersonaje(i);

            if (rect.contains(mouseX, mouseY)) {
                String personaje = CharacterImageLoader.getPersonajePorIndice(i);

                // Verificar si está desbloqueado
                if (!estaPersonajeDesbloqueado(personaje)) {
                    System.out.println("[CLICK] " + personaje + " está bloqueado");
                    return;
                }

                // Seleccionar personaje
                System.out.println("[CLICK] Personaje seleccionado: " + personaje);
                Personaje perso = Personaje.crearPersonaje(personaje);
                setPersonajeSeleccionado(perso);
                return;
            }
        }
    }

    public void setPersonajeSeleccionado(Personaje personaje) {
        if (personaje == null) return;

        this.personajeSeleccionado = personaje;
        this.rondaActual = 1;
        this.tiradasTragaperras = 0;
        this.tiendasVisitadasEnPartida = 0;
        this.comodinesUsados.clear();
        this.comodinesComprados = new boolean[6];
        this.cartaTarotActual = null;
        this.armaObtenidaTragaperras = null;
        this.puedeRepetirTragaperras = true;
        this.ruletaGirada = false;
        this.botiquinTragaperras = null;
        this.tieneBotiquinTragaperras = false;
        this.clickPulsado = false;
        this.jefe = null;
        this.jefeSpawneado = false;
        this.debeSpawnearJefe = false;
        this.tiempoPausadoMs = 0;
        this.tiempoAplicacionTarot = 0;
        this.cartasTarotActuales = null;
        this.comodinesActuales = new CartaTarot[6];
        this.costoComodines = new int[6];
        this.maxTiempoSobrevividoMs = 0;

        handler.limpiarTodo();
        Player player = new Player(
            DefaultSettings.worldWidth / 2,
            DefaultSettings.worldHeight / 2,
            ID.PLAYER,
            personajeSeleccionado
        );
        player.equiparArma(Arma.crearRevolver());
        handler.addObject(player);

        if (camera != null) {
            camera.tick(player);
        }

        long ahora = System.currentTimeMillis();
        this.partidaStartTime = ahora;
        this.oleadaStartTime = ahora;
        this.ultimoSpawnEnemigo = ahora;

        // Establecer estado JUGANDO solo cuando todo esté completamente preparado
        this.estadoActual = EstadoJuego.JUGANDO;
    }

    public Personaje getPersonaSeleccionado() { return personajeSeleccionado; }
    public EstadoJuego getEstadoActual() { return estadoActual; }
    public void setEstadoActual(EstadoJuego nuevoEstado) { this.estadoActual = nuevoEstado; }
    public Camera getCamera() { return camera; }
    public Handler getHandler() { return handler; }
    public Player getPlayer() { return handler != null ? handler.getPlayer() : null; }
    public Arma getArmaObtenidaTragaperras() { return armaObtenidaTragaperras; }

    public boolean isPersonajeDesbloqueado(int indice) {
        switch (indice) {
            case 0: return true; // Mapache siempre
            case 1: return progreso.isGatoDesbloqueado();
            case 2: return progreso.isRanaDesbloqueada();
            case 3: return progreso.isColieDesbloqueado();
            default: return false;
        }
    }

    public dto.PartidaDTO obtenerPartidaDTO() {
        Player p = getPlayer();
        dto.PlayerDTO playerDTO = p != null ? p.toDTO() : null;
        long tiempoOleada = System.currentTimeMillis() - oleadaStartTime;
        return new dto.PartidaDTO(
            rondaActual,
            tiradasTragaperras,
            maxTiempoSobrevividoMs,
            tiempoOleada,
            estadoActual.name(),
            playerDTO
        );
    }

    public static void main(String[] args) {
        new Game();
    }
}

