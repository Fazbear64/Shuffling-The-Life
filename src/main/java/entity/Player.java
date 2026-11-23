package entity;

import config.DefaultSettings;
import dto.PlayerDTO;
import java.awt.*;

public class Player extends GameObject implements Damageable, Collidable {
    private Personaje personaje;
    private int velocidadMovimiento = 4; // Velocidad base del jugador
    private int vidaMax = 100;
    private int vidaActual = 100;
    private int dañoBase = 1;
    private int monedas = 0; // Monedas/Divisa recolectada

    // Sistema de armas
    private Arma armaEquipada;
    private long ultimoDisparoMs = 0;
    private long ultimoDanioRecibidoMs = 0;
    private static final long COOLDOWN_DANIO_MS = 250; // 250ms de delay entre cada daño de enemigo
    private float bonusDañoRevolver = 0.0f;
    private float bonusDañoEscopeta = 0.0f;
    private float bonusDañoMinigun = 0.0f;
    private float bonusDañoLanzallamas = 0.0f;

    // Banderas de control de movimiento (WASD y Flechas)
    private boolean movingUp = false;
    private boolean movingDown = false;
    private boolean movingLeft = false;
    private boolean movingRight = false;

    // Sistema de animación de sprites
    private visual.SpriteLoader.Direction direccionActual = visual.SpriteLoader.Direction.FRONT;
    private visual.SpriteLoader.AnimationState estadoAnimacion = visual.SpriteLoader.AnimationState.IDLE;
    private int frameAnimacion = 0;
    private long ultimoCambioFrame = 0;
    private static final long INTERVALO_FRAME = 150; // ms entre frames de animación
    private boolean disparando = false;

    public Player(int x, int y, ID id, Personaje personaje) {
        super(x, y, id);
        this.personaje = personaje;
        this.armaEquipada = Arma.crearRevolver(); // Arma inicial por defecto (Revolver)

        if (personaje != null) {
            aplicarBonificacionesPersonaje();
            // Cargar sprites del personaje
            if (personaje.toString() != null) {
                visual.SpriteLoader.cargarSpritesPersonaje(personaje.toString());
            }
        }
    }

    private void aplicarBonificacionesPersonaje() {
        if (personaje == null) return;

        float vidaBonus = personaje.getBonificacionVida();
        float velocidadBonus = personaje.getBonificacionVelocidad();
        float danoBonus = personaje.getBonificacionDanio();

        this.vidaMax = (int) (this.vidaMax * (1.0f + vidaBonus));
        this.vidaActual = this.vidaMax;
        this.velocidadMovimiento = (int) (this.velocidadMovimiento * (1.0f + velocidadBonus));
        if (this.velocidadMovimiento < 1) {
            this.velocidadMovimiento = 1;
        }

        if (danoBonus > 0) {
            this.dañoBase += (int) (this.dañoBase * danoBonus);
        }

        System.out.println("Personaje aplicado: Vida Max=" + vidaMax + ", Velocidad=" + velocidadMovimiento + ", Daño=" + dañoBase);
    }

    // Aplicar efecto de Carta de Tarot elegida en la tienda
    public void aplicarCartaTarot(CartaTarot carta) {
        if (carta == null) return;

        String tipoEfecto = carta.getTipoEfecto();
        float valor = carta.getValor();

        System.out.println("[DEBUG EFECTO] Aplicando: " + carta.getNombre() + " | Tipo: " + tipoEfecto + " | Valor: " + valor);
        System.out.println("[DEBUG EFECTO] ANTES - Vida: " + vidaActual + "/" + vidaMax + " | Daño: " + dañoBase + " | Vel: " + velocidadMovimiento);

        if (tipoEfecto.contains("vida") && !tipoEfecto.contains("daño")) {
            int extraVida = (int) (vidaMax * Math.abs(valor));
            if (valor > 0) {
                vidaMax += extraVida;
                vidaActual += extraVida;
            } else {
                vidaMax = Math.max(20, vidaMax - extraVida);
                vidaActual = Math.min(vidaActual, vidaMax);
            }
        } else if (tipoEfecto.contains("velocidad") && !tipoEfecto.contains("daño")) {
            int deltaVel = Math.round(velocidadMovimiento * valor);
            if (deltaVel == 0 && valor != 0) {
                deltaVel = valor > 0 ? 1 : -1;
            }
            this.velocidadMovimiento = Math.max(1, this.velocidadMovimiento + deltaVel);
        } else if (tipoEfecto.equals("daño")) {
            int deltaDaño = (int) (dañoBase * Math.abs(valor));
            if (valor > 0) {
                dañoBase += Math.max(1, deltaDaño);
            } else {
                dañoBase = Math.max(1, dañoBase - deltaDaño);
            }
        } else if (tipoEfecto.equals("daño-velocidad")) {
            // Efecto 50/50: +50% daño, -20% velocidad
            int deltaDaño = (int) (dañoBase * 0.50f);
            dañoBase += Math.max(1, deltaDaño);
            int deltaVel = Math.round(velocidadMovimiento * -0.20f);
            this.velocidadMovimiento = Math.max(1, this.velocidadMovimiento + deltaVel);
            System.out.println("[DEBUG EFECTO] Aplicado daño-velocidad: +" + deltaDaño + " daño, " + deltaVel + " velocidad");
        } else if (tipoEfecto.equals("daño-quieto")) {
            // Efecto 50/50: +60% daño, velocidad a 0 (congelado)
            int deltaDaño = (int) (dañoBase * 0.60f);
            dañoBase += Math.max(1, deltaDaño);
            this.velocidadMovimiento = 0;
            System.out.println("[DEBUG EFECTO] Aplicado daño-quieto: +" + deltaDaño + " daño, velocidad = 0");
        } else if (tipoEfecto.equals("vida-daño")) {
            // Efecto 50/50: -25% vida, +55% daño
            int extraVida = (int) (vidaMax * 0.25f);
            vidaMax = Math.max(20, vidaMax - extraVida);
            vidaActual = Math.min(vidaActual, vidaMax);
            int deltaDaño = (int) (dañoBase * 0.55f);
            dañoBase += Math.max(1, deltaDaño);
            System.out.println("[DEBUG EFECTO] Aplicado vida-daño: -" + extraVida + " vida, +" + deltaDaño + " daño");
        }

        System.out.println("[DEBUG EFECTO] DESPUÉS - Vida: " + vidaActual + "/" + vidaMax + " | Daño: " + dañoBase + " | Vel: " + velocidadMovimiento);
    }

    // Comprar mejora (comodín) en la tienda
    public boolean gastarMonedas(int cantidad) {
        if (monedas >= cantidad) {
            monedas -= cantidad;
            return true;
        }
        return false;
    }

    public void ganarMonedas(int cantidad) {
        this.monedas += cantidad;
    }

    public void tick() {
        // Actualizar posición
        if (movingUp) y -= velocidadMovimiento;
        if (movingDown) y += velocidadMovimiento;
        if (movingLeft) x -= velocidadMovimiento;
        if (movingRight) x += velocidadMovimiento;

        // Actualizar dirección basada en último movimiento
        if (movingLeft) direccionActual = visual.SpriteLoader.Direction.LEFT;
        else if (movingRight) direccionActual = visual.SpriteLoader.Direction.RIGHT;
        else if (movingUp) direccionActual = visual.SpriteLoader.Direction.BACK;
        else if (movingDown) direccionActual = visual.SpriteLoader.Direction.FRONT;

        // Verificar si está disparando
        long ahora = System.currentTimeMillis();
        disparando = (ahora - ultimoDisparoMs) < 200; // 200ms de animación de disparo

        // Actualizar animación
        boolean estaMoviendo = movingUp || movingDown || movingLeft || movingRight;

        if (disparando) {
            estadoAnimacion = visual.SpriteLoader.AnimationState.SHOOT;
        } else if (estaMoviendo) {
            estadoAnimacion = visual.SpriteLoader.AnimationState.WALK;
        } else {
            estadoAnimacion = visual.SpriteLoader.AnimationState.IDLE;
        }

        // Cambiar frames de animación
        if (ahora - ultimoCambioFrame > INTERVALO_FRAME) {
            frameAnimacion = (frameAnimacion + 1) % 2; // 2 frames para idle/walk
            ultimoCambioFrame = ahora;
        }

        // Limitar dentro del mundo
        int anchoPersonaje = 40;
        int altoPersonaje = 60;
        if (x < 0) x = 0;
        if (x > DefaultSettings.worldWidth - anchoPersonaje) x = DefaultSettings.worldWidth - anchoPersonaje;
        if (y < 0) y = 0;
        if (y > DefaultSettings.worldHeight - altoPersonaje) y = DefaultSettings.worldHeight - altoPersonaje;
    }

    public void render(Graphics g) {
        // Intentar cargar sprite
        if (personaje != null) {
            String nombrePersonaje = getNombrePersonaje();
            var sprite = visual.SpriteLoader.obtenerSprite(
                nombrePersonaje,
                estadoAnimacion,
                direccionActual,
                frameAnimacion
            );

            if (sprite != null) {
                // Dibujar sprite (40x60)
                g.drawImage(sprite, x, y, 40, 60, null);
            } else {
                // Fallback: cuadro azul si no hay sprite
                g.setColor(Color.BLUE);
                g.fillRect(x, y, 40, 60);
            }
        } else {
            // Sin personaje: cuadro azul
            g.setColor(Color.BLUE);
            g.fillRect(x, y, 40, 60);
        }

        // Barra de vida pequeña sobre el jugador
        g.setColor(Color.RED);
        g.fillRect(x, y - 10, 40, 5);
        g.setColor(Color.GREEN);
        int anchoVida = (int) (40.0 * ((double) vidaActual / vidaMax));
        g.fillRect(x, y - 10, Math.max(0, anchoVida), 5);
    }

    /**
     * Obtener nombre del personaje para cargar sprites
     */
    private String getNombrePersonaje() {
        if (personaje == null) return "mapache";
        String nombre = personaje.toString().toLowerCase();
        if (nombre.contains("mapache")) return "mapache";
        if (nombre.contains("gato")) return "gato";
        if (nombre.contains("rana")) return "rana";
        if (nombre.contains("collie")) return "collie";
        return "mapache";
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, 32, 32);
    }

    public void recibirDaño(int cantidad) {
        long ahora = System.currentTimeMillis();
        if (ahora - ultimoDanioRecibidoMs < COOLDOWN_DANIO_MS) {
            return;
        }
        this.vidaActual -= cantidad;
        if (this.vidaActual < 0) this.vidaActual = 0;
        this.ultimoDanioRecibidoMs = ahora;
    }

    public boolean estaMuerto() {
        return vidaActual <= 0;
    }

    public void resetMovement() {
        this.movingUp = false;
        this.movingDown = false;
        this.movingLeft = false;
        this.movingRight = false;
    }

    public Arma getArmaEquipada() {
        return armaEquipada;
    }

    public void equiparArma(Arma arma) {
        if (arma != null) {
            this.armaEquipada = arma;
            System.out.println("Arma equipada: " + arma.getNombre());
        }
    }

    public float getBonusDañoArma(TipoArma tipo) {
        if (tipo == null) return 0.0f;
        switch (tipo) {
            case REVOLVER: return bonusDañoRevolver;
            case ESCOPETA: return bonusDañoEscopeta;
            case MINIGUN: return bonusDañoMinigun;
            case LANZALLAMAS: return bonusDañoLanzallamas;
            default: return 0.0f;
        }
    }

    public void agregarBonusDañoArma(TipoArma tipo, float bonus) {
        if (tipo == null) return;
        switch (tipo) {
            case REVOLVER: bonusDañoRevolver += bonus; break;
            case ESCOPETA: bonusDañoEscopeta += bonus; break;
            case MINIGUN: bonusDañoMinigun += bonus; break;
            case LANZALLAMAS: bonusDañoLanzallamas += bonus; break;
        }
    }

    public int calcularDañoParaArma(Arma arma) {
        if (arma == null) return Math.max(1, dañoBase);
        float base = (dañoBase * arma.getMultiplicadorDaño()) + arma.getDañoBonus();
        float bonus = getBonusDañoArma(arma.getTipo());
        float total = base * (1.0f + bonus);
        return Math.max(1, Math.round(total));
    }

    public boolean puedeDisparar() {
        long cooldown = (armaEquipada != null) ? armaEquipada.getCooldownMs() : 380L;
        return (System.currentTimeMillis() - ultimoDisparoMs) >= cooldown;
    }

    public void registrarDisparo() {
        this.ultimoDisparoMs = System.currentTimeMillis();
    }

    public PlayerDTO toDTO() {
        String nombre = personaje != null ? personaje.toString() : "Ninguno";
        dto.ArmaDTO armaDTO = armaEquipada != null ? armaEquipada.toDTO() : null;
        return new PlayerDTO(x, y, vidaActual, vidaMax, dañoBase, velocidadMovimiento, monedas, nombre, armaDTO);
    }

    public void actualizarDesdeDTO(PlayerDTO dto) {
        if (dto == null) return;
        this.x = dto.getX();
        this.y = dto.getY();
        this.vidaActual = dto.getVidaActual();
        this.vidaMax = dto.getVidaMax();
        this.dañoBase = dto.getDañoBase();
        this.velocidadMovimiento = Math.max(1, dto.getVelocidadMovimiento());
        this.monedas = dto.getMonedas();
        if (dto.getArma() != null) {
            this.armaEquipada = Arma.fromDTO(dto.getArma());
        }
    }

    public int getVida() {
        return vidaActual;
    }

    // Getters y Setters
    public void setMovingUp(boolean movingUp) { this.movingUp = movingUp; }
    public void setMovingDown(boolean movingDown) { this.movingDown = movingDown; }
    public void setMovingLeft(boolean movingLeft) { this.movingLeft = movingLeft; }
    public void setMovingRight(boolean movingRight) { this.movingRight = movingRight; }
    public void setVelocidadMovimiento(int velocidadMovimiento) { this.velocidadMovimiento = Math.max(1, velocidadMovimiento); }

    public Personaje getPersonaje() { return personaje; }
    public int getVidaActual() { return vidaActual; }

    // Setter para vidaActual (usado al recolectar botiquín)
    public void setVidaActual(int vidaActual) {
        this.vidaActual = vidaActual;
        if (this.vidaActual > this.vidaMax) {
            this.vidaActual = this.vidaMax;
        }
        if (this.vidaActual < 0) {
            this.vidaActual = 0;
        }
    }

    public int getVidaMax() { return vidaMax; }
    public int getDañoBase() { return dañoBase; }
    public int getVelocidadMovimiento() { return velocidadMovimiento; }
    public int getMonedas() { return monedas; }
}