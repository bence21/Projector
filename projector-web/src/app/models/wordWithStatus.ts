import { ReviewedWordStatus } from './reviewedWord';

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
}
