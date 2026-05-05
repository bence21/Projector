import { Injectable } from '@angular/core';
import { MatDialog, MatSnackBar } from '@angular/material';
import { Song, SongService } from './song-service.service';
import { Language } from '../models/language';
import { SongWordValidationService } from './song-word-validation.service';
import {
  QuickReviewSummaryDialogComponent,
  QuickReviewSummaryDialogData
} from '../ui/quick-review-summary-dialog/quick-review-summary-dialog.component';
import { SongWordValidationDialogComponent } from '../ui/song-word-validation-dialog/song-word-validation-dialog.component';
import { ConfirmActionDialogComponent } from '../ui/confirm-action-dialog/confirm-action-dialog.component';
import { SongWordValidationResult } from '../models/songWordValidationResult';
import { checkAuthenticationError, ErrorUtil, generalError } from '../util/error-util';

type QuickReviewFlowState = {
  data: QuickReviewSummaryDialogData;
  dialogRef: any;
  topSimilar: Song;
  dialogClosed: boolean;
  similarityReady: boolean;
  validationReady: boolean;
  notes: string[];
};

@Injectable()
export class QuickReviewFlowService {

  constructor(
    private songService: SongService,
    private songWordValidationService: SongWordValidationService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
  ) {
  }

  runQuickReview(
    fullSong: Song,
    selectedLanguage: Language,
    setBusy: (busy: boolean) => void,
    onMerged: () => void,
    onFatalError: (err: any) => void
  ): void {
    const data = this.createQuickReviewDialogData(fullSong);
    const dialogRef = this.dialog.open(QuickReviewSummaryDialogComponent, {
      width: '95vw',
      maxWidth: '1400px',
      data: data
    });
    const state: QuickReviewFlowState = {
      data: data,
      dialogRef: dialogRef,
      topSimilar: null,
      dialogClosed: false,
      similarityReady: false,
      validationReady: false,
      notes: []
    };
    this.startQuickReviewSimilarityCheck(fullSong, state, setBusy, onFatalError);
    this.startQuickReviewWordValidationCheck(fullSong, selectedLanguage, state, setBusy);
    this.registerQuickReviewDialogCloseHandler(fullSong, state, setBusy, onMerged);
  }

  private createQuickReviewDialogData(fullSong: Song): QuickReviewSummaryDialogData {
    const languageLabel = fullSong.languageDTO ? fullSong.languageDTO.printLanguage() : '(unknown)';
    return {
      title: 'Quick review — confirm',
      newSongTitle: fullSong.title,
      languageLabel: languageLabel,
      similarTitle: 'Loading...',
      newSong: fullSong,
      notes: ['Checks are running: similarity, then word validation.'],
      loadingMessage: 'Preparing quick review...',
      footerHint: 'Saves the new song (published / not deleted) and merges its version group with the top similar song.',
      confirmText: 'Publish & merge',
      cancelText: 'Cancel',
      canConfirm: false
    };
  }

  private setQuickReviewDialogData(state: QuickReviewFlowState, patch: Partial<QuickReviewSummaryDialogData>): void {
    if (state.dialogClosed) {
      return;
    }
    Object.assign(state.data, patch);
  }

  private tryEnableQuickReviewConfirm(state: QuickReviewFlowState): void {
    if (state.similarityReady && state.validationReady) {
      this.setQuickReviewDialogData(state, {
        notes: state.notes.length > 0 ? state.notes : ['No blocking issues found.'],
        loadingMessage: undefined,
        canConfirm: true
      });
    }
  }

  private startQuickReviewSimilarityCheck(
    fullSong: Song,
    state: QuickReviewFlowState,
    setBusy: (busy: boolean) => void,
    onFatalError: (err: any) => void
  ): void {
    this.songService.getSimilar(fullSong).subscribe(
      (similarSongs) => {
        if (!similarSongs || similarSongs.length === 0) {
          setBusy(false);
          state.dialogRef.close(false);
          this.snackBar.open(
            'No similar songs found for "' + fullSong.title + '". Open the song to review manually.',
            'Close',
            { duration: 6000 }
          );
          return;
        }
        state.topSimilar = similarSongs[0];
        state.similarityReady = true;
        this.setQuickReviewDialogData(state, {
          similarTitle: state.topSimilar.title,
          similarSong: state.topSimilar,
          loadingMessage: state.validationReady ? undefined : 'Similarity found. Validating words...'
        });
        this.tryEnableQuickReviewConfirm(state);
      },
      (err) => {
        setBusy(false);
        state.dialogRef.close(false);
        onFatalError(err);
      }
    );
  }

  private startQuickReviewWordValidationCheck(
    fullSong: Song,
    selectedLanguage: Language,
    state: QuickReviewFlowState,
    setBusy: (busy: boolean) => void
  ): void {
    this.songWordValidationService.validateWords(fullSong).subscribe(
      (validation) => {
        const hasBlocking =
          (validation.bannedWords && validation.bannedWords.length > 0) ||
          (validation.rejectedWords && validation.rejectedWords.length > 0);
        if (hasBlocking) {
          setBusy(false);
          state.dialogRef.close(false);
          this.snackBar.open('Blocked: resolve banned or rejected words before quick review.', 'Close', { duration: 5000 });
          this.dialog.open(SongWordValidationDialogComponent, {
            width: '700px',
            data: {
              validationResult: validation,
              language: selectedLanguage,
              publish: true,
              song: fullSong,
              validationService: this.songWordValidationService
            }
          });
          return;
        }
        if (validation.unreviewedWords && validation.unreviewedWords.length > 0) {
          state.notes.push(`${validation.unreviewedWords.length} unreviewed word(s) — you may still publish.`);
        }
        if (validation.hasMixedLanguageWarning) {
          this.confirmQuickReviewMixedLanguageWarning(validation, state, setBusy);
        } else {
          state.validationReady = true;
          this.tryEnableQuickReviewConfirm(state);
        }
      },
      (err) => {
        console.error(err);
        setBusy(false);
        state.dialogRef.close(false);
        this.snackBar.open('Word validation failed. Try again or review manually.', 'Close', { duration: 5000 });
      }
    );
  }

  private confirmQuickReviewMixedLanguageWarning(
    validation: SongWordValidationResult,
    state: QuickReviewFlowState,
    setBusy: (busy: boolean) => void
  ): void {
    const foreignWordCount = validation.foreignWordCount || 0;
    const totalReviewedWordCount = validation.totalReviewedWordCount || 0;
    const foreignWordRatio = validation.foreignWordRatio || 0;
    const foreignLanguages = validation.foreignLanguages || [];
    const languageText = foreignLanguages.length > 0
      ? ` Detected foreign languages: ${foreignLanguages.join(', ')}.`
      : '';
    const confirmDialogRef = this.dialog.open(ConfirmActionDialogComponent, {
      width: '560px',
      data: {
        title: 'Mixed-Language Warning',
        message: `This song contains many foreign-language words (${foreignWordCount}/${totalReviewedWordCount}, ${Math.round((foreignWordRatio || 0) * 100)}%). A small amount is okay, but heavy language mixing should be avoided.${languageText} Continue quick review?`,
        confirmText: 'Continue',
        cancelText: 'Cancel'
      }
    });
    confirmDialogRef.afterClosed().subscribe((confirmed) => {
      if (!confirmed) {
        setBusy(false);
        state.dialogRef.close(false);
        return;
      }
      state.notes.push('Mixed-language warning was confirmed.');
      state.validationReady = true;
      this.tryEnableQuickReviewConfirm(state);
    });
  }

  private registerQuickReviewDialogCloseHandler(
    fullSong: Song,
    state: QuickReviewFlowState,
    setBusy: (busy: boolean) => void,
    onMerged: () => void
  ): void {
    state.dialogRef.afterClosed().subscribe((confirmed) => {
      state.dialogClosed = true;
      if (!confirmed || !state.topSimilar) {
        setBusy(false);
        return;
      }
      this.saveAndMergeQuickReviewSongs(fullSong, state.topSimilar, setBusy, onMerged);
    });
  }

  private saveAndMergeQuickReviewSongs(
    fullSong: Song,
    topSimilar: Song,
    setBusy: (busy: boolean) => void,
    onMerged: () => void
  ): void {
    const saveAndMerge = () => {
      fullSong.deleted = false;
      this.songService.updateSong('admin', fullSong).subscribe(
        () => {
          this.songService.mergeVersionGroup(topSimilar.uuid, fullSong.uuid, 'admin').subscribe(
            (res) => {
              setBusy(false);
              if (res.status === 202) {
                this.snackBar.open(`Merged "${fullSong.title}" with "${topSimilar.title}".`, 'Close', { duration: 4000 });
                onMerged();
              } else {
                this.snackBar.open('Merge did not complete. Check the song pages.', 'Close', { duration: 6000 });
              }
            },
            (err) => {
              setBusy(false);
              if (ErrorUtil.errorIsNeededLogin(err)) {
                checkAuthenticationError(saveAndMerge, this, err, this.dialog);
              } else {
                console.log(err);
                const body = err._body != null ? err._body : 'Merge failed';
                this.snackBar.open(body, 'Close', { duration: 5000 });
              }
            }
          );
        },
        (err) => {
          setBusy(false);
          if (ErrorUtil.errorIsNeededLogin(err)) {
            checkAuthenticationError(saveAndMerge, this, err, this.dialog);
          } else {
            console.log(err);
            const body = err._body != null ? err._body : 'Save failed';
            this.snackBar.open(body, 'Close', { duration: 5000 });
          }
        }
      );
    };
    saveAndMerge();
  }
}
