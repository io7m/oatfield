/*
 * Copyright © 2022 Mark Raynsford <code@io7m.com> https://www.io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.io7m.oatfield.tests;

import com.io7m.oatfield.api.OFBundleIndexerConfiguration;
import com.io7m.oatfield.api.OFBundleIndexerFactoryType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class OFBundleIndexerContract
{
  private static final Logger LOG =
    LoggerFactory.getLogger(OFBundleIndexerContract.class);

  private OFBundleIndexerFactoryType indexers;
  private Path directory;
  private Path output;

  protected abstract OFBundleIndexerFactoryType indexers();

  @BeforeEach
  public final void setup()
    throws IOException
  {
    this.indexers =
      this.indexers();
    this.directory =
      OFTestDirectories.createTempDirectory();
    this.output =
      this.directory.resolve("main.jar");

    LOG.debug("output: {}", this.output);
  }

  @AfterEach
  public final void tearDown()
    throws IOException
  {
    OFTestDirectories.deleteDirectory(this.directory);
  }

  /**
   * Trying to index a nonexistent bundle fails.
   *
   * @throws Exception On errors
   */

  @Test
  public final void testIndexNonexistent()
    throws Exception
  {
    final var configuration =
      new OFBundleIndexerConfiguration(
        List.of(this.directory.resolve("nonexistent.jar")),
        this.directory.resolve("output.obr"),
        this.directory.toUri(),
        "Oatfield"
      );

    final var ex =
      assertThrows(IOException.class, () -> {
        try (var indexer = this.indexers.createIndexer(configuration)) {
          indexer.execute();
        }
      });
    assertTrue(ex.getMessage().contains("not a regular file"));
  }

  /**
   * Indexing a bundle works.
   *
   * @throws Exception On errors
   */

  @Test
  public final void testIndexWorks()
    throws Exception
  {
    final var obrFile =
      this.directory.resolve("output.obr");

    final var configuration =
      new OFBundleIndexerConfiguration(
        List.of(this.resourceOf("example0.jar")),
        obrFile,
        this.directory.toUri(),
        "Oatfield"
      );

    try (var indexer = this.indexers.createIndexer(configuration)) {
      indexer.execute();
    }
    assertTrue(Files.isRegularFile(obrFile));
  }

  private Path resourceOf(
    final String name)
    throws IOException
  {
    return OFTestDirectories.resourceOf(
      OFBundleIndexerContract.class,
      this.directory,
      name
    );
  }
}
