import { Injectable } from '@angular/core';
import { BaseModel } from '../models/base-model';
import { Http, Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import 'rxjs/add/observable/throw';

@Injectable()
export class ApiService {

  constructor(private http: Http) {
  }

  private static handleError(error: Response | any) {
    console.error(error);
    return Observable.throw(error);
  }
  public getOne<T>(c: new (data) => T, api_url: string): Observable<T> {
    return this.http
      .get(api_url)
      .map(response => {
        return new c(response.json());
      })
      .catch(ApiService.handleError);
  }

  public getAll<T>(c: new (data) => T, api_url: string): Observable<T[]> {
    return this.http
      .get(api_url)
      .map(response => {
        const models = response.json();
        return models.map((model) => new c(model));
      })
      .catch(ApiService.handleError);
  }

  public getAllByPost<T>(c: new (data) => T, api_url: string, t: T): Observable<T[]> {
    return this.http
      .post(api_url, t)
      .map(response => {
        const models = response.json();
        return models.map((model) => new c(model));
      })
      .catch(ApiService.handleError);
  }

  public create<T>(c: new (data) => T, api_url: string, t: T): Observable<T> {
    return this.http
      .post(api_url, t)
      .map(response => {
        return new c(response.json());
      })
      .catch(ApiService.handleError);
  }

  public postWithBody<T>(c: new (data) => T, api_url: string, body: any): Observable<T> {
    return this.http
      .post(api_url, body)
      .map(response => {
        return new c(response.json());
      })
      .catch(ApiService.handleError);
  }

  public getById<T>(c: new (data) => T, api_url: string, id: number): Observable<T> {
    return this.http
      .get(api_url + id)
      .map(response => {
        const asJson = response.json();
        if (asJson == "") {
          return null;
        }
        return new c(asJson);
      })
      .catch(ApiService.handleError);
  }

  public update<T extends BaseModel>(c: new (data) => T, api_url: string, t: T): Observable<T> {
    return this.http
      .put(api_url + t.id, t)
      .map(response => {
        return new c(response.json());
      })
      .catch(ApiService.handleError);
  }

  public put<T extends BaseModel>(c: new (data) => T, api_url: string, t: T): Observable<T> {
    return this.http
      .put(api_url, t)
      .map(response => {
        return new c(response.json());
      })
      .catch(ApiService.handleError);
  }

  public deleteById(api_url: string, id): Observable<null> {
    return this.http
      .delete(api_url + id)
      .map(() => null)
      .catch(ApiService.handleError);
  }

  public delete(api_url: string): Observable<null> {
    return this.http
      .delete(api_url)
      .map(() => null)
      .catch(ApiService.handleError);
  }

  getAttribute<T>(c: new (data) => T, apiUrl: string) {
    return this.http
      .get(apiUrl)
      .map(response => {
        return new c(response.json());
      })
      .catch(ApiService.handleError);
  }

  post(api_url: string) {
    return this.http
      .post(api_url, null)
      .map(response => {
        return response;
      })
      .catch(ApiService.handleError);
  }

  getTime() {
    return this.http.get('/getTime')
      .map(response => response);
  }
}
