import { Component, OnDestroy, OnInit } from '@angular/core';
import { Subscription } from "rxjs/Subscription";
import { AuthService } from "../../services/auth.service";
import { ActivatedRoute } from "@angular/router";
import { User } from "../../models/user";
import { UserDataService } from "../../services/user-data.service";
import { DomSanitizer, SafeResourceUrl, Title } from "@angular/platform-browser";
import { MatDialog } from "@angular/material";
import { Role, getAllRole } from '../../models/role';
import { Language } from '../../models/language';
import { LanguageDataService } from '../../services/language-data.service';
import { checkAuthenticationError } from '../../util/error-util';


@Component({
  selector: 'app-user',
  templateUrl: './user.component.html',
  styleUrls: ['./user.component.css']
})
export class UserComponent implements OnInit, OnDestroy {

  originalUser: User;
  user: User;
  public safeUrl: SafeResourceUrl = null;
  private sub: Subscription;
  roles: Role[] = [];
  remainedLanguages: Language[] = [];
  allLanguages: Language[] = [];

  constructor(
    private activatedRoute: ActivatedRoute,
    private userService: UserDataService,
    private titleService: Title,
    public auth: AuthService,
    public sanitizer: DomSanitizer,
    private languageService: LanguageDataService,
    private dialog: MatDialog) {
    auth.getUserFromLocalStorage();
    this.user = new User();
    this.user.email = "Loading";
    this.originalUser = new User(this.user);
    languageService.getAll().subscribe(
      (languages) => {
        this.allLanguages = languages;
        this.setOriginalUser(this.user);
      }
    );
    this.getRoles();
  }

  ngOnInit() {
    this.titleService.setTitle('User');

    this.sub = this.activatedRoute.params.subscribe(params => {
      if (params['userId']) {
        const userId = params['userId'];
        this.userService.getUser(userId).subscribe(
          (user) => {
            this.user = user;
            if (user.isAdmin()) {
              this.roles.push(Role.ROLE_ADMIN);
            }
            this.setOriginalUser(this.user);
          },
          (err) => {
            checkAuthenticationError(this.ngOnInit, this, err, this.dialog);
          });
      }
    });
  }

  private getIndexFromLanguages(languages: Language[], language: Language): number {
    for (let i = 0; i < languages.length; ++i) {
      if (language.uuid == languages[i].uuid) {
        return i;
      }
    }
    return -1;
  }

  private setOriginalUser(user: User) {
    this.originalUser = new User(user);
    this.originalUser.reviewLanguages = [];
    this.remainedLanguages = this.allLanguages;
    if (user.reviewLanguages != undefined) {
      for (let language of user.reviewLanguages) {
        this.originalUser.reviewLanguages.push(language);
        const index = this.getIndexFromLanguages(this.remainedLanguages, language);
        if (index > -1) {
          this.remainedLanguages.splice(index, 1);
        }
      }
    }
  }

  selectLanguage(language: Language) {
    this.user.reviewLanguages.push(language);
    const index = this.remainedLanguages.indexOf(language, 0);
    if (index > -1) {
      this.remainedLanguages.splice(index, 1);
    }
  }

  private getRoles() {
    let roles = getAllRole();
    const index = roles.indexOf(Role.ROLE_ADMIN, 0);
    if (index > -1) {
      roles.splice(index, 1);
    }
    this.roles = roles;
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
  }

  getRoleString(role: Role): string {
    return Role.getAsString(role);
  }

  onApplyRoleButtonClick() {
    const updateUser = new User(this.originalUser);
    updateUser.role = this.user.role;
    this.update(updateUser);
  }

  removeLanguage(language: Language) {
    const index = this.user.reviewLanguages.indexOf(language, 0);
    if (index > -1) {
      this.user.reviewLanguages.splice(index, 1);
      this.remainedLanguages.push(language);
    }
  }

  reviewLanguagesDiffers() {
    if (this.originalUser.reviewLanguages == undefined && this.user.reviewLanguages != undefined) {
      return true;
    }
    if (this.originalUser.reviewLanguages == undefined || this.user.reviewLanguages == undefined) {
      return false;
    }
    if (this.originalUser.reviewLanguages.length != this.user.reviewLanguages.length) {
      return true;
    }
    for (let i = 0; i < this.originalUser.reviewLanguages.length; ++i) {
      if (this.originalUser.reviewLanguages[i].uuid != this.user.reviewLanguages[i].uuid) {
        return true;
      }
    }
    return false;
  }

  onApplyReviewLanguagesButtonClick() {
    const updateUser = new User(this.originalUser);
    updateUser.reviewLanguages = this.user.reviewLanguages;
    this.update(updateUser);
  }

  private update(updateUser: User) {
    this.userService.updateAsAdmin(updateUser).subscribe(() => {
      this.setOriginalUser(updateUser);
    }, (err) => {
      checkAuthenticationError(this.ngOnInit, this, err, this.dialog);
    });
  }

  getLanguageString(language: Language): string {
    return language == undefined ? '' : language.printLanguage();
  }
}
