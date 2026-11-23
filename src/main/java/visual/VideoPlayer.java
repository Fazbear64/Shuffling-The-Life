package visual;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class VideoPlayer {
    private Process videoProcess;
    private static final String VIDEO_PATH = "resources/cinematic.mp4";
    private Thread videoThread;

    public void playVideo() {
        videoThread = new Thread(() -> {
            try {
                File videoFile = new File(VIDEO_PATH);
                if (!videoFile.exists()) {
                    System.out.println("Video no encontrado: " + VIDEO_PATH);
                    return;
                }

                String absolutePath = videoFile.getAbsolutePath();
                String osName = System.getProperty("os.name").toLowerCase();
                String[] command = null;

                if (osName.contains("win")) {
                    // Windows: intentar con VLC primero, si no existe, usar reproductor predeterminado
                    String[] vlcPaths = {
                        "C:\\Program Files\\VideoLAN\\VLC\\vlc.exe",
                        "C:\\Program Files (x86)\\VideoLAN\\VLC\\vlc.exe"
                    };

                    boolean vlcFound = false;
                    for (String vlcPath : vlcPaths) {
                        if (Files.exists(Paths.get(vlcPath))) {
                            command = new String[]{vlcPath, "--fullscreen", "--play-and-exit", absolutePath};
                            vlcFound = true;
                            break;
                        }
                    }

                    if (!vlcFound) {
                        // Usar reproductor predeterminado de Windows
                        command = new String[]{"cmd", "/c", "start", "\"\"", absolutePath};
                    }
                } else if (osName.contains("mac")) {
                    command = new String[]{"open", "-a", "VLC", absolutePath};
                } else {
                    // Linux
                    command = new String[]{"vlc", "--fullscreen", "--play-and-exit", absolutePath};
                }

                if (command != null) {
                    videoProcess = new ProcessBuilder(command).start();
                    System.out.println("Reproduciendo video: " + absolutePath);
                    videoProcess.waitFor();
                    System.out.println("Video finalizado");
                }
            } catch (Exception e) {
                System.out.println("Error al reproducir video: " + e.getMessage());
                e.printStackTrace();
            }
        });
        videoThread.setDaemon(true);
        videoThread.start();
    }

    public void stopVideo() {
        if (videoProcess != null && videoProcess.isAlive()) {
            videoProcess.destroyForcibly();
            System.out.println("Video detenido");
        }
    }

    public boolean isVideoPlaying() {
        return videoProcess != null && videoProcess.isAlive();
    }
}
