import { Component, Output, EventEmitter } from '@angular/core';
import { MatDialog } from '@angular/material';
import { BaseWordActionButtonComponent } from '../base-word-action-button/base-word-action-button.component';
import { ReviewedWordStatus } from '../../models/reviewedWord';

@Component({
  selector: 'app-mark-as-not-sure-button',
  templateUrl: './mark-as-not-sure-button.component.html',
  styleUrls: ['./mark-as-not-sure-button.component.css']
})
export class MarkAsNotSureButtonComponent extends BaseWordActionButtonComponent {
  @Output() notSureClick = new EventEmitter<void>();

  constructor(dialog: MatDialog) {
    super(dialog);
  }

  protected getStatusName(): string {
    return 'Not sure';
  }

  protected getTargetStatus(): ReviewedWordStatus {
    return ReviewedWordStatus.NOT_SURE;
  }

  protected emitAction(): void {
    this.notSureClick.emit();
  }
}
