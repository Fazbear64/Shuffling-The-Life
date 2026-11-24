package entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PersonajeTest {

    @Test
    public void testMapacheBonificaciones() {
        Personaje mapache = Personaje.crearPersonaje(Personaje.PERSONAJE_MAPACHE);
        assertNotNull(mapache);
        assertTrue(mapache.isDesbloqueado());
        assertEquals(0.10f, mapache.getBonificacionVida(), 0.001);
        assertEquals(0.15f, mapache.getBonificacionDanio(), 0.001);
        assertEquals(0.10f, mapache.getBonificacionVelocidad(), 0.001);
    }

    @Test
    public void testGatoBonificaciones() {
        Personaje gato = Personaje.crearPersonaje(Personaje.PERSONAJE_GATO);
        assertNotNull(gato);
        assertFalse(gato.isDesbloqueado());
        assertEquals(0.20f, gato.getBonificacionVelocidad(), 0.001);
    }

    @Test
    public void testRanaBonificaciones() {
        Personaje rana = Personaje.crearPersonaje(Personaje.PERSONAJE_RANA);
        assertNotNull(rana);
        assertFalse(rana.isDesbloqueado());
        assertEquals(0.30f, rana.getBonificacionVida(), 0.001);
        assertEquals(-0.10f, rana.getBonificacionVelocidad(), 0.001);
    }

    @Test
    public void testCollieBonificaciones() {
        Personaje collie = Personaje.crearPersonaje(Personaje.PERSONAJE_COLLIE);
        assertNotNull(collie);
        assertFalse(collie.isDesbloqueado());
        assertEquals(0.20f, collie.getBonificacionVelocidad(), 0.001);
        assertEquals(0.15f, collie.getBonificacionDanio(), 0.001);
    }
}