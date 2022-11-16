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

package com.io7m.oatfield.vanilla.internal;

import aQute.bnd.osgi.resource.ResourceBuilder;
import com.io7m.oatfield.api.OFBundleReaderType;
import org.osgi.resource.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import static java.util.zip.ZipFile.OPEN_READ;

/**
 * The default bundle reader.
 */

public final class OFBundleReader
  implements OFBundleReaderType
{
  private final AtomicBoolean closed;
  private final Path path;
  private final TreeSet<String> files;
  private final SortedSet<String> filesRead;
  private JarFile jar;
  private Resource resource;
  private Manifest manifest;
  private Attributes attributes;

  /**
   * The default bundle reader.
   *
   * @param inputPath The input path
   */

  public OFBundleReader(
    final Path inputPath)
  {
    this.path =
      Objects.requireNonNull(inputPath, "inputPath");
    this.closed =
      new AtomicBoolean(false);
    this.files =
      new TreeSet<String>();
    this.filesRead =
      Collections.unmodifiableSortedSet(this.files);
  }

  private void checkNotClosed()
  {
    if (this.closed.get()) {
      throw new IllegalStateException("Reader is closed!");
    }
  }

  @Override
  public void close()
    throws IOException
  {
    if (this.closed.compareAndSet(false, true)) {
      this.jar.close();
    }
  }

  /**
   * Start reading. Must be called exactly once.
   * @throws Exception On errors
   */

  public void start()
    throws Exception
  {
    this.jar =
      new JarFile(this.path.toFile(), false, OPEN_READ);
    this.manifest =
      this.jar.getManifest();

    if (this.manifest == null) {
      throw new IOException(
        "File '%s' does not contain a jar manifest.".formatted(this.path)
      );
    }

    this.attributes =
      this.manifest.getMainAttributes();

    final var fileIter =
      this.jar.entries()
        .asIterator();

    while (fileIter.hasNext()) {
      this.files.add(fileIter.next().getName());
    }

    final var builder = new ResourceBuilder();
    builder.addFile(this.path.toFile(), this.path.toUri());
    this.resource = builder.build();
  }

  @Override
  public String bundleSymbolicName()
  {
    this.checkNotClosed();
    return this.attributes.getValue("Bundle-SymbolicName");
  }

  @Override
  public Optional<String> bundleVersion()
  {
    this.checkNotClosed();
    return Optional.ofNullable(
      this.attributes.getValue("Bundle-Version")
    );
  }

  @Override
  public Resource toResource()
  {
    this.checkNotClosed();
    return this.resource;
  }

  @Override
  public SortedSet<String> files()
  {
    this.checkNotClosed();
    return this.filesRead;
  }

  @Override
  public InputStream contentFor(
    final String name)
    throws IOException
  {
    Objects.requireNonNull(name, "name");
    this.checkNotClosed();

    final var entry = this.jar.getEntry(name);
    if (entry == null) {
      throw new NoSuchFileException(name);
    }
    return this.jar.getInputStream(entry);
  }
}
