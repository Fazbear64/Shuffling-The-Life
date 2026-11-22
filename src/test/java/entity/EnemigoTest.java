package entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EnemigoTest {

    @Test
    public void testEnemigoNormal() {
        Enemigo normal = new Enemigo(100, 100, ID.ENEMIGO);
        assertEquals(2, normal.getVida());
        assertEquals(2, normal.getVidaMax());
        assertEquals(1, normal.getDaño());
        assertEquals(2.0f, normal.getVelocidad(), 0.001);
        assertEquals(28, normal.getAncho());
        assertEquals(28, normal.getAlto());
        assertFalse(normal.estaMuerto());

        normal.recibirDaño(1);
        assertEquals(1, normal.getVida());
        assertFalse(normal.estaMuerto());

        normal.recibirDaño(1);
        assertTrue(normal.estaMuerto());
    }

    @Test
    public void testEnemigoTanque() {
        Enemigo tanque = new Enemigo(100, 100, ID.ENEMIGO_TANQUE);
        assertEquals(6, tanque.getVida());
        assertEquals(6, tanque.getVidaMax());
        assertEquals(2, tanque.getDaño());
        assertEquals(1.1f, tanque.getVelocidad(), 0.001);
        assertEquals(42, tanque.getAncho());
        assertEquals(42, tanque.getAlto());

        // Es más grande que el normal
        Enemigo normal = new Enemigo(100, 100, ID.ENEMIGO);
        assertTrue(tanque.getAncho() > normal.getAncho());
        assertTrue(tanque.getVidaMax() > normal.getVidaMax());
        assertTrue(tanque.getDaño() > normal.getDaño());
        assertTrue(tanque.getVelocidad() < normal.getVelocidad());

        tanque.recibirDaño(4);
        assertEquals(2, tanque.getVida());
        assertFalse(tanque.estaMuerto());

        tanque.recibirDaño(2);
        assertTrue(tanque.estaMuerto());
    }

    @Test
    public void testEnemigoVeloz() {
        Enemigo veloz = new Enemigo(100, 100, ID.ENEMIGO_VELOZ);
        assertEquals(1, veloz.getVida());
        assertEquals(1, veloz.getVidaMax());
        assertEquals(1, veloz.getDaño());
        assertEquals(3.5f, veloz.getVelocidad(), 0.001);
        assertEquals(20, veloz.getAncho());
        assertEquals(20, veloz.getAlto());

        // Es más rápido y con menos vida que el normal
        Enemigo normal = new Enemigo(100, 100, ID.ENEMIGO);
        assertTrue(veloz.getVelocidad() > normal.getVelocidad());
        assertTrue(veloz.getVidaMax() < normal.getVidaMax());

        // Fácil de matar (1 golpe)
        veloz.recibirDaño(1);
        assertTrue(veloz.estaMuerto());
    }

    @Test
    public void testPerseguirJugador() {
        Enemigo normal = new Enemigo(100, 100, ID.ENEMIGO);
        normal.perseguir(200, 100);
        assertTrue(normal.getX() > 100);
        assertEquals(100, normal.getY());
    }
}
