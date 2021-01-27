package cc.bukkitPlugin.pds.util.stream;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

public class ByteArrayDataOutputStream extends DataOutputStream {

    protected ByteArrayOutputStream mBAOStream;

    public ByteArrayDataOutputStream() {
        super(new ByteArrayOutputStream());

        this.mBAOStream = (ByteArrayOutputStream)this.out;
    }

    public synchronized byte toByteArray()[] {
        return this.mBAOStream.toByteArray();
    }

}
