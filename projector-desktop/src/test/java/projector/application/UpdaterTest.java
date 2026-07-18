package projector.application;

import org.junit.Assert;
import org.junit.Test;
import projector.Credentials;

public class UpdaterTest {

    @Test
    public void getUrlTest() {
        Assert.assertEquals(Credentials.BASE_URL + "/api/files/projectorUpdate41.zip", Updater.getInstance().getUrl(41));
    }
}
