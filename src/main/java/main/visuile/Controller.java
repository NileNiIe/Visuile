package main.visuile;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.layout.Pane;
import javafx.scene.media.AudioSpectrumListener;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.io.File;
import java.net.URL;
import java.util.*;

import static java.lang.Thread.sleep;

public class Controller implements Initializable {
    @FXML public AreaChart<String, Number> spectrum;
    @FXML private Label songLabel;
    @FXML private Button playPause, skipBackButton, skipForwardButton, styleBackButton, styleForwardButton;
    @FXML private Slider volumeSlider;
    @FXML private ProgressBar songProgressBar;

    private Media media;
    private MediaPlayer mediaPlayer;

    private File directory;
    private File[] files;

    private ArrayList<File> songs;
    private ArrayList<Integer> style;

    private int songNumber;

    private Timer timer;
    private TimerTask task;
    private boolean running;
    private XYChart.Data[] series1Data;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        songs = new ArrayList<File>();
        directory = new File("src/main/Music");
        files = directory.listFiles();

        if(files != null){
            for(File file: files){
                songs.add(file);
                System.out.println(file);
            }
        }





        XYChart.Series<String, Number> series1 = new XYChart.Series<>();
        series1Data = new XYChart.Data[32 + 2];
        for (int i = 0; i < series1Data.length; i++) {
            series1Data[i] = new XYChart.Data<>(Integer.toString(i + 1), 0);
            //noinspection unchecked
            series1.getData().add(series1Data[i]);
        }
        spectrum.getData().add(series1);

        media = new Media(songs.get(songNumber).toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setOnReady(new PreparationWorker());
        updateLabel();
    }



    private float[] createFilledBuffer(int size, float fillValue) {
        float[] floats = new float[size];
        Arrays.fill(floats, fillValue);
        return floats;
    }

    public void toggleSong(){
        if(!running) {
            mediaPlayer.play();
            running = true;
            playPause.setText("⏸");
        }
        else {
            mediaPlayer.pause();
            running = false;
            playPause.setText("▶");
        }
    }
    public void rewind(){
        if(mediaPlayer.getCurrentTime().greaterThan(Duration.seconds(2.0)))
            mediaPlayer.seek(mediaPlayer.getCurrentTime().negate());
        else{
            boolean temp = false;
            if(running)
                temp = true;
            mediaPlayer.pause();
            if(songNumber>0)
                songNumber--;
            else
                songNumber=songs.size()-1;
            mediaPlayer = new MediaPlayer(new Media(songs.get(songNumber).toURI().toString()));
            mediaPlayer.setOnReady(new PreparationWorker());
            if(temp)
                mediaPlayer.play();
            updateLabel();
        }

    }

    public void forward(){
        boolean temp = false;
        if(running)
            temp = true;
        mediaPlayer.pause();
        if(songNumber<songs.size()-1)
            songNumber++;
        else
            songNumber=0;
        mediaPlayer = new MediaPlayer(new Media(songs.get(songNumber).toURI().toString()));
        mediaPlayer.setOnReady(new PreparationWorker());

        if(temp)
            mediaPlayer.play();
        updateLabel();
    }
    public void updateLabel(){
        String newLabel = songs.get(songNumber).getName();
        newLabel = newLabel.substring(0,newLabel.indexOf(".mp3"));
        songLabel.setText(newLabel);
    }

    public void styleForward(){

    }

    public void styleRewind(){

    }

    public void adjustVolume() {
        mediaPlayer.setVolume(volumeSlider.getValue() / 200);
        System.out.println(mediaPlayer.getVolume());
    }


    public void beginTimer(){

    }

    public void cancelTimer(){

    }


    private class SpektrumListener implements AudioSpectrumListener {
        float[] buffer = createFilledBuffer(32, mediaPlayer.getAudioSpectrumThreshold());

        @Override
        public void spectrumDataUpdate(double timestamp, double duration, float[] magnitudes, float[] phases) {
            Platform.runLater(() -> {
                //noinspection unchecked
                series1Data[0].setYValue(0);
                //noinspection unchecked
                series1Data[32 + 1].setYValue(0);
                for (int i = 0; i < magnitudes.length; i++) {
                    if (magnitudes[i] >= buffer[i]) {
                        buffer[i] = magnitudes[i];
                        //noinspection unchecked
                        series1Data[i + 1].setYValue(magnitudes[i] - mediaPlayer.getAudioSpectrumThreshold());
                    } else {
                        //noinspection unchecked
                        series1Data[i + 1].setYValue(buffer[i] - mediaPlayer.getAudioSpectrumThreshold());
                        buffer[i] -= 2;
                    }
                }
            });
        }
    }

    private class PreparationWorker implements Runnable{
        public void run(){
            mediaPlayer.setAudioSpectrumListener(new SpektrumListener());
            mediaPlayer.setAudioSpectrumNumBands(32);
        }
    }
}
