package projector.application;

import com.google.gson.annotations.Expose;
import projector.controller.util.ProjectionData;
import projector.model.Language;
import projector.model.Song;
import projector.service.ServiceManager;
import projector.service.SongService;

import java.util.ArrayList;
import java.util.List;

public class ProjectorState {

    @Expose
    private boolean loaded = false;
    @Expose
    private boolean isBlank;
    @Expose
    private ProjectionType projectionType;
    @Expose
    private ProjectionData projectionData;
    @Expose
    private String activeText;
    @Expose
    private String selectedSongUuid;
    @Expose
    private Long selectedSongId;
    @Expose
    private String selectedLanguageUuid;
    @Expose
    private List<ScheduleEntryState> scheduleEntries = new ArrayList<>();
    @Expose
    private int scheduleSelectedIndex = -1;
    @Expose
    private int songVerseSelectedIndex = -1;
    @Expose
    private String selectedTabId;
    @Expose
    private List<String> openPdfPaths = new ArrayList<>();
    @Expose
    private List<String> openVideoPaths = new ArrayList<>();
    @Expose
    private List<PdfViewerTabState> openPdfTabs = new ArrayList<>();
    @Expose
    private List<VideoViewerTabState> openVideoTabs = new ArrayList<>();
    @Expose
    private String galleryFolderPath;
    @Expose
    private String gallerySelectedImagePath;
    @Expose
    private String projectionFileImagePath;
    @Expose
    private String bibleSearchText;
    @Expose
    private int bibleSearchMatchIndex = -1;
    @Expose
    private String selectedBibleUuid;
    @Expose
    private int selectedBibleBookIndex = -1;
    @Expose
    private int selectedBiblePartIndex = -1;
    @Expose
    private List<Integer> selectedBibleVerseIndices = new ArrayList<>();
    @Expose
    private List<BibleReferenceSlotState> bibleReferenceSlots = new ArrayList<>();
    @Expose
    private int selectedReferenceListIndex = -1;

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
            selectedSongUuid = null;
            selectedSongId = null;
            return;
        }
        this.selectedSongUuid = selectedSong.getUuid();
        if (this.selectedSongUuid == null) {
            this.selectedSongId = selectedSong.getId();
        } else {
            this.selectedSongId = null;
        }
    }

    public Language getSelectedLanguage() {
        return ServiceManager.getLanguageService().findByUuid(selectedLanguageUuid);
    }

    public void setSelectedLanguage(Language language) {
        if (language == null) {
            selectedLanguageUuid = null;
            return;
        }
        this.selectedLanguageUuid = language.getUuid();
    }

    public List<ScheduleEntryState> getScheduleEntries() {
        return scheduleEntries;
    }

    public void setScheduleEntries(List<ScheduleEntryState> scheduleEntries) {
        this.scheduleEntries = scheduleEntries != null ? scheduleEntries : new ArrayList<>();
    }

    public int getScheduleSelectedIndex() {
        return scheduleSelectedIndex;
    }

    public void setScheduleSelectedIndex(int scheduleSelectedIndex) {
        this.scheduleSelectedIndex = scheduleSelectedIndex;
    }

    public int getSongVerseSelectedIndex() {
        return songVerseSelectedIndex;
    }

    public void setSongVerseSelectedIndex(int songVerseSelectedIndex) {
        this.songVerseSelectedIndex = songVerseSelectedIndex;
    }

    public String getSelectedTabId() {
        return selectedTabId;
    }

    public void setSelectedTabId(String selectedTabId) {
        this.selectedTabId = selectedTabId;
    }

    public List<String> getOpenPdfPaths() {
        return openPdfPaths;
    }

    public void setOpenPdfPaths(List<String> openPdfPaths) {
        this.openPdfPaths = openPdfPaths != null ? openPdfPaths : new ArrayList<>();
    }

    public List<String> getOpenVideoPaths() {
        return openVideoPaths;
    }

    public void setOpenVideoPaths(List<String> openVideoPaths) {
        this.openVideoPaths = openVideoPaths != null ? openVideoPaths : new ArrayList<>();
    }

    public List<PdfViewerTabState> getOpenPdfTabs() {
        return openPdfTabs;
    }

    public void setOpenPdfTabs(List<PdfViewerTabState> openPdfTabs) {
        this.openPdfTabs = openPdfTabs != null ? openPdfTabs : new ArrayList<>();
    }

    public List<VideoViewerTabState> getOpenVideoTabs() {
        return openVideoTabs;
    }

    public void setOpenVideoTabs(List<VideoViewerTabState> openVideoTabs) {
        this.openVideoTabs = openVideoTabs != null ? openVideoTabs : new ArrayList<>();
    }

    public String getGalleryFolderPath() {
        return galleryFolderPath;
    }

    public void setGalleryFolderPath(String galleryFolderPath) {
        this.galleryFolderPath = galleryFolderPath;
    }

    public String getGallerySelectedImagePath() {
        return gallerySelectedImagePath;
    }

    public void setGallerySelectedImagePath(String gallerySelectedImagePath) {
        this.gallerySelectedImagePath = gallerySelectedImagePath;
    }

    public String getProjectionFileImagePath() {
        return projectionFileImagePath;
    }

    public void setProjectionFileImagePath(String projectionFileImagePath) {
        this.projectionFileImagePath = projectionFileImagePath;
    }

    public String getBibleSearchText() {
        return bibleSearchText;
    }

    public void setBibleSearchText(String bibleSearchText) {
        this.bibleSearchText = bibleSearchText;
    }

    public int getBibleSearchMatchIndex() {
        return bibleSearchMatchIndex;
    }

    public void setBibleSearchMatchIndex(int bibleSearchMatchIndex) {
        this.bibleSearchMatchIndex = bibleSearchMatchIndex;
    }

    public String getSelectedBibleUuid() {
        return selectedBibleUuid;
    }

    public void setSelectedBibleUuid(String selectedBibleUuid) {
        this.selectedBibleUuid = selectedBibleUuid;
    }

    public int getSelectedBibleBookIndex() {
        return selectedBibleBookIndex;
    }

    public void setSelectedBibleBookIndex(int selectedBibleBookIndex) {
        this.selectedBibleBookIndex = selectedBibleBookIndex;
    }

    public int getSelectedBiblePartIndex() {
        return selectedBiblePartIndex;
    }

    public void setSelectedBiblePartIndex(int selectedBiblePartIndex) {
        this.selectedBiblePartIndex = selectedBiblePartIndex;
    }

    public List<Integer> getSelectedBibleVerseIndices() {
        return selectedBibleVerseIndices;
    }

    public void setSelectedBibleVerseIndices(List<Integer> selectedBibleVerseIndices) {
        this.selectedBibleVerseIndices = selectedBibleVerseIndices != null
                ? new ArrayList<>(selectedBibleVerseIndices)
                : new ArrayList<>();
    }

    public List<BibleReferenceSlotState> getBibleReferenceSlots() {
        return bibleReferenceSlots;
    }

    public void setBibleReferenceSlots(List<BibleReferenceSlotState> bibleReferenceSlots) {
        this.bibleReferenceSlots = bibleReferenceSlots != null ? bibleReferenceSlots : new ArrayList<>();
    }

    public int getSelectedReferenceListIndex() {
        return selectedReferenceListIndex;
    }

    public void setSelectedReferenceListIndex(int selectedReferenceListIndex) {
        this.selectedReferenceListIndex = selectedReferenceListIndex;
    }
}
