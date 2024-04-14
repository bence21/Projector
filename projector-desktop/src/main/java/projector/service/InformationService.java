package projector.service;

import projector.model.Information;

public interface InformationService extends CrudService<Information> {

    Information findFirst() throws ServiceException;
}
