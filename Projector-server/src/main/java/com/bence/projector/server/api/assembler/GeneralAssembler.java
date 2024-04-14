package com.bence.projector.server.api.assembler;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public interface GeneralAssembler<M, D> {

    D createDto(M m);

    M createModel(D d);

    M updateModel(M m, D d);

    default List<M> createModelList(final List<D> ds) {
        if (ds != null) {
            return ds.stream().map(this::createModel).collect(Collectors.toList());
        } else {
            return null;
        }
    }

    default List<D> createDtoList(final List<M> ms) {
        if (ms != null) {
            return ms.stream().map(this::createDto).collect(Collectors.toList());
        } else {
            return null;
        }
    }

    default List<D> createDtoList(final M[] ms) {
        if (ms != null) {
            List<D> dList = new ArrayList<>(ms.length);
            for (M m : ms) {
                dList.add(createDto(m));
            }
            return dList;
        } else {
            return null;
        }
    }
}
