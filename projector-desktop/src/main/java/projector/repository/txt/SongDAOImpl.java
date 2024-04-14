package projector.repository.txt;

import com.bence.projector.common.dto.SongFavouritesDTO;
import com.bence.projector.common.dto.SongViewsDTO;
import projector.model.Song;
import projector.model.SongVerse;
import projector.repository.RepositoryException;
import projector.repository.SongDAO;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class SongDAOImpl implements SongDAO {
    private static final String path = "songs.txt";

    @Override
    public List<Song> findAll() throws RepositoryException {
        FileInputStream fStream;
        try {
            fStream = new FileInputStream(path);
            BufferedReader br = new BufferedReader(new InputStreamReader(fStream, StandardCharsets.UTF_8));
            LinkedList<Song> songs = new LinkedList<>();
            br.mark(4);
            if ('\ufeff' != br.read()) {
                br.reset(); // not the BOM marker
            }
            String strLine;
            strLine = br.readLine();
            if (strLine == null) {
                throw new RepositoryException("Could not read all SongBooks");
            }
            while (!strLine.trim().startsWith("qqqqqqq") && !strLine.trim().startsWith("?qqqqqqq")) {
                strLine = br.readLine();
                if (strLine == null) {
                    break;
                }
            }
            while (strLine != null && strLine.trim().startsWith("qqqqqqq")
                    || ((strLine = br.readLine()) != null && strLine.trim().startsWith("qqqqqqq"))) {
                StringBuilder tpmFileText = new StringBuilder();
                tpmFileText.append(strLine).append(System.lineSeparator());
                strLine = br.readLine();
                tpmFileText.append(strLine).append(System.lineSeparator());
                Song tmpSong = new Song();
                List<SongVerse> tmpVerses = new ArrayList<>();
                tmpSong.setTitle(strLine);
                boolean isChorus;
                boolean wasChorus = false;
                SongVerse tmpChorus = null;
                while ((strLine = br.readLine()) != null && !strLine.trim().startsWith("qqqqqqq")) {
                    //noinspection StatementWithEmptyBody
                    while (strLine.trim().isEmpty() && (strLine = br.readLine()) != null) {

                    }
                    if (strLine != null && !strLine.trim().startsWith("qqqqqqq")) {
//                        tpmFileText += System.lineSeparator() + strLine + System.lineSeparator();
                        tpmFileText.append(System.lineSeparator()).append(strLine).append(System.lineSeparator());
                        if (strLine.toLowerCase().contains("chorus")) {
                            isChorus = true;
                        } else {
                            isChorus = false;
                            if (wasChorus) {
                                tmpVerses.add(tmpChorus);
                                SongVerse repeated = new SongVerse();
                                repeated.setRepeated(true);
                                repeated.setChorus(true);
                                repeated.setText(tmpChorus.getText());
                                tmpChorus = repeated;
                            }
                        }
                        if (strLine.toLowerCase().startsWith("chorus") || strLine.toLowerCase().startsWith("verse")
                                || strLine.toLowerCase().startsWith("slide")) {
                            strLine = br.readLine();
                            tpmFileText.append(strLine).append(System.lineSeparator());
                        }
                        StringBuilder tmpVers = new StringBuilder().append(strLine);
                        while ((strLine = br.readLine()) != null && !strLine.trim().isEmpty()) {
                            tmpVers.append("\n").append(strLine);
                            tpmFileText.append(strLine).append(System.lineSeparator());
                        }
                        if (isChorus) {
                            tmpChorus = new SongVerse();
                            tmpChorus.setText(tmpVers.toString());
                            tmpChorus.setChorus(true);
                        } else {
                            final SongVerse songVerse = new SongVerse();
                            songVerse.setText(tmpVers.toString());
                            tmpVerses.add(songVerse);
                        }
                        if (isChorus) {
                            wasChorus = true;
                        }
                    } else {
                        break;
                    }
                }
                if (wasChorus) {
                    tmpVerses.add(tmpChorus);
                }
                List<SongVerse> tmpStringVerses = new ArrayList<>(tmpVerses.size());
                tmpStringVerses.addAll(tmpVerses);
                tmpSong.setVerses(tmpStringVerses);
                tmpSong.setFileText(tpmFileText.toString());
                songs.add(tmpSong);
            }
            br.close();
            return songs;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RepositoryException("Could not read all Songs");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RepositoryException("e:Could not read all Songs");
        }
    }

    @Override
    public Song findById(Long id) throws RepositoryException {
        return null;
    }

    @Override
    public Song create(Song song) throws RepositoryException {
        return song;
    }

    @Override
    public List<Song> create(List<Song> models) throws RepositoryException {
        return models;
    }

    @Override
    public Song update(Song song) throws RepositoryException {
        return song;
    }

    @Override
    public boolean delete(Song song) throws RepositoryException {
        return false;
    }

    @Override
    public boolean deleteAll(List<Song> models) throws RepositoryException {
        return false;
    }

    @Override
    public void update(List<Song> models) {

    }

    @Override
    public Song findByUuid(String uuid) {
        return null;
    }

    @Override
    public Song findByTitle(String title) {
        return null;
    }

    @Override
    public List<Song> findAllByVersionGroup(String versionGroup) {
        return null;
    }

    @Override
    public void saveViews(List<SongViewsDTO> songViewsDTOS) {

    }

    @Override
    public void saveFavouriteCount(List<SongFavouritesDTO> songFavouritesDTOS) {

    }
}
