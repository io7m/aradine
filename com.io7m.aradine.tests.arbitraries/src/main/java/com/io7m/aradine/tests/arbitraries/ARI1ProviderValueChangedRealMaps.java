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


package com.io7m.aradine.tests.arbitraries;

import net.jqwik.api.Arbitrary;
import net.jqwik.api.providers.ArbitraryProvider;
import net.jqwik.api.providers.TypeUsage;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A provider of values.
 */

public final class ARI1ProviderValueChangedRealMaps
  implements ArbitraryProvider
{
  /**
   * A provider of values.
   */

  public ARI1ProviderValueChangedRealMaps()
  {

  }

  @Override
  public boolean canProvideFor(
    final TypeUsage targetType)
  {
    return targetType.canBeAssignedTo(
      TypeUsage.of(
        Map.class,
        TypeUsage.forType(Integer.class),
        TypeUsage.forType(ARI1ValueChangedReal.class)
      )
    );
  }

  @Override
  public int priority()
  {
    return 100;
  }

  @Override
  public Set<Arbitrary<?>> provideFor(
    final TypeUsage targetType,
    final SubtypeProvider subtypeProvider)
  {
    return Set.of(get());
  }

  /**
   * @return An arbitrary instance
   */

  public static Arbitrary<Map<Integer, ARI1ValueChangedReal>> get()
  {
    return ARI1ProviderValueChangedReal.get()
      .list()
      .reduce(new HashMap<>(), (m, v) -> {
        m.put(Integer.valueOf(v.time()), v);
        return m;
      });
  }
}
