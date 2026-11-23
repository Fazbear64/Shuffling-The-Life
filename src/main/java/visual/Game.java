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
    private long maxTiempoSobrevividoMs = 0;
    private long partidaStartTime = 0;

    private static final long DURACION_OLEADA_MS = 60_000L;
    private long oleadaStartTime;
    private long ultimoSpawnEnemigo = 0;

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

    public Game() {
        handler = new Handler();
        camera = new Camera(0, 0);

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
            if (jefe != null && !jefe.estaMuerto()) {
                jefe.setTarget(player.getX() + 16, player.getY() + 16);
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

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 16));
            g.drawString("Ronda: " + rondaActual, 20, 30);

            long tiempoTranscurridoMs = System.currentTimeMillis() - oleadaStartTime;
            long segundosSobrevividos = tiempoTranscurridoMs / 1000;
            g.drawString("Tiempo oleada: " + segundosSobrevividos + "s / 60s", 20, 55);
            g.drawString("Record Vivo: " + (maxTiempoSobrevividoMs / 1000) + "s", 20, 80);

            Player p = handler.getPlayer();
            if (p != null) {
                g.drawString("Vida: " + p.getVidaActual() + " / " + p.getVidaMax(), 20, 105);
                g.drawString("Monedas: " + p.getMonedas(), 20, 130);
                Arma arma = p.getArmaEquipada();
                if (arma != null) {
                    g.drawString("Arma: " + arma.getNombre() + " (Daño: " + p.calcularDañoParaArma(arma) + ")", 20, 155);
                }
            }

            RankingService.Snapshot rk = ranking.getSnapshot();
            if (rk.estado == RankingService.Estado.LISTO && rk.mejorPersonal > 0
                    && nombreJugadorActual().equals(rk.jugador)) {
                g.drawString("Récord en línea: " + rk.mejorPersonal + "s", 20, 180);
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
        g.setFont(new Font("Arial", Font.BOLD, 48));
        g.drawString("🎬 SHUFFLING THE LIFE 🎬", centerX - 300, centerY - 100);

        g.setFont(new Font("Arial", Font.PLAIN, 32));
        g.drawString("Reproduciendo cinemática...", centerX - 200, centerY);

        // Animación de puntos
        long tiempoTranscurrido = System.currentTimeMillis() - cinemáticaStartTime;
        int puntos = (int) ((tiempoTranscurrido / 500) % 4);
        String textoPuntos = ".".repeat(puntos);

        g.setFont(new Font("Arial", Font.PLAIN, 24));
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
        if (estadoActual == EstadoJuego.JUGANDO) {
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

        // Generar costos FIJOS para esta tienda
        for (int i = 0; i < 6; i++) {
            costoComodines[i] = 70 + (i * 5);
        }

        // Desbloquear personaje según tiendas visitadas
        int tiendaActual = tiradasTragaperras / 3; // Cada tienda es cada 3 oleadas
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

        // Si venimos de tienda tarot, spawneamos el jefe
        if (debeSpawnearJefe) {
            spawnearJefe();
            debeSpawnearJefe = false;
        }

        estadoActual = EstadoJuego.JUGANDO;
        oleadaStartTime = System.currentTimeMillis();
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
                // B para usar botiquín (recuperar vida completa)
                if (keyCode == KeyEvent.VK_B) {
                    if (player.getVidaActual() < player.getVidaMax()) {
                        player.setVidaActual(player.getVidaMax());
                        System.out.println("¡Botiquín usado! Vida completa recuperada.");
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
        comodinesUsados.clear();
        comodinesComprados = new boolean[6];
        cartaTarotActual = null;
        armaObtenidaTragaperras = null;
        puedeRepetirTragaperras = true;
        maxTiempoSobrevividoMs = 0;
        partidaStartTime = 0;

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
        g.setFont(new Font("Arial", Font.BOLD, 36));
        g.drawString("BIENVENIDO A SHUFFLING THE LIFE", DefaultSettings.screenWidth/2 - 320, 100);

        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString("¿Cuál es tu nombre?", DefaultSettings.screenWidth/2 - 140, 180);

        // Caja de entrada
        g.setColor(Color.DARK_GRAY);
        g.fillRect(DefaultSettings.screenWidth/2 - 200, 220, 400, 60);
        g.setColor(Color.WHITE);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRect(DefaultSettings.screenWidth/2 - 200, 220, 400, 60);

        // Texto ingresado
        g.setFont(new Font("Arial", Font.PLAIN, 28));
        g.setColor(Color.YELLOW);
        String textoMostrado = nombreEntrando.isEmpty() ? "Escribe tu nombre..." : nombreEntrando;
        g.drawString(textoMostrado, DefaultSettings.screenWidth/2 - 190, 265);

        // Instrucciones
        g.setFont(new Font("Arial", Font.PLAIN, 16));
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
        g.setFont(new Font("Arial", Font.BOLD, 48));
        String titulo = "Shuffling-The-Life";
        int tituloWidth = g.getFontMetrics().stringWidth(titulo);
        g.drawString(titulo, DefaultSettings.screenWidth/2 - tituloWidth/2, 80);

        // Cargar imágenes de personajes
        CharacterImageLoader.cargarTodos();

        // Posiciones de los personajes en fila - CENTRADOS
        int anchoPersonaje = 160;
        int altoPersonaje = 220;
        int espacioEntre = 30;
        int anchoTotal = (4 * anchoPersonaje) + (3 * espacioEntre);
        int inicioPosX = (DefaultSettings.screenWidth - anchoTotal) / 2;

        int[] posicionesX = new int[4];
        for (int i = 0; i < 4; i++) {
            posicionesX[i] = inicioPosX + (i * (anchoPersonaje + espacioEntre));
        }
        int posY = 140;

        // Dibujar personajes
        for (int i = 0; i < 4; i++) {
            String personaje = CharacterImageLoader.getPersonajePorIndice(i);
            boolean desbloqueado = estaPersonajeDesbloqueado(personaje);

            // Cargar imagen del personaje
            java.awt.image.BufferedImage img = CharacterImageLoader.obtenerImagen(personaje);

            if (desbloqueado && img != null) {
                // DESBLOQUEADO: imagen normal con marco blanco
                g.setColor(Color.WHITE);
                g2d.setStroke(new java.awt.BasicStroke(3));
                g2d.drawRect(posicionesX[i], posY, anchoPersonaje, altoPersonaje);

                g.drawImage(img, posicionesX[i], posY, anchoPersonaje, altoPersonaje, null);
            } else if (img != null) {
                // BLOQUEADO: silueta oscura
                g.setColor(new Color(30, 30, 30, 200)); // Gris muy oscuro semitransparente
                g.fillRect(posicionesX[i], posY, anchoPersonaje, altoPersonaje);

                // Marco gris oscuro
                g.setColor(Color.DARK_GRAY);
                g2d.setStroke(new java.awt.BasicStroke(2));
                g2d.drawRect(posicionesX[i], posY, anchoPersonaje, altoPersonaje);

                // Dibujar imagen con mucha opacidad reducida para silueta
                g2d.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.3f));
                g.drawImage(img, posicionesX[i], posY, anchoPersonaje, altoPersonaje, null);
                g2d.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 1.0f));
            } else {
                // Fallback: color de fondo
                g.setColor(new Color(50, 50, 50));
                g.fillRect(posicionesX[i], posY, anchoPersonaje, altoPersonaje);

                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.PLAIN, 12));
                String nombre = getNombrePersonajeCapitalizado(personaje);
                int nombreWidth = g.getFontMetrics().stringWidth(nombre);
                g.drawString(nombre, posicionesX[i] + (anchoPersonaje - nombreWidth)/2, posY + altoPersonaje/2);
            }

            // Texto debajo del personaje (nombre)
            g.setColor(desbloqueado ? Color.GREEN : Color.GRAY);
            g.setFont(new Font("Arial", Font.BOLD, 14));
            String nombre = getNombrePersonajeCapitalizado(personaje);
            int nombreWidth = g.getFontMetrics().stringWidth(nombre);
            g.drawString(nombre, posicionesX[i] + (anchoPersonaje - nombreWidth)/2, posY + altoPersonaje + 25);

            // Texto de bloqueo si está bloqueado
            if (!desbloqueado) {
                g.setColor(Color.RED);
                g.setFont(new Font("Arial", Font.PLAIN, 10));
                String bloqueado = "[BLOQUEADO]";
                int bloqueadoWidth = g.getFontMetrics().stringWidth(bloqueado);
                g.drawString(bloqueado, posicionesX[i] + (anchoPersonaje - bloqueadoWidth)/2, posY + altoPersonaje + 45);
            }
        }

        // Texto "Selecciona el personaje..." debajo
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 18));
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
    private java.awt.Rectangle getRectanglePersonaje(int indice) {
        int[] posicionesX = {100, 310, 520, 730};
        int anchoPersonaje = 180;
        int altoPersonaje = 240;
        int posY = 150;

        return new java.awt.Rectangle(posicionesX[indice], posY, anchoPersonaje, altoPersonaje);
    }

    private void renderTragaperras(Graphics g) {
        g.setColor(new Color(15, 15, 28));
        g.fillRect(0, 0, DefaultSettings.screenWidth, DefaultSettings.screenHeight);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 28));
        g.drawString("🎰 MÁQUINA TRAGAPERRAS 🎰", DefaultSettings.screenWidth / 2 - 210, 75);

        Player player = handler.getPlayer();
        int monedasActuales = player != null ? player.getMonedas() : 0;

        g.setFont(new Font("Arial", Font.BOLD, 16));
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
            g.setFont(new Font("Arial", Font.BOLD, 22));
            g.setColor(Color.YELLOW);
            g.drawString("🎰 RULETA LISTA PARA GIRAR 🎰", boxX + 85, boxY + 70);

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 16));
            g.drawString("Premios: Botiquín, Armas Nuevas o Nada", boxX + 115, boxY + 120);

            g.setColor(Color.CYAN);
            g.setFont(new Font("Arial", Font.BOLD, 18));
            g.drawString("Presiona [ESPACIO] o [ENTER] para GIRAR", boxX + 85, boxY + 185);
        } else {
            g.setFont(new Font("Arial", Font.BOLD, 17));
            g.setColor(Color.YELLOW);
            g.drawString("¡LA RULETA SE HA DETENIDO!", boxX + 150, boxY + 40);

            // CASO 1: BOTIQUÍN (sin arma)
            if (tieneBotiquinTragaperras && armaObtenidaTragaperras == null) {
                g.setColor(Color.MAGENTA);
                g.setFont(new Font("Arial", Font.BOLD, 32));
                String botiquinTxt = "★ BOTIQUÍN ★";
                int strW = g.getFontMetrics().stringWidth(botiquinTxt);
                g.drawString(botiquinTxt, DefaultSettings.screenWidth / 2 - strW / 2, boxY + 100);

                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.PLAIN, 16));
                g.drawString("¡Vida completa recuperada al recogerlo!", boxX + 130, boxY + 145);

                g.setColor(Color.LIGHT_GRAY);
                g.setFont(new Font("Arial", Font.PLAIN, 14));
                g.drawString("Tu arma actual se mantiene sin cambios", boxX + 140, boxY + 175);

            // CASO 2: NADA (sigues con tu arma actual)
            } else if (!tieneBotiquinTragaperras && armaObtenidaTragaperras == null) {
                g.setColor(Color.GRAY);
                g.setFont(new Font("Arial", Font.BOLD, 32));
                String nadaTxt = "¡NADA!";
                int strW = g.getFontMetrics().stringWidth(nadaTxt);
                g.drawString(nadaTxt, DefaultSettings.screenWidth / 2 - strW / 2, boxY + 100);

                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.PLAIN, 16));
                g.drawString("La ruleta no te dio nada esta vez", boxX + 150, boxY + 145);

                g.setColor(Color.CYAN);
                g.setFont(new Font("Arial", Font.BOLD, 15));
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
                g.setFont(new Font("Arial", Font.BOLD, 30));
                String armaTxt = "★ " + armaObtenidaTragaperras.getNombre().toUpperCase() + " ★";
                int strW = g.getFontMetrics().stringWidth(armaTxt);
                g.drawString(armaTxt, DefaultSettings.screenWidth / 2 - strW / 2, boxY + 85);

                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.PLAIN, 15));
                g.drawString(armaObtenidaTragaperras.getTipo().getDescripcion(), boxX + 35, boxY + 125);

                g.setFont(new Font("Arial", Font.PLAIN, 14));
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
                g.setFont(new Font("Arial", Font.BOLD, 15));
                g.drawString("¡EQUIPADA AUTOMÁTICAMENTE AL JUGADOR!", boxX + 115, boxY + 225);
            }

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 16));
            g.drawString("Presiona [ESPACIO] o [ENTER] para volver al combate",
                         DefaultSettings.screenWidth / 2 - 240, boxY + boxH + 60);

            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.drawString("[R] Repetir tirada (10 monedas)",
                         DefaultSettings.screenWidth / 2 - 160, boxY + boxH + 85);
        }
    }

    private void renderTiendaNormal(Graphics g) {
        g.setColor(Color.CYAN);
        g.setFont(new Font("Arial", Font.BOLD, 26));
        g.drawString("TIENDA DE COMODINES POKER", DefaultSettings.screenWidth/2 - 180, 48);

        Player player = handler.getPlayer();
        int monedasActuales = player != null ? player.getMonedas() : 0;

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("Monedas: " + monedasActuales, DefaultSettings.screenWidth/2 - 50, 75);
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.drawString("Elige un comodin (1-6) para comprarlo. [ENTER] para continuar.", 60, 102);

        int startY = 135;
        int lineH = 34;
        for (int i = 0; i < 6; i++) {
            if (comodinesActuales[i] == null) continue;

            boolean comprado = comodinesComprados[i];
            CartaTarot comodin = comodinesActuales[i];
            int costo = costoComodines[i];
            boolean puedePagar = player != null && player.getMonedas() >= costo;

            if (comprado) {
                g.setColor(Color.GRAY);
            } else if (puedePagar) {
                g.setColor(Color.YELLOW);
            } else {
                g.setColor(new Color(180, 80, 80));
            }

            String estado = comprado ? " [COMPRADO]" : " (" + costo + " monedas)";
            g.drawString((i + 1) + ". " + comodin.getNombre() + " - " + comodin.getTipoEfecto() + estado,
                         60, startY + i * lineH);
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 13));
        g.drawString("[ENTER/ESPACIO] Pasar a cartas de Tarot ->", 60, startY + 6 * lineH + 15);
    }

    private void renderTiendaTarot(Graphics g) {
        g.setColor(new Color(180, 100, 255));
        g.setFont(new Font("Arial", Font.BOLD, 26));
        g.drawString("SELECCION DE CARTA TAROT", DefaultSettings.screenWidth/2 - 180, 80);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 15));
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

            g.setFont(new Font("Arial", Font.BOLD, 15));
            g.drawString((i + 1) + ". " + carta.getNombre(), 80, startY + i * lineH);
            g.setFont(new Font("Arial", Font.PLAIN, 13));
            g.setColor(Color.LIGHT_GRAY);
            g.drawString("   Efecto: " + carta.getEfecto() + "  |  " + carta.getTipoEfecto()
                         + "  |  " + String.format("%.0f%%", Math.abs(carta.getValor()) * 100),
                         80, startY + i * lineH + 20);
        }
    }

    private void renderGameOver(Graphics g) {
        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        g.drawString("HAS MUERTO - GAME OVER!", DefaultSettings.screenWidth/2 - 210, 150);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 24));
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
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        g.drawString("Presiona [R] o [ENTER] para Reintentar", DefaultSettings.screenWidth/2 - 160, 300);

        RankingRenderer.dibujar(g, ranking.getSnapshot(), nombreJugadorActual(), DefaultSettings.screenWidth / 2, 350);
    }

    /**
     * Manejar clicks en la pantalla de selección de personaje
     */
    public void manejarClickSeleccionPersonaje(int mouseX, int mouseY) {
        // Posiciones de los personajes
        int[] posicionesX = {100, 310, 520, 730};
        int anchoPersonaje = 180;
        int altoPersonaje = 240;
        int posY = 150;

        // Verificar click en cada personaje
        for (int i = 0; i < 4; i++) {
            java.awt.Rectangle rect = new java.awt.Rectangle(posicionesX[i], posY, anchoPersonaje, altoPersonaje);

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
        this.personajeSeleccionado = personaje;
        this.estadoActual = EstadoJuego.JUGANDO;
        this.rondaActual = 1;
        this.tiradasTragaperras = 0;
        this.comodinesUsados.clear();
        this.comodinesComprados = new boolean[6];
        this.cartaTarotActual = null;
        this.armaObtenidaTragaperras = null;
        this.puedeRepetirTragaperras = true;
        this.partidaStartTime = System.currentTimeMillis();

        if (personajeSeleccionado != null) {
            handler.limpiarTodo();
            Player player = new Player(
                DefaultSettings.worldWidth / 2,
                DefaultSettings.worldHeight / 2,
                ID.PLAYER,
                personajeSeleccionado
            );
            player.equiparArma(Arma.crearRevolver());
            handler.addObject(player);
        }

        oleadaStartTime = System.currentTimeMillis();
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
