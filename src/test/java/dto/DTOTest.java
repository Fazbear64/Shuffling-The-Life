package dto;

import entity.CartaTarot;
import entity.Enemigo;
import entity.ID;
import entity.Personaje;
import entity.Player;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DTOTest {

    @Test
    public void testPlayerDTOConversion() {
        Personaje p = Personaje.crearPersonaje(Personaje.PERSONAJE_MAPACHE);
        Player player = new Player(100, 200, ID.PLAYER, p);
        player.ganarMonedas(50);

        PlayerDTO dto = player.toDTO();
        assertNotNull(dto);
        assertEquals(100, dto.getX());
        assertEquals(200, dto.getY());
        assertEquals(player.getVidaActual(), dto.getVidaActual());
        assertEquals(player.getVidaMax(), dto.getVidaMax());
        assertEquals(player.getDañoBase(), dto.getDañoBase());
        assertEquals(player.getVelocidadMovimiento(), dto.getVelocidadMovimiento());
        assertEquals(50, dto.getMonedas());

        PlayerDTO nuevoDTO = new PlayerDTO(150, 250, 80, 100, 2, 5, 120, "Mapache");
        player.actualizarDesdeDTO(nuevoDTO);
        assertEquals(150, player.getX());
        assertEquals(250, player.getY());
        assertEquals(80, player.getVidaActual());
        assertEquals(5, player.getVelocidadMovimiento());
        assertEquals(120, player.getMonedas());
    }

    @Test
    public void testEnemigoDTOConversion() {
        Enemigo tanque = new Enemigo(50, 75, ID.ENEMIGO_TANQUE);
        EnemigoDTO dto = tanque.toDTO();

        assertNotNull(dto);
        assertEquals(50, dto.getX());
        assertEquals(75, dto.getY());
        assertEquals(ID.ENEMIGO_TANQUE, dto.getId());
        assertEquals(6, dto.getVida());
        assertEquals(6, dto.getVidaMax());
        assertEquals(2, dto.getDaño());
        assertEquals(1.1f, dto.getVelocidad(), 0.001);
        assertEquals(42, dto.getAncho());
        assertEquals(42, dto.getAlto());
    }

    @Test
    public void testCartaTarotDTOConversion() {
        CartaTarot carta = new CartaTarot("Fuerza", CartaTarot.EFECTO_POSITIVO, 0.25f, "daño");
        CartaTarotDTO dto = carta.toDTO();

        assertNotNull(dto);
        assertEquals("Fuerza", dto.getNombre());
        assertEquals(CartaTarot.EFECTO_POSITIVO, dto.getEfecto());
        assertEquals(0.25f, dto.getValor(), 0.001);
        assertEquals("daño", dto.getTipoEfecto());

        CartaTarot restaurada = CartaTarot.fromDTO(dto);
        assertNotNull(restaurada);
        assertEquals(carta.getNombre(), restaurada.getNombre());
        assertEquals(carta.getEfecto(), restaurada.getEfecto());
        assertEquals(carta.getValor(), restaurada.getValor(), 0.001);
    }

    @Test
    public void testPersonajeDTO() {
        Personaje p = Personaje.crearPersonaje(Personaje.PERSONAJE_MAPACHE);
        PersonajeDTO dto = p.toDTO();

        assertNotNull(dto);
        assertTrue(dto.isDesbloqueado());
        assertEquals(0.10f, dto.getBonificacionVida(), 0.001);
    }

    @Test
    public void testPartidaDTO() {
        PlayerDTO pDto = new PlayerDTO(10, 20, 100, 100, 1, 4, 30, "Mapache");
        PartidaDTO partida = new PartidaDTO(2, 1, 45000L, 15000L, "JUGANDO", pDto);

        assertEquals(2, partida.getRondaActual());
        assertEquals(1, partida.getTiradasTragaperras());
        assertEquals(45000L, partida.getMaxTiempoSobrevividoMs());
        assertEquals(15000L, partida.getTiempoActualOleadaMs());
        assertEquals("JUGANDO", partida.getEstadoActual());
        assertNotNull(partida.getJugador());
        assertEquals(30, partida.getJugador().getMonedas());
    }
}
