package projector.application;

import com.google.gson.annotations.Expose;
import projector.controller.util.ProjectionData;
import projector.model.Language;
import projector.model.Song;
import projector.service.ServiceManager;
import projector.service.SongService;

public class ProjectorState {

    @Expose
    private boolean loaded = false;
    @Expose
    private boolean isBlank;
    @Expose
    private ProjectionType projectionType;
    private ProjectionData projectionData;
    @Expose
    private String activeText;
    @Expose
    private String selectedSongUuid;
    @Expose
    private Long selectedSongId;
    @Expose
    private String selectedLanguageUuid;

    public boolean isLoaded() {
        return loaded;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public boolean isBlank() {
        return isBlank;
    }

    public void setBlank(boolean isBlank) {
        this.isBlank = isBlank;
    }

    public ProjectionType getProjectionType() {
        return projectionType;
    }

    public void setProjectionType(ProjectionType projectionType) {
        this.projectionType = projectionType;
    }

    public ProjectionData getProjectionData() {
        return projectionData;
    }

    public void setProjectionData(ProjectionData projectionDTO) {
        this.projectionData = projectionDTO;
    }

    public String getActiveText() {
        return activeText;
    }

    public void setActiveText(String activeText) {
        this.activeText = activeText;
    }

    public Song getSelectedSong() {
        SongService songService = ServiceManager.getSongService();
        if (selectedSongUuid != null) {
            Song byUuid = songService.findByUuid(selectedSongUuid);
            if (byUuid != null) {
                return byUuid;
            }
        }
        if (selectedSongId != null) {
            return songService.findById(selectedSongId);
        }
        return null;
    }

    public void setSelectedSong(Song selectedSong) {
        if (selectedSong == null) {
            return;
        }
        this.selectedSongUuid = selectedSong.getUuid();
        if (this.selectedSongUuid == null) {
            this.selectedSongId = selectedSong.getId();
        }
    }

    public Language getSelectedLanguage() {
        return ServiceManager.getLanguageService().findByUuid(selectedLanguageUuid);
    }

    public void setSelectedLanguage(Language language) {
        if (language == null) {
            return;
        }
        this.selectedLanguageUuid = language.getUuid();
    }
}
