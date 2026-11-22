package entity;

import lombok.Getter;
import lombok.Setter;

import java.awt.*;

@Getter
@Setter
public abstract class GameObject {
        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public ID getId() {
            return id;
        }

        public void setId(ID id) {
            this.id = id;
        }

        public int getVel() {
            return vel;
        }

        public void setVel(int vel) {
            this.vel = vel;
        }
        protected int x, y;
        protected ID id;
        protected int vel;

        public GameObject (int x, int y, ID id){
            this.x = x;
            this.y = y;
            this.id = id;
        }

        public abstract void tick();
        public abstract void render(Graphics g);
}
