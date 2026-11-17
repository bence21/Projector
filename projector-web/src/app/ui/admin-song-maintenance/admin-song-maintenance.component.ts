import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { MatDialog, MatSnackBar } from '@angular/material';
import { DeleteSongsWithNoVersesResult, Song, SongService } from '../../services/song-service.service';
import { checkAuthenticationError, ErrorUtil } from '../../util/error-util';
import { LanguageDataService } from '../../services/language-data.service';
import { AuthService } from '../../services/auth.service';
import { Language } from '../../models/language';
import { User } from '../../models/user';
import { SELECTED_LANGUGAGE } from '../../util/constants';
import { SongListComponent } from '../song-list/song-list.component';
import { ConfirmActionDialogComponent } from '../confirm-action-dialog/confirm-action-dialog.component';

const NEAR_DUPLICATE_CUTOFF_STORAGE_KEY = 'adminSongMaintenance_nearDuplicateCutoffDate';

@Component({
  selector: 'app-admin-song-maintenance',
  templateUrl: './admin-song-maintenance.component.html',
  styleUrls: ['./admin-song-maintenance.component.css']
})
export class AdminSongMaintenanceComponent implements OnInit {

  loadingSimilarBatch = false;
  loadingRemoveDuplicates = false;
  loadingNoVerseSongs = false;
  deletingNoVerseSongs = false;
  noVerseSongs: Song[] = [];

  languages: Language[] = [];
  selectedLanguage: Language;
  /** When true, batch endpoints without language scope (admin "All" in language selector). */
  runForAllLanguages = false;

  /** Server query param visibility: public vs non-public songs in the batch. */
  similarBatchVisibility: 'public' | 'nonPublic' = 'public';

  /** YYYY-MM-DD for admin near-duplicate eligibility; persisted in localStorage. */
  nearDuplicateCutoffDate: string;

  constructor(
    private titleService: Title,
    private songService: SongService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog,
    private languageDataService: LanguageDataService,
    private auth: AuthService,
  ) {
    const saved = localStorage.getItem(NEAR_DUPLICATE_CUTOFF_STORAGE_KEY);
    this.nearDuplicateCutoffDate = saved && saved.trim().length > 0 ? saved.trim() : '';
  }

  ngOnInit() {
    this.titleService.setTitle('Song maintenance');
    this.loadNoVerseSongs();
    this.languageDataService.getAll().subscribe((languages) => {
      const user: User = this.auth.getUser();
      this.languages = [];
      const selectedLanguageFromLocalStorage = SongListComponent.getSelectedLanguageFromLocalStorage(languages);
      let was = false;
      for (const language of languages) {
        if (user.hasReviewerRoleForLanguage(language)) {
          this.languages.push(language);
          if (language.uuid === selectedLanguageFromLocalStorage.uuid) {
            this.selectedLanguage = language;
            was = true;
          }
        }
      }
      if (!was && this.languages.length > 0) {
        this.selectedLanguage = this.languages[0];
      }
    });
  }

  onNearDuplicateCutoffDateChange(value: string): void {
    if (value == null || value === '') {
      localStorage.removeItem(NEAR_DUPLICATE_CUTOFF_STORAGE_KEY);
    } else {
      localStorage.setItem(NEAR_DUPLICATE_CUTOFF_STORAGE_KEY, value);
    }
  }

  onLanguageChange(language: Language) {
    this.selectedLanguage = language;
    localStorage.setItem(SELECTED_LANGUGAGE, JSON.stringify(this.selectedLanguage));
    this.runForAllLanguages = false;
  }

  onAllSelected() {
    this.runForAllLanguages = true;
  }

  get maintenanceActionsDisabled(): boolean {
    return !this.runForAllLanguages && !this.selectedLanguage;
  }

  loadNoVerseSongs(): void {
    this.loadingNoVerseSongs = true;
    this.songService.getSongsWithNoVerses().subscribe(
      (songs) => {
        this.noVerseSongs = songs || [];
        this.loadingNoVerseSongs = false;
      },
      (err) => {
        this.loadingNoVerseSongs = false;
        if (ErrorUtil.errorIsNeededLogin(err)) {
          checkAuthenticationError(this.loadNoVerseSongs, this, err, this.dialog);
        } else {
          this.snackBar.open('Could not load songs with no verses.', undefined, { duration: 5000 });
        }
      }
    );
  }

  songPageUrl(song: Song): string {
    return '/#/song/' + song.uuid;
  }

  countNoVerseSongsForDelete(): { softDeleted: number; erased: number } {
    let softDeleted = 0;
    let erased = 0;
    for (const song of this.noVerseSongs) {
      if (this.songIsPublic(song)) {
        softDeleted++;
      } else {
        erased++;
      }
    }
    return { softDeleted, erased };
  }

  /** Same rules as server {@code Song#isPublic()} for list DTOs (uuid-only, no {@code hasId} gate). */
  private songIsPublic(song: Song): boolean {
    const isBackUp = song.isBackUp === true;
    const blocking = song.hasBlockingWordIssues === true;
    return !song.reviewerErased && !song.deleted && !isBackUp && !blocking;
  }

  deleteAllNoVerseSongs(): void {
    if (this.deletingNoVerseSongs || this.noVerseSongs.length === 0) {
      return;
    }
    const { softDeleted, erased } = this.countNoVerseSongsForDelete();
    const confirmDialogRef = this.dialog.open(ConfirmActionDialogComponent, {
      data: {
        title: 'Delete all songs with no verses?',
        message: `${this.noVerseSongs.length} song(s) have no verses.`,
        reason: `${softDeleted} public song(s) will be hidden (soft delete).\n${erased} non-public song(s) will be permanently removed.`,
        confirmText: 'Delete all',
        cancelText: 'Cancel',
      },
    });
    confirmDialogRef.afterClosed().subscribe((confirmed) => {
      if (!confirmed) {
        return;
      }
      this.deletingNoVerseSongs = true;
      this.songService.deleteSongsWithNoVerses().subscribe(
        (result: DeleteSongsWithNoVersesResult) => {
          this.deletingNoVerseSongs = false;
          const msg = `Done. Hidden: ${result.softDeletedCount}, permanently removed: ${result.erasedCount}.`;
          this.snackBar.open(msg, undefined, { duration: 7000 });
          this.loadNoVerseSongs();
        },
        (err) => {
          this.deletingNoVerseSongs = false;
          if (ErrorUtil.errorIsNeededLogin(err)) {
            checkAuthenticationError(this.deleteAllNoVerseSongs, this, err, this.dialog);
          } else {
            this.snackBar.open('Delete failed.', undefined, { duration: 5000 });
          }
        }
      );
    });
  }

  private languageUuidForBatch(): string | undefined {
    return this.runForAllLanguages ? undefined : (this.selectedLanguage && this.selectedLanguage.uuid);
  }

  runMarkSimilarSongsBatch(): void {
    if (this.loadingSimilarBatch || this.maintenanceActionsDisabled) {
      return;
    }
    this.loadingSimilarBatch = true;
    const cutoff = this.nearDuplicateCutoffDate && this.nearDuplicateCutoffDate.trim().length > 0
      ? this.nearDuplicateCutoffDate.trim()
      : undefined;
    this.songService.runMarkSimilarSongsBatch(this.languageUuidForBatch(), this.similarBatchVisibility, cutoff).subscribe(
      () => {
        const scope = this.similarBatchVisibility === 'nonPublic' ? 'non-public' : 'public';
        const msg = this.runForAllLanguages
          ? `Mark similar songs finished (${scope}).`
          : `Mark similar songs finished (${this.selectedLanguage.nativeName}, ${scope}).`;
        this.snackBar.open(msg, undefined, { duration: 5000 });
        this.loadingSimilarBatch = false;
      },
      (err) => {
        this.loadingSimilarBatch = false;
        if (ErrorUtil.errorIsNeededLogin(err)) {
          checkAuthenticationError(this.runMarkSimilarSongsBatch, this, err, this.dialog);
        } else {
          this.snackBar.open('Mark similar songs failed.', undefined, { duration: 5000 });
        }
      }
    );
  }

  runRemoveDuplicateUploads(): void {
    if (this.loadingRemoveDuplicates || this.maintenanceActionsDisabled) {
      return;
    }
    this.loadingRemoveDuplicates = true;
    this.songService.runRemoveDuplicateUploads(this.languageUuidForBatch()).subscribe(
      () => {
        const msg = this.runForAllLanguages
          ? 'Remove duplicate uploads finished.'
          : `Remove duplicate uploads finished (${this.selectedLanguage.nativeName}).`;
        this.snackBar.open(msg, undefined, { duration: 5000 });
        this.loadingRemoveDuplicates = false;
      },
      (err) => {
        this.loadingRemoveDuplicates = false;
        if (ErrorUtil.errorIsNeededLogin(err)) {
          checkAuthenticationError(this.runRemoveDuplicateUploads, this, err, this.dialog);
        } else {
          this.snackBar.open('Remove duplicate uploads failed.', undefined, { duration: 5000 });
        }
      }
    );
  }
}
