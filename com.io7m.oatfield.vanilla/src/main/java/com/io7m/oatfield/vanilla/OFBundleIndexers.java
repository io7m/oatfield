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


package com.io7m.oatfield.vanilla;

import com.io7m.oatfield.api.OFBundleIndexerConfiguration;
import com.io7m.oatfield.api.OFBundleIndexerFactoryType;
import com.io7m.oatfield.api.OFBundleIndexerType;
import com.io7m.oatfield.vanilla.internal.OFBundleIndexer;

import java.util.Objects;

/**
 * The default factory of bundle indexers.
 */

public final class OFBundleIndexers implements OFBundleIndexerFactoryType
{
  /**
   * The default factory of bundle indexers.
   */

  public OFBundleIndexers()
  {

  }

  @Override
  public OFBundleIndexerType createIndexer(
    final OFBundleIndexerConfiguration configuration)
  {
    return new OFBundleIndexer(
      Objects.requireNonNull(configuration, "configuration")
    );
  }
}
