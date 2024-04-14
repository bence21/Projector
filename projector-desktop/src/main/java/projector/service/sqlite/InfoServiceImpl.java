package projector.service.sqlite;

import projector.model.sqlite.Info;
import projector.repository.DAOFactory;

import java.util.List;

public class InfoServiceImpl extends AbstractBaseService<Info> implements InfoService {

    public InfoServiceImpl() {
        super(DAOFactory.getInstance().getInfoDAO());
    }

    @Override
    public String getDescription() {
        List<Info> info = findAll();
        for (Info anInfo : info) {
            if (anInfo.getName().trim().equalsIgnoreCase("description")) {
                return anInfo.getValue();
            }
        }
        return "";
    }
}