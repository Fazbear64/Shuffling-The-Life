package visual;

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

import java.awt.*;
import java.util.LinkedList;
import java.util.concurrent.CopyOnWriteArrayList;

public class Handler {
    private final java.util.List<GameObject> object = new CopyOnWriteArrayList<>();
    private GameObject jugadorActual;

    public void tick(){
        Player player = getPlayer();

        for (GameObject tempObject : object) {
            if (tempObject == null) continue;
            tempObject.tick();

            // Si es enemigo, persigue al jugador
            if (tempObject instanceof Enemigo && player != null) {
                Enemigo enemigo = (Enemigo) tempObject;
                enemigo.perseguir(player.getX(), player.getY());

                // Colisión Enemigo <-> Jugador
                Rectangle rectEnemigo = enemigo.getBounds();
                Rectangle rectPlayer = player.getBounds();
                if (rectPlayer.intersects(rectEnemigo)) {
                    player.recibirDaño(enemigo.getDaño());
                }
            }

            // Colisiones de Monedas <-> Jugador (Recolección)
            if (tempObject instanceof Moneda && player != null) {
                Moneda moneda = (Moneda) tempObject;
                if (player.getBounds().intersects(moneda.getBounds())) {
                    player.ganarMonedas(moneda.getValor());
                    object.remove(moneda);
                }
            }

            // Colisiones de Botiquín <-> Jugador (Recuperación de vida)
            if (tempObject instanceof Botiquin && player != null) {
                Botiquin botiquin = (Botiquin) tempObject;
                if (player.getBounds().intersects(botiquin.getBounds())) {
                    player.setVidaActual(player.getVidaMax()); // Recuperar toda la vida
                    object.remove(botiquin);
                    System.out.println("¡Botiquín usado! Vida completa recuperada.");
                }
            }

            // Colisiones de Proyectiles con Enemigos y Jefe
            if (tempObject instanceof Proyectil) {
                Proyectil proj = (Proyectil) tempObject;
                Rectangle rectProj = proj.getBounds();
                boolean colisionada = false;

                // Colisión con Jefe (revisar todos los segmentos)
                for (GameObject other : object) {
                    if (other instanceof Jefe && !colisionada) {
                        Jefe jefe = (Jefe) other;
                        // Revisar colisión con cada segmento del jefe
                        for (Jefe.Segmento seg : jefe.getSegmentos()) {
                            Rectangle rectSeg = new Rectangle((int) seg.x, (int) seg.y, 64, 64);
                            if (rectProj.intersects(rectSeg)) {
                                jefe.recibirDañoEnSegmento((int) seg.x + 32, (int) seg.y + 32, proj.getDaño());
                                object.remove(proj);
                                colisionada = true;
                                break;
                            }
                        }
                    }
                }

                // Colisión con Enemigos normales
                if (!colisionada) {
                    for (GameObject other : object) {
                        if (other instanceof Enemigo) {
                            Enemigo enem = (Enemigo) other;
                            if (rectProj.intersects(enem.getBounds())) {
                                enem.recibirDaño(proj.getDaño());
                                object.remove(proj);

                                // Si el enemigo muere a causa del impacto, soltar una moneda
                                if (enem.estaMuerto()) {
                                    int valorMoneda = (enem.getId() == ID.ENEMIGO_TANQUE) ? 3 : 1;
                                    Moneda moneda = new Moneda(enem.getX() + enem.getAncho() / 2 - 8, enem.getY() + enem.getAlto() / 2 - 8, ID.MONEDA, valorMoneda);
                                    object.add(moneda);
                                }
                                colisionada = true;
                                break;
                            }
                        }
                    }
                }
            }
        }

        object.removeIf(obj -> (obj instanceof Enemigo && ((Enemigo) obj).estaMuerto()) ||
                               (obj instanceof Proyectil && (
                                   ((Proyectil) obj).estaExpirado() ||
                                   obj.getX() < 0 || obj.getX() > config.DefaultSettings.worldWidth ||
                                   obj.getY() < 0 || obj.getY() > config.DefaultSettings.worldHeight
                               )) ||
                               (obj instanceof Botiquin && ((Botiquin) obj).isExpirado()) ||
                               (obj instanceof Moneda && ((Moneda) obj).isExpirado()));
    }

    public void render(Graphics g){
        for (GameObject tempObject : object) {
            if (tempObject != null) {
                tempObject.render(g);
            }
        }
    }

    public void addObject(GameObject object){
        if (object != null) {
            this.object.add(object);
            if (object instanceof Player) {
                this.jugadorActual = object;
            }
        }
    }

    public void deleteObject(GameObject object){
        this.object.remove(object);
        if (this.jugadorActual == object) {
            this.jugadorActual = null;
        }
    }

    public void limpiarEnemigos() {
        object.removeIf(obj -> obj instanceof Enemigo);
    }

    public void limpiarTodo() {
        object.clear();
        jugadorActual = null;
    }

    public Player getPlayer() {
        if (jugadorActual instanceof Player) {
            return (Player) jugadorActual;
        }
        return null;
    }

    public Personaje getPersonajeActivo() {
        Player jugador = getPlayer();
        if (jugador != null && jugador.getPersonaje() != null) {
            return jugador.getPersonaje();
        }
        return null;
    }
}