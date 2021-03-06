/*
 * Copyright © 2021 Mark Raynsford <code@io7m.com> http://io7m.com
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

package com.io7m.aradine.graph.vanilla.internal;

import com.io7m.aradine.graph.api.ARAudioGraphConnectionType;
import com.io7m.aradine.graph.api.ARAudioGraphNodeType;
import com.io7m.aradine.graph.vanilla.ARAudioGraph;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public abstract class ARGraphNode implements ARAudioGraphNodeType
{
  private final ARAudioGraph graph;
  private final UUID id;

  public ARGraphNode(
    final ARAudioGraph inGraph,
    final UUID inId)
  {
    this.graph =
      Objects.requireNonNull(inGraph, "graph");
    this.id =
      Objects.requireNonNull(inId, "id");
  }

  public final ARAudioGraph graph()
  {
    return this.graph;
  }

  @Override
  public final UUID id()
  {
    return this.id;
  }

  @Override
  public final boolean equals(
    final Object o)
  {
    if (this == o) {
      return true;
    }
    if (o == null || !Objects.equals(this.getClass(), o.getClass())) {
      return false;
    }
    final ARGraphNode that = (ARGraphNode) o;
    return this.id.equals(that.id);
  }

  @Override
  public final int hashCode()
  {
    return Objects.hash(this.id);
  }

  public abstract void onIncomingConnectionsChanged(
    Set<ARAudioGraphConnectionType> connections);
}
