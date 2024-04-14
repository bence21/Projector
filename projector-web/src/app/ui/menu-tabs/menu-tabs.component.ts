import { Component, OnInit } from '@angular/core';
import { AuthService } from "../../services/auth.service";
import { User } from '../../models/user';
import { MatIconRegistry } from '@angular/material';
import { DomSanitizer } from '@angular/platform-browser';

const VERSION_SVG = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"> <path d="M 12 3 C 10.346 3 9 4.346 9 6 L 9 18 C 9 19.654 10.346 21 12 21 L 19 21 C 20.654 21 22 19.654 22 18 L 22 6 C 22 4.346 20.654 3 19 3 L 12 3 z M 8 4 C 6.402 4 5 5.402 5 7 L 5 17 C 5 18.598 6.402 20 8 20 L 8 18 C 7.462 18 7 17.45 7 17 L 7 7 C 7 6.55 7.462 6 8 6 L 8 4 z M 12 5 L 19 5 C 19.552 5 20 5.449 20 6 L 20 18 C 20 18.552 19.552 19 19 19 L 12 19 C 11.448 19 11 18.552 11 18 L 11 6 C 11 5.449 11.448 5 12 5 z M 4 6 C 3.29 6 2.867 6.28725 2.625 6.53125 C 1.999 7.16225 1.994 8.09175 2 8.84375 L 2 9.09375 L 2 15.53125 C 1.994 16.18525 1.98825 16.92075 2.53125 17.46875 C 2.87825 17.81875 3.372 18 4 18 L 4 6 z"/> </svg>';

class MenuTab {
  link = '';
  icon = '';
  title = '';
}

const ACCOUNT = 'Account';

@Component({
  selector: 'app-menu-tabs',
  templateUrl: './menu-tabs.component.html',
  styleUrls: ['./menu-tabs.component.css']
})
export class MenuTabsComponent implements OnInit {

  menuTabs = [
    { link: '/songs', icon: 'menu', title: 'Songs' },
    { link: '/login', icon: 'person', title: 'Login' },
    { link: '/desktop-app', icon: 'devices', title: 'Desktop app' },
  ];
  adminMenuTabs = [
    { link: '/songs', icon: 'menu', title: 'Songs' },
    { link: '/addNewSong', icon: 'add_box', title: 'Add song' },
    { link: '/addNewSongCollection', icon: 'add_box', title: 'Add song collection' },
    { link: '/suggestions', icon: 'announcement', title: 'Suggestions' },
    { link: '/versionLinks', iconSvg: 'versions', title: 'Version links' },
    { link: '/admin/users', icon: 'supervised_user_circle', title: 'Users' },
    { link: '/user/notifications', icon: 'notifications', title: 'Notifications' },
    { link: '/desktop-app', icon: 'devices', title: 'Desktop app' },
    { link: '/admin/YouTube-Video-Checker', icon: 'fact_check', title: 'YouTube checker' },
  ];
  userMenuTabs = [
    { link: '/songs', icon: 'menu', title: 'Songs' },
    { link: '/addNewSong', icon: 'add_box', title: 'Add song' },
    { link: '/desktop-app', icon: 'devices', title: 'Desktop app' },
  ];
  reviewerMenuTabs = [
    { link: '/songs', icon: 'menu', title: 'Songs' },
    { link: '/addNewSong', icon: 'add_box', title: 'Add song' },
    { link: '/suggestions', icon: 'announcement', title: 'Suggestions' },
    { link: '/user/notifications', icon: 'notifications', title: 'Notifications' },
    { link: '/desktop-app', icon: 'devices', title: 'Desktop app' },
  ];

  accountName = ACCOUNT;

  constructor(
    public auth: AuthService,
    iconRegistry: MatIconRegistry,
    sanitizer: DomSanitizer,
  ) {
    iconRegistry.addSvgIconLiteral('thumbs-up', sanitizer.bypassSecurityTrustHtml(VERSION_SVG));
  }

  ngOnInit() {
    this.checkActivatedMenuTab();
    setInterval(() => this.checkActivatedMenuTab(), 2000);
  }

  checkActivatedMenuTab(): void {
    if (this.isLoggedInUser()) {
      const user: User = this.auth.getUser();
      if (!user.activated) {
        if (!this.isLastMenuTabActivate()) {
          this.addActivateMenuTab();
        }
      } else {
        this.removeActivateMenuTab();
      }
      this.accountName = user.surname + ' ' + user.firstName;
      if (this.accountName.trim() == '') {
        this.accountName = ACCOUNT;
      }
    }
  }

  isLastMenuTabActivate(): boolean {
    let lastMenuTab = this.adminMenuTabs[this.adminMenuTabs.length - 1];
    return lastMenuTab.link == '/activate';
  }

  removeActivateMenuTab() {
    if (this.isLastMenuTabActivate()) {
      this.adminMenuTabs.pop();
      this.userMenuTabs.pop();
      this.reviewerMenuTabs.pop();
    }
  }

  addActivateMenuTab() {
    let activateMenuTab = new MenuTab();
    activateMenuTab.link = '/activate';
    activateMenuTab.icon = 'how_to_reg';
    activateMenuTab.title = 'Active account';
    this.adminMenuTabs.push(activateMenuTab);
    this.userMenuTabs.push(activateMenuTab);
    this.reviewerMenuTabs.push(activateMenuTab);
  }

  isLoggedInUser() {
    const user: User = this.auth.getUser();
    return this.auth.isLoggedIn && user != undefined && user.role != undefined;
  }

  isAdmin(): Boolean {
    const user: User = this.auth.getUser();
    return this.isLoggedInUser() && user.isAdmin();
  }

  isUser(): Boolean {
    const user: User = this.auth.getUser();
    return this.isLoggedInUser() && user.isUser();
  }

  isReviewer(): Boolean {
    const user: User = this.auth.getUser();
    return this.isLoggedInUser() && user.isReviewer();
  }
}
