import { async, TestBed } from '@angular/core/testing';

import { AppComponent } from './app.component';
import { CdkTableModule } from '@angular/cdk/table';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';
import { MatCardModule, MatAutocompleteModule, MatNativeDateModule, MatToolbarModule, MatButtonModule, MatSlideToggleModule, MatListModule, MatTableModule } from '@angular/material';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { PlunkerMaterialModule } from './app.module';
import { AppRoutingModule } from './modules/app-routing.module';
import { ApiService } from './services/api.service';
import { AuthGuard } from './services/auth-guard.service';
import { AuthService } from './services/auth.service';
import { UserDataService } from './services/user-data.service';
import { LoginComponent } from './ui/login/login.component';
import { SongComponent } from './ui/song/song.component';
import { SuggestionComponent } from './ui/suggestion/suggestion.component';
import { UsersComponent } from './ui/users/users.component';
import { UserComponent } from './ui/user/user.component';
import { NewSongComponent } from './ui/new-song/new-song.component';
import { NewSongCollectionComponent } from './ui/new-song-collection/new-song-collection.component';
import { StatisticsListComponent } from './ui/statistics-list/statistics-list.component';
import { ReviewerStatisticsComponent } from './ui/reviewer-statistics/reviewer-statistics.component';
import { SuggestionListComponent } from './ui/suggestion-list/suggestion-list.component';
import { VersionLinkListComponent } from './ui/version-link-list/version-link-list.component';
import { VersionLinkComponent } from './ui/version-link/version-link.component';
import { SongListComponent } from './ui/song-list/song-list.component';
import { UserRegisterComponent } from './ui/user-register/user-register.component';
import { ActivationComponent } from './ui/activation/activation.component';
import { ActivateComponent } from './ui/activate/activate.component';
import { NotificationSettingsComponent } from './ui/notification-settings/notification-settings.component';
import { ProjectorComponent } from './ui/projector/projector.component';
import { YoutubeVideoCheckerComponent } from './ui/youtube-video-checker/youtube-video-checker.component';
import { ForgottenPasswordComponent } from './ui/forgotten-password/forgotten-password.component';
import { ChangePasswordByTokenComponent } from './ui/forgotten-password/change-password-by-token/change-password-by-token.component';
import { AccountComponent } from './ui/account/account.component';
import { NotFoundComponent } from './ui/not-found/not-found.component';
import { MenuTabsComponent } from './ui/menu-tabs/menu-tabs.component';
import { DownloadAppComponent } from './ui/download-app/download-app.component';
import { CompareSongsComponent } from './ui/compare-songs/compare-songs.component';
import { EditSongComponent } from './ui/edit-song/edit-song.component';
import { YoutubeIdCheckComponent } from './ui/youtube-id-check/youtube-id-check.component';
import { EditTitleComponent } from './ui/edit-title/edit-title.component';
import { ReviewerStatisticsListComponent } from './ui/reviewer-statistics-list/reviewer-statistics-list.component';
import { WordsSpellCheckerComponent } from './ui/words-spell-checker/words-spell-checker.component';

describe('AppComponent', () => {
  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        AppComponent,
        LoginComponent,
        SongComponent,
        SuggestionComponent,
        UsersComponent,
        UserComponent,
        NewSongComponent,
        NewSongCollectionComponent,
        StatisticsListComponent,
        ReviewerStatisticsComponent,
        SuggestionListComponent,
        VersionLinkListComponent,
        VersionLinkComponent,
        SongListComponent,
        UserRegisterComponent,
        ActivationComponent,
        ActivateComponent,
        NotificationSettingsComponent,
        ProjectorComponent,
        YoutubeVideoCheckerComponent,
        ForgottenPasswordComponent,
        ChangePasswordByTokenComponent,
        AccountComponent,
        NotFoundComponent,
        MenuTabsComponent,
        DownloadAppComponent,
        EditSongComponent,
        // NewLanguageComponent,
        CompareSongsComponent,
        // ShareComponent,
        // AuthenticateComponent,
        // OpenInAppComponent,
        // AddToCollectionComponent,
        ReviewerStatisticsListComponent,
        // SongCollectionElementComponent,
        EditTitleComponent,
        YoutubeIdCheckComponent,
        WordsSpellCheckerComponent,
      ],
      imports: [
        BrowserModule,
        BrowserAnimationsModule,
        MatCardModule,
        HttpModule,
        HttpClientModule,
        MatAutocompleteModule,
        PlunkerMaterialModule,
        FormsModule,
        ReactiveFormsModule,
        AppRoutingModule,
        MatNativeDateModule,

        MatToolbarModule,
        MatButtonModule,
        MatSlideToggleModule,
        MatListModule,
        MatTableModule,
        CdkTableModule,
      ],
      providers: [
        AuthService,
        AuthGuard,
        UserDataService,
        ApiService,
      ],
    }).compileComponents();
  }));

  it('should create the app', async(() => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.debugElement.componentInstance;
    expect(app).toBeTruthy();
  }));

  it(`should have as title 'app'`, async(() => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.debugElement.componentInstance;
    expect(app.title).toEqual('app');
  }));

  it('should render title in a h1 tag', async(() => {
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    const compiled = fixture.debugElement.nativeElement;
    expect(compiled.querySelector('h1').textContent).toContain('Welcome to app!');
  }));
});
