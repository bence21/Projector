import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { Song, SongService } from '../../services/song-service.service';
import { DuplicateYouTubeGroup, groupDuplicateYouTubeIds } from '../../util/youtube.util';

@Component({
  selector: 'app-duplicate-youtube-ids',
  templateUrl: './duplicate-youtube-ids.component.html',
  styleUrls: ['./duplicate-youtube-ids.component.css']
})
export class DuplicateYoutubeIdsComponent implements OnInit {

  loading = true;
  loadError: string = null;
  duplicateGroups: DuplicateYouTubeGroup[] = [];
  totalSongsWithYouTube = 0;

  constructor(
    private titleService: Title,
    private songService: SongService,
  ) { }

  ngOnInit() {
    this.titleService.setTitle('Duplicate YouTube IDs');
    this.songService.getSongsContainingYouTube().subscribe(
      (songs) => {
        const active = songs.filter(s => !s.deleted && !s.reviewerErased);
        this.totalSongsWithYouTube = active.length;
        this.duplicateGroups = groupDuplicateYouTubeIds(active);
        this.loading = false;
      },
      () => {
        this.loadError = 'Could not load songs with YouTube links. Admin login required.';
        this.loading = false;
      }
    );
  }

  songLink(song: Song | { uuid?: string; id?: string }): string {
    const id = song.uuid || song.id;
    return id ? '/#/song/' + id : '';
  }

  watchUrl(youtubeId: string): string {
    return 'https://www.youtube.com/watch?v=' + youtubeId;
  }

  get duplicateSongCount(): number {
    let count = 0;
    for (const group of this.duplicateGroups) {
      count += group.songs.length;
    }
    return count;
  }
}
