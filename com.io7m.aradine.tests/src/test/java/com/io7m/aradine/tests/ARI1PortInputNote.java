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


package com.io7m.aradine.tests;

import com.io7m.aradine.instrument.spi1.ARI1EventBufferType;
import com.io7m.aradine.instrument.spi1.ARI1EventNoteType;
import com.io7m.aradine.instrument.spi1.ARI1PortId;
import com.io7m.aradine.instrument.spi1.ARI1PortInputNoteType;

import java.nio.DoubleBuffer;
import java.util.List;
import java.util.Objects;

public final class ARI1PortInputNote
  implements ARI1PortInputNoteType
{
  private final ARI1EventBufferType<ARI1EventNoteType> eventBuffer;
  private final ARI1PortId id;

  public ARI1PortInputNote(
    final ARI1PortId inId)
  {
    this.id =
      Objects.requireNonNull(inId, "inName");
    this.eventBuffer =
      new ARI1EventBuffer<>();
  }

  @Override
  public ARI1PortId id()
  {
    return this.id;
  }

  @Override
  public List<? extends ARI1EventNoteType> eventsTake(
    final int frameIndex)
  {
    return this.eventBuffer.eventsTake(frameIndex);
  }

  public void eventAdd(
    final ARI1EventNoteType event)
  {
    this.eventBuffer.eventAdd(event);
  }
}