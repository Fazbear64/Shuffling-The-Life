package logic;

import java.io.*;

public class Progreso {
    private static final String SAVE_FILE = "progreso.dat";

    private String nombreJugador = "Jugador";      // Nombre del jugador
    private long puntuacionMaxima = 0;             // Puntuación máxima en segundos

    private boolean mapacheDesbloqueado = true;   // Siempre desbloqueado
    private boolean gatoDesbloqueado = false;     // Se desbloquea después de tienda 1
    private boolean ranaDesbloqueada = false;     // Se desbloquea después de tienda 2
    private boolean colieDesbloqueado = false;    // Se desbloquea después de tienda 3

    private int tiendaVisitadas = 0; // Contador de tiendas visitadas

    public Progreso() {
        cargar();
    }

    // Getters
    public String getNombreJugador() { return nombreJugador; }
    public long getPuntuacionMaxima() { return puntuacionMaxima; }
    public boolean isMapacheDesbloqueado() { return mapacheDesbloqueado; }
    public boolean isGatoDesbloqueado() { return gatoDesbloqueado; }
    public boolean isRanaDesbloqueada() { return ranaDesbloqueada; }
    public boolean isColieDesbloqueado() { return colieDesbloqueado; }
    public int getTiendaVisitadas() { return tiendaVisitadas; }

    // Setters con validación
    public void setNombreJugador(String nombre) {
        if (nombre != null && !nombre.trim().isEmpty()) {
            this.nombreJugador = nombre.trim();
            guardar();
        }
    }

    public void actualizarPuntuacionMaxima(long puntuacion) {
        if (puntuacion > this.puntuacionMaxima) {
            this.puntuacionMaxima = puntuacion;
            guardar();
        }
    }

    public void desbloquearPorTienda(int numTiendas) {
        if (numTiendas < 0) return;

        this.tiendaVisitadas = numTiendas;

        if (numTiendas >= 1) gatoDesbloqueado = true;
        if (numTiendas >= 2) ranaDesbloqueada = true;
        if (numTiendas >= 3) colieDesbloqueado = true;

        guardar();
    }

    public int getPersonajesDesbloqueados() {
        int count = 1; // Mapache siempre
        if (gatoDesbloqueado) count++;
        if (ranaDesbloqueada) count++;
        if (colieDesbloqueado) count++;
        return count;
    }

    private void guardar() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SAVE_FILE))) {
            oos.writeUTF(nombreJugador);
            oos.writeLong(puntuacionMaxima);
            oos.writeBoolean(gatoDesbloqueado);
            oos.writeBoolean(ranaDesbloqueada);
            oos.writeBoolean(colieDesbloqueado);
            oos.writeInt(tiendaVisitadas);
        } catch (IOException e) {
            System.out.println("No se pudo guardar progreso: " + e.getMessage());
        }
    }

    private void cargar() {
        File file = new File(SAVE_FILE);
        if (!file.exists()) return;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(SAVE_FILE))) {
            nombreJugador = ois.readUTF();
            puntuacionMaxima = ois.readLong();
            gatoDesbloqueado = ois.readBoolean();
            ranaDesbloqueada = ois.readBoolean();
            colieDesbloqueado = ois.readBoolean();
            tiendaVisitadas = ois.readInt();
        } catch (IOException e) {
            System.out.println("No se pudo cargar progreso: " + e.getMessage());
        }
    }
}