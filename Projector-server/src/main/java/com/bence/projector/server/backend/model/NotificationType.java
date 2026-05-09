package com.bence.projector.server.backend.model;

public enum NotificationType {
    SONG_EMPTY,
    NEW_USER,
    /** Admin batched emails for new version-group link requests (SongLink). */
    VERSION_GROUP_LINK
}
