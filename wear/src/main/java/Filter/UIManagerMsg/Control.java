// automatically generated by the FlatBuffers compiler, do not modify

package Filter.UIManagerMsg;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

@SuppressWarnings("unused")
public final class Control extends Struct {
  public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
  public Control __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public byte ctrlsrc() { return bb.get(bb_pos + 0); }
  public byte apmode() { return bb.get(bb_pos + 1); }
  public float eng() { return bb.getFloat(bb_pos + 4); }
  public float rud() { return bb.getFloat(bb_pos + 8); }
  public float cog() { return bb.getFloat(bb_pos + 12); }
  public float sog() { return bb.getFloat(bb_pos + 16); }
  public float rev() { return bb.getFloat(bb_pos + 20); }

  public static int createControl(FlatBufferBuilder builder, byte ctrlsrc, byte apmode, float eng, float rud, float cog, float sog, float rev) {
    builder.prep(4, 24);
    builder.putFloat(rev);
    builder.putFloat(sog);
    builder.putFloat(cog);
    builder.putFloat(rud);
    builder.putFloat(eng);
    builder.pad(2);
    builder.putByte(apmode);
    builder.putByte(ctrlsrc);
    return builder.offset();
  }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

    public Control get(int j) { return get(new Control(), j); }
    public Control get(Control obj, int j) {  return obj.__assign(__element(j), bb); }
  }
}

