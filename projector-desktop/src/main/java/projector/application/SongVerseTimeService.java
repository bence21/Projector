package projector.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.model.Song;
import projector.utils.AppProperties;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class SongVerseTimeService {

    private static final Logger LOG = LoggerFactory.getLogger(SongVerseTimeService.class);

    private static SongVerseTimeService instance;
    private boolean lastVersionWasSaved;
    private List<SongVerseTime> songVerseTimes;
    private final HashMap<String, List<SongVerseTime>> uuidSongVerseTimes = new HashMap<>();
    private final HashMap<Long, List<SongVerseTime>> idSongVerseTimes = new HashMap<>();
    private final HashMap<String, List<SongVerseTime>> titleSongVerseTimes = new HashMap<>();

    private SongVerseTimeService() {
        FileInputStream fStream;
        try {
            fStream = new FileInputStream(getSongVersTimesFilePath());
            BufferedReader br = new BufferedReader(new InputStreamReader(fStream, StandardCharsets.UTF_8));
            br.mark(4);
            if ('\ufeff' != br.read()) {
                br.reset(); // not the BOM marker
            }
            String strLine;
            DateFormat formatter = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy");
            songVerseTimes = new LinkedList<>();
            Date date = null;
            while (br.ready() && (strLine = br.readLine()) != null) {
                try {
                    date = formatter.parse(strLine);
                    readTitleAndTimes(br, strLine, date);
                    break;
                } catch (ParseException ignored) {
                }
            }
            while (br.ready() && (strLine = br.readLine()) != null) {
                if (strLine.trim().isEmpty()) {
                    continue;
                }
                try {
                    date = formatter.parse(strLine);
                } catch (ParseException e) {
                    if (readTitleAndTimes(br, strLine, date)) break;
                }
            }
            br.close();
        } catch (FileNotFoundException ignored) {
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            songVerseTimes = new LinkedList<>();
            SongVerseTime tmp = new SongVerseTime("", 0);
            songVerseTimes.add(tmp);
        }
    }

    @SuppressWarnings("SpellCheckingInspection")
    public static String getSongVersTimesFilePath() {
        return AppProperties.getInstance().getWorkDirectory() + "songVersTimes";
    }

    public synchronized static SongVerseTimeService getInstance() {
        if (instance == null) {
            instance = new SongVerseTimeService();
        }
        return instance;
    }

    private boolean readTitleAndTimes(BufferedReader br, String titleLine, Date date) throws IOException {
        if (titleLine.equals(getLastVersionLine())) {
            setLastVersionWasSaved(true);
            titleLine = br.readLine();
        }
        String strLine2 = br.readLine();
        if (strLine2 == null) {
            return true;
        }
        String[] split = strLine2.split(" ");
        try {
            SongVerseTime songVerseTime = new SongVerseTime(titleLine, split.length);
            songVerseTime.setDate(date);
            double[] times = songVerseTime.getVerseTimes();
            for (int i = 0; i < split.length; ++i) {
                times[i] = Double.parseDouble(split[i]);
            }
            if (lastVersionWasSaved) {
                songVerseTime.setSongUuid(br.readLine());
                songVerseTime.setSongId(getParseLong(br));
                songVerseTime.setSongTextLength(getParseInt(br));
                addToHashMap(songVerseTime);
            }
            songVerseTimes.add(songVerseTime);
        } catch (NumberFormatException ignored) {
        }
        return false;
    }

    private void addToHashMap(SongVerseTime songVerseTime) {
        addToStringKeyHashMap(songVerseTime, songVerseTime.getSongUuid(), uuidSongVerseTimes);
        Long id = songVerseTime.getSongId();
        if (id != null) {
            List<SongVerseTime> verseTimeList;
            if (idSongVerseTimes.containsKey(id)) {
                verseTimeList = idSongVerseTimes.get(id);
            } else {
                verseTimeList = new ArrayList<>();
                idSongVerseTimes.put(id, verseTimeList);
            }
            verseTimeList.add(songVerseTime);
        }
        addToStringKeyHashMap(songVerseTime, songVerseTime.getSongTitle(), titleSongVerseTimes);
    }

    private void addToStringKeyHashMap(SongVerseTime songVerseTime, String key, HashMap<String, List<SongVerseTime>> hashMap) {
        if (key != null && !key.isEmpty()) {
            List<SongVerseTime> verseTimeList;
            if (hashMap.containsKey(key)) {
                verseTimeList = hashMap.get(key);
            } else {
                verseTimeList = new ArrayList<>();
                hashMap.put(key, verseTimeList);
            }
            verseTimeList.add(songVerseTime);
        }
    }

    private static int getParseInt(BufferedReader br) {
        try {
            return Integer.parseInt(br.readLine());
        } catch (Exception e) {
            return 0;
        }
    }

    private static long getParseLong(BufferedReader br) {
        try {
            return Long.parseLong(br.readLine());
        } catch (Exception e) {
            return 0;
        }
    }

    public synchronized double[] getAverageTimes(Song song, int n) {
        if (songVerseTimes == null) {
            return new double[0];
        }
        List<SongVerseTime> verseTimeList = getTimesById(song, n);
        if (verseTimeList == null || verseTimeList.isEmpty()) {
            verseTimeList = getTimesByUuid(song, n);
            if (verseTimeList == null || verseTimeList.isEmpty()) {
                verseTimeList = getTimesByTitle(song, n);
            }
        }
        if (verseTimeList != null && !verseTimeList.isEmpty()) {
            double[] times = new double[n];
            int[] counts = initializeCounts(times);
            for (SongVerseTime songVerseTime : verseTimeList) {
                sumAndCount(songVerseTime, times, counts);
            }
            return averageByCounts(times, counts);
        }
        return getOldAverageTimes(song);
    }

    private static int[] initializeCounts(double[] times) {
        int[] counts = new int[times.length];
        for (int j = 0; j < times.length; ++j) {
            counts[j] = 1;
        }
        return counts;
    }

    private static void sumAndCount(SongVerseTime songVerseTime, double[] times, int[] counts) {
        double[] verseTimes = songVerseTime.getVerseTimes();
        for (int j = 0; j < verseTimes.length && j < times.length; ++j) {
            times[j] += verseTimes[j];
            ++counts[j];
        }
    }

    private double[] getOldAverageTimes(Song song) {
        int i = songVerseTimes.size() - 1;
        String title = song.getTitle();
        while (i >= 0 && !songVerseTimeMatchCondition(title, songVerseTimes.get(i), song)) {
            --i;
        }
        if (i >= 0 && songVerseTimeMatchCondition(title, songVerseTimes.get(i), song)) {
            double[] times = songVerseTimes.get(i).getVerseTimes();
            int[] counts = initializeCounts(times);
            while (i > 0) {
                --i;
                SongVerseTime songVerseTime = songVerseTimes.get(i);
                if (songVerseTimeMatchCondition(title, songVerseTime, song)) {
                    sumAndCount(songVerseTime, times, counts);
                }
            }
            return averageByCounts(times, counts);
        }
        return null;
    }

    private static double[] averageByCounts(double[] times, int[] counts) {
        for (int j = 0; j < times.length; ++j) {
            times[j] /= counts[j];
        }
        return times;
    }

    private List<SongVerseTime> getTimesById(Song song, int n) {
        Long key = song.getId();
        return filterSongVerseTimesForN(n, idSongVerseTimes.get(key), song);
    }

    private List<SongVerseTime> getTimesByUuid(Song song, int n) {
        String key = song.getUuid();
        return filterSongVerseTimesForN(n, uuidSongVerseTimes.get(key), song);
    }

    private List<SongVerseTime> getTimesByTitle(Song song, int n) {
        String key = song.getTitle();
        return filterSongVerseTimesForN(n, titleSongVerseTimes.get(key), song);
    }

    private static List<SongVerseTime> filterSongVerseTimesForN(int n, List<SongVerseTime> verseTimeList, Song song) {
        if (verseTimeList == null) {
            return null;
        }
        List<SongVerseTime> filteredVerseTimeList = new ArrayList<>();
        int songTextLength = song.getVersesText().length();
        for (SongVerseTime songVerseTime : verseTimeList) {
            if (songVerseTime.getVerseTimes().length == n && songVerseTime.closeTextLength(songTextLength)) {
                filteredVerseTimeList.add(songVerseTime);
            }
        }
        return filteredVerseTimeList;
    }

    private boolean songVerseTimeMatchCondition(String title, SongVerseTime songVerseTime, Song song) {
        String uuid = song.getUuid();
        if (uuid != null && uuid.equals(songVerseTime.getSongUuid())) {
            return true;
        }
        Long id = song.getId();
        if (id != null && id.equals(songVerseTime.getSongId())) {
            return true;
        }
        String songTitle = songVerseTime.getSongTitle();
        return songTitle != null && songTitle.equals(title);
    }

    public boolean isLastVersionWasSaved() {
        return lastVersionWasSaved;
    }

    public void setLastVersionWasSaved(boolean lastVersionWasSaved) {
        this.lastVersionWasSaved = lastVersionWasSaved;
    }

    public String getLastVersionLine() {
        return "gCDRZnpdhR: SongVerseTime version: 2";
    }
}
