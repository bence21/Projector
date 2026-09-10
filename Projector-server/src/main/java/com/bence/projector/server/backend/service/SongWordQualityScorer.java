package com.bence.projector.server.backend.service;

import com.bence.projector.common.dto.ReviewedWordStatusDTO;
import com.bence.projector.common.dto.WordWithStatus;

import java.util.Arrays;
import java.util.List;

/**
 * Word-quality score in {@code [0, 100]}: occurrence-weighted average of per-tier weights, then clamped.
 * Longer songs do not inflate the score — only the mix of word statuses matters.
 * Severe negative weights (e.g. banned/rejected) drag the average down before clamping.
 */
public final class SongWordQualityScorer {

    /**
     * Nine buckets; each has an intrinsic weight used in the blend (most are {@code 0}–{@code 100};
     * {@link #REJECTED_BANNED} may be strongly negative).
     */
    public enum Tier {
        REVIEWED_GOOD(100), CONTEXT_SPECIFIC(95), AUTO_PUBLIC(90), AUTO_BIBLE(89), ACCEPTED(75), NOT_SURE(15), UNREVIEWED(10), ACCEPTED_ARCHAIC(5), REJECTED_BANNED(-1000);

        private final int qualityWeight;

        Tier(int qualityWeight) {
            this.qualityWeight = qualityWeight;
        }

        /**
         * Weight for words in this tier (typically {@code 0}–{@code 100}; may be negative for penalties).
         */
        public int qualityWeight() {
            return qualityWeight;
        }
    }

    /**
     * Per-tier occurrence-weight mass and derived metrics. Immutable after build.
     */
    public static final class TierBreakdown {
        private final long[] massByTier;

        private TierBreakdown(long[] massByTier) {
            this.massByTier = Arrays.copyOf(massByTier, massByTier.length);
        }

        public static TierBreakdown fromWords(List<WordWithStatus> words) {
            long[] mass = new long[Tier.values().length];
            if (words != null) {
                for (WordWithStatus w : words) {
                    Tier tier = classify(w);
                    int ow = occurrenceWeight(w.getCountInSong());
                    mass[tier.ordinal()] += ow;
                }
            }
            return new TierBreakdown(mass);
        }

        public long mass(Tier tier) {
            return massByTier[tier.ordinal()];
        }

        public long totalMass() {
            long sum = 0;
            for (long m : massByTier) {
                sum += m;
            }
            return sum;
        }

        /**
         * This tier's share of total word-mass, {@code 0}–{@code 100} (percent points).
         * All tiers' shares sum to {@code 100} when non-empty (apart from rounding).
         */
        public int tierMassPercent0to100(Tier tier) {
            long total = totalMass();
            if (total <= 0) {
                return 0;
            }
            return (int) Math.round(100.0 * mass(tier) / (double) total);
        }

        /**
         * Blended song score: {@code sum_t mass(t) * weight(t) / sum mass}, rounded and clamped to {@code [0, 100]}.
         *
         * @return integer in {@code [0, 100]}; {@code 0} if there is no mass
         */
        public int overallScore0to100() {
            long total = totalMass();
            if (total <= 0) {
                return 0;
            }
            long weightedSum = 0;
            for (Tier t : Tier.values()) {
                weightedSum += mass(t) * (long) t.qualityWeight();
            }
            int mean = (int) Math.round((double) weightedSum / (double) total);
            return Math.max(0, Math.min(100, mean));
        }
    }

    private SongWordQualityScorer() {
    }

    public static TierBreakdown summarize(List<WordWithStatus> words) {
        return TierBreakdown.fromWords(words);
    }

    /**
     * @return same as {@link TierBreakdown#overallScore0to100()} for the given words
     */
    public static int computeTotalScore(List<WordWithStatus> words) {
        if (words == null || words.isEmpty()) {
            return 0;
        }
        return TierBreakdown.fromWords(words).overallScore0to100();
    }

    public static Tier classify(WordWithStatus w) {
        ReviewedWordStatusDTO status = w != null ? w.getStatus() : null;
        if (status == null) {
            return Tier.UNREVIEWED;
        }
        return switch (status) {
            case REVIEWED_GOOD -> Tier.REVIEWED_GOOD;
            case CONTEXT_SPECIFIC -> Tier.CONTEXT_SPECIFIC;
            case AUTO_ACCEPTED_FROM_BIBLE -> Tier.AUTO_BIBLE;
            case ACCEPTED -> isArchaicCategory(w.getCategory()) ? Tier.ACCEPTED_ARCHAIC : Tier.ACCEPTED;
            case AUTO_ACCEPTED_FROM_PUBLIC -> Tier.AUTO_PUBLIC;
            case NOT_SURE -> Tier.NOT_SURE;
            case UNREVIEWED -> Tier.UNREVIEWED;
            default -> Tier.REJECTED_BANNED;
        };
    }

    /**
     * Package-visible for tests: intrinsic tier weight for this word's bucket.
     */
    static int tierWeight(WordWithStatus w) {
        return classify(w).qualityWeight();
    }

    private static int occurrenceWeight(Integer countInSong) {
        if (countInSong == null || countInSong < 1) {
            return 1;
        }
        return countInSong;
    }

    static boolean isArchaicCategory(String category) {
        if (category == null) {
            return false;
        }
        return "Archaic".equalsIgnoreCase(category.trim());
    }
}
