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

import com.io7m.oatfield.vanilla.internal.OFAttributeSplitting;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.StringLength;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class OFAttributeSpellingTest
{
  /**
   * Check that text is broken into lines of less than 73 characters.
   *
   * @param text The text
   */

  @Property
  public void testArbitraryText(
    @ForAll @StringLength(min = 0, max = 100 * 72) final String text)
  {
    final var lines =
      OFAttributeSplitting.split(text);

    final var lineCount =
      lines.size();
    final var expectedLines =
      (int) Math.ceil((double) text.length() / 72.0);

    assertTrue(lineCount >= expectedLines);

    for (final var line : lines) {
      assertTrue(line.length() <= 72);
    }
    for (int index = 1; index < lineCount; ++index) {
      assertEquals(' ', lines.get(index).charAt(0));
    }
  }
}
