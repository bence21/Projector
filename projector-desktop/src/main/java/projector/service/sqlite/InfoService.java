package projector.service.sqlite;

import projector.model.sqlite.Info;
import projector.service.CrudService;

public interface InfoService extends CrudService<Info> {
    String getDescription();
}
