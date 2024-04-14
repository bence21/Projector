package projector.repository.txt;

import projector.model.Song;
import projector.model.SongBook;
import projector.repository.RepositoryException;
import projector.repository.SongBookDAO;
import projector.service.ServiceManager;
import projector.service.SongService;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class SongBookDAOImpl implements SongBookDAO {

    private static final String path = "data/SongBook.txt";

    @Override
    public List<SongBook> findAll() throws RepositoryException {
        try {
            FileInputStream inputStream = new FileInputStream(path);
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            LinkedList<SongBook> songBooks = new LinkedList<>();
            br.mark(4);
            if ('\ufeff' != br.read()) {
                br.reset(); // not the BOM marker
            }
            String strLine;
            strLine = br.readLine();
            if (strLine == null) {
                throw new RepositoryException("Could not read SongBooks");
            }
            while (!strLine.trim().startsWith("qqqqqqq")) {
                strLine = br.readLine();
                if (strLine == null) {
                    break;
                }
            }
            SongService songService = ServiceManager.getSongService();
            List<Song> songs = songService.findAll();
            songs.sort(Comparator.comparing(Song::getTitle));
            while (strLine != null && strLine.trim().startsWith("qqqqqqq")
                    || ((strLine = br.readLine()) != null && strLine.trim().startsWith("qqqqqqq"))) {
                SongBook songBook = new SongBook();
                strLine = br.readLine();
                songBook.setUuid(strLine);
                strLine = br.readLine();
                songBook.setTitle(strLine);
                int n = Integer.parseInt(br.readLine());
                List<Song> songList = new ArrayList<>(n);
                List<String> songTitles = new ArrayList<>(n);
                for (int i = 0; i < n; ++i) {
                    songTitles.add(br.readLine());
                }
                Collections.sort(songTitles);
                int i = 0;
                for (String title : songTitles) {
                    Song song = songs.get(i++);
                    int compareTo = song.getTitle().compareTo(title);
                    while (compareTo < 0) {
                        if (i == songs.size()) {
                            break;
                        }
                        song = songs.get(i++);
                        compareTo = song.getTitle().compareTo(title);
                    }
                    if (compareTo == 0) {
                        songList.add(song);
                    }
                }

                songBook.setSongs(songList);
                songBooks.add(songBook);
            }
            br.close();
            return songBooks;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RepositoryException("Could not read SongBooks", e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RepositoryException("e:Could not read SongBooks", e);
        }
    }

    @Override
    public SongBook findById(Long id) throws RepositoryException {
        return null;
    }

    @Override
    public SongBook create(SongBook songBook) throws RepositoryException {
        FileOutputStream outputStream;
        try {
            File file = new File("data");
            if (!file.exists()) {
                try {
                    boolean mkdirs = file.mkdirs();
                    if (!mkdirs) {
                        throw new Exception();
                    }
                } catch (Exception e) {
                    throw new RepositoryException("Could not create new folder: data", e);
                }
            }
            outputStream = new FileOutputStream(path, true);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
            writer.write("qqqqqqq" + System.lineSeparator());
            writer.write(songBook.getUuid() + System.lineSeparator());
            writer.write(songBook.getTitle() + System.lineSeparator());
            List<Song> songs = songBook.getSongs();
            writer.write(songs.size() + System.lineSeparator());
            for (Song song : songs) {
                writer.write(song.getTitle() + System.lineSeparator());
            }
            writer.close();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            throw new RepositoryException("Could not creat SongBook", e);
        }
        return songBook;
    }

    @Override
    public List<SongBook> create(List<SongBook> models) throws RepositoryException {
        return models;
    }

    @Override
    public SongBook update(SongBook songBook) throws RepositoryException {
        List<SongBook> songBooks = findAll();
        boolean was = false;
        for (int i = 0; i < songBooks.size(); ++i) {
            SongBook book = songBooks.get(i);
            if (book.getUuid().equals(songBook.getUuid())) {
                songBooks.set(i, songBook);
                was = true;
                break;
            }
        }
        if (was) {
            try {
                File file = new File(path);
                if (!file.delete()) {
                    return null;
                }
            } catch (SecurityException x) {
                x.printStackTrace();
            }
            for (SongBook book : songBooks) {
                create(book);
            }
            return songBook;
        }
        return null;
    }

    @Override
    public boolean delete(SongBook songBook) throws RepositoryException {
        List<SongBook> songBooks = findAll();
        boolean was = false;
        for (SongBook book : songBooks) {
            if (book.getUuid().equals(songBook.getUuid())) {
                songBooks.remove(book);
                was = true;
                break;
            }
        }
        if (was) {
            try {
                File file = new File(path);
                if (!file.delete()) {
                    return false;
                }
            } catch (SecurityException x) {
                x.printStackTrace();
            }
            for (SongBook book : songBooks) {
                create(book);
            }
        }
        return true;
    }

    @Override
    public boolean deleteAll(List<SongBook> models) throws RepositoryException {
        return false;
    }

    @Override
    public void update(List<SongBook> models) {

    }

    @Override
    public SongBook findByUuid(String uuid) {
        return null;
    }
}
