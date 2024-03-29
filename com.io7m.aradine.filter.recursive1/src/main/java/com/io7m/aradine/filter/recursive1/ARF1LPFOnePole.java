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

import java.nio.DoubleBuffer;

/**
 * A trivial, recursive, one-pole low pass filter. Filter instances are stateful
 * (they store the most recently processed output frame) and therefore each
 * instance should only be used on a single audio stream.
 *
 * @see "https://www.dspguide.com/ch19.htm"
 */

@ARIntroducesLatency(frames = 1)
public final class ARF1LPFOnePole
{
  private double cutoff;
  private double previous;
  private double a0;
  private double b1;

  /**
   * Create a new filter.
   */

  public ARF1LPFOnePole()
  {
    this.cutoff = 1.0;
    this.previous = 0.0;
  }

  /**
   * <p>Specify a new frequency cutoff value.</p>
   *
   * <p>This a value in the range
   * {@code [0, 1]} where {@code 0} effectively blocks all frequencies,
   * {@code 0.5} blocks roughly half of the higher frequency spectrum, and
   * {@code 1.0} blocks nothing.</p>
   *
   * @param newCutoff The cutoff
   */

  public void setCutoff(
    final @ARNormalizedUnsigned double newCutoff)
  {
    this.cutoff = Math.min(1.0, Math.max(0.0, newCutoff));

    /*
     * Filter coefficients for a trivial 1 pole LPF.
     */

    this.a0 = this.cutoff;
    this.b1 = 1.0 - this.cutoff;
  }

  /**
   * Apply a filter to the given frame in the input buffer, writing the filtered
   * frame to the corresponding frame of the output buffer.
   *
   * @param frame  The frame index
   * @param input  The input buffer
   * @param output The output buffer
   */

  public void processOneFrameBuffers(
    final int frame,
    final DoubleBuffer input,
    final DoubleBuffer output)
  {
    output.put(frame, this.processOneFrame(input.get(frame)));
  }

  /**
   * Process a single input frame.
   *
   * @param input The input frame
   *
   * @return The output frame
   */

  public double processOneFrame(
    final double input)
  {
    /*
     * Grab the current input frame, and the previous output frame.
     */

    final var out1 = this.previous;

    /*
     * Apply the filter kernel.
     */

    final var t0 = this.a0 * input;
    final var t1 = this.b1 * out1;
    final var out0 = t0 + t1;

    this.previous = out0;
    return out0;
  }
}
