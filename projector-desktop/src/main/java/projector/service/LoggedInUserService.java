package projector.service;

import projector.model.LoggedInUser;

public interface LoggedInUserService extends CrudService<LoggedInUser> {
    LoggedInUser findByEmail(String email);
}
