import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { ApiService } from './api.service';
import { Language } from '../models/language';
import { NormalizedWordBunch } from '../models/normalizedWordBunch';
import { ChangeWord } from '../models/changeWord';

@Injectable()
export class NormalizedWordBunchDataService {

  constructor(private api: ApiService) {
  }

  getAll(language: Language): Observable<NormalizedWordBunch[]> {
    return this.api.getAll(NormalizedWordBunch, 'admin/api/normalizedWordBunches/' + language.uuid);
  }

  changeAll(language: Language, changeWord: ChangeWord) {
    return this.api.create(ChangeWord, '/admin/api/normalizedWordBunch/changeAll/' + language.uuid, changeWord);
  }
}
