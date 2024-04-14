package projector.application;

import org.junit.Assert;
import org.junit.Test;

public class UpdaterTest {

    @Test
    public void getUrlTest() {
        Assert.assertEquals(Updater.getInstance().getUrl(41), "http://localhost:8080/api/files/projectorUpdate41.zip");
    }
}