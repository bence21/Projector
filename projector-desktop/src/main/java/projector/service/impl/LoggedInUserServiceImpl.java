package projector.service.impl;

import projector.model.LoggedInUser;
import projector.repository.DAOFactory;
import projector.repository.LoggedInUserRepository;
import projector.service.LoggedInUserService;

public class LoggedInUserServiceImpl extends AbstractBaseService<LoggedInUser> implements LoggedInUserService {

    private final LoggedInUserRepository loggedInUserRepository;

    public LoggedInUserServiceImpl() {
        super(DAOFactory.getInstance().getLoggedInUserDAO());
        loggedInUserRepository = DAOFactory.getInstance().getLoggedInUserDAO();
    }

    @Override
    public LoggedInUser findByEmail(String email) {
        return loggedInUserRepository.findByEmail(email);
    }
}
