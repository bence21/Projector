package com.bence.projector.common.dto;

import java.util.Date;
import java.util.List;

public class SongDTO extends BaseDTO {

    private String title;
    private Date createdDate;
    private Date modifiedDate;
    private List<SongVerseDTO> songVerseDTOS;
    private boolean deleted = false;
    private LanguageDTO languageDTO;
    private Boolean uploaded;
    private long views;
    private String createdByEmail;
    private String originalId;
    private String versionGroup;
    private String youtubeUrl;
    private Long favourites;
    private String author;
    private List<Short> verseOrderList;
    private String backUpSongId;
    private String lastModifiedByUserEmail;
    private String verseOrder;
    private Boolean reviewerErased;

    public SongDTO() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<SongVerseDTO> getSongVerseDTOS() {
        return songVerseDTOS;
    }

    public void setSongVerseDTOS(List<SongVerseDTO> songVerseDTOS) {
        this.songVerseDTOS = songVerseDTOS;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public LanguageDTO getLanguageDTO() {
        return languageDTO;
    }

    public void setLanguageDTO(LanguageDTO languageDTO) {
        this.languageDTO = languageDTO;
    }

    public Boolean getUploaded() {
        return uploaded;
    }

    public void setUploaded(Boolean uploaded) {
        this.uploaded = uploaded;
    }

    public long getViews() {
        return views;
    }

    public void setViews(long views) {
        this.views = views;
    }

    public String getCreatedByEmail() {
        return createdByEmail;
    }

    public void setCreatedByEmail(String createdByEmail) {
        this.createdByEmail = createdByEmail;
    }

    public String getOriginalId() {
        return originalId;
    }

    public void setOriginalId(String originalId) {
        this.originalId = originalId;
    }

    public String getVersionGroup() {
        return versionGroup;
    }

    public void setVersionGroup(String versionGroup) {
        this.versionGroup = versionGroup;
    }

    public String getYoutubeUrl() {
        return youtubeUrl;
    }

    public void setYoutubeUrl(String youtubeUrl) {
        this.youtubeUrl = youtubeUrl;
    }

    public long getFavourites() {
        return favourites;
    }

    public void setFavourites(long favourites) {
        this.favourites = favourites;
    }

    public String getVerseOrder() {
        if (verseOrderList == null) {
            return null;
        }
        StringBuilder verseOrder = new StringBuilder();
        boolean first = true;
        for (Short index : verseOrderList) {
            if (!first) {
                verseOrder.append(",");
            }
            verseOrder.append(index);
            first = false;
        }
        return verseOrder.toString();
    }

    public void setVerseOrder(String verseOrder) {
        this.verseOrder = verseOrder;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public List<Short> getVerseOrderList() {
        return verseOrderList;
    }

    public void setVerseOrderList(List<Short> verseOrderList) {
        this.verseOrderList = verseOrderList;
    }

    public String getBackUpSongId() {
        return backUpSongId;
    }

    public void setBackUpSongId(String backUpSongId) {
        this.backUpSongId = backUpSongId;
    }

    public String getLastModifiedByUserEmail() {
        return lastModifiedByUserEmail;
    }

    public void setLastModifiedByUserEmail(String lastModifiedByUserEmail) {
        this.lastModifiedByUserEmail = lastModifiedByUserEmail;
    }

    public Boolean getReviewerErased() {
        return reviewerErased;
    }

    public void setReviewerErased(boolean reviewerErased) {
        this.reviewerErased = reviewerErased;
    }
}
