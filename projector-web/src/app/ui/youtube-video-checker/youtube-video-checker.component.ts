import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { Song, SongService } from '../../services/song-service.service';
import { YoutubeIdCheckResult, YoutubeIdCheckResultType } from '../youtube-id-check/youtube-id-check.component';

@Component({
  selector: 'app-youtube-video-checker',
  templateUrl: './youtube-video-checker.component.html',
  styleUrls: ['./youtube-video-checker.component.css']
})
export class YoutubeVideoCheckerComponent implements OnInit {

  songTitles: Song[] = [];
  private i: number;
  songForPlayer: Song;
  timeoutForSongMap: Map<string, boolean> = new Map<string, boolean>();
  progressValue = 0;
  totalSteps = 0;
  songsWithProblems: Song[] = [];

  constructor(
    private titleService: Title,
    private songService: SongService,
  ) { }

  ngOnInit() {
    this.titleService.setTitle('YouTube video checker');
    this.songService.getSongsContainingYouTube().subscribe((songs) => {
      songs = songs.filter(s => !s.deleted && !s.reviewerErased);
      this.songTitles = Song.sortByModifiedDate(songs);
      this.totalSteps = songs.length;
      this.checkYouTubeVideos();
    });
  }

  private checkYouTubeVideos() {
    if (this.songTitles.length == 0) {
      return;
    }
    this.i = 0;
    this.checkYouTubeVideo();
  }

  private checkYouTubeVideo() {
    const song = this.getCurrentSong();
    if (song == undefined) {
      return;
    }
    this.initializePlayer(song);
  }

  private initializePlayer(song: Song) {
    this.songForPlayer = song;
    setTimeout(() => {
      this.checkAndGoToNextSong(song); // we give one second to check.
    }, 1000);
  }

  onYouTubeIdCheckResult(result: YoutubeIdCheckResult) {
    if (result.type != YoutubeIdCheckResultType.OK) {
      this.songsWithProblems.push(result.song);
      this.printSongUrl(this.getCurrentSong());
      if (result.song.youtubeUrl != result.videoId) {
        console.log('result.song.youtubeUrl: ' + result.song.youtubeUrl);
        console.log('result.videoId: ' + result.videoId);
        console.log('result.song.youtubeUrl != result.videoId:  ' + result.song.title);
        this.printSongUrl(result.song);
      }
    }
    this.checkAndGoToNextSong(result.song);
  }

  private checkAndGoToNextSong(song: Song) {
    const key = song.id;
    if (!this.timeoutForSongMap.has(key)) {
      this.timeoutForSongMap.set(key, true);
      this.goToNextSong();
    }
  }

  private printSongUrl(song: Song) {
    console.log(song.getLink());
    console.log(song.title);
  }

  private goToNextSong() {
    ++this.i;
    if (this.totalSteps > 0) {
      this.progressValue = this.i / this.totalSteps * 100;
    }
    this.checkYouTubeVideo();
  }

  private getCurrentSong() {
    const i = this.i;
    if (i >= 0 && i < this.songTitles.length) {
      return this.songTitles[i];
    }
    return undefined;
  }

}