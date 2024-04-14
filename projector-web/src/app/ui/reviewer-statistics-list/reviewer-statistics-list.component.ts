import { Component, OnInit, Input } from '@angular/core';
import { BehaviorSubject } from 'rxjs/BehaviorSubject';
import { Observable } from 'rxjs/Observable';
import { DataSource } from '@angular/cdk/table';
import { User } from '../../models/user';
import { SongService, Song } from '../../services/song-service.service';
import { PageEvent, MatDatepickerInputEvent } from '@angular/material';
import { FormControl } from '@angular/forms';
import { SongListComponent } from '../song-list/song-list.component';
import { compare } from '../../util/sort-util';

class ReviewerStatistics {
  nr: number;
  song: Song;
}

export class StatisticsDatabase {
  dataChange: BehaviorSubject<ReviewerStatistics[]> = new BehaviorSubject<ReviewerStatistics[]>([]);

  constructor(reviewerStatisticsList: ReviewerStatistics[]) {
    if (reviewerStatisticsList !== null) {
      const copiedData = this.data;
      let nr = 0;
      for (const reviewerStatistics of reviewerStatisticsList) {
        reviewerStatistics.nr = ++nr;
        copiedData.push(reviewerStatistics);
        this.dataChange.next(copiedData);
      }

      this.dataChange.next(copiedData);
    }
  }

  get data(): ReviewerStatistics[] {
    return this.dataChange.value;
  }
}

export class StatisticsDataSource extends DataSource<any> {
  constructor(private _statisticsDatabase: StatisticsDatabase) {
    super();
  }

  connect(): Observable<ReviewerStatistics[]> {
    return this._statisticsDatabase.dataChange;
  }

  disconnect() {
  }
}

@Component({
  selector: 'app-reviewer-statistics-list',
  templateUrl: './reviewer-statistics-list.component.html',
  styleUrls: ['./reviewer-statistics-list.component.css']
})
export class ReviewerStatisticsListComponent implements OnInit {

  displayedColumns = ['Nr', 'reviewedDate', 'title', 'lastColumn'];
  @Input()
  user: User;
  dataSource: StatisticsDataSource | null;
  songControl: FormControl;
  pageE: PageEvent;
  songTitles: Song[];
  filteredSongs: Observable<Song[]>;
  filteredSongsList: Song[];
  paginatedSongs: Song[];
  PAGE_INDEX = 'pageIndex2';
  PAGE_SIZE = 'pageSize2';
  minDate = new Date(0);
  afterDate = new Date(0);
  lastFilter = '';

  constructor(
    private songService: SongService,
  ) {
    this.songControl = new FormControl();
    this.filteredSongsList = [];
    this.paginatedSongs = [];
    this.songTitles = [];
  }

  ngOnInit() {
    this.filteredSongs = this.songControl.valueChanges
      .startWith(null)
      .map(filterValue => filterValue ? this.filterStates(filterValue) : this.songTitles.slice());
    this.initializePageEvent();
    this.filteredSongs.subscribe(filteredSongsList => {
      this.afterFilter(filteredSongsList);
    }
    );
    this.getSongTitles();
  }

  private afterFilter(filteredSongsList: Song[]) {
    let pageIndex = JSON.parse(sessionStorage.getItem(this.PAGE_INDEX));
    let start = pageIndex * this.pageE.pageSize;
    while (start > filteredSongsList.length) {
      pageIndex -= 1;
      start = pageIndex * this.pageE.pageSize;
    }
    this.pageE.pageIndex = pageIndex;
    const end = (pageIndex + 1) * this.pageE.pageSize;
    this.paginatedSongs = filteredSongsList.slice(start, end);
    this.filteredSongsList = filteredSongsList;
    this.refillData();
  }

  private initializePageEvent() {
    const pageEvent = new PageEvent();
    pageEvent.pageSize = JSON.parse(sessionStorage.getItem(this.PAGE_SIZE));
    pageEvent.pageIndex = JSON.parse(sessionStorage.getItem(this.PAGE_INDEX));
    if (pageEvent.pageSize == undefined) {
      pageEvent.pageSize = 10;
    }
    if (pageEvent.pageIndex == undefined) {
      pageEvent.pageIndex = 0;
    }
    this.pageE = pageEvent;
  }

  private getSongTitles() {
    this.songService.getAllSongTitlesReviewedByUser(this.user).subscribe((songs) => {
      this.songTitles = songs;
      this.sort();
      this.fillData(songs);
      this.triggerChange();
    });
  }

  private triggerChange() {
    this.songControl.updateValueAndValidity();
  }

  private fillData(songs: Song[]) {
    const reviewerStatistics: ReviewerStatistics[] = [];
    for (const song of songs) {
      const reviewerStatistic = new ReviewerStatistics();
      reviewerStatistic.song = song;
      reviewerStatistics.push(reviewerStatistic);
    }
    const database = new StatisticsDatabase(reviewerStatistics);
    this.dataSource = new StatisticsDataSource(database);
  }

  pageEvent(pageEvent: PageEvent) {
    this.pageE = pageEvent;
    this.refillData();
  }

  private refillData() {
    const start = this.pageE.pageIndex * this.pageE.pageSize;
    sessionStorage.setItem(this.PAGE_INDEX, JSON.stringify(this.pageE.pageIndex));
    sessionStorage.setItem(this.PAGE_SIZE, JSON.stringify(this.pageE.pageSize));
    const end = (this.pageE.pageIndex + 1) * this.pageE.pageSize;
    this.paginatedSongs = this.filteredSongsList.slice(start, end);
    this.fillData(this.paginatedSongs);
  }

  filterStates(filter: string) {
    filter = SongListComponent.stripAccents(filter);
    this.lastFilter = filter;
    return this.filter();
  }

  private filter() {
    return this.songTitles.filter(song => {
      return this.filterByTitle(song) && this.filterByDate(song);
    });
  }

  private filterByDate(song: Song): boolean {
    return new Date(song.modifiedDate) >= this.afterDate;
  }

  private filterByTitle(song: Song): boolean {
    return SongListComponent.stripAccents(song.title).indexOf(this.lastFilter) >= 0;
  }

  afterDateChange(event: MatDatepickerInputEvent<Date>) {
    this.afterDate = event.value;
    this.afterFilter(this.filterStates(this.lastFilter));
  }

  private sort() {
    this.sortSongTitlesByModifiedDate();
  }

  private sortSongTitlesByModifiedDate() {
    this.songTitles.sort((song1, song2) => {
      return compare(song2.modifiedDate, song1.modifiedDate);
    });
  }

}
