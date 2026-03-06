/**
 * DTO for a single row from the paginated words spell checker API.
 * Maps to backend NormalizedWordBunchRowDTO.
 */
export class NormalizedWordBunchRowDTO {
  nr: number;
  confidencePercentage: number;
  word: string;
  count: number;
  correction: string;
  song: any;  // SongTitleDTO: { id, title, ... }
  problematic: boolean;
  allOccurrencesAutoCapitalized?: boolean;
  reviewedWord: any;  // ReviewedWordDTO
  /** True when word and bestWord had different lengths; correction shown as-is. */
  correctionLengthMismatch?: boolean;

  constructor(values: Object = {}) {
    Object.assign(this, values);
  }
}
