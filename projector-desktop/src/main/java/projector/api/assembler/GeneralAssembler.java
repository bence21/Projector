package projector.api.assembler;

import java.util.List;
import java.util.stream.Collectors;

public interface GeneralAssembler<M, D> {

    D createDto(M m);

    M createModel(D d);

    M updateModel(M m, D d);

    List<M> createModelList(final List<D> ds);

    default List<D> createDtoList(final List<M> ms) {
        if (ms != null) {
            return ms.stream().map(this::createDto).collect(Collectors.toList());
        } else {
            return null;
        }
    }
}
