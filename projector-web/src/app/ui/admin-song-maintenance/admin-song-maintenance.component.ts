import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { MatDialog, MatSnackBar } from '@angular/material';
import { SongService } from '../../services/song-service.service';
import { checkAuthenticationError, ErrorUtil } from '../../util/error-util';

@Component({
  selector: 'app-admin-song-maintenance',
  templateUrl: './admin-song-maintenance.component.html',
  styleUrls: ['./admin-song-maintenance.component.css']
})
export class AdminSongMaintenanceComponent implements OnInit {

  loadingSimilarBatch = false;
  loadingRemoveDuplicates = false;

  constructor(
    private titleService: Title,
    private songService: SongService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog,
  ) {
  }

  ngOnInit() {
    this.titleService.setTitle('Song maintenance');
  }

  runMarkSimilarSongsBatch(): void {
    if (this.loadingSimilarBatch) {
      return;
    }
    this.loadingSimilarBatch = true;
    this.songService.runMarkSimilarSongsBatch().subscribe(
      () => {
        this.snackBar.open('Mark similar songs finished.', undefined, { duration: 5000 });
        this.loadingSimilarBatch = false;
      },
      (err) => {
        this.loadingSimilarBatch = false;
        if (ErrorUtil.errorIsNeededLogin(err)) {
          checkAuthenticationError(this.runMarkSimilarSongsBatch, this, err, this.dialog);
        } else {
          this.snackBar.open('Mark similar songs failed.', undefined, { duration: 5000 });
        }
      }
    );
  }

  runRemoveDuplicateUploads(): void {
    if (this.loadingRemoveDuplicates) {
      return;
    }
    this.loadingRemoveDuplicates = true;
    this.songService.runRemoveDuplicateUploads().subscribe(
      () => {
        this.snackBar.open('Remove duplicate uploads finished.', undefined, { duration: 5000 });
        this.loadingRemoveDuplicates = false;
      },
      (err) => {
        this.loadingRemoveDuplicates = false;
        if (ErrorUtil.errorIsNeededLogin(err)) {
          checkAuthenticationError(this.runRemoveDuplicateUploads, this, err, this.dialog);
        } else {
          this.snackBar.open('Remove duplicate uploads failed.', undefined, { duration: 5000 });
        }
      }
    );
  }
}
