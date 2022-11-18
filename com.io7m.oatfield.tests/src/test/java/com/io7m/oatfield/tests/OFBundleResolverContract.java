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

import com.io7m.oatfield.api.OFBundleResolutionException;
import com.io7m.oatfield.api.OFBundleResolverConfiguration;
import com.io7m.oatfield.api.OFBundleResolverFactoryType;
import com.io7m.oatfield.vanilla.OFBundleReaders;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.resource.Requirement;
import org.osgi.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

public abstract class OFBundleResolverContract
{
  private static final Logger LOG =
    LoggerFactory.getLogger(OFBundleResolverContract.class);

  private OFBundleResolverFactoryType resolvers;
  private Path directory;
  private OFBundleReaders readers;

  protected abstract OFBundleResolverFactoryType resolvers();

  @BeforeEach
  public final void setup()
    throws IOException
  {
    this.readers =
      new OFBundleReaders();
    this.resolvers =
      this.resolvers();
    this.directory =
      OFTestDirectories.createTempDirectory();
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
  public final void testMandatoryNotPresent()
    throws Exception
  {
    final var unreachable =
      this.osgiResourceOf("com.io7m.junreachable.core-4.0.0.jar");

    final var configuration =
      new OFBundleResolverConfiguration(
        List.of(),
        List.of(unreachable),
        List.of()
      );

    final var ex =
      assertThrows(OFBundleResolutionException.class, () -> {
        this.resolvers.createResolver(configuration)
          .execute();
      });

    logFailure(ex.unmetRequirements());
  }

  private static void logFailure(
    final List<Requirement> requirements)
  {
    for (final var requirement : requirements) {
      LOG.debug("Unresolved requirement:");
      LOG.debug("  Resource: {}", requirement.getResource());
      requirement.getDirectives().forEach((name, value) -> {
        LOG.debug("  Unmet requirement: {} {}", name, value);
      });
      requirement.getAttributes().forEach((name, value) -> {
        LOG.debug("  Unmet requirement: {} {}", name, value);
      });
    }
  }

  private Resource osgiResourceOf(
    final String name)
    throws IOException
  {
    try (var reader =
           this.readers.createReader(this.resourceOf(name))) {
      return reader.toResource();
    }
  }

  private Path resourceOf(
    final String name)
    throws IOException
  {
    return OFTestDirectories.resourceOf(
      OFBundleResolverContract.class,
      this.directory,
      name
    );
  }
}
