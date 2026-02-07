export const STATUS_GOOD = 'good';
export const STATUS_UNREVIEWED = 'unreviewed';
export const STATUS_BANNED = 'banned';
export const STATUS_REJECTED = 'rejected';

export type WordStatus = typeof STATUS_GOOD | typeof STATUS_UNREVIEWED | typeof STATUS_BANNED | typeof STATUS_REJECTED;

export interface WordWithStatus {
  word: string;
  status: WordStatus;
  suggestions?: string[];
  countInSong?: number;
  countInAllSongs?: number;
}
