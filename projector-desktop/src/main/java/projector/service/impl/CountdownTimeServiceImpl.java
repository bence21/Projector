package projector.service.impl;

import projector.model.CountdownTime;
import projector.repository.DAOFactory;
import projector.service.CountdownTimeService;

public class CountdownTimeServiceImpl extends AbstractBaseService<CountdownTime> implements CountdownTimeService {

    public CountdownTimeServiceImpl() {
        super(DAOFactory.getInstance().getCountdownTimeDAO());
    }
}
