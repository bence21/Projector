package projector.model;

import com.bence.projector.common.model.SectionType;
import com.google.gson.annotations.Expose;
import com.j256.ormlite.field.DatabaseField;
import projector.application.Settings;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static com.bence.projector.common.util.StringUtils.trimLongString;
import static projector.utils.StringUtils.stripAccents;

public class SongVerse extends BaseEntity {

    private static final int MAX_TEXT_LENGTH = 1000;
    @Expose
    @DatabaseField(width = MAX_TEXT_LENGTH)
    private String text;
    @Expose
    @DatabaseField(width = MAX_TEXT_LENGTH)
    private String secondText;
    @DatabaseField(width = MAX_TEXT_LENGTH)
    private String strippedText;
    @Expose
    @DatabaseField
    private boolean chorus;
    private boolean repeated;
    @DatabaseField(foreign = true, foreignAutoRefresh = true, index = true)
    private Song mainSong;
    @DatabaseField
    private Integer sectionTypeData;
    private SectionType sectionType;

    public SongVerse() {
    }

    public SongVerse(SongVerse songVerse) {
        this.text = songVerse.text;
        if (text != null) {
            strippedText = stripAccents(text.toLowerCase());
        }
        this.chorus = songVerse.chorus;
        this.repeated = songVerse.repeated;
        this.secondText = songVerse.secondText;
        this.mainSong = songVerse.mainSong;
        this.sectionTypeData = songVerse.sectionTypeData;
        this.sectionType = songVerse.sectionType;
    }

    static List<SongVerse> cloneList(List<SongVerse> songVerses) {
        if (songVerses == null) {
            return null;
        }
        List<SongVerse> clonedSongVerses = new ArrayList<>(songVerses.size());
        for (SongVerse songVerse : songVerses) {
            clonedSongVerses.add(new SongVerse(songVerse));
        }
        return clonedSongVerses;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = trimLongString(text, MAX_TEXT_LENGTH);
        strippedText = stripAccents(this.text.toLowerCase());
    }

    public boolean isChorus() {
        if (sectionTypeData == null) {
            return chorus;
        }
        return SectionType.getInstance(sectionTypeData) == SectionType.CHORUS;
    }

    public void setChorus(boolean chorus) {
        this.chorus = chorus;
    }

    public boolean isRepeated() {
        return repeated;
    }

    public void setRepeated(boolean repeated) {
        this.repeated = repeated;
    }

    public void setMainSong(Song mainSong) {
        this.mainSong = mainSong;
    }

    public String getStrippedText() {
        return strippedText;
    }

    public String getSecondText() {
        return secondText;
    }

    public void setSecondText(String secondText) {
        this.secondText = trimLongString(secondText, MAX_TEXT_LENGTH);
    }

    public SectionType getSectionType() {
        if (sectionTypeData == null) {
            sectionType = SectionType.VERSE;
            if (isChorus()) {
                sectionType = SectionType.CHORUS;
            }
        } else {
            sectionType = SectionType.getInstance(sectionTypeData);
        }
        return sectionType;
    }

    public void setSectionType(SectionType sectionType) {
        this.sectionType = sectionType;
        this.sectionTypeData = sectionType.getValue();
    }

    private String getSectionTypeString() {
        Settings settings = Settings.getInstance();
        ResourceBundle bundle = settings.getResourceBundle();
        return switch (getSectionType()) {
            case INTRO -> bundle.getString("letter_intro");
            case VERSE -> bundle.getString("letter_verse");
            case PRE_CHORUS -> bundle.getString("letter_pre_chorus");
            case CHORUS -> bundle.getString("letter_chorus");
            case BRIDGE -> bundle.getString("letter_bridge");
            case CODA -> bundle.getString("letter_coda");
        };
    }

    private int getSongVerseCountBySectionType(SectionType sectionType) {
        if (mainSong == null) {
            return 0;
        }
        int count = 0;
        for (SongVerse verse : mainSong.getVerses()) {
            if (verse.getSectionType() == sectionType) {
                ++count;
                if (verse.equals(this)) {
                    break;
                }
            }
        }
        return count;
    }

    private int getSongVerseCountBySectionType() {
        return getSongVerseCountBySectionType(getSectionType());
    }

    private boolean hasOtherSameTypeInSong() {
        if (mainSong == null) {
            return false;
        }
        for (SongVerse verse : mainSong.getVerses()) {
            if (verse.getSectionType() == getSectionType() && !verse.equals(this)) {
                return true;
            }
        }
        return false;
    }

    public String getSectionTypeStringWithCount() {
        String sectionTypeString = getSectionTypeString();
        if (hasOtherSameTypeInSong()) {
            sectionTypeString += getSongVerseCountBySectionType();
        }
        return sectionTypeString;
    }

    public String getLongSectionTypeStringWithCount() {
        String sectionTypeString = getSectionTypeString(sectionType);
        if (hasOtherSameTypeInSong()) {
            sectionTypeString += getSongVerseCountBySectionType();
        }
        return sectionTypeString;
    }

    public String getSectionTypeString(SectionType sectionType) {
        Settings settings = Settings.getInstance();
        ResourceBundle bundle = settings.getResourceBundle();
        return switch (sectionType) {
            case INTRO -> bundle.getString("intro");
            case VERSE -> bundle.getString("verse");
            case PRE_CHORUS -> bundle.getString("pre_chorus");
            case CHORUS -> bundle.getString("chorus");
            case BRIDGE -> bundle.getString("bridge");
            case CODA -> bundle.getString("coda");
        };
    }

    public boolean equals(SongVerse other) {
        String uuid = getUuid();
        String otherUuid = other.getUuid();
        if (uuid != null && otherUuid != null) {
            return uuid.equals(otherUuid);
        }
        Long id = getId();
        Long otherId = other.getId();
        if (id != null && otherId != null) {
            return id.equals(otherId);
        }
        return equalsOtherText(other);
    }

    private boolean equalsOtherText(SongVerse other) {
        if (text == null) {
            return other.text == null;
        }
        return text.equals(other.text);
    }

    public short getVerseOrderIndex() {
        short index = 0;
        if (mainSong == null) {
            return 0;
        }
        for (SongVerse songVerse : mainSong.getVerses()) {
            if (equalsOtherText(songVerse)) {
                return index;
            }
            ++index;
        }
        return 0;
    }
}
