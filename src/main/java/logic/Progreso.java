package logic;

import entity.TipoArma;
import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class Progreso {
    private static final String SAVE_FILE = "progreso.dat";

    private String nombreJugador = "Jugador";      // Nombre del jugador
    private long puntuacionMaxima = 0;             // Puntuación máxima en segundos
    private boolean cinematicaMostrada = false;    // Bandera: si ya se mostró la cinemática

    private boolean mapacheDesbloqueado = true;   // Siempre desbloqueado
    private boolean gatoDesbloqueado = false;     // Se desbloquea después de tienda 1
    private boolean ranaDesbloqueada = false;     // Se desbloquea después de tienda 2
    private boolean colieDesbloqueado = false;    // Se desbloquea después de tienda 3

    private int tiendaVisitadas = 0; // Contador de tiendas visitadas

    // Sistema de armas desbloqueadas permanentemente
    private Set<String> armasDesbloqueadas = new HashSet<>();

    public Progreso() {
        cargar();
        // Generar archivo progreso.dat si no existe
        guardar();
    }

    // Getters
    public String getNombreJugador() { return nombreJugador; }
    public long getPuntuacionMaxima() { return puntuacionMaxima; }
    public boolean isCinematicaMostrada() { return cinematicaMostrada; }
    public boolean isMapacheDesbloqueado() { return mapacheDesbloqueado; }
    public boolean isGatoDesbloqueado() { return gatoDesbloqueado; }
    public boolean isRanaDesbloqueada() { return ranaDesbloqueada; }
    public boolean isColieDesbloqueado() { return colieDesbloqueado; }
    public int getTiendaVisitadas() { return tiendaVisitadas; }

    // Getters de armas desbloqueadas
    public Set<String> getArmasDesbloqueadas() { return new HashSet<>(armasDesbloqueadas); }
    public boolean isArmaDesbloqueada(TipoArma tipo) {
        if (tipo == null) return false;
        return armasDesbloqueadas.contains(tipo.name());
    }

    // Setters con validación
    public void setNombreJugador(String nombre) {
        if (nombre != null && !nombre.trim().isEmpty()) {
            this.nombreJugador = nombre.trim();
            guardar();
        }
    }

    public void marcarCinematicaMostrada() {
        this.cinematicaMostrada = true;
        guardar();
    }

    public void actualizarPuntuacionMaxima(long puntuacion) {
        if (puntuacion > this.puntuacionMaxima) {
            this.puntuacionMaxima = puntuacion;
            guardar();
        }
    }

    public void desbloquearPorTienda(int numTiendas) {
        if (numTiendas < 0) return;

        this.tiendaVisitadas = Math.max(this.tiendaVisitadas, numTiendas);

        if (this.tiendaVisitadas >= 1) gatoDesbloqueado = true;
        if (this.tiendaVisitadas >= 2) ranaDesbloqueada = true;
        if (this.tiendaVisitadas >= 3) colieDesbloqueado = true;

        guardar();
    }

    public int getPersonajesDesbloqueados() {
        int count = 1; // Mapache siempre
        if (gatoDesbloqueado) count++;
        if (ranaDesbloqueada) count++;
        if (colieDesbloqueado) count++;
        return count;
    }

    // Desbloquear arma permanentemente
    public void desbloquearArma(TipoArma tipo) {
        if (tipo != null && tipo != TipoArma.REVOLVER) { // El revolver siempre está disponible
            armasDesbloqueadas.add(tipo.name());
            guardar();
            System.out.println("[DEBUG PROGRESO] Arma desbloqueada permanentemente: " + tipo.name());
        }
    }

    private void guardar() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SAVE_FILE))) {
            oos.writeUTF(nombreJugador);
            oos.writeLong(puntuacionMaxima);
            oos.writeBoolean(cinematicaMostrada);
            oos.writeBoolean(gatoDesbloqueado);
            oos.writeBoolean(ranaDesbloqueada);
            oos.writeBoolean(colieDesbloqueado);
            oos.writeInt(tiendaVisitadas);

            // Guardar armas desbloqueadas
            oos.writeInt(armasDesbloqueadas.size());
            for (String arma : armasDesbloqueadas) {
                oos.writeUTF(arma);
            }
            System.out.println("[PROGRESO] Guardado en: " + new File(SAVE_FILE).getAbsolutePath());
        } catch (IOException e) {
            System.out.println("No se pudo guardar progreso: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void cargar() {
        File file = new File(SAVE_FILE);
        if (!file.exists()) return;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(SAVE_FILE))) {
            nombreJugador = ois.readUTF();
            puntuacionMaxima = ois.readLong();
            cinematicaMostrada = ois.readBoolean();
            gatoDesbloqueado = ois.readBoolean();
            ranaDesbloqueada = ois.readBoolean();
            colieDesbloqueado = ois.readBoolean();
            tiendaVisitadas = ois.readInt();

            // Cargar armas desbloqueadas (retrocompatibilidad)
            try {
                int numArmas = ois.readInt();
                armasDesbloqueadas.clear();
                for (int i = 0; i < numArmas; i++) {
                    armasDesbloqueadas.add(ois.readUTF());
                }
            } catch (Exception e) {
                // Si falla, es un archivo antiguo sin armas guardadas
                armasDesbloqueadas.clear();
            }
        } catch (IOException e) {
            System.out.println("No se pudo cargar progreso: " + e.getMessage());
        }
    }
}