package com.example.yhmtmt.aws2;// automatically generated by the FlatBuffers compiler, do not modify

import com.google.flatbuffers.BaseVector;
import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Struct;

import java.nio.ByteBuffer;

@SuppressWarnings("unused")
public final class Radar extends Struct {
  public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
  public Radar __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public byte state() { return bb.get(bb_pos + 0); }
  public int range() { return bb.getInt(bb_pos + 4); }
  public int gain() { return bb.getInt(bb_pos + 8); }
  public byte gainmode() { return bb.get(bb_pos + 12); }
  public int rain() { return bb.getInt(bb_pos + 16); }
  public byte rainmode() { return bb.get(bb_pos + 20); }
  public int sea() { return bb.getInt(bb_pos + 24); }
  public byte seamode() { return bb.get(bb_pos + 28); }
  public int ifr() { return bb.getInt(bb_pos + 32); }
  public int spd() { return bb.getInt(bb_pos + 36); }

  public static int createRadar(FlatBufferBuilder builder, byte state, int range, int gain, byte gainmode, int rain, byte rainmode, int sea, byte seamode, int ifr, int spd) {
    builder.prep(4, 40);
    builder.putInt(spd);
    builder.putInt(ifr);
    builder.pad(3);
    builder.putByte(seamode);
    builder.putInt(sea);
    builder.pad(3);
    builder.putByte(rainmode);
    builder.putInt(rain);
    builder.pad(3);
    builder.putByte(gainmode);
    builder.putInt(gain);
    builder.putInt(range);
    builder.pad(3);
    builder.putByte(state);
    return builder.offset();
  }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

    public Radar get(int j) { return get(new Radar(), j); }
    public Radar get(Radar obj, int j) {  return obj.__assign(__element(j), bb); }
  }
}

