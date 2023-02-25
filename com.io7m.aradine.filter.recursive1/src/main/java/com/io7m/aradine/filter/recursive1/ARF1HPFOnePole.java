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

package com.io7m.aradine.filter.recursive1;

import com.io7m.aradine.annotations.ARIntroducesLatency;
import com.io7m.aradine.annotations.ARNormalizedUnsigned;

/**
 * A trivial, recursive, one-pole high pass filter. Filter instances are
 * stateful (they store the most recently processed input and output frame) and
 * therefore each should therefore only be used on a single audio stream.
 *
 * @see "https://www.dspguide.com/ch19.htm"
 */

@ARIntroducesLatency(frames = 1)
public final class ARF1HPFOnePole implements ARF1FilterType
{
  private double cutoff;
  private double previousOut;
  private double previousIn;

  /**
   * Create a new filter.
   */

  public ARF1HPFOnePole()
  {
    this.cutoff = 1.0;
    this.previousOut = 0.0;
    this.previousIn = 0.0;
  }

  /**
   * <p>Specify a new frequency cutoff value.</p>
   *
   * <p>This a value in the range
   * {@code [0, 1]} where {@code 0} effectively blocks nothing, {@code 0.5}
   * blocks roughly half of the lower frequency spectrum, and {@code 1.0}
   * effectively blocks all frequencies.</p>
   *
   * @param newCutoff The cutoff
   */

  public void setCutoff(
    final @ARNormalizedUnsigned double newCutoff)
  {
    this.cutoff = Math.min(1.0, Math.max(0.0, newCutoff));
  }

  @Override
  public double processOneFrame(
    final double input)
  {
    /*
     * Filter coefficients for a trivial 1 pole HPF.
     */

    final var c = 1.0 - this.cutoff;
    final var a0 = (1.0 + c) / 2.0;
    final var a1 = -(1.0 + c) / 2.0;
    final var b1 = c;

    /*
     * Grab the current input frame, and the previous output frame.
     */

    final var in0 = input;
    final var in1 = this.previousIn;
    final var out1 = this.previousOut;

    /*
     * Apply the filter kernel.
     */

    final var t0 = a0 * in0;
    final var t1 = a1 * in1;
    final var t3 = b1 * out1;
    final var out0 = t0 + t1 + t3;

    this.previousOut = out0;
    this.previousIn = in0;
    return out0;
  }
}
