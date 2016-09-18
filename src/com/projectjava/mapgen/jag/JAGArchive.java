package com.projectjava.mapgen.jag;

import com.projectjava.mapgen.jag.bzip.BZip;

import java.nio.ByteBuffer;

public class JAGArchive implements FileLoader {

    private int numArchives;

    private JAGFile[] files;

    public JAGArchive(byte[] archive) {
        numArchives = (archive[0] & 0xff) * 256 + (archive[1] & 0xff);
        files = new JAGFile[numArchives];

        int offset = 2 + numArchives * 10;

        // extract all entries from the archive
        for(int i = 0; i < numArchives; i++) {
            int hash = (archive[i * 10 + 2] & 0xff) * 0x1000000 + (archive[i * 10 + 3] & 0xff) * 0x10000 + (archive[i * 10 + 4] & 0xff) * 256 + (archive[i * 10 + 5] & 0xff);
            int decompLen = (archive[i * 10 + 6] & 0xff) * 0x10000 + (archive[i * 10 + 7] & 0xff) * 256 + (archive[i * 10 + 8] & 0xff);
            int compLen = (archive[i * 10 + 9] & 0xff) * 0x10000 + (archive[i * 10 + 10] & 0xff) * 256 + (archive[i * 10 + 11] & 0xff);

            byte[] fileData = new byte[decompLen];
            if(decompLen != compLen) { // the entry is compressed
                BZip.decompress(fileData, decompLen, archive, compLen, offset);
            } else { // the entry is already decompressed
                System.arraycopy(archive, offset, fileData, 0, decompLen);
            }
            offset += compLen;
            files[i] = new JAGFile(hash, fileData);
        }
    }

    @Override
    public byte[] load(String file) {
        int hash = encodeFileName(file);
        for(JAGFile jfile : files) {
            if(jfile.getHash() == hash) {
                return jfile.getData();
            }
        }
        return null;
    }

    public JAGFile[] getFiles() {
        return files;
    }
    
    public static int encodeFileName(String file) {
        int hash = 0;
        file = file.toUpperCase();

        for(int i = 0; i < file.length(); i++) {
            hash = (hash * 61 + file.charAt(i)) - 32;
        }
        return hash;
    }

    // doesn't work (impossible) due to numeric overflow
    public static String decodeFileHash(int hash) {
        long realHash = (hash < 0 ? Integer.MAX_VALUE + Math.abs(hash) : hash);
        ByteBuffer buffer = ByteBuffer.allocate(50);

        while(realHash > 0) {
            buffer.put((byte) ((realHash % 61) + 32));
            realHash = (realHash - 32) / 61;
        }
        buffer.flip();
        return new String(buffer.array()).trim();
    }

}