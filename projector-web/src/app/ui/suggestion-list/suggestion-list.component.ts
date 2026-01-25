import { Component, OnInit } from '@angular/core';
import { BehaviorSubject } from 'rxjs/BehaviorSubject';
import { Observable } from 'rxjs/Observable';
import { DataSource } from '@angular/cdk/table';
import { Router } from '@angular/router';
import { SuggestionDataService } from '../../services/suggestion-data.service';
import { Suggestion } from '../../models/suggestion';
import { MatDialog } from "@angular/material";
import { AuthService } from "../../services/auth.service";
import { Title } from "@angular/platform-browser";
import { Language } from '../../models/language';
import { LanguageDataService } from '../../services/language-data.service';
import { SELECTED_LANGUGAGE } from '../../util/constants';
import { SongListComponent } from '../song-list/song-list.component';
import { User } from '../../models/user';
import { checkAuthenticationError } from '../../util/error-util';

export class SuggestionDatabase {
  dataChange: BehaviorSubject<Suggestion[]> = new BehaviorSubject<Suggestion[]>([]);

  constructor(suggestionList: Suggestion[]) {
    if (suggestionList !== null) {
      const copiedData = this.data;
      let nr = 0;
      for (const suggestion of suggestionList) {
        suggestion.nr = ++nr;
        copiedData.push(suggestion);
        this.dataChange.next(copiedData);
      }

      this.dataChange.next(copiedData);
    }
  }

  get data(): Suggestion[] {
    return this.dataChange.value;
  }
}

export class SuggestionDataSource extends DataSource<any> {
  constructor(private _suggestionDatabase: SuggestionDatabase) {
    super();
  }

  connect(): Observable<Suggestion[]> {
    return this._suggestionDatabase.dataChange;
  }

  disconnect() {
  }
}

@Component({
  selector: 'app-suggestion-list',
  templateUrl: './suggestion-list.component.html',
  styleUrls: ['./suggestion-list.component.css']
})
export class SuggestionListComponent implements OnInit {

  suggestionList: Suggestion[] = [];
  displayedColumns = ['Nr', 'createdDate', 'title', 'description', 'email', 'youtubeUrl'];
  suggestionDatabase: any;
  dataSource: SuggestionDataSource | null;
  languages: Language[];
  selectedLanguage: Language;

  constructor(public router: Router,
    private suggestionDataService: SuggestionDataService,
    private titleService: Title,
    private auth: AuthService,
    private languageDataService: LanguageDataService,
    private dialog: MatDialog) {
    this.languages = [];
  }

  ngOnInit() {
    this.titleService.setTitle('Suggestions');
    this.selectedLanguage = SongListComponent.getSelectedLanguageFromLocalStorage([]);
    this.languageDataService.getAll().subscribe(
      (languages) => {
        const user: User = this.auth.getUser();
        this.languages = [];
        const selectedLanguageFromLocalStorage = SongListComponent.getSelectedLanguageFromLocalStorage(languages);
        let was = false;
        for (let language of languages) {
          if (user.hasReviewerRoleForLanguage(language)) {
            this.languages.push(language);
            if (language.uuid == selectedLanguageFromLocalStorage.uuid) {
              this.selectedLanguage = language;
              was = true;
            }
          }
        }
        if (!was && this.languages.length > 0) {
          this.selectedLanguage = this.languages[0];
        }
        this.loadSuggestions();
      });
  }

  private loadSuggestions() {
    const role = this.auth.getUser().getRolePath();
    this.suggestionDataService.getAllInReviewByLanguage(role, this.selectedLanguage).subscribe(
      (suggestionList) => {
        this.setSuggestionList(suggestionList);
      },
      (err) => {
        checkAuthenticationError(this.loadSuggestions, this, err, this.dialog);
      }
    );
  }

  private compare(a, b) {
    if (a < b) {
      return -1;
    }
    if (a > b) {
      return 1;
    }
    return 0;
  }

  private setSuggestionList(suggestionList: Suggestion[]) {
    this.suggestionList = [];
    for (let suggestion of suggestionList.reverse()) {
      if (!suggestion.reviewed) {
        this.suggestionList.push(suggestion);
      }
    }
    this.suggestionList.sort((suggestion1, suggestion2) => {
      return this.compare(suggestion2.modifiedDate, suggestion1.modifiedDate);
    });
    this.suggestionDatabase = new SuggestionDatabase(this.suggestionList);
    this.dataSource = new SuggestionDataSource(this.suggestionDatabase);
  }

  loadAllSuggestions() {
    this.suggestionDataService.getAll().subscribe(
      (suggestionList) => {
        this.setSuggestionList(suggestionList);
      },
      (err) => {
        checkAuthenticationError(this.loadAllSuggestions, this, err, this.dialog);
      }
    );
  }

  onClick(row) {
    const suggestion = this.suggestionList[row.nr - 1];
    // noinspection JSIgnoredPromiseFromCall
    this.router.navigate(['/suggestion/', suggestion.uuid]);
  }

  openAuthenticateDialogOpened = false;

  onLanguageChange(language: Language) {
    this.selectedLanguage = language;
    this.changeLanguage();
  }

  changeLanguage() {
    localStorage.setItem(SELECTED_LANGUGAGE, JSON.stringify(this.selectedLanguage));
    this.loadSuggestions();
  }
}
