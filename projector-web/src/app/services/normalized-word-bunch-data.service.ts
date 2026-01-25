import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { ApiService } from './api.service';
import { Language } from '../models/language';
import { NormalizedWordBunch } from '../models/normalizedWordBunch';
import { ChangeWord } from '../models/changeWord';

export enum NormalizedWordBunchFilterType {
  BANNED = 'BANNED',
  REVIEWED_GOOD = 'REVIEWED_GOOD',
  CONTEXT_SPECIFIC = 'CONTEXT_SPECIFIC',
  ACCEPTED = 'ACCEPTED',
  REJECTED = 'REJECTED',
  UNREVIEWED = 'UNREVIEWED'
}

function getFilterPath(filterType: NormalizedWordBunchFilterType): string {
  switch (filterType) {
    case NormalizedWordBunchFilterType.BANNED:
      return 'banned';
    case NormalizedWordBunchFilterType.REVIEWED_GOOD:
      return 'reviewed-good';
    case NormalizedWordBunchFilterType.CONTEXT_SPECIFIC:
      return 'context-specific';
    case NormalizedWordBunchFilterType.ACCEPTED:
      return 'accepted';
    case NormalizedWordBunchFilterType.REJECTED:
      return 'rejected';
    case NormalizedWordBunchFilterType.UNREVIEWED:
      return 'unreviewed';
    default:
      throw new Error(`Unknown filter type: ${filterType}`);
  }
}

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

  getByFilter(language: Language, filterType: NormalizedWordBunchFilterType): Observable<NormalizedWordBunch[]> {
    const filterPath = getFilterPath(filterType);
    return this.api.getAll(NormalizedWordBunch, 'admin/api/normalizedWordBunches/' + language.uuid + '/' + filterPath);
  }
}
