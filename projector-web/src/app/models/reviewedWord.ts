import { BaseModel } from "./base-model";
import { Language } from "./language";

export enum ReviewedWordStatus {
  UNREVIEWED = 'UNREVIEWED',
  REVIEWED_GOOD = 'REVIEWED_GOOD',
  CONTEXT_SPECIFIC = 'CONTEXT_SPECIFIC',
  ACCEPTED = 'ACCEPTED',
  REJECTED = 'REJECTED',
  BANNED = 'BANNED',
  AUTO_ACCEPTED_FROM_PUBLIC = 'AUTO_ACCEPTED_FROM_PUBLIC'
}

export class ReviewedWord extends BaseModel {
  word = '';
  normalizedWord = '';
  language: Language;
  status: ReviewedWordStatus;
  category: string;
  contextCategory: string;
  contextDescription: string;
  reviewedByEmail: string;
  reviewedByName: string;
  reviewedDate: Date;
  notes: string;

  constructor(values: Object = {}) {
    super(values);
    Object.assign(this, values);
    if (this.language) {
      this.language = new Language(this.language);
    }
    if (this.reviewedDate) {
      this.reviewedDate = new Date(this.reviewedDate);
    }
  }
}
