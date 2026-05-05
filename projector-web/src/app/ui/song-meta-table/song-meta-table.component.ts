import { Component, Input } from '@angular/core';
import { Song } from '../../services/song-service.service';

@Component({
  selector: 'app-song-meta-table',
  templateUrl: './song-meta-table.component.html'
})
export class SongMetaTableComponent {
  @Input() song: Song;
}
