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

import com.io7m.aradine.instrument.sampler_xp0.ARIXP0SamplerFactory;
import com.io7m.aradine.instrument.spi1.ARI1ControlEventNoteOff;
import com.io7m.aradine.instrument.spi1.ARI1ControlEventNoteOn;
import com.io7m.aradine.instrument.spi1.ARI1ControlEventParameterSetSampleMap;
import com.io7m.aradine.instrument.spi1.ARI1ControlEventPitchBend;
import com.io7m.aradine.instrument.spi1.ARI1ControlEventType;
import com.io7m.aradine.instrument.spi1.ARI1ParameterId;
import com.io7m.aradine.instrument.spi1.ARI1ParameterRealType;
import com.io7m.aradine.instrument.spi1.ARI1ParameterSampleMapType;
import com.io7m.aradine.instrument.spi1.ARI1PortId;
import com.io7m.aradine.instrument.spi1.ARI1PortOutputSampledType;
import org.jaudiolibs.jnajack.Jack;
import org.jaudiolibs.jnajack.JackClient;
import org.jaudiolibs.jnajack.JackException;
import org.jaudiolibs.jnajack.JackMidi;
import org.jaudiolibs.jnajack.JackPort;
import org.jaudiolibs.jnajack.JackStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.jaudiolibs.jnajack.JackOptions.JackNoStartServer;
import static org.jaudiolibs.jnajack.JackPortFlags.JackPortIsInput;
import static org.jaudiolibs.jnajack.JackPortFlags.JackPortIsOutput;
import static org.jaudiolibs.jnajack.JackPortFlags.JackPortIsPhysical;
import static org.jaudiolibs.jnajack.JackPortType.AUDIO;
import static org.jaudiolibs.jnajack.JackPortType.MIDI;

public final class ARI1MiniJackHost
{
  private static final Logger LOG =
    LoggerFactory.getLogger(ARI1MiniJackHost.class);

  private ARI1MiniJackHost()
  {

  }

  public static void main(
    final String[] args)
    throws Exception
  {
    final var jack =
      Jack.getInstance();

    final var status =
      EnumSet.noneOf(JackStatus.class);

    final var client =
      jack.openClient("sampler0", EnumSet.of(JackNoStartServer), status);

    final var outL =
      client.registerPort("outL", AUDIO, JackPortIsOutput);
    final var outR =
      client.registerPort("outR", AUDIO, JackPortIsOutput);
    final var inM =
      client.registerPort("inM", MIDI, JackPortIsInput);

    final var samplers =
      new ARIXP0SamplerFactory();

    final var services =
      ARI1MiniInstrumentServices.create(
        samplers,
        client.getSampleRate(),
        client.getBufferSize()
      );

    client.setBuffersizeCallback((c, size) -> services.setBufferSize(size));
    client.setSampleRateCallback((c, rate) -> services.setSampleRate(rate));

    final var sampler =
      samplers.createInstrument(services);

    final var samplerOutL =
      (ARI1PortOutputSampled)
        services.declaredPort(
          new ARI1PortId(0),
          ARI1PortOutputSampledType.class
        );

    final var samplerOutR =
      (ARI1PortOutputSampled)
        services.declaredPort(
          new ARI1PortId(1),
          ARI1PortOutputSampledType.class
        );

    final var samplerMap =
      services.declaredParameter(
        new ARI1ParameterId(0),
        ARI1ParameterSampleMapType.class
      );

    final var parameterLoopPoint =
      services.declaredParameter(
        new ARI1ParameterId(1),
        ARI1ParameterRealType.class
      );

    final var messages =
      new ConcurrentLinkedQueue<ARI1ControlEventType>();

    messages.add(new ARI1ControlEventParameterSetSampleMap(
      0,
      samplerMap.id(),
      URI.create("file:///anything")
    ));

    client.setProcessCallback((c, nframes) -> {
      while (!messages.isEmpty()) {
        final var message = messages.poll();
        if (message != null) {
          sampler.receiveEvent(services, message);
        }
      }

      try {
        final var eventCount =
          JackMidi.getEventCount(inM);
        final var event =
          new JackMidi.Event();

        for (var index = 0; index < eventCount; ++index) {
          JackMidi.eventGet(event, inM, index);
          final var size = event.size();
          final var data = new byte[size];
          event.read(data);

          final var parsedEvent = parseEvent(data, event.time());
          if (parsedEvent == null) {
            continue;
          }
          sampler.receiveEvent(services, parsedEvent);
        }
      } catch (final JackException e) {
        throw new RuntimeException(e);
      }

      sampler.process(services);

      final var jackBufferL = outL.getFloatBuffer();
      final var jackBufferR = outR.getFloatBuffer();
      final var outBufferL = samplerOutL.buffer();
      final var outBufferR = samplerOutR.buffer();

      for (int index = 0; index < nframes; ++index) {
        jackBufferL.put(index, (float) outBufferL.get(index));
        jackBufferR.put(index, (float) outBufferR.get(index));
      }

      return true;
    });

    autoconnect(jack, client, List.of(outL, outR));

    client.activate();

    while (true) {
      try {
        Thread.sleep(100L);
      } catch (final InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  }

  private static ARI1ControlEventType parseEvent(
    final byte[] data,
    final int time)
  {
    if (data.length > 0) {
      final var status = (data[0] & 0b11110000) >>> 4;
      final var channel = (data[0] & 0b00001111);

      if (status == 9) {
        final var note = (int) data[1] & 0xff;
        final var velo = (int) data[2] & 0xff;
        final var velF = (double) velo / 127.0;
        return new ARI1ControlEventNoteOn(time, note, velF);
      }

      if (status == 8) {
        final var note = (int) data[1] & 0xff;
        final var velo = (int) data[2] & 0xff;
        final var velF = (double) velo / 127.0;
        return new ARI1ControlEventNoteOff(time, note, velF);
      }

      if (status == 14) {
        final var lsb = (int) data[1];
        final var msb = (int) data[2];
        final var val = (msb << 8) | lsb;
        final var valD = ((double) val) / 32768.0;
        final var valS = (valD * 2.0) - 1.0;
        return new ARI1ControlEventPitchBend(time, valS);
      }
    }

    return null;
  }

  private static void autoconnect(
    final Jack jack,
    final JackClient client,
    final List<JackPort> outputs)
    throws JackException
  {
    final var physical =
      jack.getPorts(
        client,
        null,
        AUDIO,
        EnumSet.of(JackPortIsInput, JackPortIsPhysical)
      );

    final var count = Math.min(outputs.size(), physical.length);
    for (var index = 0; index < count; index++) {
      jack.connect(client, outputs.get(index).getName(), physical[index]);
    }
  }
}
