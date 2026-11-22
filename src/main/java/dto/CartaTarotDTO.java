package dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartaTarotDTO {
    private String nombre;
    private String efecto;
    private float valor;
    private String tipoEfecto;

    public CartaTarotDTO() {
    }

    public CartaTarotDTO(String nombre, String efecto, float valor, String tipoEfecto) {
        this.nombre = nombre;
        this.efecto = efecto;
        this.valor = valor;
        this.tipoEfecto = tipoEfecto;
    }
}