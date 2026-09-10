package com.bence.projector.server.backend.service;

import com.bence.projector.common.dto.ReviewedWordStatusDTO;
import com.bence.projector.common.dto.WordWithStatus;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SongWordQualityScorerTest {

    @Test
    public void computeTotalScore_isBetweenZeroAndOneHundred() {
        WordWithStatus a = word(ReviewedWordStatusDTO.REVIEWED_GOOD, null, 1);
        WordWithStatus b = word(ReviewedWordStatusDTO.UNREVIEWED, null, 1);
        int s = SongWordQualityScorer.computeTotalScore(Arrays.asList(a, b));
        Assert.assertTrue(s >= 0 && s <= 100);
    }

    @Test
    public void reviewedGoodTierHigherThanBibleTier() {
        Assert.assertTrue(
                SongWordQualityScorer.tierWeight(word(ReviewedWordStatusDTO.REVIEWED_GOOD, null, 1))
                        > SongWordQualityScorer.tierWeight(word(ReviewedWordStatusDTO.AUTO_ACCEPTED_FROM_BIBLE, null, 1)));
    }

    @Test
    public void bibleTierHigherThanAcceptedNonArchaic() {
        Assert.assertTrue(
                SongWordQualityScorer.tierWeight(word(ReviewedWordStatusDTO.AUTO_ACCEPTED_FROM_BIBLE, null, 1))
                        > SongWordQualityScorer.tierWeight(word(ReviewedWordStatusDTO.ACCEPTED, "Dialectal", 1)));
    }

    @Test
    public void acceptedNonArchaicHigherThanArchaic() {
        Assert.assertTrue(
                SongWordQualityScorer.tierWeight(word(ReviewedWordStatusDTO.ACCEPTED, null, 1))
                        > SongWordQualityScorer.tierWeight(word(ReviewedWordStatusDTO.ACCEPTED, "Archaic", 1)));
    }

    @Test
    public void computeTotalScore_sameMix_independentOfRepeatingSameTier() {
        WordWithStatus oneRowHeavy = word(ReviewedWordStatusDTO.REVIEWED_GOOD, null, 10);
        int heavy = SongWordQualityScorer.computeTotalScore(Collections.singletonList(oneRowHeavy));

        WordWithStatus[] ten = new WordWithStatus[10];
        Arrays.fill(ten, word(ReviewedWordStatusDTO.REVIEWED_GOOD, null, 1));
        int split = SongWordQualityScorer.computeTotalScore(Arrays.asList(ten));
        Assert.assertEquals(heavy, split);
        Assert.assertEquals(100, heavy);
    }

    @Test
    public void computeTotalScore_halfGoodHalfBanned_clampsToZero() {
        WordWithStatus good = word(ReviewedWordStatusDTO.REVIEWED_GOOD, null, 1);
        WordWithStatus bad = word(ReviewedWordStatusDTO.BANNED, null, 1);
        Assert.assertEquals(0, SongWordQualityScorer.computeTotalScore(Arrays.asList(good, bad)));
    }

    @Test
    public void summarize_matchesComputeTotalScore() {
        WordWithStatus good = word(ReviewedWordStatusDTO.REVIEWED_GOOD, null, 1);
        WordWithStatus bad = word(ReviewedWordStatusDTO.BANNED, null, 1);
        List<WordWithStatus> list = Arrays.asList(good, bad);
        SongWordQualityScorer.TierBreakdown b = SongWordQualityScorer.summarize(list);
        Assert.assertEquals(SongWordQualityScorer.computeTotalScore(list), b.overallScore0to100());
    }

    @Test
    public void tierMassPercents_sumToOneHundred() {
        WordWithStatus good = word(ReviewedWordStatusDTO.REVIEWED_GOOD, null, 3);
        WordWithStatus u = word(ReviewedWordStatusDTO.UNREVIEWED, null, 1);
        SongWordQualityScorer.TierBreakdown b = SongWordQualityScorer.summarize(Arrays.asList(good, u));
        int sum = 0;
        for (SongWordQualityScorer.Tier t : SongWordQualityScorer.Tier.values()) {
            sum += b.tierMassPercent0to100(t);
        }
        Assert.assertEquals(100, sum);
    }

    @Test
    public void isArchaicCategory_caseInsensitive() {
        Assert.assertTrue(SongWordQualityScorer.isArchaicCategory("ARCHAIC"));
        Assert.assertFalse(SongWordQualityScorer.isArchaicCategory("Dialectal"));
    }

    private static WordWithStatus word(ReviewedWordStatusDTO status, String category, int countInSong) {
        return new WordWithStatus("w", status, null, countInSong, 0, category, null, null, null, null, null);
    }
}
