package visual;

import config.DefaultSettings;
import entity.Player;
import entity.Enemigo;
import entity.Explosion;
import entity.GameObject;
import entity.Player;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Game extends Canvas implements Runnable {
    private Thread thread;
    private boolean running = false;
    private Handler handler;
    private Camera camera;
    private int rondaActual = 1;
    private boolean runningSplash = true;

    public Game() {
        handler = new Handler();
        camera = new Camera(0, 0);

        // Iniciar splash screen
        new Thread(() -> {
            try {
                System.out.println("\n" + "=".repeat(50));
                System.out.println("  🎬  CINEMÁTICA DE BIENVENIDA  🎬");
                System.out.println("  " + "=".repeat(50));
                System.out.println("  Bienvenido a Shuffling The Life");
                System.out.println("  Tu aventura comienza ahora");
                System.out.println("=".repeat(50));
                System.out.println("\n[Video simulado: cinematic.mp4 en recursos/]");
                Thread.sleep(3000);
                System.out.println("\nPresiona [ENTER] para continuar al juego...");
                while (true) {
                    if (System.in.available()) {
                        System.in.read();
                        break;
                    }
                    Thread.sleep(100);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        setPreferredSize(new Dimension(DefaultSettings.screenWidth, DefaultSettings.screenHeight));
        setMaximumSize(new Dimension(DefaultSettings.screenWidth, DefaultSettings.screenHeight));
        setMinimumSize(new Dimension(DefaultSettings.screenWidth, DefaultSettings.screenHeight));
    }

    public void start() {
        running = true;
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        double amountOfTicks = 60.0;
        double ns = 1000000000 / amountOfTicks;
        double delta = 0;

        while (running) {
            long currentTime = System.nanoTime();
            delta = (currentTime - lastTime) / ns;
            lastTime = currentTime;

            update();
            render();

            try {
                Thread.sleep(16); // ~60 FPS
            } catch (InterruptedException e) {
                running = false;
            }
        }
    }

    private void update() {
        handler.tick();
    }

    private void render() {
        BufferStrategy bs = this.getBufferStrategy();
        if (bs == null) {
            bs = this.createBufferStrategy(3);
            return;
        }

        Graphics g = bs.getDrawGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, DefaultSettings.screenWidth, DefaultSettings.screenHeight);

        if (runningSplash) {
            // Mostrar mensaje de splash
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("Bienvenido a Shuffling The Life",
                        DefaultSettings.screenWidth / 2 - 150,
                        DefaultSettings.screenHeight / 2 - 10);
            g.drawString("Presiona [ENTER] para continuar",
                        150, 300);
        } else {
            handler.render(g);
        }

        g.dispose();
    }

    public synchronized void start() {
        running = true;
        thread = new Thread(this);
        thread.start();
    }

    public synchronized void stop() {
        running = false;
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackStackTrace();
        }
    }
}