package visual;

import config.DefaultSettings;
import entity.GameObject;

public class Camera {
    private float x, y;

    public Camera(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void tick(GameObject player) {
        // Centrar la cámara en el jugador
        if (player != null) {
            x = player.getX() - DefaultSettings.screenWidth / 2f + 16;
            y = player.getY() - DefaultSettings.screenHeight / 2f + 16;

            // Limitar la cámara a los bordes del mundo (2700x1800)
            if (x < 0) x = 0;
            if (y < 0) y = 0;
            if (x > DefaultSettings.worldWidth - DefaultSettings.screenWidth) {
                x = DefaultSettings.worldWidth - DefaultSettings.screenWidth;
            }
            if (y > DefaultSettings.worldHeight - DefaultSettings.screenHeight) {
                y = DefaultSettings.worldHeight - DefaultSettings.screenHeight;
            }
        }
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public void setX(float x) { this.x = x; }
    public void setY(float y) { this.y = y; }
}