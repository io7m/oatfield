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

import org.osgi.resource.Requirement;

import java.util.List;
import java.util.Objects;

/**
 * A failure to resolve a set of requirements.
 */

public final class OFBundleResolutionException extends Exception
{
  private final List<Requirement> unmetRequirements;

  /**
   * A failure to resolve a set of requirements.
   *
   * @param inRequirements The unmet requirements
   */

  public OFBundleResolutionException(
    final List<Requirement> inRequirements)
  {
    this.unmetRequirements =
      List.copyOf(
        Objects.requireNonNull(inRequirements, "inRequirements"));
  }

  /**
   * A failure to resolve a set of requirements.
   *
   * @param cause          The root cause
   * @param inRequirements The unmet requirements
   */

  public OFBundleResolutionException(
    final Throwable cause,
    final List<Requirement> inRequirements)
  {
    super(Objects.requireNonNull(cause, "cause"));
    this.unmetRequirements =
      List.copyOf(
        Objects.requireNonNull(inRequirements, "inRequirements"));
  }

  /**
   * @return The unmet requirements
   */

  public List<Requirement> unmetRequirements()
  {
    return this.unmetRequirements;
  }
}