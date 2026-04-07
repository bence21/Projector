import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { ApiService } from './api.service';
import { Language } from '../models/language';
import { ChangeWord } from '../models/changeWord';
import { NormalizedWordBunchRowDTO } from '../models/normalized-word-bunch-row-dto';

@Injectable()
export class NormalizedWordBunchDataService {

  constructor(private api: ApiService) {
  }

  changeAll(language: Language, changeWord: ChangeWord) {
    return this.api.create(ChangeWord, '/admin/api/normalizedWordBunch/changeAll/' + language.uuid, changeWord);
  }

  /**
   * Fetches a single page of spell checker rows (server-side pagination).
   * filterType: 'all' | 'problematic' | 'banned' | 'reviewed-good' | etc.
   */
  getPage(
    language: Language,
    filterType: string,
    pageIndex: number,
    pageSize: number
  ): Observable<{ content: NormalizedWordBunchRowDTO[]; totalElements: number }> {
    const url = 'admin/api/normalizedWordBunches/' + language.uuid + '/page?page=' + pageIndex + '&size=' + pageSize + '&filter=' + encodeURIComponent(filterType);
    return this.api.getPage(NormalizedWordBunchRowDTO, url);
  }

  clearCache(language: Language): Observable<any> {
    return this.api.post('admin/api/normalizedWordBunches/' + language.uuid + '/clearCache');
  }
}
