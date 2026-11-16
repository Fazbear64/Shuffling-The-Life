package visual;

import java.awt.*;

public class Game extends Canvas implements Runnable {
    public Game(){
        new Window(this);
    }

    public synchronized void start(){
    }

    public void run() {
    }

    public static void main (String[] args){
        new Game();
    }
}
