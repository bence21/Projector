package projector.service.sqlite;

import projector.model.sqlite.Verses;
import projector.repository.DAOFactory;

public class VersesServiceImpl extends AbstractBaseService<Verses> implements VersesService {

    public VersesServiceImpl() {
        super(DAOFactory.getInstance().getVersesDAO());
    }
}