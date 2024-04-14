import { Component, OnInit, OnDestroy } from '@angular/core';
import { User } from '../../models/user';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { UserDataService } from '../../services/user-data.service';
import { Title } from '@angular/platform-browser';


@Component({
  selector: 'app-reviewer-statistics',
  templateUrl: './reviewer-statistics.component.html',
  styleUrls: ['./reviewer-statistics.component.css']
})
export class ReviewerStatisticsComponent implements OnInit, OnDestroy {

  user: User;
  private sub: Subscription;

  constructor(
    private activatedRoute: ActivatedRoute,
    private userService: UserDataService,
    private titleService: Title,
  ) { }

  ngOnInit() {
    const REVIEWER_STATISTICS = 'Reviewer statistics';
    this.titleService.setTitle(REVIEWER_STATISTICS);
    this.sub = this.activatedRoute.params.subscribe(params => {
      if (params['userId']) {
        const userId = params['userId'];
        this.userService.getUser(userId).subscribe(
          (user) => {
            this.user = user;
            this.titleService.setTitle(user.email + ' ' + REVIEWER_STATISTICS);
          });
      }
    });
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
  }

}
