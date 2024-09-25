package projector.controller.util;

import com.bence.projector.common.dto.ProjectionDTO;
import projector.model.Song;
import projector.utils.scene.text.SongVersePartTextFlow;

import java.util.List;

public class ProjectionData {
    private ProjectionDTO projectionDTO;
    private Song song;
    private List<SongVersePartTextFlow> songVersePartTextFlows;

    public ProjectionDTO getProjectionDTO() {
        return projectionDTO;
    }

    public void setProjectionDTO(ProjectionDTO projectionDTO) {
        this.projectionDTO = projectionDTO;
    }

    public void setSong(Song song) {
        this.song = song;
    }

    public Song getSong() {
        return song;
    }

    public void setSongVersePartTextFlows(List<SongVersePartTextFlow> songVersePartTextFlows) {
        this.songVersePartTextFlows = songVersePartTextFlows;
    }

    public List<SongVersePartTextFlow> getSongVersePartTextFlows() {
        return songVersePartTextFlows;
    }
}
