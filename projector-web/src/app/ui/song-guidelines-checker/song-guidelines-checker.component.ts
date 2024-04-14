import { Component, Input, OnInit } from '@angular/core';
import { Guideline } from '../../models/guideline';

@Component({
  selector: 'app-song-guidelines-checker',
  templateUrl: './song-guidelines-checker.component.html',
  styleUrls: ['./song-guidelines-checker.component.css']
})
export class SongGuidelinesCheckerComponent implements OnInit {

  guidelines = [];

  @Input()
  set pGuidelines(guidelines: Guideline[]) {
    this.guidelines = guidelines;
  }

  constructor(
  ) { }

  ngOnInit() {
  }
}
