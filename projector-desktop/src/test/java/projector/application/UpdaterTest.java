package projector.application;

import org.junit.Assert;
import org.junit.Test;

public class UpdaterTest {

    @Test
    public void getUrlTest() {
        Assert.assertEquals("http://localhost:8081/api/files/projectorUpdate41.zip", Updater.getInstance().getUrl(41));
    }
}