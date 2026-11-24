package entity;

public enum TipoArma {
    REVOLVER("Revolver", "Pistola clásica precisa y equilibrada"),
    ESCOPETA("Escopeta", "Dispara múltiples perdigones en abanico"),
    MINIGUN("Minigun", "Ametralladora rotativa de altísima cadencia"),
    LANZALLAMAS("Lanzallamas", "Chorro de fuego devastador a corta distancia");

    private final String nombre;
    private final String descripcion;

    TipoArma(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
