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

import aQute.bnd.osgi.repository.SimpleIndexer;
import com.io7m.oatfield.api.OFBundleIndexerConfiguration;
import com.io7m.oatfield.api.OFBundleIndexerType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * The default bundle indexer.
 */

public final class OFBundleIndexer implements OFBundleIndexerType
{
  private final OFBundleIndexerConfiguration configuration;

  /**
   * The default bundle indexer.
   *
   * @param inConfiguration The configuration
   */

  public OFBundleIndexer(
    final OFBundleIndexerConfiguration inConfiguration)
  {
    this.configuration =
      Objects.requireNonNull(inConfiguration, "configuration");
  }

  @Override
  public void execute()
    throws IOException
  {
    final var jars =
      this.configuration.files()
        .stream()
        .map(Path::toFile)
        .toList();

    for (final var file : this.configuration.files()) {
      if (!Files.isRegularFile(file)) {
        throw new IOException(
          "File '%s' is not a regular file.".formatted(file)
        );
      }
    }

    final var indexer =
      new SimpleIndexer()
        .name(this.configuration.name())
        .base(this.configuration.baseURI())
        .files(jars);

    indexer.index(this.configuration.outputFile().toFile());
  }

  @Override
  public void close()
  {

  }
}
