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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class OFBundleReaderContract
{
  private static final Logger LOG =
    LoggerFactory.getLogger(OFBundleReaderContract.class);

  private OFBundleReaderFactoryType readers;
  private Path directory;
  private Path output;

  protected abstract OFBundleReaderFactoryType readers();

  @BeforeEach
  public final void setup()
    throws IOException
  {
    this.readers =
      this.readers();
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
   * The example0 bundle is parsed correctly.
   *
   * @throws Exception On errors
   */

  @Test
  public final void testExample0()
    throws Exception
  {
    final var file =
      this.resourceOf("example0.jar");

    try (var reader = this.readers.createReader(file)) {
      assertEquals("com.io7m.oatfield.ex0", reader.bundleSymbolicName());
      assertEquals(Optional.of("1.0.0"), reader.bundleVersion());

      final var resource = reader.toResource();

      {
        final var cs = resource.getCapabilities("osgi.identity");
        assertEquals(1, cs.size());
        final var c = cs.get(0);
        final var cd = c.getDirectives();
        assertEquals(0, cd.size());
        final var ca = c.getAttributes();
        assertEquals(3, ca.size());
        assertEquals("com.io7m.oatfield.ex0", ca.get("osgi.identity"));
        assertEquals("osgi.bundle", ca.get("type"));
        assertEquals("1.0.0", ca.get("version").toString());
      }

      {
        final var cs = resource.getCapabilities("osgi.wiring.package");
        assertEquals(2, cs.size());

        {
          final var c = cs.get(0);
          final var cd = c.getDirectives();
          assertEquals(0, cd.size());
          final var ca = c.getAttributes();
          assertEquals(4, ca.size());
          assertEquals(
            "com.io7m.oatfield.ex0",
            ca.get("bundle-symbolic-name"));
          assertEquals("1.0.0", ca.get("bundle-version").toString());
          assertEquals(
            "com.io7m.oat",
            ca.get("osgi.wiring.package").toString());
          assertEquals("0.0.0", ca.get("version").toString());
        }

        {
          final var c = cs.get(1);
          final var cd = c.getDirectives();
          assertEquals(0, cd.size());
          final var ca = c.getAttributes();
          assertEquals(4, ca.size());
          assertEquals(
            "com.io7m.oatfield.ex0",
            ca.get("bundle-symbolic-name"));
          assertEquals("1.0.0", ca.get("bundle-version").toString());
          assertEquals(
            "com.io7m.oat.ex0",
            ca.get("osgi.wiring.package").toString());
          assertEquals("1.0.0", ca.get("version").toString());
        }
      }

      {
        final var cs = resource.getCapabilities("osgi.content");
        assertEquals(1, cs.size());
        final var c = cs.get(0);
        final var cd = c.getDirectives();
        assertEquals(0, cd.size());
        final var ca = c.getAttributes();
        assertEquals(4, ca.size());
        assertEquals(
          "CF4205D7EAE98E22468DD143D4A98FFF302313994390435FB9A4F9BE65F363A0",
          ca.get("osgi.content"));
        assertEquals("336", ca.get("size").toString());
        assertEquals("application/vnd.osgi.bundle", ca.get("mime"));
      }

      {
        final var rs = resource.getRequirements("osgi.wiring.package");
        assertEquals(5, rs.size());

        {
          final var r = rs.get(0);
          final var cd = r.getDirectives();
          assertEquals(1, cd.size());
          assertEquals(
            "(&(osgi.wiring.package=com.io7m.example0)(version>=1.0.0))",
            cd.get("filter"));
          final var ca = r.getAttributes();
          assertEquals(1, ca.size());
          assertEquals("com.io7m.example0", ca.get("osgi.wiring.package"));
        }

        {
          final var r = rs.get(1);
          final var cd = r.getDirectives();
          assertEquals(1, cd.size());
          assertEquals(
            "(&(osgi.wiring.package=com.io7m.example1)(version>=2.0.0))",
            cd.get("filter"));
          final var ca = r.getAttributes();
          assertEquals(1, ca.size());
          assertEquals("com.io7m.example1", ca.get("osgi.wiring.package"));
        }

        {
          final var r = rs.get(2);
          final var cd = r.getDirectives();
          assertEquals(1, cd.size());
          assertEquals(
            "(&(osgi.wiring.package=com.io7m.example2)(version>=3.0.0))",
            cd.get("filter"));
          final var ca = r.getAttributes();
          assertEquals(1, ca.size());
          assertEquals("com.io7m.example2", ca.get("osgi.wiring.package"));
        }

        {
          final var r = rs.get(3);
          final var cd = r.getDirectives();
          assertEquals(1, cd.size());
          assertEquals(
            "(osgi.wiring.package=java.lang)",
            cd.get("filter"));
          final var ca = r.getAttributes();
          assertEquals(1, ca.size());
          assertEquals("java.lang", ca.get("osgi.wiring.package"));
        }

        {
          final var r = rs.get(4);
          final var cd = r.getDirectives();
          assertEquals(1, cd.size());
          assertEquals(
            "(osgi.wiring.package=java.util)",
            cd.get("filter"));
          final var ca = r.getAttributes();
          assertEquals(1, ca.size());
          assertEquals("java.util", ca.get("osgi.wiring.package"));
        }
      }
    }
  }

  /**
   * Trying to use a closed reader fails.
   *
   * @throws Exception On errors
   */

  @Test
  public final void testReadClosed()
    throws Exception
  {
    final var file =
      this.resourceOf("example0.jar");

    final var reader =
      this.readers.createReader(file);

    reader.close();
    assertThrows(IllegalStateException.class, reader::toResource);
  }

  /**
   * Trying to open a nonexistent bundle fails.
   *
   * @throws Exception On errors
   */

  @Test
  public final void testReadNonexistent()
    throws Exception
  {
    assertThrows(IOException.class, () -> {
      this.readers.createReader(this.directory.resolve("nonexistent.jar"));
    });
  }

  /**
   * Trying to open something that isn't a bundle fails.
   *
   * @throws Exception On errors
   */

  @Test
  public final void testReadNotBundle()
    throws Exception
  {
    final var ex =
      assertThrows(IOException.class, () -> {
        this.readers.createReader(this.resourceOf("empty.jar"));
      });
    assertTrue(ex.getMessage().contains("does not contain a jar manifest"));
  }

  private Path resourceOf(
    final String name)
    throws IOException
  {
    return OFTestDirectories.resourceOf(
      OFBundleReaderContract.class,
      this.directory,
      name
    );
  }
}
