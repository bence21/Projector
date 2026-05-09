import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material';
import { Song } from '../../services/song-service.service';

export interface QuickReviewSummaryDialogData {
  title: string;
  newSongTitle: string;
  languageLabel: string;
  similarTitle: string;
  newSong?: Song;
  similarSong?: Song;
  notes?: string[];
  footerHint?: string;
  confirmText?: string;
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
  constructor(
    public dialogRef: MatDialogRef<QuickReviewSummaryDialogComponent, boolean>,
    @Inject(MAT_DIALOG_DATA) public data: QuickReviewSummaryDialogData
  ) {}
}
