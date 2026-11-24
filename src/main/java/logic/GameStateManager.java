package logic;

import entity.Personaje;
import entity.CartaTarot;
import entity.Arma;
import java.util.HashSet;
import java.util.Set;

/**
 * Encapsula todo el estado de una partida.
 * Separa lógica de estado del renderizado/entrada en Game.java (Inversión de Dependencias).
 */
public class GameStateManager {
    private int rondaActual = 1;
    private int tiradasTragaperras = 0;
    private long maxTiempoSobrevividoMs = 0;
    private long partidaStartTime = 0;
    private long oleadaStartTime = 0;
    private long ultimoSpawnEnemigo = 0;

    private CartaTarot[] comodinesActuales = new CartaTarot[6];
    private Set<Integer> comodinesUsados = new HashSet<>();
    private boolean[] comodinesComprados = new boolean[6];
    private int[] costoComodines = new int[6];

    private Personaje personajeSeleccionado;
    private CartaTarot cartaTarotActual;
    private Arma armaObtenidaTragaperras;

    private boolean puedeRepetirTragaperras = true;
    private boolean ruletaGirada = false;

    // Getters y Setters

    public int getRondaActual() { return rondaActual; }
    public void setRondaActual(int ronda) { this.rondaActual = ronda; }

    public int getTiradasTragaperras() { return tiradasTragaperras; }
    public void setTiradasTragaperras(int tiradas) { this.tiradasTragaperras = tiradas; }

    public long getMaxTiempoSobrevividoMs() { return maxTiempoSobrevividoMs; }
    public void actualizarMaxTiempo(long nuevoTiempo) {
        if (nuevoTiempo > this.maxTiempoSobrevividoMs) {
            this.maxTiempoSobrevividoMs = nuevoTiempo;
        }
    }

    public long getPartidaStartTime() { return partidaStartTime; }
    public void setPartidaStartTime(long tiempo) { this.partidaStartTime = tiempo; }

    public long getOleadaStartTime() { return oleadaStartTime; }
    public void setOleadaStartTime(long tiempo) { this.oleadaStartTime = tiempo; }

    public long getUltimoSpawnEnemigo() { return ultimoSpawnEnemigo; }
    public void setUltimoSpawnEnemigo(long tiempo) { this.ultimoSpawnEnemigo = tiempo; }

    public CartaTarot[] getComodinesActuales() { return comodinesActuales.clone(); }
    public void setComodinesActuales(CartaTarot[] comodines) { this.comodinesActuales = comodines; }

    public Set<Integer> getComodinesUsados() { return new HashSet<>(comodinesUsados); }
    public void setComodinesUsados(Set<Integer> usados) { this.comodinesUsados = new HashSet<>(usados); }

    public boolean[] getComodinesComprados() { return comodinesComprados.clone(); }
    public void setComodinesComprados(boolean[] comprados) { this.comodinesComprados = comprados; }

    public int[] getCostoComodines() { return costoComodines.clone(); }
    public void setCostoComodines(int[] costos) { this.costoComodines = costos; }

    public Personaje getPersonajeSeleccionado() { return personajeSeleccionado; }
    public void setPersonajeSeleccionado(Personaje personaje) { this.personajeSeleccionado = personaje; }

    public CartaTarot getCartaTarotActual() { return cartaTarotActual; }
    public void setCartaTarotActual(CartaTarot carta) { this.cartaTarotActual = carta; }

    public Arma getArmaObtenidaTragaperras() { return armaObtenidaTragaperras; }
    public void setArmaObtenidaTragaperras(Arma arma) { this.armaObtenidaTragaperras = arma; }

    public boolean isPuedeRepetirTragaperras() { return puedeRepetirTragaperras; }
    public void setPuedeRepetirTragaperras(boolean puede) { this.puedeRepetirTragaperras = puede; }

    public boolean isRuletaGirada() { return ruletaGirada; }
    public void setRuletaGirada(boolean girada) { this.ruletaGirada = girada; }

    public void resetPartida() {
        rondaActual = 1;
        tiradasTragaperras = 0;
        maxTiempoSobrevividoMs = 0;
        partidaStartTime = 0;
        oleadaStartTime = 0;
        ultimoSpawnEnemigo = 0;
        comodinesUsados.clear();
        comodinesComprados = new boolean[6];
        cartaTarotActual = null;
        armaObtenidaTragaperras = null;
        puedeRepetirTragaperras = true;
        ruletaGirada = false;
        personajeSeleccionado = null;
    }
}
