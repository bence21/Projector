package com.bence.songbook.models;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.bence.songbook.ui.utils.StringUtils.stripAccents;

public class SongCollection extends BaseEntity {
    @ForeignCollectionField
    private ForeignCollection<SongCollectionElement> songCollectionElementForeignCollection;
    private List<SongCollectionElement> songCollectionElements;
    @DatabaseField
    private Date createdDate;
    @DatabaseField
    private Date modifiedDate;
    @DatabaseField
    private String name;
    private String stripedName;
    private String shortName;
    private String strippedShortName;
    @DatabaseField(foreign = true, index = true)
    private Language language;
    private boolean selected;
    private String strippedName;

    public List<SongCollectionElement> getSongCollectionElements() {
        if (songCollectionElements == null) {
            if (songCollectionElementForeignCollection == null) {
                songCollectionElements = new ArrayList<>();
                return songCollectionElements;
            }
            songCollectionElements = new ArrayList<>(songCollectionElementForeignCollection.size());
            songCollectionElements.addAll(songCollectionElementForeignCollection);
        }
        return songCollectionElements;
    }

    public void setSongCollectionElements(List<SongCollectionElement> songCollectionElements) {
        this.songCollectionElements = songCollectionElements;
    }

    public Date getCreatedDate() {
        return createdDate == null ? null : (Date) createdDate.clone();
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate == null ? null : (Date) createdDate.clone();
    }

    public Date getModifiedDate() {
        return modifiedDate == null ? null : (Date) modifiedDate.clone();
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate == null ? null : (Date) modifiedDate.clone();
        if (modifiedDate == null) {
            this.modifiedDate = new Date(0);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getShortName() {
        if (shortName == null) {
            return parseToShortName();
        }
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    private String parseToShortName() {
        StringBuilder shortName = new StringBuilder();
        String[] split = name.trim().split(" ");
        if (split.length > 1) {
            for (String s : split) {
                try {
                    shortName.append((s.charAt(0) + "").toUpperCase());
                    int i = 1;
                    while (i < s.length() && Character.isUpperCase(s.charAt(i))) {
                        shortName.append(s.charAt(i));
                        ++i;
                    }
                } catch (Exception e) {
                    shortName.append(s);
                }
            }
        } else {
            return (name.trim().charAt(0) + "").toUpperCase();
        }
        return shortName.toString();
    }

    public String getStrippedShortName() {
        if (strippedShortName == null) {
            String shortName = getShortName();
            strippedShortName = stripAccents(shortName.toLowerCase());
        }
        return strippedShortName;
    }

    public String getStripedName() {
        if (stripedName == null) {
            stripedName = stripAccents(name.toLowerCase());
        }
        return stripedName;
    }

    public String getStrippedName() {
        if (strippedName == null) {
            strippedName = stripAccents(name.toLowerCase());
        }
        return strippedName;
    }
}
