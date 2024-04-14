package com.bence.projector.server.api.assembler;

import com.bence.projector.common.dto.StackDTO;
import com.bence.projector.server.backend.model.Stack;
import org.springframework.stereotype.Component;

@Component
public class StackAssembler implements GeneralAssembler<Stack, StackDTO> {

    @Override
    public StackDTO createDto(Stack stack) {
        if (stack == null) {
            return null;
        }
        StackDTO stackDTO = new StackDTO();
        stackDTO.setUuid(stack.getUuid());
        stackDTO.setCreatedDate(stack.getCreatedDate());
        stackDTO.setEmail(stack.getEmail());
        stackDTO.setMessage(stack.getMessage());
        stackDTO.setStackTrace(stack.getStackTrace());
        stackDTO.setVersion(stack.getVersion());
        stackDTO.setCount(stack.getCount());
        return stackDTO;
    }

    @Override
    public Stack createModel(StackDTO stackDTO) {
        final Stack stack = new Stack();
        stack.setCreatedDate(stackDTO.getCreatedDate());
        return updateModel(stack, stackDTO);
    }

    @Override
    public Stack updateModel(Stack stack, StackDTO stackDTO) {
        stack.setEmail(stackDTO.getEmail());
        stack.setMessage(stackDTO.getMessage());
        stack.setStackTrace(stackDTO.getStackTrace());
        stack.setVersion(stackDTO.getVersion());
        return stack;
    }
}
