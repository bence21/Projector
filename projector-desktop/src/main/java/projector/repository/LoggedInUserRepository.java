package projector.repository;

import projector.model.LoggedInUser;

public interface LoggedInUserRepository extends CrudDAO<LoggedInUser> {
    LoggedInUser findByEmail(String email);
}
