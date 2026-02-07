import { ReviewedWordStatus } from './reviewedWord';

export interface WordWithStatus {
  word: string;
  status: ReviewedWordStatus;
  suggestions?: string[];
  countInSong?: number;
  countInAllSongs?: number;
}
