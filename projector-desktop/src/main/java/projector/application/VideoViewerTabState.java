package projector.application;

import com.google.gson.annotations.Expose;

public class VideoViewerTabState {

    @Expose
    private String filePath;
    @Expose
    private double positionSeconds;
    @Expose
    private boolean playing;
    @Expose
    private double volume = 1.0;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public double getPositionSeconds() {
        return positionSeconds;
    }

    public void setPositionSeconds(double positionSeconds) {
        this.positionSeconds = positionSeconds;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }
}
