package logic;

import javax.swing.Timer;
import java.awt.event.ActionEvent;

public class Chronometer {
    private Timer mTimer;
    private int minutes;
    private int seconds;
    private int milliseconds;
    private boolean running;

    public Chronometer(int delayMillis) {
        this.mTimer = new Timer(delayMillis, (ActionEvent e) -> tickTime(delayMillis));
    }

    public void startChronometer() {
        if (!running) {
            running = true;
            mTimer.start();
        }
    }

    public void stopChronometer() {
        running = false;
        mTimer.stop();
    }

    private void tickTime(int elapsedMillis) {
        milliseconds += elapsedMillis;
        if (milliseconds >= 1000) {
            seconds += milliseconds / 1000;
            milliseconds %= 1000;
        }
        if (seconds >= 60) {
            minutes += seconds / 60;
            seconds %= 60;
        }
    }

    public String getFormattedTime() {
        return String.format("%02d:%02d.%03d", minutes, seconds, milliseconds);
    }
}