import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { ApiService } from './api.service';
import { Language } from '../models/language';
import { ReviewedWord, ReviewedWordStatus } from '../models/reviewedWord';

@Injectable()
export class ReviewedWordDataService {

  constructor(private api: ApiService) {
  }

  detectSourceLanguages(word: string, language: Language): Observable<Language[]> {
    const params = `word=${encodeURIComponent(word)}&languageId=${encodeURIComponent(language.uuid)}`;
    return this.api.getAll(Language, `admin/api/reviewedWord/detect-language?${params}`);
  }

  getAll(language: Language): Observable<ReviewedWord[]> {
    return this.api.getAll(ReviewedWord, 'admin/api/reviewedWord/' + language.uuid);
  }

  getByStatus(language: Language, status: ReviewedWordStatus): Observable<ReviewedWord[]> {
    return this.api.getAll(ReviewedWord, 'admin/api/reviewedWord/' + language.uuid + '/status/' + status);
  }

  createOrUpdate(language: Language, reviewedWord: ReviewedWord): Observable<ReviewedWord> {
    return this.api.create(ReviewedWord, '/admin/api/reviewedWord/' + language.uuid, reviewedWord);
  }

  delete(reviewedWord: ReviewedWord): Observable<any> {
    return this.api.delete('/admin/api/reviewedWord/' + reviewedWord.uuid);
  }

  bulkUpdate(language: Language, reviewedWords: ReviewedWord[]): Observable<ReviewedWord[]> {
    return this.api.create(ReviewedWord, '/admin/api/reviewedWord/' + language.uuid + '/bulk', reviewedWords as any);
  }
}
