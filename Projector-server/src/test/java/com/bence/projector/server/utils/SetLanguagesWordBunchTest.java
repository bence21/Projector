package com.bence.projector.server.utils;

import com.bence.projector.common.model.SectionType;
import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.model.SongVerse;
import com.bence.projector.server.utils.models.NormalizedWordBunch;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

/**
 * Regression tests for {@link SetLanguages} word-bunch map lookup when song language
 * is not the same map key instance as languages from {@code findAll()}.
 */
public class SetLanguagesWordBunchTest {

    @Test
    public void getNormalizedWordBunches_resolvesLanguageByUuidWhenMapKeyDiffers() {
        String uuid = "test-lang-uuid-word-bunch";
        Language mapLanguage = new Language();
        mapLanguage.setUuid(uuid);

        Language songLanguage = new Language();
        songLanguage.setUuid(uuid);

        Song song = new Song();
        song.setLanguage(songLanguage);
        SongVerse verse = new SongVerse();
        verse.setSectionType(SectionType.CHORUS);
        verse.setText("hello world");
        song.setVerses(Collections.singletonList(verse));

        List<NormalizedWordBunch> bunches = SetLanguages.getNormalizedWordBunches(
                Collections.singletonList(song),
                Collections.singletonList(mapLanguage),
                mapLanguage
        );
        Assert.assertFalse(bunches.isEmpty());
    }
}
