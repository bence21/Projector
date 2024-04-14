//package projector.utils;
//
//public class ConvertSongs {
//
//    public ConvertSongs() {
////        final List<Song> songList = ServiceManager.getSongService().findAll();
////        final SongService songService = ServiceManager.getSongService();
////        ArrayList<com.bence.projector.backend.model.Song> convertedSongs = new ArrayList<>(songList.size());
////        for (Song song : songList) {
////            final com.bence.projector.backend.model.Song convertedSong = new com.bence.projector.backend.model.Song();
////            convertedSong.setTitle(song.getTitle());
////            ArrayList<SongVerse> convertedVerses = new ArrayList<>(song.getVerses().length);
////            for (projector.model.SongVerse songVerse : song.getVerses()) {
////                if (!songVerse.isRepeated()) {
////                    final SongVerse verse = new SongVerse();
////                    verse.setText(songVerse.getText());
////                    verse.setChorus(songVerse.isChorus());
////                    convertedVerses.add(verse);
////                }
////            }
////            convertedSong.setVerses(convertedVerses);
////            convertedSongs.add(convertedSong);
////        }
////        songService.create(convertedSongs);
//	}
//
//    public static void main(String[] args) {
//        new ConvertSongs();
//    }
//}
