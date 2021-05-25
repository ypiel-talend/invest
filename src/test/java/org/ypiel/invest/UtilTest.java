package org.ypiel.invest;

import static org.junit.jupiter.api.Assertions.*;

import static org.ypiel.invest.Util.loadFileFromResources;

import org.junit.jupiter.api.Test;

class UtilTest {

    @Test
    void loadFileFromResourcesTest() {
        final String content = loadFileFromResources("sql/test.txt");
        assertEquals("aaa\nbbb\nccc", content);
    }
}