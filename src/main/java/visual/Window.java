package visual;

import config.DefaultSettings;
import entity.Personaje;
import entity.Player;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public class Window {
    public Window(Game game){
        JFrame window = new JFrame("Shuffling The Life");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);

        // Establecer dimensiones explícitas en el canvas y el frame
        game.setPreferredSize(new Dimension(DefaultSettings.screenWidth, DefaultSettings.screenHeight));
        game.setMaximumSize(new Dimension(DefaultSettings.screenWidth, DefaultSettings.screenHeight));
        game.setMinimumSize(new Dimension(DefaultSettings.screenWidth, DefaultSettings.screenHeight));

        window.add(game);
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);

        game.setFocusable(true);
        game.requestFocusInWindow();
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                game.requestFocusInWindow();

                // Detección de clicks en selección de personaje
                if (game.getEstadoActual() == Game.EstadoJuego.SELECCION_PERSONAJE) {
                    game.manejarClickSeleccionPersonaje(e.getX(), e.getY());
                    return;
                }

                // Detección de clicks en tienda de comodines (Fortune)
                if (game.getEstadoActual() == Game.EstadoJuego.TIENDA_NORMAL) {
                    game.manejarClickTiendaNormal(e.getX(), e.getY());
                    return;
                }

                if (game.getEstadoActual() == Game.EstadoJuego.JUGANDO) {
                    Camera cam = game.getCamera();
                    if (cam != null) {
                        int worldX = e.getX() + (int) cam.getX();
                        int worldY = e.getY() + (int) cam.getY();
                        game.registrarClickPulsado(worldX, worldY);
                        game.dispararHacia(worldX, worldY);
                    }
                }
                // NO permitir salir del tragaperras con click
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                game.registrarClickSoltado();
            }
        };

        game.addMouseListener(mouseAdapter);
        window.addMouseListener(mouseAdapter);

        MouseMotionAdapter motionAdapter = new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (game.getEstadoActual() == Game.EstadoJuego.JUGANDO) {
                    Camera cam = game.getCamera();
                    if (cam != null) {
                        int worldX = e.getX() + (int) cam.getX();
                        int worldY = e.getY() + (int) cam.getY();
                        // Actualizar posición para disparo sostenido
                        game.ultimoMouseWorldX = worldX;
                        game.ultimoMouseWorldY = worldY;
                    }
                }
            }
        };

        game.addMouseMotionListener(motionAdapter);
        window.addMouseMotionListener(motionAdapter);

        KeyAdapter keyAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();

                // Pantalla de cinemática
                if (game.getEstadoActual() == Game.EstadoJuego.CINEMÁTICA) {
                    if (key == KeyEvent.VK_ENTER) {
                        game.setEstadoActual(Game.EstadoJuego.ENTRADA_NOMBRE);
                    }
                    return;
                }

                // Pantalla de entrada de nombre
                if (game.getEstadoActual() == Game.EstadoJuego.ENTRADA_NOMBRE) {
                    if (key == KeyEvent.VK_ENTER) {
                        game.confirmarNombre();
                    } else if (key == KeyEvent.VK_BACK_SPACE) {
                        game.borrarCaracterNombre();
                    }
                    return;
                }

                if (game.getEstadoActual() == Game.EstadoJuego.SELECCION_PERSONAJE) {
                    // Pantalla de selección de personaje
                    if (key == KeyEvent.VK_1) {
                        game.setPersonajeSeleccionado(Personaje.crearPersonaje(Personaje.PERSONAJE_MAPACHE));
                    } else if (key == KeyEvent.VK_2 && game.isPersonajeDesbloqueado(1)) {
                        game.setPersonajeSeleccionado(Personaje.crearPersonaje(Personaje.PERSONAJE_GATO));
                    } else if (key == KeyEvent.VK_3 && game.isPersonajeDesbloqueado(2)) {
                        game.setPersonajeSeleccionado(Personaje.crearPersonaje(Personaje.PERSONAJE_RANA));
                    } else if (key == KeyEvent.VK_4 && game.isPersonajeDesbloqueado(3)) {
                        game.setPersonajeSeleccionado(Personaje.crearPersonaje(Personaje.PERSONAJE_COLLIE));
                    }
                    return;
                }

                if (game.getEstadoActual() == Game.EstadoJuego.TRAGAPERAS ||
                        game.getEstadoActual() == Game.EstadoJuego.TIENDA_NORMAL ||
                        game.getEstadoActual() == Game.EstadoJuego.TIENDA_TAROT ||
                        game.getEstadoActual() == Game.EstadoJuego.GAME_OVER) {
                    game.manejarTeclaEspecial(key);
                    return;
                }

                if (game.getEstadoActual() == Game.EstadoJuego.JUGANDO) {
                    game.manejarTeclaEspecial(key);
                    Player player = game.getPlayer();
                    if (player != null) {
                        if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP) player.setMovingUp(true);
                        if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN) player.setMovingDown(true);
                        if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT) player.setMovingLeft(true);
                        if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) player.setMovingRight(true);
                    }
                }
            }

            @Override
            public void keyTyped(KeyEvent e) {
                // Entrada de caracteres para el nombre
                if (game.getEstadoActual() == Game.EstadoJuego.ENTRADA_NOMBRE) {
                    char c = e.getKeyChar();
                    if (Character.isLetterOrDigit(c) || Character.isWhitespace(c)) {
                        game.agregarCaracterNombre(c);
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                int key = e.getKeyCode();

                Player player = game.getPlayer();
                if (player != null) {
                    if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP) player.setMovingUp(false);
                    if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN) player.setMovingDown(false);
                    if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT) player.setMovingLeft(false);
                    if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) player.setMovingRight(false);
                }
            }
        };

        game.addKeyListener(keyAdapter);
        window.addKeyListener(keyAdapter);

        window.addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            @Override
            public void windowGainedFocus(java.awt.event.WindowEvent e) {
                game.requestFocusInWindow();
            }

            @Override
            public void windowLostFocus(java.awt.event.WindowEvent e) {
                Player player = game.getPlayer();
                if (player != null) {
                    player.resetMovement();
                }
            }
        });

        // Iniciar el hilo del juego automáticamente
        game.start();
    }
}