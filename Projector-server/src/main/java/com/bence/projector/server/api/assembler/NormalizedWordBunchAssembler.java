package com.bence.projector.server.api.assembler;

import com.bence.projector.common.dto.NormalizedWordBunchDTO;
import com.bence.projector.server.utils.models.NormalizedWordBunch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NormalizedWordBunchAssembler implements GeneralAssembler<NormalizedWordBunch, NormalizedWordBunchDTO> {

    private final WordBunchAssembler wordBunchAssembler;

    @Autowired
    public NormalizedWordBunchAssembler(WordBunchAssembler wordBunchAssembler) {
        this.wordBunchAssembler = wordBunchAssembler;
    }

    @Override
    public NormalizedWordBunchDTO createDto(NormalizedWordBunch normalizedWordBunch) {
        if (normalizedWordBunch == null) {
            return null;
        }
        NormalizedWordBunchDTO normalizedWordBunchDTO = new NormalizedWordBunchDTO();
        normalizedWordBunchDTO.setBestWord(normalizedWordBunch.getBestWord());
        normalizedWordBunchDTO.setRatio(normalizedWordBunch.getRatio());
        normalizedWordBunchDTO.setWordBunches(wordBunchAssembler.createDtoList(normalizedWordBunch.getWordBunches()));
        normalizedWordBunchDTO.setMaxBunch(wordBunchAssembler.createDto(normalizedWordBunch.getMaxBunch()));
        return normalizedWordBunchDTO;
    }

    @Override
    public NormalizedWordBunch createModel(NormalizedWordBunchDTO normalizedWordBunchDTO) {
        return null;
        // final NormalizedWordBunch normalizedWordBunch = new NormalizedWordBunch();
        // return updateModel(normalizedWordBunch, normalizedWordBunchDTO);
    }

    @Override
    public NormalizedWordBunch updateModel(NormalizedWordBunch normalizedWordBunch, NormalizedWordBunchDTO normalizedWordBunchDTO) {
        return normalizedWordBunch;
    }
}
