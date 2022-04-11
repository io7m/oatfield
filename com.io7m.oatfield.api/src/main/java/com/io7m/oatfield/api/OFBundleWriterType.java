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

package com.io7m.oatfield.api;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * The type of bundle writers.
 */

public interface OFBundleWriterType extends Closeable
{
  /**
   * Add a package import declaration.
   *
   * @param name    The name of the package
   * @param version The package version
   *
   * @return this
   */

  OFBundleWriterType addPackageImport(
    String name,
    Optional<String> version);

  /**
   * Add a package import declaration.
   *
   * @param name    The name of the package
   * @param version The package version
   *
   * @return this
   */

  default OFBundleWriterType addPackageImport(
    final String name,
    final String version)
  {
    return this.addPackageImport(name, Optional.of(version));
  }

  /**
   * Add a package import declaration.
   *
   * @param name The name of the package
   *
   * @return this
   */

  default OFBundleWriterType addPackageImport(
    final String name)
  {
    return this.addPackageImport(name, Optional.empty());
  }

  /**
   * Add a package export declaration.
   *
   * @param name    The name of the package
   * @param version The package version
   * @param uses    The list of "uses" for this package
   *
   * @return this
   */

  OFBundleWriterType addPackageExport(
    String name,
    Optional<String> version,
    List<String> uses);

  /**
   * Add a package export declaration.
   *
   * @param name    The name of the package
   * @param version The package version
   *
   * @return this
   */

  default OFBundleWriterType addPackageExport(
    final String name,
    final String version)
  {
    return this.addPackageExport(name, Optional.of(version), List.of());
  }

  /**
   * Add a package export declaration.
   *
   * @param name The name of the package
   *
   * @return this
   */

  default OFBundleWriterType addPackageExport(
    final String name)
  {
    return this.addPackageExport(name, Optional.empty(), List.of());
  }

  /**
   * Add a file to the bundle.
   *
   * @param name    The file name
   * @param storage The storage method
   * @param file    The source file
   *
   * @return this
   */

  OFBundleWriterType addFile(
    String name,
    OFBundleContentStorageMethod storage,
    Path file);

  /**
   * Execute the bundle writer, producing a bundle file.
   *
   * @throws IOException On I/O errors
   */

  void execute()
    throws IOException;
}
