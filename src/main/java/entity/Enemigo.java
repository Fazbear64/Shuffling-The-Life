package entity;

import config.DefaultSettings;
import dto.EnemigoDTO;
import java.awt.*;

public class Enemigo extends GameObject implements Damageable, Collidable {
    private int vida;
    private int vidaMax;
    private int daño;
    private float velocidad;
    private int ancho;
    private int alto;
    private Color color;

    private float exactX;
    private float exactY;

    // Timestamp de muerte para despawn tras 15 segundos (solo para gusanos de tienda)
    private long tiempoMuerteMs = -1;
    private boolean esGusanoDeTienda = false; // Solo los gusanos de tienda tardan 15s

    public Enemigo(int x, int y, ID id) {
        super(x, y, id);
        this.exactX = x;
        this.exactY = y;
        configurarSegunTipo(id);
    }

    public Enemigo(int x, int y, ID id, boolean esGusanoDeTienda) {
        super(x, y, id);
        this.exactX = x;
        this.exactY = y;
        this.esGusanoDeTienda = esGusanoDeTienda;
        configurarSegunTipo(id);
    }

    private void configurarSegunTipo(ID id) {
        if (id == ID.ENEMIGO_TANQUE) {
            // Enemigo tanque: más grande, más vida, más fuerte al golpear (mata de 4 toques - 25 dmg), más lento
            this.vidaMax = 6;
            this.vida = 6;
            this.daño = 25;
            this.velocidad = 1.1f;
            this.ancho = 42;
            this.alto = 42;
            this.color = new Color(139, 0, 0); // Rojo oscuro
        } else if (id == ID.ENEMIGO_VELOZ) {
            // Enemigo veloz: más rápido, poca vida, fácil de matar
            this.vidaMax = 1;
            this.vida = 1;
            this.daño = 10;
            this.velocidad = 3.5f;
            this.ancho = 20;
            this.alto = 20;
            this.color = new Color(255, 140, 0); // Naranja brillante
        } else if (id == ID.ENEMIGO_EXPLOSIVO) {
            // Enemigo explosivo: rápido, vida media, EXPLOTA al morir
            this.vidaMax = 3;
            this.vida = 3;
            this.daño = 20; // Daño por contacto
            this.velocidad = 2.5f;
            this.ancho = 32;
            this.alto = 32;
            this.color = new Color(255, 50, 50); // Rojo brillante (bomba)
        } else {
            // Enemigo normal
            this.vidaMax = 2;
            this.vida = 2;
            this.daño = 15;
            this.velocidad = 2.0f;
            this.ancho = 28;
            this.alto = 28;
            this.color = Color.RED;
        }
    }

    @Override
    public void tick() {
    }

    // Método para perseguir directamente las coordenadas (px, py) del jugador en el mundo
    public void perseguir(int px, int py) {
        float diffX = px - exactX;
        float diffY = py - exactY;
        float distance = (float) Math.sqrt(diffX * diffX + diffY * diffY);

        if (distance > 1) {
            exactX += (diffX / distance) * velocidad;
            exactY += (diffY / distance) * velocidad;
            this.x = Math.round(exactX);
            this.y = Math.round(exactY);
        }

        // Limitar dentro del mundo
        if (x < 0) {
            x = 0;
            exactX = 0;
        }
        if (x > DefaultSettings.worldWidth - ancho) {
            x = DefaultSettings.worldWidth - ancho;
            exactX = x;
        }
        if (y < 0) {
            y = 0;
            exactY = 0;
        }
        if (y > DefaultSettings.worldHeight - alto) {
            y = DefaultSettings.worldHeight - alto;
            exactY = y;
        }
    }

    @Override
    public void render(Graphics g) {
        g.setColor(color);
        g.fillRect(x, y, ancho, alto);

        // Barra de vida pequeña del enemigo
        g.setColor(Color.BLACK);
        g.fillRect(x, y - 8, ancho, 4);
        g.setColor(Color.ORANGE);
        int anchoVida = (int) (ancho * ((double) Math.max(0, vida) / vidaMax));
        g.fillRect(x, y - 8, anchoVida, 4);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, ancho, alto);
    }

    public void recibirDaño(int cantidad) {
        this.vida -= cantidad;
    }

    public int getVida() {
        return vida;
    }

    public int getVidaMax() {
        return vidaMax;
    }

    public int getDaño() {
        return daño;
    }

    public float getVelocidad() {
        return velocidad;
    }

    public int getAncho() {
        return ancho;
    }

    public int getAlto() {
        return alto;
    }

    public boolean estaMuerto() {
        return vida <= 0;
    }

    public void registrarMuerte() {
        // Solo registrar muerte con delay si es gusano de tienda
        if (esGusanoDeTienda && tiempoMuerteMs < 0) {
            tiempoMuerteMs = System.currentTimeMillis();
            System.out.println("[DEBUG] Gusano de tienda murió. Será eliminado en 15s. Timestamp: " + tiempoMuerteMs);
        }
    }

    public boolean debeEliminar() {
        // Los enemigos normales se eliminan instantáneamente cuando mueren
        if (!esGusanoDeTienda) {
            return estaMuerto();
        }
        // Los gusanos de tienda tardan 15 segundos tras morir
        if (tiempoMuerteMs < 0) return false;
        return (System.currentTimeMillis() - tiempoMuerteMs) > 15000; // 15 segundos
    }

    public EnemigoDTO toDTO() {
        return new EnemigoDTO(x, y, id, vida, vidaMax, daño, velocidad, ancho, alto);
    }
}