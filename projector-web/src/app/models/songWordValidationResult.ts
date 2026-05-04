import { RejectedWordSuggestion } from './rejectedWordSuggestion';
import { WordWithStatus } from './wordWithStatus';

export class SongWordValidationResult {
  unreviewedWords: string[];
  bannedWords: string[];
  rejectedWords: RejectedWordSuggestion[];
  hasIssues: boolean;
  wordsWithStatus?: WordWithStatus[];
  hasMixedLanguageWarning?: boolean;
  foreignWordCount?: number;
  totalReviewedWordCount?: number;
  foreignWordRatio?: number;
  foreignLanguages?: string[];

  constructor(values: Object = {}) {
    Object.assign(this, values);
    if (this.rejectedWords) {
      this.rejectedWords = this.rejectedWords.map(rw => new RejectedWordSuggestion(rw));
    }
  }
}
