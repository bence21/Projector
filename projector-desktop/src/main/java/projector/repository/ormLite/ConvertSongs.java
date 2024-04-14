package projector.repository.ormLite;

import projector.model.Song;
import projector.model.SongBook;
import projector.model.SongVerse;
import projector.repository.RepositoryException;
import projector.repository.txt.SongBookDAOImpl;
import projector.repository.txt.SongDAOImpl;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class ConvertSongs {
    public static void main(String[] args) {
    }

    public static void convertToDatabase() {
        SongDAOImpl songDAOTxt = new SongDAOImpl();
        Date date = new Date();
        final List<Song> songs = songDAOTxt.findAll();
        Date date2 = new Date();
        System.out.println(date2.getTime() - date.getTime());
        SongRepositoryImpl songRepository;
        try {
            songRepository = new SongRepositoryImpl();
            try {
                date = new Date();
                removeRepeatedSongVerse(songs);
                songRepository.create(songs);
                final List<Song> all = songRepository.findAll();
                System.out.println("all = " + all.size());
                SongBookDAOImpl songBookDAO = new SongBookDAOImpl();
                final List<SongBook> songBooks = songBookDAO.findAll();
                SongBookRepositoryImpl songBookRepository = new SongBookRepositoryImpl();
//				for (SongBook songBook : songBooks) {
//					for (Song song : songBook.getSongs()) {
//						song.setSongBooks(new ArrayList<>());
//					}
//				}
//				for (SongBook songBook : songBooks) {
//					for (Song song : songBook.getSongs()) {
//						song.getSongBooks().add(songBook);
//					}
//				}
                songBookRepository.create(songBooks);
//				for (SongBook songBook : songBooks) {
//					songRepository.update(songBook.getSongs());
//				}
                date2 = new Date();
                System.out.println(date2.getTime() - date.getTime());
            } catch (RepositoryException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void removeRepeatedSongVerse(List<Song> songs) {
        for (Song song : songs) {
            final List<SongVerse> verses = song.getVerses();
            for (int i = verses.size() - 1; i >= 0; --i) {
                if (verses.get(i).isRepeated()) {
                    verses.remove(i);
                }
            }
        }
    }
}
