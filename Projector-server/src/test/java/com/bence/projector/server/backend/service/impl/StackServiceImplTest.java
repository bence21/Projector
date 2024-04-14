package com.bence.projector.server.backend.service.impl;

import com.bence.projector.server.backend.model.Stack;
import com.bence.projector.server.backend.service.StackService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class StackServiceImplTest extends BaseServiceTest {

    @Autowired
    private StackService stackService;

    @Test
    public void testSaveLob() {
        Stack stack = new Stack();
        String longText = getLongText();
        stack.setStackTrace(longText);
        stackService.save(stack);
        Stack stackServiceOne = stackService.findOne(stack.getId());
        Assert.assertEquals(longText, stackServiceOne.getStackTrace());
    }

    private String getLongText() {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < 10000; ++i) {
            s.append(i);
        }
        return s.toString();
    }
}