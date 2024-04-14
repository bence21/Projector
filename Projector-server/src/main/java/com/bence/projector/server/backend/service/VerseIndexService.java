package com.bence.projector.server.backend.service;

import com.bence.projector.server.backend.model.VerseIndex;

import java.util.List;

public interface VerseIndexService {
    void save(List<VerseIndex> verseIndices);
}
