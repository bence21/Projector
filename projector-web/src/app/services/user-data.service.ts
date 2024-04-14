import { Injectable } from '@angular/core';
import { ApiService } from './api.service';
import { User } from '../models/user';
import { Observable } from 'rxjs/Observable';

@Injectable()
export class UserDataService {

  constructor(private api: ApiService) {
  }

  getUser(userId): Observable<User> {
    return this.api.getById(User, 'admin/api/user/', userId);
  }

  getLoggedInUser(): Observable<User> {
    return this.api.getOne(User, '/api/username');
  }

  addUser(user: User, language: string): Observable<User> {
    return this.api.create(User, 'api/users?language=' + language, user);
  }

  activate(activationCode: string) {
    return this.api.post('user/api/user/activate?activationCode=' + activationCode);
  }

  resendActivation(): Observable<any> {
    return this.api.post('user/api/sendActivation');
  }

  getAll(): Observable<User[]> {
    return this.api.getAll(User, 'admin/api/users');
  }

  updateAsAdmin(user: User) {
    user.id = user.uuid;
    return this.api.update(User, 'admin/api/user/', user);
  }

  update(user: User) {
    user.id = user.uuid;
    return this.api.update(User, 'user/api/user/', user);
  }

  requireForgottenToken(email: string) {
    return this.api.post('/forgotten_password?email=' + email)
  }

  resetPassword(token: string, user: User) {
    return this.api.put(User, '/reset_password?token=' + token, user)
      .map(response => response);
  }
}
