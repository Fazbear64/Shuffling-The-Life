package entity;

public interface Damageable {
    void recibirDaño(int cantidad);
    boolean estaMuerto();
    int getVida();
}
