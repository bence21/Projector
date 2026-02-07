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
}
