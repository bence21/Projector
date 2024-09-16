import { Component, Input, OnInit } from '@angular/core';
import { Guideline } from '../../models/guideline';

@Component({
  selector: 'app-song-guideline-checker',
  templateUrl: './song-guideline-checker.component.html',
  styleUrls: ['./song-guideline-checker.component.css']
})
export class SongGuidelineCheckerComponent implements OnInit {

  guideline = new Guideline();
  checkboxState: boolean = false;

  @Input()
  set pGuideline(guideline: Guideline) {
    this.guideline = guideline;
    this.checkboxState = guideline.checkboxState;
  }

  constructor() { }

  ngOnInit() {
  }

  onCheckBoxClick(event: Event) {
    event.stopPropagation();
    this.guideline.checkboxState = !this.checkboxState; // inverted because checkboxState here not yet updated!
  }

  shouldBeExpanded(): boolean {
    return !this.checkboxState;
  }

}
