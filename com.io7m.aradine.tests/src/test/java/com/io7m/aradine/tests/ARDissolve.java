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

import com.io7m.jsamplebuffer.vanilla.SampleBufferDouble;
import com.io7m.jsamplebuffer.xmedia.SXMSampleBuffers;

import javax.sound.sampled.AudioFileFormat;
import java.nio.file.Paths;

public final class ARDissolve
{
  private ARDissolve()
  {

  }

  public static void main(
    final String[] args)
    throws Exception
  {
    final var fileIn =
      Paths.get(args[0]);
    final var fileOut =
      Paths.get(args[1]);

    final var bufferIn =
      SXMSampleBuffers.readSampleBufferFromFile(
        fileIn,
        SampleBufferDouble::createWithHeapBuffer
      );

    final var frameData = new double[bufferIn.channels()];
    for (var frame = 0L; frame < bufferIn.frames(); ++frame) {
      final var probable =
        Math.sqrt((double) frame / (double) bufferIn.frames());

      bufferIn.frameGetExact(frame, frameData);
      for (int index = 0; index < frameData.length; ++index) {
        if (Math.random() < probable) {
          frameData[index] = 0.0;
        }
      }
      bufferIn.frameSetExact(frame, frameData);
    }

    SXMSampleBuffers.writeSampleBufferToFile(
      bufferIn,
      AudioFileFormat.Type.WAVE,
      fileOut
    );
  }
}
