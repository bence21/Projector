import {Injectable} from '@angular/core';
import {Observable} from 'rxjs/Observable';
import {Statistics} from '../models/statistics';
import {ApiService} from './api.service';

@Injectable()
export class StatisticsDataService {

  constructor(private api: ApiService) {
  }

  getAll(): Observable<Statistics[]> {
    return this.api.getAll(Statistics, 'admin/api/statistics');
  }
}
