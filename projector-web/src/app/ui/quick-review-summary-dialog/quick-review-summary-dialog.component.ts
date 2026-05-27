import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material';
import { Song } from '../../services/song-service.service';
import { ConfirmActionDialogComponent } from '../confirm-action-dialog/confirm-action-dialog.component';

export type QuickReviewSummaryDialogResult =
  | { action: 'merge' }
  | { action: 'replaceDuplicate' };

export interface QuickReviewSummaryDialogData {
  title: string;
  newSongTitle: string;
  languageLabel: string;
  similarTitle: string;
  newSong?: Song;
  similarSong?: Song;
  notes?: string[];
  /** @deprecated Prefer footerHintMerge + footerHintReplace */
  footerHint?: string;
  footerHintMerge?: string;
  footerHintReplace?: string;
  confirmText?: string;
  mergeText?: string;
  replaceDuplicateText?: string;
  cancelText?: string;
  loadingMessage?: string;
  canConfirm?: boolean;
}

@Component({
  selector: 'app-quick-review-summary-dialog',
  templateUrl: './quick-review-summary-dialog.component.html',
  styleUrls: ['./quick-review-summary-dialog.component.css']
})
export class QuickReviewSummaryDialogComponent {

  primaryIsReplaceDuplicate = false;

  constructor(
    public dialogRef: MatDialogRef<QuickReviewSummaryDialogComponent, QuickReviewSummaryDialogResult | false>,
    @Inject(MAT_DIALOG_DATA) public data: QuickReviewSummaryDialogData,
    private dialog: MatDialog,
  ) {}

  onTextsMatchChange(match: boolean): void {
    this.primaryIsReplaceDuplicate = match;
  }

  get displayedFooterHint(): string {
    if (this.primaryIsReplaceDuplicate) {
      return this.data.footerHintReplace || this.data.footerHint || '';
    }
    return this.data.footerHintMerge || this.data.footerHint || '';
  }

  mergeLabel(): string {
    return this.data.mergeText || this.data.confirmText || 'Publish & merge';
  }

  replaceLabel(): string {
    return this.data.replaceDuplicateText || 'Copy collections & erase';
  }

  onPrimaryMerge(): void {
    this.dialogRef.close({ action: 'merge' });
  }

  onPrimaryReplace(): void {
    this.dialogRef.close({ action: 'replaceDuplicate' });
  }

  onSecondaryMerge(): void {
    const confirmDialogRef = this.dialog.open(ConfirmActionDialogComponent, {
      width: '560px',
      data: {
        title: 'Publish & merge anyway?',
        message: 'Under your current compare settings the lyrics already match the top similar song.',
        reason: 'Merging still links version groups and keeps this song published. Continue only if that is what you want.',
        confirmText: this.mergeLabel(),
        cancelText: this.data.cancelText || 'Cancel',
      },
    });
    confirmDialogRef.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.dialogRef.close({ action: 'merge' });
      }
    });
  }

  onSecondaryReplace(): void {
    const confirmDialogRef = this.dialog.open(ConfirmActionDialogComponent, {
      width: '560px',
      data: {
        title: 'Copy collections & erase anyway?',
        message: 'The lyrics still differ under your current compare settings.',
        reason:
          'This will copy collection memberships to the similar song and permanently erase this song; differing lines will be lost.',
        confirmText: this.replaceLabel(),
        cancelText: this.data.cancelText || 'Cancel',
      },
    });
    confirmDialogRef.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.dialogRef.close({ action: 'replaceDuplicate' });
      }
    });
  }
}
