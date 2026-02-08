import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material';

@Component({
  selector: 'app-confirm-action-dialog',
  template: `
    <h2 mat-dialog-title>{{data.title}}</h2>
    <mat-dialog-content>
      <p>{{data.message}}</p>
    </mat-dialog-content>
    <mat-dialog-actions>
      <button mat-button [mat-dialog-close]="false">{{data.cancelText || 'Cancel'}}</button>
      <button mat-button [mat-dialog-close]="true" color="primary">{{data.confirmText || 'Confirm'}}</button>
    </mat-dialog-actions>
  `,
  styles: []
})
export class ConfirmActionDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<ConfirmActionDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: {
      title: string;
      message: string;
      confirmText?: string;
      cancelText?: string;
    }
  ) {}
}
