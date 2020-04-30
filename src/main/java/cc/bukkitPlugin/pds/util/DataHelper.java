package cc.bukkitPlugin.pds.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import cc.commons.util.tools.LimitedIterator;

public class DataHelper {

    public static <T> byte[] writeMapData(Collection<T> pContainer, BiConsumer<DataOutputStream, T> pCoder) throws IOException {
        ByteArrayOutputStream tBAOStream = new ByteArrayOutputStream();
        DataOutputStream tDOStream = new DataOutputStream(tBAOStream);

        tDOStream.write(pContainer.size());
        pContainer.forEach((entry) -> pCoder.accept(tDOStream, entry));

        tDOStream.flush();
        return tBAOStream.toByteArray();
    }

    public static <K, V> void readMapData(byte[] pData, Consumer<DataInputStream> pDeCoder) throws IOException {
        ByteArrayInputStream tBAIStream = new ByteArrayInputStream(pData);
        DataInputStream tDIStream = new DataInputStream(tBAIStream);

        LimitedIterator.c(tDIStream.read()).forEach(index -> pDeCoder.accept(tDIStream));
    }

    public static byte[] readBytes(DataInputStream pInput) {
        byte[] tData = new byte[0];
        try {
            tData = new byte[pInput.readInt()];
            pInput.readFully(tData);
        } catch (IOException ignore) {
        }
        return tData;
    }

    public static void writeBytes(DataOutputStream pOut, byte[] pData) {
        try {
            pOut.writeInt(pData.length);
            pOut.write(pData);
        } catch (IOException ignore) {
        }
    }

    public static String readStr(DataInputStream pInput) {
        return new String(readBytes(pInput), StandardCharsets.UTF_8);
    }

    public static void writeStr(DataOutputStream pOut, String pStr) {
        writeBytes(pOut, pStr.getBytes(StandardCharsets.UTF_8));
    }

}
