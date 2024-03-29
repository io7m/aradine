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


package com.io7m.aradine.envelope.table1;

import static com.io7m.aradine.envelope.table1.AREnvelopeInterpolation.CONSTANT_CURRENT;

/**
 * A three-state envelope definition.
 *
 * @param attack  The initial attack state
 * @param sustain The cyclic sustain state
 * @param release The release state
 */

public record AREnvelopeADR(
  AREnvelopeTable attack,
  AREnvelopeTable sustain,
  AREnvelopeTable release)
{
  /**
   * A three-state envelope definition.
   *
   * @param attack  The initial attack state
   * @param sustain The cyclic sustain state
   * @param release The release state
   */

  public AREnvelopeADR
  {
    attack.setFirst(1.0, CONSTANT_CURRENT);
    sustain.setFirst(1.0, CONSTANT_CURRENT);
    release.setFirst(0.0, CONSTANT_CURRENT);
  }

  /**
   * Set the new sample rate.
   *
   * @param inSampleRate The new sample rate
   */

  public void setSampleRate(
    final long inSampleRate)
  {
    this.attack.setSampleRate(inSampleRate);
    this.sustain.setSampleRate(inSampleRate);
    this.release.setSampleRate(inSampleRate);
  }
}
