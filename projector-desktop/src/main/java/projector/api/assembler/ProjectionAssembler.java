package projector.api.assembler;

import com.bence.projector.common.dto.ProjectionDTO;
import projector.model.Bible;
import projector.model.VerseIndex;

import java.util.ArrayList;
import java.util.List;

public class ProjectionAssembler {

    private static ProjectionAssembler instance;

    private ProjectionAssembler() {
    }

    public static ProjectionAssembler getInstance() {
        if (instance == null) {
            instance = new ProjectionAssembler();
        }
        return instance;
    }

    public void setVerseIndices(ProjectionDTO projectionDTO, List<VerseIndex> indices) {
        if (indices != null) {
            List<Long> verseIndices = new ArrayList<>(indices.size());
            for (VerseIndex verseIndex : indices) {
                verseIndices.add(verseIndex.getIndexNumber());
            }
            projectionDTO.setVerseIndices(verseIndices);
        }
    }

    public void setSelectedBible(ProjectionDTO projectionDTO, Bible bible) {
        projectionDTO.setSelectedBibleUuid(bible.getUuid());
        projectionDTO.setSelectedBibleName(bible.getName());
    }

    public void setSelectedVerses(ProjectionDTO projectionDTO, int selectedBook, int selectedPart, List<Integer> verseIndicesByPart) {
        projectionDTO.setSelectedBook(selectedBook);
        projectionDTO.setSelectedPart(selectedPart);
        projectionDTO.setVerseIndicesByPart(verseIndicesByPart);
    }
}
