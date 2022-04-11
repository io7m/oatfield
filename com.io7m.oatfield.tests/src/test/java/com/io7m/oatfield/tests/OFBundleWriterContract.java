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

import com.io7m.oatfield.api.OFBundleReaderFactoryType;
import com.io7m.oatfield.api.OFBundleWriterConfiguration;
import com.io7m.oatfield.api.OFBundleWriterFactoryType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

import static com.io7m.oatfield.api.OFBundleContentStorageMethod.STORE_DEFLATED;
import static com.io7m.oatfield.api.OFBundleContentStorageMethod.STORE_UNCOMPRESSED;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public abstract class OFBundleWriterContract
{
  private static final Logger LOG =
    LoggerFactory.getLogger(OFBundleWriterContract.class);

  private OFBundleWriterFactoryType writers;
  private OFBundleReaderFactoryType readers;
  private Path directory;
  private Path output;

  protected abstract OFBundleWriterFactoryType writers();

  protected abstract OFBundleReaderFactoryType readers();

  @BeforeEach
  public final void setup()
    throws IOException
  {
    this.writers =
      this.writers();
    this.readers =
      this.readers();
    this.directory =
      OFTestDirectories.createTempDirectory();
    this.output =
      this.directory.resolve("main.jar");

    LOG.debug("output: {}", this.output);
  }

  /**
   * A simple example works.
   *
   * @throws Exception On errors
   */

  @Test
  public final void testExample0()
    throws Exception
  {
    final var config =
      new OFBundleWriterConfiguration(
        this.output,
        "com.io7m.oatfield.ex0",
        "1.0.0"
      );

    try (var writer = this.writers.createWriter(config)) {
      writer.addPackageImport("com.io7m.example0", "1.0.0");
      writer.addPackageImport("com.io7m.example1", "2.0.0");
      writer.addPackageImport("com.io7m.example2", "3.0.0");
      writer.addPackageImport("java.lang");
      writer.addPackageImport("java.util");
      writer.addPackageExport("com.io7m.oat.ex0", "1.0.0");
      writer.addPackageExport("com.io7m.oat");
      writer.execute();
    }

    try (var reader = this.readers.createReader(this.output)) {
      assertEquals("com.io7m.oatfield.ex0", reader.bundleSymbolicName());
      assertEquals(Optional.of("1.0.0"), reader.bundleVersion());
    }
  }

  /**
   * The most minimal example works.
   *
   * @throws Exception On errors
   */

  @Test
  public final void testMinimal0()
    throws Exception
  {
    final var config =
      new OFBundleWriterConfiguration(
        this.output,
        "com.io7m.oatfield.ex0",
        "1.0.0"
      );

    try (var writer = this.writers.createWriter(config)) {
      writer.execute();
    }

    try (var reader = this.readers.createReader(this.output)) {
      assertEquals("com.io7m.oatfield.ex0", reader.bundleSymbolicName());
      assertEquals(Optional.of("1.0.0"), reader.bundleVersion());
    }
  }

  /**
   * Trying to use a closed writer fails.
   *
   * @throws Exception On errors
   */

  @Test
  public final void testWriteClosed()
    throws Exception
  {
    final var config =
      new OFBundleWriterConfiguration(
        this.output,
        "com.io7m.oatfield.ex0",
        "1.0.0"
      );

    final var writer = this.writers.createWriter(config);
    writer.close();
    assertThrows(IllegalStateException.class, writer::execute);
  }

  /**
   * Including deflated content works.
   *
   * @throws Exception On errors
   */

  @Test
  public final void testContentDeflated()
    throws Exception
  {
    final var config =
      new OFBundleWriterConfiguration(
        this.output,
        "com.io7m.oatfield.ex0",
        "1.0.0"
      );

    final var golden = this.resourceOf("golden.txt");
    try (var writer = this.writers.createWriter(config)) {
      writer.addFile("golden.txt", STORE_DEFLATED, golden);
      writer.execute();
    }

    try (var reader = this.readers.createReader(this.output)) {
      assertEquals("com.io7m.oatfield.ex0", reader.bundleSymbolicName());
      assertEquals(Optional.of("1.0.0"), reader.bundleVersion());

      assertEquals(
        Set.of("META-INF/MANIFEST.MF", "golden.txt"),
        reader.files()
      );

      final var expected =
        Files.readAllBytes(golden);
      final var received =
        reader.contentFor("golden.txt").readAllBytes();

      assertArrayEquals(expected, received);
    }
  }

  /**
   * Including stored content works.
   *
   * @throws Exception On errors
   */

  @Test
  public final void testContentStored()
    throws Exception
  {
    final var config =
      new OFBundleWriterConfiguration(
        this.output,
        "com.io7m.oatfield.ex0",
        "1.0.0"
      );

    final var golden = this.resourceOf("golden.txt");
    try (var writer = this.writers.createWriter(config)) {
      writer.addFile("golden.txt", STORE_UNCOMPRESSED, golden);
      writer.execute();
    }

    try (var reader = this.readers.createReader(this.output)) {
      assertEquals("com.io7m.oatfield.ex0", reader.bundleSymbolicName());
      assertEquals(Optional.of("1.0.0"), reader.bundleVersion());

      assertEquals(
        Set.of("META-INF/MANIFEST.MF", "golden.txt"),
        reader.files()
      );

      final var expected =
        Files.readAllBytes(golden);
      final var received =
        reader.contentFor("golden.txt").readAllBytes();

      assertArrayEquals(expected, received);
    }
  }

  private Path resourceOf(
    final String name)
    throws IOException
  {
    return OFTestDirectories.resourceOf(
      OFBundleWriterContract.class,
      this.directory,
      name
    );
  }
}
