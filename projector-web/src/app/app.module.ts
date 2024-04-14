import { BrowserModule, Title } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import {
  MatAutocompleteModule,
  MatButtonModule,
  MatButtonToggleModule,
  MatCardModule,
  MatCheckboxModule,
  MatChipsModule,
  MatDatepickerModule,
  MatDialogModule,
  MatExpansionModule,
  MatGridListModule,
  MatIconModule,
  MatInputModule,
  MatListModule,
  MatMenuModule,
  MatNativeDateModule,
  MatPaginatorModule,
  MatProgressBarModule,
  MatProgressSpinnerModule,
  MatRadioModule,
  MatRippleModule,
  MatSelectModule,
  MatSidenavModule,
  MatSliderModule,
  MatSlideToggleModule,
  MatSnackBarModule,
  MatSortModule,
  MatStepperModule,
  MatTableModule,
  MatTabsModule,
  MatToolbarModule,
  MatTooltipModule
} from '@angular/material';
import { A11yModule } from '@angular/cdk/a11y';
import { DragDropModule } from '@angular/cdk/drag-drop';
import { PortalModule } from '@angular/cdk/portal';
import { ScrollingModule } from '@angular/cdk/scrolling';
import { CdkStepperModule } from '@angular/cdk/stepper';
import { CdkTreeModule } from '@angular/cdk/tree';
import { MatBadgeModule } from '@angular/material/badge';
import { MatBottomSheetModule } from '@angular/material/bottom-sheet';
import { MatDividerModule } from '@angular/material/divider';
import { MatTreeModule } from '@angular/material/tree';

import { AppComponent } from './app.component';
import { SongListComponent } from './ui/song-list/song-list.component';
import { SongService } from './services/song-service.service';
import { ApiService } from './services/api.service';
import { HttpModule } from '@angular/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { CdkTableModule } from '@angular/cdk/table';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MenuTabsComponent } from './ui/menu-tabs/menu-tabs.component';
import { NotFoundComponent } from './ui/not-found/not-found.component';
import { AppRoutingModule } from './modules/app-routing.module';
import { NewSongComponent } from './ui/new-song/new-song.component';
import { UserDataService } from './services/user-data.service';
import { LoginComponent } from './ui/login/login.component';
import { AuthService } from './services/auth.service';
import { AuthGuard } from './services/auth-guard.service';
import { StatisticsListComponent } from './ui/statistics-list/statistics-list.component';
import { StatisticsDataService } from './services/statistics-data.service';
import { SongComponent } from './ui/song/song.component';
import { EditSongComponent } from './ui/edit-song/edit-song.component';
import { LanguageDataService } from "./services/language-data.service";
import { NewLanguageComponent } from "./ui/new-language/new-language.component";
import { CompareSongsComponent } from './ui/compare-songs/compare-songs.component';
import { SuggestionComponent } from './ui/suggestion/suggestion.component';
import { SuggestionDataService } from "./services/suggestion-data.service";
import { SuggestionListComponent } from "./ui/suggestion-list/suggestion-list.component";
import { DownloadAppComponent } from './ui/download-app/download-app.component';
import { ShareComponent } from './ui/share/share.component';
import { HttpClientModule } from "@angular/common/http";
import { AuthenticateComponent } from "./ui/authenticate/authenticate.component";
import { OpenInAppComponent } from './ui/open-in-app/open-in-app.component';
import { AddToCollectionComponent } from './ui/add-to-collection/add-to-collection.component';
import { SongCollectionDataService } from "./services/song-collection-data.service";
import { UserRegisterComponent } from './ui/user-register/user-register.component';
import { ActivationComponent } from './ui/activation/activation.component';
import { UsersComponent } from './ui/users/users.component';
import { UserComponent } from './ui/user/user.component';
import { NotificationSettingsComponent } from './ui/notification-settings/notification-settings.component';
import { UserPropertiesDataService } from './services/user-properties-data.service';
import { ProjectorComponent } from './ui/projector/projector.component';
import { ForgottenPasswordComponent } from './ui/forgotten-password/forgotten-password.component';
import { ChangePasswordByTokenComponent } from './ui/forgotten-password/change-password-by-token/change-password-by-token.component';
import { ReviewerStatisticsComponent } from './ui/reviewer-statistics/reviewer-statistics.component';
import { ReviewerStatisticsListComponent } from './ui/reviewer-statistics-list/reviewer-statistics-list.component';
import { VersionLinkListComponent } from './ui/version-link-list/version-link-list.component';
import { SongLinkDataService } from './services/song-link-data.service';
import { VersionLinkComponent } from './ui/version-link/version-link.component';
import { SongCollectionElementComponent } from './ui/song-collection-element/song.collection.element';
import { ActivateComponent } from './ui/activate/activate.component';
import { NewSongCollectionComponent } from './ui/new-song-collection/new-song-collection.component';
import { EditTitleComponent } from './ui/edit-title/edit-title.component';
import { AccountComponent } from './ui/account/account.component';
import { YoutubeVideoCheckerComponent } from './ui/youtube-video-checker/youtube-video-checker.component';
import { YoutubeIdCheckComponent } from './ui/youtube-id-check/youtube-id-check.component';
import { SongGuidelinesComponent } from './ui/song-guidelines/song-guidelines.component';
import { SongGuidelinesCheckerComponent } from './ui/song-guidelines-checker/song-guidelines-checker.component';
import { GuidelineDataService } from './services/guidelines-data.service';
import { SongGuidelineCheckerComponent } from './ui/song-guideline-checker/song-guideline-checker.component';

@NgModule({
  exports: [
    CdkTableModule,
    MatAutocompleteModule,
    MatButtonModule,
    MatButtonToggleModule,
    MatCardModule,
    MatCheckboxModule,
    MatChipsModule,
    MatStepperModule,
    MatDatepickerModule,
    MatDialogModule,
    MatExpansionModule,
    MatGridListModule,
    MatIconModule,
    MatInputModule,
    MatListModule,
    MatMenuModule,
    MatNativeDateModule,
    MatPaginatorModule,
    MatProgressBarModule,
    MatProgressSpinnerModule,
    MatRadioModule,
    MatRippleModule,
    MatSelectModule,
    MatSidenavModule,
    MatSliderModule,
    MatSlideToggleModule,
    MatSnackBarModule,
    MatSortModule,
    MatTableModule,
    MatTabsModule,
    MatToolbarModule,
    MatTooltipModule,
    A11yModule,
    CdkStepperModule,
    CdkTreeModule,
    DragDropModule,
    MatBadgeModule,
    MatBottomSheetModule,
    MatDividerModule,
    MatTreeModule,
    PortalModule,
    ScrollingModule,
  ]
})
export class PlunkerMaterialModule {
}

@NgModule({
  declarations: [
    AppComponent,
    SongListComponent,
    MenuTabsComponent,
    NotFoundComponent,
    NewSongComponent,
    LoginComponent,
    StatisticsListComponent,
    SongComponent,
    EditSongComponent,
    NewLanguageComponent,
    CompareSongsComponent,
    SuggestionComponent,
    SuggestionListComponent,
    DownloadAppComponent,
    ShareComponent,
    AuthenticateComponent,
    OpenInAppComponent,
    AddToCollectionComponent,
    UserRegisterComponent,
    ActivationComponent,
    UsersComponent,
    UserComponent,
    NotificationSettingsComponent,
    ProjectorComponent,
    ForgottenPasswordComponent,
    ChangePasswordByTokenComponent,
    ReviewerStatisticsComponent,
    ReviewerStatisticsListComponent,
    VersionLinkListComponent,
    VersionLinkComponent,
    SongCollectionElementComponent,
    ActivateComponent,
    NewSongCollectionComponent,
    EditTitleComponent,
    AccountComponent,
    YoutubeVideoCheckerComponent,
    YoutubeIdCheckComponent,
    SongGuidelinesComponent,
    SongGuidelinesCheckerComponent,
    SongGuidelineCheckerComponent,
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
  ],
  entryComponents: [
    NewLanguageComponent,
    ShareComponent,
    AuthenticateComponent,
    OpenInAppComponent,
    AddToCollectionComponent,
    SongCollectionElementComponent,
  ],
  providers: [
    ApiService,
    AuthService,
    AuthGuard,
    SongService,
    UserDataService,
    StatisticsDataService,
    SuggestionDataService,
    LanguageDataService,
    SongCollectionDataService,
    UserPropertiesDataService,
    Title,
    SongLinkDataService,
    GuidelineDataService,
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}
