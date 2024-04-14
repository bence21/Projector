package com.bence.songbook.models;

import android.content.Context;

import com.bence.projector.common.model.SectionType;
import com.bence.songbook.R;
import com.j256.ormlite.field.DatabaseField;

import java.io.Serializable;

import static com.bence.songbook.ui.utils.StringUtils.stripAccents;

public class SongVerse extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    @DatabaseField
    private String text;
    @DatabaseField
    private String strippedText;
    @DatabaseField
    private boolean isChorus;
    @DatabaseField(foreign = true, index = true)
    private Song song;
    @DatabaseField
    private Integer sectionTypeData;
    private SectionType sectionType;

    public SongVerse() {
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        strippedText = stripAccents(text.toLowerCase());
    }

    public boolean isChorus() {
        return isChorus || sectionType == SectionType.CHORUS;
    }

    public void setChorus(boolean chorus) {
        isChorus = chorus;
    }

    public Song getSong() {
        return song;
    }

    public void setSong(Song song) {
        this.song = song;
    }

    public String getStrippedText() {
        return strippedText;
    }

    SectionType getSectionType() {
        if (sectionTypeData == null) {
            sectionType = SectionType.VERSE;
        } else {
            sectionType = SectionType.getInstance(sectionTypeData);
        }
        if (isChorus) {
            sectionType = SectionType.CHORUS;
        }
        return sectionType;
    }

    public void setSectionType(SectionType sectionType) {
        this.sectionType = sectionType;
        this.sectionTypeData = sectionType.getValue();
    }

    private String getSectionTypeString(Context context) {
        switch (getSectionType()) {
            case INTRO:
                return context.getString(R.string.letter_intro);
            case VERSE:
                return context.getString(R.string.letter_verse);
            case PRE_CHORUS:
                return context.getString(R.string.letter_pre_chorus);
            case CHORUS:
                return context.getString(R.string.letter_chorus);
            case BRIDGE:
                return context.getString(R.string.letter_bridge);
            case CODA:
                return context.getString(R.string.letter_coda);
        }
        return "";
    }

    private int getSongVerseCountBySectionType() {
        int count = 0;
        for (SongVerse verse : song.getVerses()) {
            if (verse.getSectionType() == getSectionType()) {
                ++count;
                if (verse.equals(this)) {
                    break;
                }
            }
        }
        return count;
    }

    private boolean hasOtherSameTypeInSong() {
        for (SongVerse verse : song.getVerses()) {
            if (verse.getSectionType() == getSectionType() && !verse.equals(this)) {
                return true;
            }
        }
        return false;
    }

    public String getSectionTypeStringWithCount(Context context) {
        String sectionTypeString = getSectionTypeString(context);
        if (hasOtherSameTypeInSong()) {
            sectionTypeString += getSongVerseCountBySectionType();
        }
        return sectionTypeString;
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
        return text.equals(other.text);
    }
}
