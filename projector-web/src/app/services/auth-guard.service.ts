import { Injectable } from '@angular/core';
import {
  ActivatedRouteSnapshot,
  CanActivate,
  CanActivateChild,
  CanLoad,
  Route,
  Router,
  RouterStateSnapshot
} from '@angular/router';
import { AuthService } from './auth.service';
import { Role } from '../models/role';
import { User } from '../models/user';

@Injectable()
export class AuthGuard implements CanActivate, CanActivateChild, CanLoad {
  constructor(private authService: AuthService, private router: Router) {
  }

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    this.getUserFromLocalStorage();
    const url: string = state.url;
    this.authService.redirectUrl = url;
    return this.checkLogin(url);
  }

  canActivateChild(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    this.getUserFromLocalStorage();
    this.authService.redirectUrl = state.url;
    return this.canActivate(route, state);
  }

  canLoad(route: Route): boolean {
    this.getUserFromLocalStorage();
    const url = `/${route.path}`;
    return this.checkLogin(url);
  }

  checkLogin(url: string): boolean {
    if (this.authService.isLoggedIn) {
      if (url.startsWith('/admin')) {
        const user = this.authService.getUser();
        if (user != null && user.isAdmin()) {
          return true;
        }
      } else if (url.startsWith('/user')) {
        const user = this.authService.getUser();
        if (user != null && user.isUser()) {
          return true;
        }
      } else {
        return true;
      }
    }
    // Store the attempted URL for redirecting
    this.authService.redirectUrl = url;
    // noinspection JSIgnoredPromiseFromCall
    this.router.navigate(['/login']);
    return false;
  }

  getUserFromLocalStorage() {
    this.authService.setUser(new User(JSON.parse(localStorage.getItem('currentUser'))));
  }
}
