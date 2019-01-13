import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

public class SimplePlayerWithPlayFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final  String path="/home/alinso/Videos/";
    private Map<String,Long> mapFileNameAndDuration;

    private ActionListenerImpl actionListenerImpl;

    private Canvas videoSurface;
    private JPanel controlPanel;
    private JButton playButton;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


    private MediaPlayerFactory mediaPlayerFactory;
    private EmbeddedMediaPlayer mediaPlayer;

    public SimplePlayerWithPlayFrame() {
        super("porn player");

        // Setups components and listeners.
        actionListenerImpl = new ActionListenerImpl();

        videoSurface = new Canvas();
        videoSurface.setBackground(Color.BLACK);
        videoSurface.setPreferredSize(new Dimension(400, 300));  // Initial dummy dimension.

        controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.LINE_AXIS));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        playButton = new JButton("SHUFFLE");
        playButton.addActionListener(actionListenerImpl);

        // Setups vlcj objects.
        String[] vlcArgs = {
                "--quiet",            // Avoids warning and information messages.
                "--quiet-synchro",    // Avoids debug infos about video output synchronization.
                "--intf", "dummy",    // No interface.

        };

        mediaPlayerFactory = new MediaPlayerFactory(vlcArgs);
        mediaPlayerFactory.setUserAgent("Simple Player by ali");

        mediaPlayer = mediaPlayerFactory.newEmbeddedMediaPlayer();
        mediaPlayer.setVideoSurface(mediaPlayerFactory.newVideoSurface(videoSurface));
        mediaPlayer.addMediaPlayerEventListener(new MediaPlayerEventListenerImpl());

        // Layouts the components.
        controlPanel.add(playButton);

        add(videoSurface, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);

        // Setups the frame.
        addWindowListener(new WindowListenerImpl());
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);   // Centers the frame on the screen.

        this.mapFileNameAndDuration = mapFileNameAndDuration();

    }

    public class VideoChangerByTime implements Runnable {
        public void run() {
            changeFilm();
        }
    }

    private void packVideo(Dimension videoDimension) {
        // Packs the frame to the size appropriate to fit the video.
        videoSurface.setPreferredSize(videoDimension);
        pack();
        setLocationRelativeTo(null);   // Centers the frame on the screen.
    }


    private Map<String, Long> mapFileNameAndDuration() {
        File folder = new File(SimplePlayerWithPlayFrame.path);
        File[] listOfFiles = folder.listFiles();
        Map<String, Long> result = new HashMap<String, Long>();

        for(File file:listOfFiles[0].listFiles()){

                result.put(file.getAbsolutePath(), (long)(30*60));
        }
        return result;
    }


    // Implementation of ActionListener for actions from components.
    private class ActionListenerImpl implements ActionListener {
        public void actionPerformed(ActionEvent e) {

            changeFilm();
        }
    }

    private void changeFilm(){

        Random randVideo = new Random();
        Random randStart =  new Random();


        Integer randVideoIndex=randVideo.nextInt(mapFileNameAndDuration.size());
        String videoFile="";
        Integer randStartTime=0;

        int i=0;
        for(Map.Entry<String, Long> entry : mapFileNameAndDuration.entrySet())
        {
            if(i==randVideoIndex){
                videoFile  =entry.getKey();
                randStartTime = randStart.nextInt(Math.toIntExact(entry.getValue()-90));
            }
            i++;
        }

            mediaPlayer.playMedia(videoFile, ":start-time="+randStartTime, ":stop-time="+(randStartTime+600));
    }




    // Implementation of MediaPlayerEventListener for video events.
    // NOTE: media player events are dispatched in the context of a thread
    // that is NOT the AWT/Swing Event Dispatch Thread.
    private class MediaPlayerEventListenerImpl extends MediaPlayerEventAdapter {
        @Override
        public void videoOutput(MediaPlayer mediaPlayer, int newCount) {
            final Dimension videoDimension = mediaPlayer.getVideoDimension();

            if (videoDimension != null) {
                // Uses invokeLater to execute packVideo in the context of the EDT.
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        packVideo(videoDimension);
                    }
                });
            }
        }
    }

    // Implementation of WindowListener for the frame.
    private class WindowListenerImpl extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent e) {
            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }

            if (mediaPlayerFactory != null) {
                mediaPlayerFactory.release();
                mediaPlayerFactory = null;
            }
        }
    }
}