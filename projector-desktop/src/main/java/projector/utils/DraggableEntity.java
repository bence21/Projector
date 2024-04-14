package projector.utils;

import projector.model.BaseEntity;

public class DraggableEntity<T extends BaseEntity> {
    private T entity;
    private int listViewIndex;

    public DraggableEntity(T t) {
        entity = t;
    }

    public T getEntity() {
        return entity;
    }

    public void setEntity(T entity) {
        this.entity = entity;
    }

    public int getListViewIndex() {
        return listViewIndex;
    }

    public void setListViewIndex(int listViewIndex) {
        this.listViewIndex = listViewIndex;
    }
}
