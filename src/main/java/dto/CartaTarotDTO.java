package dto;

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

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEfecto() { return efecto; }
    public void setEfecto(String efecto) { this.efecto = efecto; }

    public float getValor() { return valor; }
    public void setValor(float valor) { this.valor = valor; }

    public String getTipoEfecto() { return tipoEfecto; }
    public void setTipoEfecto(String tipoEfecto) { this.tipoEfecto = tipoEfecto; }
}