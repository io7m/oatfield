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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Functions to split text into 72 character lines fit for use in jar
 * manifests.
 */

public final class OFAttributeSplitting
{
  private OFAttributeSplitting()
  {

  }

  /**
   * Split the given text into one or more lines of no more than 72 characters,
   * with all but the first line indented with a single space.
   *
   * @param text The text
   *
   * @return The output lines
   */

  public static List<String> split(
    final String text)
  {
    Objects.requireNonNull(text, "text");

    final var codePoints =
      text.codePoints()
        .iterator();

    final var lines = new ArrayList<String>();
    final var lineBuffer = new StringBuilder(72);
    while (codePoints.hasNext()) {
      if (lineBuffer.length() == 72) {
        lines.add(lineBuffer.toString());
        lineBuffer.setLength(0);
        lineBuffer.append(' ');
      }

      final var codepoint = codePoints.nextInt();
      lineBuffer.appendCodePoint(codepoint);
    }

    if (lineBuffer.length() > 0) {
      lines.add(lineBuffer.toString());
    }

    return List.copyOf(lines);
  }
}
