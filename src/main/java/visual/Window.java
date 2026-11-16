package visual;

import dto.DefaultSettings;

import javax.swing.*;
import java.awt.*;

public class Window extends Canvas {
    public Window(Game game){
        JFrame window = new JFrame("Shuffling The Life");
        window.setSize(DefaultSettings.screenWidth, DefaultSettings.screenHeight);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.setLocationRelativeTo(null);
        window.setVisible(true);
        window.add(game);
        game.start();
    }
}
