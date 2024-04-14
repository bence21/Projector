package com.bence.projector.server.backend.service.impl;

import com.bence.projector.server.backend.model.VerseIndex;
import com.bence.projector.server.backend.repository.VerseIndexRepository;
import com.bence.projector.server.backend.service.VerseIndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VerseIndexServiceImpl implements VerseIndexService {

    @Autowired
    private VerseIndexRepository verseIndexRepository;

    @Override
    public void save(List<VerseIndex> verseIndices) {
        if (verseIndices == null) {
            return;
        }
        verseIndexRepository.saveAll(verseIndices);
    }
}
