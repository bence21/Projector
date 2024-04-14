import { Component, OnInit } from '@angular/core';
import { GuidelineDataService } from '../../services/guidelines-data.service';

@Component({
  selector: 'app-song-guidelines',
  templateUrl: './song-guidelines.component.html',
  styleUrls: ['./song-guidelines.component.css']
})
export class SongGuidelinesComponent implements OnInit {

  guidelines = [];


  constructor(
    private guidelineDataService: GuidelineDataService,
  ) { }

  ngOnInit() {
    this.guidelines = this.guidelineDataService.getAll();
  }

}
