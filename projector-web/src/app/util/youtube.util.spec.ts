import {
  extractYouTubeVideoId,
  findSimilarSongsSharingYouTubeId,
  groupDuplicateYouTubeIds
} from './youtube.util';

describe('youtube.util', () => {
  describe('extractYouTubeVideoId', () => {
    it('extracts bare and watch URL ids', () => {
      expect(extractYouTubeVideoId('_MLXd_4tH48')).toBe('_MLXd_4tH48');
      expect(extractYouTubeVideoId('https://www.youtube.com/watch?v=_MLXd_4tH48')).toBe('_MLXd_4tH48');
    });
  });

  describe('groupDuplicateYouTubeIds', () => {
    it('returns empty for null or unique ids', () => {
      expect(groupDuplicateYouTubeIds(null)).toEqual([]);
      expect(groupDuplicateYouTubeIds([
        { id: '1', title: 'A', youtubeUrl: 'aaaaaaaaaaa' },
        { id: '2', title: 'B', youtubeUrl: 'bbbbbbbbbbb' }
      ])).toEqual([]);
    });

    it('groups shared ids and skips deleted songs', () => {
      const groups = groupDuplicateYouTubeIds([
        { id: '1', title: 'A', youtubeUrl: '_MLXd_4tH48' },
        { id: '2', title: 'B', youtubeUrl: 'https://youtu.be/_MLXd_4tH48' },
        { id: '3', title: 'C', youtubeUrl: '_MLXd_4tH48', deleted: true },
        { id: '4', title: 'D', youtubeUrl: 'ccccccccccc' }
      ]);
      expect(groups.length).toBe(1);
      expect(groups[0].youtubeId).toBe('_MLXd_4tH48');
      expect(groups[0].songs.map(s => s.id)).toEqual(['1', '2']);
    });
  });

  describe('findSimilarSongsSharingYouTubeId', () => {
    it('finds similar songs with the same YouTube id', () => {
      const song = { uuid: '5c88e6ea16d76100044cbd34', youtubeUrl: '_MLXd_4tH48' };
      const similar = [
        { uuid: '160ce13e-b648-4367-a38e-8cd27c184fb3', title: 'Nyisd meg szemed', youtubeUrl: '_MLXd_4tH48' },
        { uuid: 'other', title: 'Other', youtubeUrl: 'ddddddddddd' }
      ];
      const shared = findSimilarSongsSharingYouTubeId(song, similar);
      expect(shared.length).toBe(1);
      expect(shared[0].uuid).toBe('160ce13e-b648-4367-a38e-8cd27c184fb3');
    });

    it('returns empty when current song has no YouTube id', () => {
      expect(findSimilarSongsSharingYouTubeId({ uuid: '1' }, [{ uuid: '2', youtubeUrl: 'aaaaaaaaaaa' }]))
        .toEqual([]);
    });
  });
});
