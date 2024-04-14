package com.bence.projector.server.backend.model;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    public BaseEntity() {
    }

    public BaseEntity(BaseEntity baseEntity) {
        this.id = baseEntity.id;
    }

    public boolean isSameId(BaseEntity baseEntity) {
        return id != null && baseEntity != null && baseEntity.id != null && id.equals(baseEntity.id);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
