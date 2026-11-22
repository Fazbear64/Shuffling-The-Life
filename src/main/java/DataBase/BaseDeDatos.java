package DataBase;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Acceso a la tabla {@code records} de Neon DB (PostgreSQL).
 *
 * Todos los métodos son BLOQUEANTES (hacen red): no llamarlos desde el hilo del
 * juego ni desde el EDT. Usar {@link RankingService}, que los ejecuta en segundo plano.
 *
 * Configuración. Cada valor se busca primero en una variable de entorno y, si no
 * existe, en el archivo db.properties de la carpeta de trabajo (raíz del proyecto):
 *   NEON_DB_PASS  (obligatoria) contraseña del usuario de Neon
 *   NEON_DB_USER  (opcional)    por defecto "neondb_owner"
 *   NEON_DB_URL   (opcional)    URL JDBC completa; por defecto el host pooler de Neon
 * db.properties NO debe subirse a Git (está en .gitignore).
 */
public class BaseDeDatos {

    private static final Properties ARCHIVO = cargarArchivo();

    private static final String URL = valor("NEON_DB_URL",
            "jdbc:postgresql://ep-blue-cherry-beubmwuq-pooler.c-9.us-east-2.aws.neon.tech/neondb?sslmode=require");
    private static final String USER = valor("NEON_DB_USER", "neondb_owner");
    private static final String PASS = valor("NEON_DB_PASS", null);

    private static final int MAX_LARGO_NOMBRE = 50; // VARCHAR(50) en la tabla

    private static Properties cargarArchivo() {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream("db.properties")) {
            props.load(in);
        } catch (IOException e) {
            // Normal si el archivo no existe: se usan solo las variables de entorno.
        }
        return props;
    }

    /** Variable de entorno > db.properties > valor por defecto. Los espacios sobrantes se recortan. */
    private static String valor(String clave, String porDefecto) {
        String v = System.getenv(clave);
        if (v == null || v.isBlank()) v = ARCHIVO.getProperty(clave);
        return (v == null || v.isBlank()) ? porDefecto : v.trim();
    }

    /** Abre una conexión nueva. Quien la reciba debe cerrarla (try-with-resources). */
    static Connection getConnection() throws SQLException {
        if (PASS == null || PASS.isEmpty()) {
            throw new SQLException("Falta NEON_DB_PASS (variable de entorno o db.properties)");
        }
        Properties props = new Properties();
        props.setProperty("user", USER);
        props.setProperty("password", PASS);
        // Tiempos máximos (segundos) para que una red caída no deje colgada la consulta.
        props.setProperty("loginTimeout", "10");
        props.setProperty("connectTimeout", "8");
        props.setProperty("socketTimeout", "10");
        return DriverManager.getConnection(URL, props);
    }

    private static String normalizarNombre(String nombre) {
        if (nombre == null) return null;
        String limpio = nombre.trim();
        if (limpio.isEmpty()) return null;
        return limpio.length() > MAX_LARGO_NOMBRE ? limpio.substring(0, MAX_LARGO_NOMBRE) : limpio;
    }

    // ---------------------------------------------------------------- crear tabla

    /** Opcional: la tabla ya existe en Neon. Se deja por comodidad en entornos nuevos. */
    public static void crearTablaRecords() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS records ("
                + "id SERIAL PRIMARY KEY, "
                + "nombre_jugador VARCHAR(50) NOT NULL UNIQUE, "
                + "puntuacion INT NOT NULL DEFAULT 0, "
                + "fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.execute();
        }
    }

    // ------------------------------------------------------------------- guardar

    /**
     * Guarda la puntuación (segundos sobrevividos) del jugador. Hay una fila por nombre:
     * solo se actualiza si la nueva puntuación SUPERA a la guardada, y en ese caso
     * también se actualiza la fecha. Un nombre vacío se ignora.
     */
    public static void guardarRecord(String nombreJugador, int puntuacion) throws SQLException {
        try (Connection conn = getConnection()) {
            guardarRecord(conn, nombreJugador, puntuacion);
        }
    }

    static void guardarRecord(Connection conn, String nombreJugador, int puntuacion) throws SQLException {
        String nombre = normalizarNombre(nombreJugador);
        if (nombre == null) return;

        String sql = "INSERT INTO records (nombre_jugador, puntuacion) VALUES (?, ?) "
                + "ON CONFLICT (nombre_jugador) DO UPDATE "
                + "SET puntuacion = EXCLUDED.puntuacion, fecha_registro = CURRENT_TIMESTAMP "
                + "WHERE EXCLUDED.puntuacion > records.puntuacion";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setInt(2, Math.max(0, puntuacion));
            ps.executeUpdate();
        }
    }

    // ------------------------------------------------------------------ consultar

    /** Mejor puntuación guardada para ese jugador, o 0 si no tiene registro. */
    public static int obtenerMejorRecord(String nombreJugador) throws SQLException {
        try (Connection conn = getConnection()) {
            return obtenerMejorRecord(conn, nombreJugador);
        }
    }

    static int obtenerMejorRecord(Connection conn, String nombreJugador) throws SQLException {
        String nombre = normalizarNombre(nombreJugador);
        if (nombre == null) return 0;

        String sql = "SELECT puntuacion FROM records WHERE nombre_jugador = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("puntuacion") : 0;
            }
        }
    }

    /** Los {@code limite} mejores récords, de mayor a menor. Lista vacía si la tabla está vacía. */
    public static List<RecordEntry> obtenerTopRecords(int limite) throws SQLException {
        try (Connection conn = getConnection()) {
            return obtenerTopRecords(conn, limite);
        }
    }

    static List<RecordEntry> obtenerTopRecords(Connection conn, int limite) throws SQLException {
        if (limite <= 0) return Collections.emptyList();

        String sql = "SELECT nombre_jugador, puntuacion FROM records "
                + "ORDER BY puntuacion DESC, fecha_registro ASC LIMIT ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limite);
            try (ResultSet rs = ps.executeQuery()) {
                List<RecordEntry> resultado = new ArrayList<>();
                while (rs.next()) {
                    resultado.add(new RecordEntry(rs.getString("nombre_jugador"), rs.getInt("puntuacion")));
                }
                return Collections.unmodifiableList(resultado);
            }
        }
    }

    // ------------------------------------------------------------------- modelo

    public static final class RecordEntry {
        private final String nombre;
        private final int puntuacion;

        public RecordEntry(String nombre, int puntuacion) {
            this.nombre = nombre;
            this.puntuacion = puntuacion;
        }

        public String getNombre() {
            return nombre;
        }

        public int getPuntuacion() {
            return puntuacion;
        }

        @Override
        public String toString() {
            return nombre + ": " + puntuacion;
        }
    }
}
