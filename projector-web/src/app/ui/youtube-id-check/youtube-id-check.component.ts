import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Song } from '../../services/song-service.service';
declare var YT: any; // Import the YouTube API

declare global {
  interface Window {
    onYouTubeIframeAPIReady?: () => void;
    player: any;
    onYouTubeIframeAPIReadyWas: boolean;
  }
}


export enum YoutubeIdCheckResultType {
  OK, BAD_URL, NOT_FOUND
}

export class YoutubeIdCheckResult {
  type: YoutubeIdCheckResultType;
  videoId: string;
  song: Song;
}

@Component({
  selector: 'app-youtube-id-check',
  templateUrl: './youtube-id-check.component.html',
  styleUrls: ['./youtube-id-check.component.css']
})
export class YoutubeIdCheckComponent implements OnInit {
  private _videoId: string = null;
  private player: any;
  private _song: Song = null;

  @Input()
  set videoId(videoId: string) {
    this._song = null; // so we know that it's not initialized with song
    this.setVideoId(videoId);
  }

  private setVideoId(videoId: string) {
    this._videoId = videoId;
    this.initializePlayer(videoId);
  }

  @Input()
  set song(song: Song) {
    this._song = song;
    if (song != undefined) {
      this.setVideoId(song.youtubeUrl);
    }
  }

  @Output()
  onResult: EventEmitter<YoutubeIdCheckResult> = new EventEmitter<YoutubeIdCheckResult>();

  constructor(
  ) { }

  ngOnInit() {
  }

  private initializePlayer(videoId: string) {
    // console.log(videoId);
    if (window.onYouTubeIframeAPIReadyWas) {
      this.createPlayer(videoId);
    } else {
      // console.log('window.onYouTubeIframeAPIReady');
      window.onYouTubeIframeAPIReady = () => {
        window.onYouTubeIframeAPIReadyWas = true;
        // console.log('new YT.Player');
        this.createPlayer(videoId);
      };
    }

    var tag = document.createElement('script');
    tag.src = "https://www.youtube.com/iframe_api";
    var firstScriptTag = document.getElementsByTagName('script')[0];
    firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);
  }

  private createPlayer(videoId: string) {
    // console.log('private createPlayer(videoId: string): ' + videoId);
    if (this.player && (this.player.loadVideoById) && (typeof this.player.loadVideoById === 'function')) {
      this.player.loadVideoById(videoId);
    } else {
      window.player = new YT.Player('playerDiv', {
        height: '390',
        width: '640',
        videoId: videoId,
        playerVars: {
          'playsinline': 1,
          'mute': 1
        },
        events: {
          'onReady': (_event: any) => this.onPlayerReady(),
          'onError': (_event: any) => this.onPlayerError(),
          'onStateChange': (_event: any) => this.onStateChange(),
        }
      });
      this.player = window.player;
    }
  }

  private getResult(type: YoutubeIdCheckResultType, videoId: string): YoutubeIdCheckResult {
    const result = new YoutubeIdCheckResult();
    result.type = type;
    result.videoId = videoId;
    return result;
  }

  private emitResult(type: YoutubeIdCheckResultType) {
    const videoId = this.player.getVideoData().video_id;
    // console.log('emitResult: ' + videoId);
    const result = this.getResult(type, videoId);
    result.song = this._song;
    this.onResult.emit(result);
  }

  onStateChange() {
    // console.log('onStateChange');
    this.emitResult(YoutubeIdCheckResultType.OK);
  }

  onPlayerReady() {
    // console.log('onPlayerReady');
    this.emitResult(YoutubeIdCheckResultType.OK);
  }

  onPlayerError() {
    // console.log('onPlayerError');
    // console.log(event);
    this.emitResult(YoutubeIdCheckResultType.NOT_FOUND);
  }

}