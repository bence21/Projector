import {Component, OnInit} from '@angular/core';
import {BehaviorSubject} from 'rxjs/BehaviorSubject';
import {Observable} from 'rxjs/Observable';
import {DataSource} from '@angular/cdk/table';
import {Router} from '@angular/router';
import {StatisticsDataService} from '../../services/statistics-data.service';
import {Statistics} from '../../models/statistics';
import {Title} from "@angular/platform-browser";

export class StatisticsDatabase {
  dataChange: BehaviorSubject<Statistics[]> = new BehaviorSubject<Statistics[]>([]);

  constructor(statisticsList: Statistics[]) {
    if (statisticsList !== null) {
      const copiedData = this.data;
      let nr = 0;
      for (const statistics of statisticsList) {
        statistics.nr = ++nr;
        copiedData.push(statistics);
        this.dataChange.next(copiedData);
      }

      this.dataChange.next(copiedData);
    }
  }

  get data(): Statistics[] {
    return this.dataChange.value;
  }
}

export class StatisticsDataSource extends DataSource<any> {
  constructor(private _statisticsDatabase: StatisticsDatabase) {
    super();
  }

  connect(): Observable<Statistics[]> {
    return this._statisticsDatabase.dataChange;
  }

  disconnect() {
  }
}

@Component({
  selector: 'app-statistics-list',
  templateUrl: './statistics-list.component.html',
  styleUrls: ['./statistics-list.component.css']
})
export class StatisticsListComponent implements OnInit {

  statisticsList: Statistics[] = [];
  displayedColumns = ['Nr', 'accessedDate', 'remoteAddress', 'uri'];
  statisticsDatabase: any;
  dataSource: StatisticsDataSource | null;

  constructor(public router: Router,
              private titleService: Title,
              private statisticsDataService: StatisticsDataService) {
  }

  ngOnInit() {
    this.titleService.setTitle('Statistics');
    this.statisticsDataService.getAll().subscribe(
      (statisticsList) => {
        this.statisticsList = statisticsList.reverse();
        this.statisticsDatabase = new StatisticsDatabase(this.statisticsList);
        this.dataSource = new StatisticsDataSource(this.statisticsDatabase);
      }
    );
  }

}
