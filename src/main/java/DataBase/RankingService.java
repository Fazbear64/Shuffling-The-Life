package DataBase;

import DataBase.BaseDeDatos.RecordEntry;

import java.sql.Connection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Guarda y consulta récords en segundo plano y expone el último resultado como un
 * {@link Snapshot} inmutable, seguro de leer desde el render loop en cada frame.
 *
 * - Nunca lanza excepciones hacia el juego: cualquier fallo queda en Estado.ERROR.
 * - Un único hilo (daemon) serializa las operaciones, así "guardar y luego leer"
 *   siempre ocurre en ese orden y no impide cerrar el juego.
 */
public final class RankingService {

    public enum Estado { SIN_CARGAR, CARGANDO, LISTO, ERROR }

    /** Foto inmutable del último estado conocido del ranking. */
    public static final class Snapshot {
        public final Estado estado;
        public final List<RecordEntry> top;   // nunca null
        public final String jugador;          // jugador al que corresponde mejorPersonal (puede ser null)
        public final int mejorPersonal;       // -1 si no se conoce
        public final String mensaje;          // texto para mostrar en ERROR (puede ser null)

        Snapshot(Estado estado, List<RecordEntry> top, String jugador, int mejorPersonal, String mensaje) {
            this.estado = estado;
            this.top = top;
            this.jugador = jugador;
            this.mejorPersonal = mejorPersonal;
            this.mensaje = mensaje;
        }
    }

    private final int limiteTop;
    private final ExecutorService ejecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "ranking-db");
        t.setDaemon(true);
        return t;
    });

    private volatile Snapshot snapshot =
            new Snapshot(Estado.SIN_CARGAR, Collections.emptyList(), null, -1, null);

    public RankingService(int limiteTop) {
        this.limiteTop = limiteTop;
    }

    /** Último estado conocido. Rápido y sin bloqueos: llamar libremente desde render(). */
    public Snapshot getSnapshot() {
        return snapshot;
    }

    /** Solo consulta (top + mejor marca de {@code jugador}, que puede ser null). */
    public void refrescar(String jugador) {
        ejecutar(jugador, false, 0);
    }

    /** Guarda la puntuación y, cuando termina, vuelve a consultar el ranking. */
    public void guardarYRefrescar(String jugador, int puntuacion) {
        ejecutar(jugador, true, puntuacion);
    }

    private void ejecutar(String jugador, boolean guardar, int puntuacion) {
        // Mientras carga se conserva lo último que se tenía para no dejar la pantalla en blanco.
        Snapshot previo = snapshot;
        snapshot = new Snapshot(Estado.CARGANDO, previo.top, previo.jugador, previo.mejorPersonal, null);

        try {
            ejecutor.execute(() -> tarea(jugador, guardar, puntuacion));
        } catch (RuntimeException e) {
            snapshot = new Snapshot(Estado.ERROR, previo.top, previo.jugador, previo.mejorPersonal,
                    "No se pudo iniciar la consulta");
        }
    }

    private void tarea(String jugador, boolean guardar, int puntuacion) {
        try (Connection conn = BaseDeDatos.getConnection()) {
            if (guardar) {
                BaseDeDatos.guardarRecord(conn, jugador, puntuacion);
            }
            List<RecordEntry> top = BaseDeDatos.obtenerTopRecords(conn, limiteTop);
            int mejor = (jugador == null || jugador.trim().isEmpty())
                    ? -1
                    : BaseDeDatos.obtenerMejorRecord(conn, jugador);
            snapshot = new Snapshot(Estado.LISTO, top, jugador, mejor, null);
        } catch (Exception e) {
            System.err.println("[Ranking] Error con Neon DB: " + e.getMessage());
            String mensaje = (e.getMessage() != null && e.getMessage().contains("NEON_DB_PASS"))
                    ? "Falta la contraseña: crea db.properties"
                    : "Sin conexión con el servidor de récords";
            Snapshot actual = snapshot; // conserva lo último que sí se pudo cargar
            snapshot = new Snapshot(Estado.ERROR, actual.top, actual.jugador, actual.mejorPersonal, mensaje);
        }
    }
}
