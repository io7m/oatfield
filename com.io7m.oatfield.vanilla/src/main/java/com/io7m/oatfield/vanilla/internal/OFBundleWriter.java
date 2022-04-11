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

import com.io7m.oatfield.api.OFBundleContentStorageMethod;
import com.io7m.oatfield.api.OFBundleWriterConfiguration;
import com.io7m.oatfield.api.OFBundleWriterType;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import static java.util.zip.ZipEntry.DEFLATED;
import static java.util.zip.ZipEntry.STORED;

/**
 * The default bundle writer.
 */

public final class OFBundleWriter
  implements OFBundleWriterType
{
  private static final FileTime REPRODUCIBLE_TIME =
    FileTime.from(Instant.parse("2000-01-01T00:00:00Z"));

  private final OFBundleWriterConfiguration configuration;
  private final AtomicBoolean closed;
  private final TreeMap<String, String> headers;
  private final SortedSet<PackageImport> packageImports;
  private final SortedSet<PackageExport> packageExports;
  private final TreeMap<String, StoreFile> files;

  /**
   * The default bundle writer.
   *
   * @param inConfiguration The bundle configuration
   */

  public OFBundleWriter(
    final OFBundleWriterConfiguration inConfiguration)
  {
    this.configuration =
      Objects.requireNonNull(inConfiguration, "configuration");
    this.closed =
      new AtomicBoolean(false);
    this.headers =
      new TreeMap<>();
    this.packageImports =
      new TreeSet<>();
    this.packageExports =
      new TreeSet<>();
    this.files =
      new TreeMap<>();
  }

  private static String encodePackageExport(
    final PackageExport exportV)
  {
    final var builder = new StringBuilder(64);
    builder.append(exportV.name);
    exportV.version.ifPresent(v -> {
      builder.append(";version=\"");
      builder.append(v);
      builder.append("\"");
    });
    if (!exportV.uses.isEmpty()) {
      builder.append(";uses=\"");
      builder.append(String.join(",", exportV.uses));
      builder.append("\"");
    }
    return builder.toString();
  }

  private static String encodePackageImport(
    final PackageImport importV)
  {
    final var builder = new StringBuilder(64);
    builder.append(importV.name);
    importV.version.ifPresent(v -> {
      builder.append(";version=\"");
      builder.append(v);
      builder.append("\"");
    });
    return builder.toString();
  }

  private static void encodeEntry(
    final BufferedWriter writer,
    final Map.Entry<String, String> entry)
    throws IOException
  {
    final var notSplit =
      "%s: %s".formatted(entry.getKey(), entry.getValue());

    final var lines =
      OFAttributeSplitting.split(notSplit);

    for (final var line : lines) {
      writer.append(line);
      writer.append('\n');
    }
  }

  private void checkNotClosed()
  {
    if (this.closed.get()) {
      throw new IllegalStateException("Writer is closed!");
    }
  }

  @Override
  public void close()
    throws IOException
  {
    if (this.closed.compareAndSet(false, true)) {
      // Nothing to close
    }
  }

  /**
   * Start writing. Must be called exactly once.
   */

  public void start()
  {
    this.headers.put(
      "Bundle-SymbolicName", this.configuration.symbolicName());
    this.headers.put(
      "Bundle-Version", this.configuration.bundleVersion());
    this.headers.put(
      "Bundle-ManifestVersion", "2");
  }

  @Override
  public OFBundleWriterType addPackageImport(
    final String name,
    final Optional<String> version)
  {
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(version, "version");

    this.checkNotClosed();
    this.packageImports.add(new PackageImport(name, version));
    return this;
  }

  @Override
  public OFBundleWriterType addPackageExport(
    final String name,
    final Optional<String> version,
    final List<String> uses)
  {
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(version, "version");
    Objects.requireNonNull(uses, "uses");

    this.checkNotClosed();
    this.packageExports.add(
      new PackageExport(
        name,
        version,
        uses.stream().sorted().toList())
    );
    return this;
  }

  @Override
  public OFBundleWriterType addFile(
    final String name,
    final OFBundleContentStorageMethod storage,
    final Path file)
  {
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(storage, "storage");
    Objects.requireNonNull(file, "file");

    this.checkNotClosed();
    this.files.put(name, new StoreFile(name, file, storage));
    return this;
  }

  @Override
  public void execute()
    throws IOException
  {
    this.checkNotClosed();

    this.encodePackageImports();
    this.encodePackageExports();

    try (var zipOutput =
           new ZipOutputStream(
             new BufferedOutputStream(
               Files.newOutputStream(
                 this.configuration.outputFile(),
                 WRITE, TRUNCATE_EXISTING, CREATE)))) {

      final var e = new ZipEntry("META-INF/MANIFEST.MF");
      e.setLastModifiedTime(REPRODUCIBLE_TIME);
      e.setLastAccessTime(REPRODUCIBLE_TIME);
      e.setCreationTime(REPRODUCIBLE_TIME);
      zipOutput.putNextEntry(e);
      zipOutput.write(this.serializeManifest());
      zipOutput.closeEntry();

      for (final var entry : this.files.entrySet()) {
        serializeFile(zipOutput, entry.getValue());
      }

      zipOutput.flush();
      zipOutput.finish();
    }
  }

  private static void serializeFile(
    final ZipOutputStream zipOutput,
    final StoreFile value)
    throws IOException
  {
    final var e = new ZipEntry(value.name());
    e.setLastModifiedTime(REPRODUCIBLE_TIME);
    e.setLastAccessTime(REPRODUCIBLE_TIME);
    e.setCreationTime(REPRODUCIBLE_TIME);

    final var crc = new CRC32();
    final var buffer = new byte[8192];
    var size = 0L;
    try (var stream = Files.newInputStream(value.file)) {
      while (true) {
        final var r = stream.read(buffer);
        if (r == -1) {
          break;
        }
        size = size + (long) r;
        crc.update(buffer, 0, r);
      }
    }
    e.setCrc(crc.getValue());
    e.setSize(size);
    e.setMethod(
      switch (value.method) {
        case STORE_DEFLATED -> DEFLATED;
        case STORE_UNCOMPRESSED -> STORED;
      }
    );

    zipOutput.putNextEntry(e);
    try (var stream = Files.newInputStream(value.file)) {
      stream.transferTo(zipOutput);
    }
    zipOutput.closeEntry();
  }

  private void encodePackageExports()
  {
    if (this.packageExports.isEmpty()) {
      this.headers.remove("Export-Package");
    } else {
      this.headers.put(
        "Export-Package",
        this.packageExports.stream()
          .sorted()
          .map(OFBundleWriter::encodePackageExport)
          .collect(Collectors.joining(","))
      );
    }
  }

  private void encodePackageImports()
  {
    if (this.packageImports.isEmpty()) {
      this.headers.remove("Import-Package");
    } else {
      this.headers.put(
        "Import-Package",
        this.packageImports.stream()
          .sorted()
          .map(OFBundleWriter::encodePackageImport)
          .collect(Collectors.joining(","))
      );
    }
  }

  private byte[] serializeManifest()
    throws IOException
  {
    try (var outputStream = new ByteArrayOutputStream()) {
      try (var writer = new BufferedWriter(
        new OutputStreamWriter(outputStream, UTF_8))) {
        writer.append("Manifest-Version: 1.0");
        writer.append('\n');

        for (final var entry : this.headers.entrySet()) {
          encodeEntry(writer, entry);
        }

        writer.append('\n');
        writer.flush();
        return outputStream.toByteArray();
      }
    }
  }

  private record StoreFile(
    String name,
    Path file,
    OFBundleContentStorageMethod method)
  {

  }

  private record PackageImport(
    String name,
    Optional<String> version)
    implements Comparable<PackageImport>
  {
    @Override
    public int compareTo(
      final PackageImport other)
    {
      return Comparator.comparing(PackageImport::name)
        .thenComparing(o -> o.version.orElse(""))
        .compare(this, other);
    }
  }

  private record PackageExport(
    String name,
    Optional<String> version,
    List<String> uses)
    implements Comparable<PackageExport>
  {
    @Override
    public int compareTo(
      final PackageExport other)
    {
      return Comparator.comparing(PackageExport::name)
        .thenComparing(o -> o.version.orElse(""))
        .thenComparing(o -> o.uses.toString())
        .compare(this, other);
    }
  }
}
