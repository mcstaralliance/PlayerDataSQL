package cc.bukkitPlugin.pds.util.stream;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

public class ByteArrayDataInputStream extends DataInputStream {

    public ByteArrayDataInputStream(byte[] pData) {
        super(new ByteArrayInputStream(pData));
    }

}
