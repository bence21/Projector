import {Injectable} from '@angular/core';
import {Observable} from 'rxjs/Observable';
import {ApiService} from './api.service';
import {Language} from "../models/language";

@Injectable()
export class LanguageDataService {

  constructor(private api: ApiService) {
  }

  getAll(): Observable<Language[]> {
    return this.api.getAll(Language, 'api/languages');
  }

  create(language: Language) {
    return this.api.create(Language, 'api/language', language);
  }
}
