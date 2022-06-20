/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2011 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.util;

import static org.junit.Assert.*;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.dita.dost.store.StreamStore;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.apache.commons.io.FileUtils;
import org.dita.dost.TestUtils;

public final class JobTest {

    private static final File resourceDir = TestUtils.getResourceDir(JobTest.class);
    private static final File srcDir = new File(resourceDir, "src");
    private static File tempDir;
    private static Job job;
    
    @BeforeClass
    public static void setUp() throws IOException {
        tempDir = TestUtils.createTempDir(JobTest.class);
        TestUtils.copy(srcDir, tempDir);
        job = new Job(tempDir, new StreamStore(tempDir, new XMLUtils()));
    }

    @Test
    public void testGetProperty() {
        assertEquals("/foo/bar", job.getProperty(INPUT_DIR));
        assertEquals("file:/foo/bar", job.getProperty(INPUT_DIR_URI));
    }

    @Test
    public void testSetProperty() {
        job.setProperty("foo", "bar");
        assertEquals("bar", job.getProperty("foo"));
    }

    @Test
    public void testGetFileInfo() throws URISyntaxException {
        final URI relative = new URI("foo/bar.dita");
        final URI absolute = tempDir.toURI().resolve(relative);
        final Job.FileInfo fi = new Job.FileInfo.Builder().uri(relative).build();
        job.add(fi);
        assertEquals(fi, job.getFileInfo(relative));
        assertEquals(fi, job.getFileInfo(absolute));
        assertNull(job.getFileInfo((URI) null));
    }

    @Test
    public void testGetInputMap() {
        assertEquals(toURI("foo"), job.getInputMap());
    }

    @Test
    public void testGetValue() throws URISyntaxException {
        assertEquals(new URI("file:/foo/bar"), job.getInputDir());
    }

    @Test
    @Ignore
    public void write_performance_large() throws IOException {
        for (int i = 0; i < 60_000; i++) {
            job.add(Job.FileInfo.builder()
                    .src(new File(tempDir, "topic_" + i + ".dita").toURI())
                    .uri(new File("topic_" + i + ".dita").toURI())
                    .result(new File(tempDir, "topic_" + i + ".html").toURI())
                    .format("dita")
                    .hasKeyref(true)
                    .hasLink(true)
                    .build());
        }
        final long start = System.currentTimeMillis();
        job.write();
        final long end = System.currentTimeMillis();
        System.out.println(((end - start)) + " ms");
    }
    
    @Test
    public void writeUTF8() throws Exception {
        //Reset default encoding field in order to re-compute it after setting the file.encoding system property.
        Field defaultCSField = Class.forName("java.nio.charset.Charset").getDeclaredField("defaultCharset");
        defaultCSField.setAccessible(true);
        defaultCSField.set(null, null);
        String initialEncoding = System.getProperty("file.encoding");
        try {
            System.getProperties().setProperty("file.encoding", "ASCII");
            job.add(Job.FileInfo.builder()
                    .src(new File(tempDir, "\u633F.dita").toURI())
                    .uri(new File("\u633F.dita").toURI())
                    .result(new File(tempDir, "\u633F.html").toURI())
                    .format("dita")
                    .hasKeyref(true)
                    .hasLink(true)
                    .build());
            job.write();
            File jobFile = new File(tempDir, ".job.xml");
            assertTrue(FileUtils.readFileToString(jobFile, StandardCharsets.UTF_8).contains("\u633F.dita"));
        } finally {
            System.getProperties().setProperty("file.encoding", initialEncoding);
        }
    }

    @AfterClass
    public static void tearDown() throws IOException {
        TestUtils.forceDelete(tempDir);
    }
    
}
