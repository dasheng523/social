package com.mengxinya.ys.task.com.mengxinya.ys.mock;

import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(EasyMockExtension.class)
public class MockTest extends EasyMockSupport {

    @Mock
    private TestInterface testInterface;

    @Test
    public void testAdd() {
        testInterface.add(1, 2);
    }
}
