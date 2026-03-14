import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { ApiService } from './api.service';
import { Language } from '../models/language';
import { ReviewedWord, ReviewedWordStatus } from '../models/reviewedWord';
import { AuthService } from './auth.service';

@Injectable()
export class ReviewedWordDataService {

  constructor(
    private api: ApiService,
    private auth: AuthService
  ) {
  }

  private getRolePath(): string {
    const user = this.auth.getUser();
    if (user) {
      const rolePath = user.getRolePath();
      if (rolePath) {
        return rolePath;
      }
    }
    return 'user';
  }

  private getReviewedWordApiBasePath(): string {
    return `/${this.getRolePath()}/api/reviewedWord`;
  }

  detectSourceLanguages(word: string, language: Language): Observable<Language[]> {
    const params = `word=${encodeURIComponent(word)}&languageId=${encodeURIComponent(language.uuid)}`;
    return this.api.getAll(Language, `${this.getReviewedWordApiBasePath()}/detect-language?${params}`);
  }

  getAll(language: Language): Observable<ReviewedWord[]> {
    return this.api.getAll(ReviewedWord, `${this.getReviewedWordApiBasePath()}/${language.uuid}`);
  }

  getByStatus(language: Language, status: ReviewedWordStatus): Observable<ReviewedWord[]> {
    return this.api.getAll(ReviewedWord, `${this.getReviewedWordApiBasePath()}/${language.uuid}/status/${status}`);
  }

  createOrUpdate(language: Language, reviewedWord: ReviewedWord): Observable<ReviewedWord> {
    return this.api.create(ReviewedWord, `${this.getReviewedWordApiBasePath()}/${language.uuid}`, reviewedWord);
  }

  delete(reviewedWord: ReviewedWord): Observable<any> {
    return this.api.delete(`${this.getReviewedWordApiBasePath()}/${reviewedWord.uuid}`);
  }

  bulkUpdate(language: Language, reviewedWords: ReviewedWord[]): Observable<ReviewedWord[]> {
    return this.api.create(ReviewedWord, `${this.getReviewedWordApiBasePath()}/${language.uuid}/bulk`, reviewedWords as any);
  }
}
