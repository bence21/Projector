import {Injectable} from '@angular/core';
import {Observable} from 'rxjs/Observable';
import {ApiService} from './api.service';
import { UserProperties } from '../models/userProperties';

@Injectable()
export class UserPropertiesDataService {

  constructor(private api: ApiService) {
  }

  get(): Observable<UserProperties> {
    return this.api.getOne(UserProperties, 'user/api/userProperties');
  }

  save(userProperties: UserProperties): Observable<UserProperties> {
    return this.api.put(UserProperties, 'user/api/userProperties', userProperties);
  }
}
