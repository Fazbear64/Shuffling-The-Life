package entity;

import java.awt.*;

public abstract class GameObject {
        protected int x, y;
        protected ID id;
        protected int vel;

        public GameObject (int x, int y, ID id){
            this.x = x;
            this.y = y;
            this.id = id;
        }

        // Getters y setters (necesarios para compile.bat que no procesa Lombok)
        public int getX() { return x; }
        public int getY() { return y; }
        public ID getId() { return id; }
        public void setId(ID id) { this.id = id; }
        public int getVel() { return vel; }
        public void setVel(int vel) { this.vel = vel; }

        public abstract void tick();
        public abstract void render(Graphics g);
}