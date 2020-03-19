// automatically generated by the FlatBuffers compiler, do not modify

package Filter.UIManagerMsg;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

@SuppressWarnings("unused")
public final class Position extends Struct {
  public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
  public Position __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public double lat() { return bb.getDouble(bb_pos + 0); }
  public double lon() { return bb.getDouble(bb_pos + 8); }
  public double alt() { return bb.getDouble(bb_pos + 16); }

  public static int createPosition(FlatBufferBuilder builder, double lat, double lon, double alt) {
    builder.prep(8, 24);
    builder.putDouble(alt);
    builder.putDouble(lon);
    builder.putDouble(lat);
    return builder.offset();
  }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

    public Position get(int j) { return get(new Position(), j); }
    public Position get(Position obj, int j) {  return obj.__assign(__element(j), bb); }
  }
}

