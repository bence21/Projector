import { ReviewedWordStatus } from './reviewedWord';
import { Language } from './language';

export interface WordWithStatus {
  word: string;
  status: ReviewedWordStatus;
  suggestions?: string[];
  countInSong?: number;
  countInAllSongs?: number;
  category?: string;
  notes?: string;
  contextCategory?: string;
  contextDescription?: string;
  sourceLanguage?: Language;
  foreignLanguageType?: 'BORROWED' | 'FOREIGN' | number; /* number = ordinal: 0 BORROWED, 1 FOREIGN */
  /** True when treated as reviewed only via the capitalized-word rule (lowercase form already reviewed). */
  inheritedFromCapitalizedReview?: boolean;
  /** True when every occurrence of this word in the song is in an auto-capitalized position (first in sentence or first in line). */
  allOccurrencesAutoCapitalized?: boolean;
}

export class WordWithStatus {
  /**
   * True when the word is shown in auto-capped form (inherited from capitalized review or all occurrences auto-capitalized).
   */
  public static isAutoCapped(wordWithStatus: WordWithStatus): boolean {
    return wordWithStatus.inheritedFromCapitalizedReview === true ||
      wordWithStatus.allOccurrencesAutoCapitalized === true;
  }
}
