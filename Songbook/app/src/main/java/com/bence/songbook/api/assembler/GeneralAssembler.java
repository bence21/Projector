package com.bence.songbook.api.assembler;

import java.util.List;

public interface GeneralAssembler<M, D> {

    M createModel(D d);

    M updateModel(M m, D d);

    List<M> createModelList(final List<D> ds);
}
