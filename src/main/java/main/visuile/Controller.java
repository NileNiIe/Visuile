package main.visuile;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.media.AudioSpectrumListener;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;


import java.io.File;
import java.net.URL;
import java.util.*;

public class Controller implements Initializable {
    @FXML public AreaChart<String, Number> spectrum;
    @FXML private Label songLabel;
    @FXML private Button playPause, skipBackButton, skipForwardButton, styleBackButton, styleForwardButton, addSongButton;
    @FXML private Slider volumeSlider;
    @FXML private ProgressBar songProgressBar;

    private MediaPlayer mediaPlayer;

    private boolean running;
    private XYChart.Data[] series1Data;

    private ArrayList<File> queue;
    private int nowPlayingIndex;
    private FileChooser fileChooser;

    public Controller() {
        queue = new ArrayList<File>();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //songs = new ArrayList<File>();
        //directory = new File("src/main/Music");
        //files = directory.listFiles();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose a song...");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Audio Files", "*.mp3", "*.wav"));

        /**
        if(files != null){
            for(File file: files){
                songs.add(file);
                System.out.println(file);
            }
        }
         */

        XYChart.Series<String, Number> series1 = new XYChart.Series<>();
        series1Data = new XYChart.Data[32 + 2];
        for (int i = 0; i < series1Data.length; i++) {
            series1Data[i] = new XYChart.Data<>(Integer.toString(i + 1), 0);
            series1.getData().add(series1Data[i]);
        }
        spectrum.getData().add(series1);

        /**
        media = new Media(songs.get(songNumber).toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setOnReady(new PreparationWorker());
         */
        songLabel.setText("Please select a song...");
    }

    public void openFile(){
        FileChooser fc = new FileChooser();
        File file = fc.showOpenDialog(songLabel.getScene().getWindow());
        if (file !=null)
            enqueue(file);
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
    /**
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
     */
    //currently, this will just rewind the song to the start, since you cant rewind a queue
    public void rewind(){
        /**
        if(mediaPlayer.getCurrentTime().greaterThan(Duration.seconds(2.0)))
            mediaPlayer.seek(mediaPlayer.getCurrentTime().negate());
        else {
            if (nowPlayingIndex == 0) {
                nowPlayingIndex = queue.size()-1;
            } else {
                nowPlayingIndex--;
            }
            playSongAtIndex(nowPlayingIndex);
        }
        **/
        mediaPlayer.seek(mediaPlayer.getCurrentTime().negate());

    }

    //skips forward 1 but also removes the current song from the queue
    public void forward(){
        /**
        if(nowPlayingIndex+1==queue.size()){
            nowPlayingIndex=0;
        }
        else{
            nowPlayingIndex++;
        }
        */
        if(queue.size()>1) {
            queue.remove(0);
        }
        playSongAtIndex(0);

    }

    public void playSongAtIndex(int index){
        if(running)
            mediaPlayer.pause();

        mediaPlayer = new MediaPlayer(new Media(queue.get(nowPlayingIndex).toURI().toString()));
        mediaPlayer.setOnReady(new PreparationWorker());
        updateLabel();
        if(running)
            mediaPlayer.play();
    }

    public void updateLabel(){
        String newLabel = queue.get(nowPlayingIndex).getName();
        newLabel = newLabel.substring(0,newLabel.indexOf(".mp3"));
        songLabel.setText(newLabel);
    }

    //These two guys dont do anything :P might be removed idk
    public void styleForward(){}
    public void styleRewind(){

    }

    //THIS DOESNT REALLY WORK. PROBABLY FIX LATER
    public void adjustVolume() {
        mediaPlayer.setVolume(volumeSlider.getValue() / 200);
        System.out.println(mediaPlayer.getVolume());
    }

    public void enqueue(File file) {
        queue.add(file);
        if(queue.size()==1) {
            nowPlayingIndex=0;
            playSongAtIndex(nowPlayingIndex);
        }
    }

    private class SpectrumListener implements AudioSpectrumListener {
        float[] buffer = createFilledBuffer(32, mediaPlayer.getAudioSpectrumThreshold());

        @Override
        public void spectrumDataUpdate(double timestamp, double duration, float[] magnitudes, float[] phases) {
            Platform.runLater(() -> {
                series1Data[0].setYValue(0);
                series1Data[32 + 1].setYValue(0);
                for (int i = 0; i < magnitudes.length; i++) {
                    if (magnitudes[i] >= buffer[i]) {
                        buffer[i] = magnitudes[i];
                        series1Data[i + 1].setYValue(magnitudes[i] - mediaPlayer.getAudioSpectrumThreshold());
                    } else {
                        series1Data[i + 1].setYValue(buffer[i] - mediaPlayer.getAudioSpectrumThreshold());
                        buffer[i] -= 2;
                    }
                }
            });
        }
    }

    private class PreparationWorker implements Runnable{
        public void run(){
            mediaPlayer.setAudioSpectrumListener(new SpectrumListener());
            mediaPlayer.setAudioSpectrumNumBands(32);
        }
    }
}
