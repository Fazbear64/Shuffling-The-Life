package entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CartaTarotTest {

    @Test
    public void testGenerarOpcionesAleatorias() {
        CartaTarot[] opciones = CartaTarot.generarOpcionesAleatorias();
        assertNotNull(opciones);
        assertEquals(3, opciones.length);

        for (CartaTarot carta : opciones) {
            assertNotNull(carta);
            assertNotNull(carta.getNombre());
            assertNotNull(carta.getEfecto());
        }
    }

    @Test
    public void testGenerarPositivo() {
        CartaTarot carta = CartaTarot.generarPositivoAleatorio();
        assertNotNull(carta);
        assertEquals(CartaTarot.EFECTO_POSITIVO, carta.getEfecto());
        assertTrue(carta.getValor() > 0);
    }

    @Test
    public void testGenerarNegativo() {
        CartaTarot carta = CartaTarot.generarNegativoAleatorio();
        assertNotNull(carta);
        assertEquals(CartaTarot.EFECTO_NEGATIVO, carta.getEfecto());
        assertTrue(carta.getValor() < 0);
    }
}