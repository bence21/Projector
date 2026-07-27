package projector.controller.biblesearch;

import org.junit.Assert;
import org.junit.Test;
import projector.model.BibleVerse;
import projector.model.Book;
import projector.model.Chapter;

import java.util.ArrayList;
import java.util.List;

public class BibleContentSnapshotsTest {

    @Test
    public void snapshotCopiesListWithoutIterator() {
        List<String> source = new ArrayList<>();
        source.add("a");
        source.add("b");
        List<String> snapshot = copyLikeSnapshot(source);
        source.add("c");
        Assert.assertEquals(2, snapshot.size());
        Assert.assertEquals("a", snapshot.get(0));
    }

    @Test
    public void versesSnapshotHandlesEmptyChapter() {
        Chapter chapter = new Chapter();
        Assert.assertTrue(BibleContentSnapshots.verses(chapter).isEmpty());
    }

    private static <T> List<T> copyLikeSnapshot(List<T> source) {
        if (source == null || source.isEmpty()) {
            return List.of();
        }
        List<T> copy = new ArrayList<>(source.size());
        for (int i = 0; i < source.size(); i++) {
            copy.add(source.get(i));
        }
        return copy;
    }
}
