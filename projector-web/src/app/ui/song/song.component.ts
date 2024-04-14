import { Component, Input, OnDestroy, OnInit, ElementRef, ViewChild } from '@angular/core';
import { ActivatedRoute, NavigationStart, Router } from '@angular/router';
import { Song, SongService } from '../../services/song-service.service';
import { Subscription } from 'rxjs/Subscription';
import { AuthService } from '../../services/auth.service';
import { DomSanitizer, Meta, SafeResourceUrl, Title } from "@angular/platform-browser";
import { MatDialog, MatSnackBar } from "@angular/material";
import { ShareComponent } from "../share/share.component";
import { OpenInAppComponent } from "../open-in-app/open-in-app.component";
import { AddToCollectionComponent } from "../add-to-collection/add-to-collection.component";
import { MobileOsTypeEnum } from "../../util/enums";
import { SongCollection, SongCollectionElement } from '../../models/songCollection';
import { SongCollectionDataService } from '../../services/song-collection-data.service';
import { SuggestionDataService } from '../../services/suggestion-data.service';
import { Suggestion } from '../../models/suggestion';
import { SongCollectionElementComponent } from '../song-collection-element/song.collection.element';
import { checkAuthenticationError, ErrorUtil, generalError, openAuthenticateDialog } from '../../util/error-util';
import { YoutubeIdCheckResult, YoutubeIdCheckResultType } from '../youtube-id-check/youtube-id-check.component';

@Component({
  selector: 'app-song',
  templateUrl: './song.component.html',
  styleUrls: ['./song.component.css']
})
export class SongComponent implements OnInit, OnDestroy {
  song: Song;
  originalSong: Song;
  editing = false;
  showSimilarities = false;
  similar: Song[];
  secondSong: Song;
  receivedSimilar = false;
  markText = "Mark for version group";
  marked = false;
  markedForVersionSong: Song;
  songsByVersionGroup: Song[] = [];
  public safeUrl: SafeResourceUrl = null;
  public youtubeId: string = null;
  public idCheckOk = true;
  public youtubeIdChecked = false;
  public isAndroid = false;
  private sub: Subscription;
  public isIos = false;
  collections: SongCollection[] = [];
  unreviewedSuggestions: Suggestion[] = [];
  private markedVersionGroup_key = "markedForVersionSong";
  @ViewChild("versions", { static: false }) versionsElement: ElementRef;
  hasReviewerRoleForSong: boolean;
  routerSubscription: Subscription;

  constructor(private activatedRoute: ActivatedRoute,
    private songService: SongService,
    private dialog: MatDialog,
    public auth: AuthService,
    private titleService: Title,
    private snackBar: MatSnackBar,
    private router: Router,
    private songCollectionService: SongCollectionDataService,
    private suggestionDataService: SuggestionDataService,
    private meta: Meta,
    public sanitizer: DomSanitizer) {
    auth.getUserFromLocalStorage();
    setInterval(() => this.refreshMergeSong(), 2000);
    this.refreshMergeSong();
    this.routerSubscription = this.router.events.subscribe((event) => {
      if (event instanceof NavigationStart) {
        this.onNavigationStart();
      }
    });
  }

  @Input()
  set i_song(pSong: Song) {
    const song = new Song(pSong);
    for (const songVerse of song.songVerseDTOS) {
      songVerse.lines = [];
      for (const s of songVerse.text.split('\n')) {
        songVerse.lines.push(s);
      }
    }
    this.originalSong = new Song(song);
    if (song.originalId !== undefined) {
      this.songService.getSong(song.originalId).subscribe((song) => {
        this.originalSong = song;
      });
    }
    this.song = song;
    this.loadCollections(song.uuid);
    this.calculateUrlId(song.youtubeUrl);
    this.titleService.setTitle(this.song.title);
    this.songsByVersionGroup = [];
    this.loadSongDetails();
    this.loadVersionGroup();
    this.loadSuggestions();
    if (!song.deleted) {
      history.replaceState('data to be passed', this.song.title, window.location.href.replace('/#/song/', '/song/'));
    }
    this.refreshMergeSong();
  }

  loadSongDetails() {
    this.checkReviewerRoleForSong();
  }

  // noinspection JSMethodCanBeStatic
  public openInNewTab(song: Song) {
    window.open('/#/song/' + song.uuid);
  }

  ngOnInit() {
    this.secondSong = null;
    if (this.song === undefined) {
      this.song = new Song();
      this.song.title = 'Loading';
      this.song.songVerseDTOS = [];
    }
    this.sub = this.activatedRoute.params.subscribe(params => {
      const songId = params['songId'];
      if (songId) {
        this.songService.getSong(songId).subscribe((song) => {
          for (const songVerse of song.songVerseDTOS) {
            songVerse.lines = [];
            for (const s of songVerse.text.split('\n')) {
              songVerse.lines.push(s);
            }
          }
          this.song = song;
          this.calculateUrlId(song.youtubeUrl);
          this.titleService.setTitle(this.song.title);
          if (!song.deleted) {
            (<any>window).ga('set', 'page', "/song/" + songId);
            (<any>window).ga('send', 'pageview');
            history.replaceState('data to be passed', this.song.title, window.location.href.replace('/#/song/', '/song/'));
          }
          this.originalSong = new Song(song);
          if (song.originalId !== undefined) {
            this.songService.getSong(song.originalId).subscribe((song) => {
              this.originalSong = song;
            });
          }
          this.isAndroid = /(android)/i.test(navigator.userAgent);
          this.isIos = /iPad|iPhone|iPod/i.test(navigator.userAgent);
          if (this.isAndroid) {
            this.showOpenInAppDialog(MobileOsTypeEnum.Android);
          } else if (this.isIos) {
            this.showOpenInAppDialog(MobileOsTypeEnum.Ios);
          }
          this.loadSongDetails();
          this.loadVersionGroup();
          this.showSimilarOnStart();
          this.loadSuggestions();
          this.loadCollections(songId);
          this.refreshMergeSong();
          this.setCanonicalUrl(songId);
        }, (err) => {
          console.log('songId: ' + songId);
          console.log(err);
        });
      }
    });
  }

  private onNavigationStart(): void {
    this.meta.removeTag('rel="canonical"');
  }

  private setCanonicalUrl(songId) {
    try {
      const s = 'http://localhost:8080/song/' + songId;
      this.meta.updateTag({ rel: 'canonical', href: s });
    } catch (error) {
      console.log(error);
    }
  }

  private loadCollections(songId: any) {
    this.songCollectionService.getAllBySongId(songId).subscribe(
      (songCollections) => {
        this.collections = songCollections;
      }
    );
  }

  refreshMergeSong(): void {
    this.markedForVersionSong = JSON.parse(localStorage.getItem(this.markedVersionGroup_key));
    if (this.song != undefined) {
      this.marked = this.markedForVersionSong != null && this.song != null && this.markedForVersionSong.uuid == this.song.uuid;
    }
    if (this.marked) {
      this.markText = "Remove mark for version group";
    } else {
      this.markText = "Mark for version group";
    }
  }

  showSimilarOnStart() {
    const user = this.auth.getUser();
    if (this.auth.isLoggedIn && user != undefined && (this.hasReviewerRoleForSong || user.isAdmin())) {
      this.showSimilar();
    }
  }

  loadVersionGroup() {
    this.songsByVersionGroup = [];
    let id = this.song.versionGroup;
    if (id == null && this.song.uuid != undefined) {
      id = this.song.uuid;
    }
    this.songService.getSongsByVersionGroup(id).subscribe((songs) => {
      for (const song of songs) {
        if (song.uuid != this.song.uuid) {
          this.songsByVersionGroup.push(song);
        }
      }
    });

  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
    this.routerSubscription.unsubscribe();
  }

  deleteSong() {
    const role = this.auth.getUser().getRolePath();
    this.songService.deleteById(role, this.song.uuid).subscribe(() => {
      this.ngOnInit();
      history.replaceState('data to be passed', this.song.title, window.location.href.replace('/song/', '/#/song/'));
    });
  }

  eraseSong() {
    const role = this.auth.getUser().getRolePath();
    this.songService.eraseById(role, this.song.uuid).subscribe(
      () => {
        // noinspection JSIgnoredPromiseFromCall
        this.router.navigate(['/songs']);
      },
      (err) => {
        if (err.statusText === 'Method Not Allowed' || ErrorUtil.errorIsNeededLogin(err)) {
          checkAuthenticationError(this.eraseSong, this, err, this.dialog);
        } else {
          console.log(err);
          this.snackBar.open(err._body, 'Close', {
            duration: 5000
          })
        }
      }
    );
  }

  publishSong() {
    this.songService.publishById(this.song.uuid).subscribe(() => {
      window.location.reload();
    });
  }

  editSong() {
    this.editing = true;
  }

  showSimilar() {
    this.similar = [];
    this.receivedSimilar = false;
    this.songService.getSimilar(this.song).subscribe((songs) => {
      this.similar = songs;
      if (songs.length > 0) {
        this.secondSong = this.similar[0];
      }
      this.receivedSimilar = true;
    });
    this.showSimilarities = true;
  }

  selectSecondSong(song: Song) {
    this.secondSong = song;
  }

  copySongCollectionElementsToSimilar() {
    for (const collection of this.collections) {
      for (const collectionElement of collection.songCollectionElements) {
        let songCollectionElement = new SongCollectionElement();
        songCollectionElement.ordinalNumber = collectionElement.ordinalNumber;
        songCollectionElement.songUuid = this.secondSong.uuid;
        this.updateSongCollectionElement(collection, songCollectionElement);
      }
    }
  }

  private updateSongCollectionElement(selectedSongCollection: SongCollection, songCollectionElement: SongCollectionElement) {
    const role = this.auth.getUser().getRolePath();
    this.songCollectionService.putInCollection(selectedSongCollection, songCollectionElement, role).subscribe(() => {
      this.snackBar.open(selectedSongCollection.name + " " + songCollectionElement.ordinalNumber + " copied.", 'Close', {
        duration: 2000
      })
    }, (err) => {
      if (ErrorUtil.errorIsNeededLogin(err)) {
        checkAuthenticationError(this.copySongCollectionElementsToSimilar, this, err, this.dialog);
      } else {
        this.snackBar.open(err._body, 'Close', {
          duration: 2000
        })
        console.log(err);
      }
    });
  }

  markForVersionGroup() {
    this.marked = !this.marked;
    if (this.marked) {
      let copySong = Song.copy(this.song);
      copySong.removeCircularReference();
      localStorage.setItem(this.markedVersionGroup_key, JSON.stringify(copySong));
    } else {
      this.markedForVersionSong = null;
      localStorage.setItem(this.markedVersionGroup_key, null);
    }
    this.refreshMergeSong();
  }

  mergeVersionGroup() {
    this.markedForVersionSong = JSON.parse(localStorage.getItem(this.markedVersionGroup_key));
    if (this.markedForVersionSong == null) {
      return;
    }
    const user = this.auth.getUser().isAdmin() ? 'admin' : 'user';
    this.songService.mergeVersionGroup(this.song.uuid, this.markedForVersionSong.uuid, user).subscribe(
      (res) => {
        if (res.status === 202) {
          if (this.auth.getUser().isAdmin()) {
            this.ngOnInit();
          }
          this.snackBar.open('Merged ' + this.song.title + ' with ' + this.markedForVersionSong.title, 'Close', {
            duration: 4000
          })
        } else {
          console.log(res);
          openAuthenticateDialog(this.mergeVersionGroup, this, this.dialog);
        }
      },
      (err) => {
        if (ErrorUtil.errorIsNeededLogin(err)) {
          checkAuthenticationError(this.mergeVersionGroup, this, err, this.dialog);
        } else {
          console.log(err);
          this.snackBar.open(err._body, 'Close', {
            duration: 5000
          })
        }
      }
    );
  }

  removeVersionGroup() {
    this.songService.removeVersionGroup(this.song.uuid).subscribe(
      (res) => {
        if (res.status === 202) {
          this.ngOnInit();
          this.snackBar.open(this.song.title + ' removed from version group', 'Close', {
            duration: 4000
          })
        } else {
          console.log(res);
        }
      },
      (err) => {
        generalError(this.removeVersionGroup, this, err, this.dialog, this.snackBar);
      }
    );
  }

  versionGroupRemovable(): boolean {
    const user = this.auth.getUser();
    if (user == null || !user.isAdmin()) {
      return false;
    }
    return this.songsByVersionGroup.length > 0;
  }

  openShareDialog(): void {
    const config = {
      data: {
        uuid: this.song.uuid,
        title: this.song.title,
      }
    };
    const dialogRef = this.dialog.open(ShareComponent, config);

    dialogRef.afterClosed().subscribe(() => {
    });
  }

  showOpenInAppDialog(mobileOsType: MobileOsTypeEnum): void {
    if (localStorage.getItem("OpenInAppComponent_dontShow") != undefined) {
      return;
    }
    localStorage.setItem("mobileOsType", MobileOsTypeEnum[mobileOsType]);
    const config = {
      data: {
        uuid: this.song.uuid,
        title: this.song.title,
      }
    };
    const dialogRef = this.dialog.open(OpenInAppComponent, config);

    dialogRef.afterClosed().subscribe(() => {
    });
  }

  calculateUrlId(url: string) {
    this.youtubeId = null;
    if (url == undefined) {
      this.safeUrl = null;
      return;
    }
    let youtubeUrl = url.replace("https://www.youtube.com/watch?v=", "");
    youtubeUrl = youtubeUrl.replace("https://www.youtube.com/embed/", "");
    youtubeUrl = youtubeUrl.replace("https://youtu.be/", "");
    let indexOf = youtubeUrl.indexOf('?');
    if (indexOf >= 0) {
      youtubeUrl = youtubeUrl.substring(0, indexOf);
    }
    if (youtubeUrl.length < 21 && youtubeUrl.length > 9) {
      this.youtubeId = youtubeUrl;
      this.safeUrl = this.sanitizer.bypassSecurityTrustResourceUrl("https://www.youtube.com/embed/" + youtubeUrl);
      setTimeout(() => {
        this.youtubeIdChecked = true; // we give one second to check. If no response then it should try to show the youtube video
      }, 1000);
    } else {
      this.safeUrl = null;
    }
  }

  onYouTubeIdCheckResult(result: YoutubeIdCheckResult) {
    this.idCheckOk = result.type == YoutubeIdCheckResultType.OK;
    this.youtubeIdChecked = true;
  }

  showYouTubeVideo(): boolean {
    return this.youtubeIdChecked;
  }

  addToCollectionSong() {
    this.openAddToCollectionDialog();
  }

  openAddToCollectionDialog(): void {
    const dialogRef = this.dialog.open(AddToCollectionComponent, {
      data: {
        song: this.song
      }
    });
    dialogRef.afterClosed().subscribe((result) => {
      if (result === 'ok') {
      }
    });
  }

  private loadSuggestions() {
    const user = this.auth.getUser();
    const role = user.getRolePath();
    if (this.auth.isLoggedIn && user != undefined && (this.hasReviewerRoleForSong || user.isAdmin())) {
      this.showSimilar();
      this.suggestionDataService.getAllBySong(role, this.song).subscribe(
        (suggestionList) => {
          this.unreviewedSuggestions = [];
          for (const suggestion of suggestionList) {
            if (!suggestion.reviewed) {
              this.unreviewedSuggestions.push(suggestion);
            }
          }
        },
        (err) => {
          checkAuthenticationError(this.loadSuggestions, this, err, this.dialog);
        }
      );
    }
  }

  conditionForShowingCollection() {
    const user = this.auth.getUser();
    return this.auth.isLoggedIn && user != undefined && (user.isAdmin() || this.hasReviewerRoleForSong);
  }

  private checkReviewerRoleForSong() {
    this.hasReviewerRoleForSong = false;
    let user = this.auth.getUser();
    if (user == undefined || user.email == '') {
      return;
    }
    this.songService.hasReviewerRoleForSong(this.song).subscribe(
      (booleanResponse) => {
        this.hasReviewerRoleForSong = booleanResponse.response;
        this.showSimilarOnStart();
      },
      (err) => {
        checkAuthenticationError(this.checkReviewerRoleForSong, this, err, this.dialog);
      }
    );
  }

  onCollectionElementClick(collectionElement: SongCollectionElement, collection: SongCollection) {
    const user = this.auth.getUser();
    if ((this.auth.login) && (user != undefined) && this.hasPermissionToAddSongsToCollection()) {
      const config = {
        data: {
          collectionElement: collectionElement,
          songCollection: collection,
          song: this.song,
        }
      };
      const dialogRef = this.dialog.open(SongCollectionElementComponent, config);
      dialogRef.afterClosed().subscribe((result) => {
        if (result === 'ok') {
          window.location.reload();
        }
      });
    }
  }

  hasPermissionToAddSongsToCollection(): Boolean {
    return this.auth.getUser().isAdmin() || this.hasReviewerRoleForSong;
  }
}
