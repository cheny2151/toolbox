package cn.cheny.toolbox.POIUtils.utils;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class CloneUtils {

    private final static Map<Integer, Object> CACHE = new LinkedHashMap<>();

    private final static int MAX_CACHE_COUNT = 10;

    public CloneUtils() {
    }

    public static <T> T clone(T beCloned) {
        try {
            return readBytesToObject(writeObjectToBytes(beCloned));
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static <T> byte[] writeObjectToBytes(T beCloned) throws IOException {
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteOutputStream);
        objectOutputStream.writeObject(beCloned);
        byte[] bytes = byteOutputStream.toByteArray();
        CACHE.putIfAbsent(beCloned.hashCode(),bytes);
        return bytes;
    }

    private static <T> T readBytesToObject(byte[] bytes) throws IOException, ClassNotFoundException {
        ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(bytes));
        return (T) objectInputStream.readObject();
    }

}
