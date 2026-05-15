import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material';

@Component({
  selector: 'app-confirm-action-dialog',
  template: `
    <h2 mat-dialog-title>{{data.title}}</h2>
    <mat-dialog-content>
      <p class="confirm-message">{{data.message}}</p>
      <p *ngIf="data.reason" class="confirm-reason">{{data.reason}}</p>
    </mat-dialog-content>
    <mat-dialog-actions>
      <button mat-button [mat-dialog-close]="false">{{data.cancelText || 'Cancel'}}</button>
      <button mat-button [mat-dialog-close]="true" color="primary">{{data.confirmText || 'Confirm'}}</button>
    </mat-dialog-actions>
  `,
  styles: [`
    .confirm-reason {
      margin-top: 12px;
      white-space: pre-line;
    }
  `]
})
export class ConfirmActionDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<ConfirmActionDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: {
      title: string;
      message: string;
      reason?: string;
      confirmText?: string;
      cancelText?: string;
    }
  ) {}
}
