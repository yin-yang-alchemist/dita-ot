/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2017 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.module.reader;

import static org.dita.dost.util.Constants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import org.dita.dost.TestUtils;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.reader.GenListModuleReader;
import org.dita.dost.store.StreamStore;
import org.dita.dost.util.Job;
import org.dita.dost.util.XMLUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.xml.sax.SAXException;

public class TopicReaderModuleTest {

  @TempDir
  public File tempDir;

  private TopicReaderModule reader;

  @BeforeEach
  public void setUp() throws SAXException, IOException {
    reader = new TopicReaderModule();
    reader.setLogger(new TestUtils.TestLogger());
    final Job job = new Job(tempDir, new StreamStore(tempDir, new XMLUtils()));
    job.setInputFile(URI.create("file:///foo/bar/baz.ditamap"));
    job.setInputMap(URI.create("baz.ditamap"));
    job.setInputDir(URI.create("file:///foo/bar/"));
    job.add(
      new Job.FileInfo.Builder()
        .src(URI.create("file:///foo/bar/baz.ditamap"))
        .uri(URI.create("baz.ditamap"))
        .isInput(true)
        .build()
    );
    reader.setJob(job);
    final PipelineHashIO input = new PipelineHashIO();
    input.setAttribute(ANT_INVOKER_EXT_PARAM_DITADIR, tempDir.getAbsolutePath());
    input.setAttribute(ANT_INVOKER_EXT_PARAM_GENERATECOPYOUTTER, "1");
    input.setAttribute(ANT_INVOKER_EXT_PARAM_OUTTERCONTROL, Job.OutterControl.FAIL.toString());
    input.setAttribute(ANT_INVOKER_EXT_PARAM_CRAWL, "topic");
    input.setAttribute(ANT_INVOKER_EXT_PARAM_OUTPUTDIR, tempDir.getAbsolutePath());
    input.setAttribute(ANT_INVOKER_PARAM_PROFILING_ENABLED, Boolean.FALSE.toString());
    reader.parseInputParameters(input);
    reader.init();
    reader.initFilters();
  }

  @Test
  public void categorizeReferenceFileTopic() throws Exception {
    reader.categorizeReferenceFile(new GenListModuleReader.Reference(URI.create("file:///foo/bar/baz.dita")));
    assertEquals(0, reader.htmlSet.size());
    assertEquals(1, reader.waitList.size());
  }

  @Test
  public void categorizeReferenceFileDitamap() throws Exception {
    reader.categorizeReferenceFile(
      new GenListModuleReader.Reference(URI.create("file:///foo/bar/baz.ditamap"), ATTR_FORMAT_VALUE_DITAMAP)
    );
    assertEquals(0, reader.htmlSet.size());
    assertEquals(0, reader.waitList.size());
  }

  @Test
  public void categorizeReferenceFileDitaval() throws Exception {
    reader.categorizeReferenceFile(
      new GenListModuleReader.Reference(URI.create("file:///foo/bar/baz.ditaval"), ATTR_FORMAT_VALUE_DITAVAL)
    );
    assertEquals(0, reader.htmlSet.size());
    assertEquals(0, reader.formatSet.size());
  }

  @Test
  public void categorizeReferenceFileHtml() throws Exception {
    reader.categorizeReferenceFile(
      new GenListModuleReader.Reference(URI.create("file:///foo/bar/baz.html"), ATTR_FORMAT_VALUE_HTML)
    );
    assertEquals(1, reader.htmlSet.size());
    assertEquals(0, reader.formatSet.size());
  }

  @Test
  public void categorizeReferenceFileImage() throws Exception {
    reader.categorizeReferenceFile(
      new GenListModuleReader.Reference(URI.create("file:///foo/bar/baz.jpg"), ATTR_FORMAT_VALUE_IMAGE)
    );
    assertEquals(0, reader.htmlSet.size());
    assertEquals(1, reader.formatSet.size());
  }
}
