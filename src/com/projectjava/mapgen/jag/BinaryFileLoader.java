package com.projectjava.mapgen.jag;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

public class BinaryFileLoader implements FileLoader {
    
    private Map<String, byte[]> cache = new HashMap();
    
    @Override
    public byte[] load(String file) {
        if(cache.containsKey(file)) {
            return cache.get(file);
        }
        try {
            FileInputStream fis = new FileInputStream(file);
            FileChannel channel = fis.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate((int) channel.size());
            channel.read(buffer);
            channel.close();
            fis.close();
            cache.put(file, buffer.array());
            return buffer.array();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}