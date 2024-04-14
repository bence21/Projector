import { Component, Inject, OnInit } from "@angular/core";
import { MatDialogRef, MatSnackBar, MAT_DIALOG_DATA } from "@angular/material";
import { SongCollection, SongCollectionElement } from "../../models/songCollection";
import { AuthService } from "../../services/auth.service";
import { SongCollectionDataService } from "../../services/song-collection-data.service";
import { Song } from "../../services/song-service.service";

@Component({
  selector: 'app-song-collection-element',
  templateUrl: './song.collection.element.html',
  styleUrls: ['./song.collection.element.css']
})
export class SongCollectionElementComponent implements OnInit {

  collectionElement: SongCollectionElement;
  songCollection: SongCollection;
  song: Song;

  constructor(
    private dialogRef: MatDialogRef<SongCollectionElementComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private songCollectionDataService: SongCollectionDataService,
    private snackBar: MatSnackBar,
    private auth: AuthService,
  ) {
    this.collectionElement = data.collectionElement;
    this.songCollection = data.songCollection;
    this.song = data.song;
  }

  ngOnInit() {

  }

  onDeleteClick() {
    const role = this.auth.getUser().getRolePath();
    this.songCollectionDataService.deleteSongCollectionElement(this.collectionElement, this.songCollection, this.song, role).subscribe(
      (_response) => {
        this.dialogRef.close('ok');
      },
      () => {
        this.snackBar.open('Something wrong', undefined, {
          duration: 3000
        });
      }
    )
  }
}