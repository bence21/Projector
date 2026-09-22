import { DuplicateYoutubeIdsComponent } from './duplicate-youtube-ids.component';

describe('DuplicateYoutubeIdsComponent', () => {
  it('should expose song and watch links', () => {
    const component = new DuplicateYoutubeIdsComponent(
      { setTitle: () => undefined } as any,
      { getSongsContainingYouTube: () => ({ subscribe: () => undefined }) } as any
    );
    expect(component.songLink({ uuid: 'abc' })).toBe('/#/song/abc');
    expect(component.watchUrl('abcdefghijk')).toBe('https://www.youtube.com/watch?v=abcdefghijk');
  });
});
