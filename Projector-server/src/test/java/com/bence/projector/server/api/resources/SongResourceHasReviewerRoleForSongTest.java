package com.bence.projector.server.api.resources;

import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.Role;
import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.model.User;
import org.junit.Test;

import java.util.Collections;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Permission matrix for {@link SongResource#hasReviewerRoleForSong(User, Song)}.
 */
public class SongResourceHasReviewerRoleForSongTest {

    @Test
    public void admin_hasPermission_forAnySong() {
        User admin = user("admin@test.com", Role.ROLE_ADMIN, true);
        Song song = song("other@test.com", language(1L), false, false);
        assertTrue(SongResource.hasReviewerRoleForSong(admin, song));
    }

    @Test
    public void reviewer_withMatchingReviewLanguage_hasPermission() {
        Language language = language(42L);
        User reviewer = user("reviewer@test.com", Role.ROLE_REVIEWER, true);
        reviewer.setReviewLanguages(Collections.singletonList(language));
        Song song = song("uploader@test.com", language, false, false);
        assertTrue(SongResource.hasReviewerRoleForSong(reviewer, song));
    }

    @Test
    public void reviewer_withoutMatchingReviewLanguage_denied() {
        User reviewer = user("reviewer@test.com", Role.ROLE_REVIEWER, true);
        reviewer.setReviewLanguages(Collections.singletonList(language(1L)));
        Song song = song("uploader@test.com", language(2L), false, false);
        assertFalse(SongResource.hasReviewerRoleForSong(reviewer, song));
    }

    @Test
    public void user_owner_activated_hasPermission() {
        String email = "owner@test.com";
        User owner = user(email, Role.ROLE_USER, true);
        Song song = song(email, language(1L), false, false);
        assertTrue(SongResource.hasReviewerRoleForSong(owner, song));
    }

    @Test
    public void user_owner_notActivated_publicSong_denied() {
        String email = "owner@test.com";
        User owner = user(email, Role.ROLE_USER, false);
        owner.setReviewLanguages(Collections.emptyList());
        Song song = publicSong(email, language(1L));
        assertFalse(SongResource.hasReviewerRoleForSong(owner, song));
    }

    @Test
    public void user_owner_notActivated_recentNonPublicSong_hasPermission() {
        String email = "owner@test.com";
        User owner = user(email, Role.ROLE_USER, false);
        Song song = song(email, language(1L), true, false);
        song.setCreatedDate(new Date());
        assertTrue(SongResource.hasReviewerRoleForSong(owner, song));
    }

    @Test
    public void plainUser_notOwner_noReviewLanguage_denied() {
        User user = user("user@test.com", Role.ROLE_USER, true);
        user.setReviewLanguages(Collections.emptyList());
        Song song = song("other@test.com", language(1L), false, false);
        assertFalse(SongResource.hasReviewerRoleForSong(user, song));
    }

    @Test
    public void nullUser_denied() {
        Song song = song("x@test.com", language(1L), false, false);
        assertFalse(SongResource.hasReviewerRoleForSong(null, song));
    }

    @Test
    public void nullSong_denied() {
        User user = user("user@test.com", Role.ROLE_USER, true);
        assertFalse(SongResource.hasReviewerRoleForSong(user, null));
    }

    private static User user(String email, Role role, boolean activated) {
        User user = new User();
        user.setEmail(email);
        user.setRole(role);
        user.setActivated(activated);
        return user;
    }

    private static Language language(long id) {
        Language language = new Language();
        language.setId(id);
        language.setUuid("lang-" + id);
        return language;
    }

    @SuppressWarnings("SameParameterValue")
    private static Song song(String createdByEmail, Language language, boolean deleted, boolean uploaded) {
        Song song = new Song();
        song.setUuid("song-uuid");
        song.setCreatedByEmail(createdByEmail);
        song.setLanguage(language);
        song.setDeleted(deleted);
        song.setUploaded(uploaded);
        song.setCreatedDate(new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(3)));
        return song;
    }

    private static Song publicSong(String createdByEmail, Language language) {
        Song song = song(createdByEmail, language, false, false);
        song.setReviewerErased(false);
        song.setHasBlockingWordIssues(false);
        song.setIsBackUp(false);
        return song;
    }
}
