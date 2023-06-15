package fun.jyoiko.compress.gzip;

import fun.jyoiko.compress.Compress;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Gzip implements Compress {
    private static final int BUFFER_SIZE = 1024 * 4;
    @Override
    public byte[] compress(byte[] bytes) {
        if (bytes == null) {
            throw new NullPointerException("bytes is null");
        }
        try(    ByteArrayOutputStream byteArrayOutputStream= new ByteArrayOutputStream();
                GZIPOutputStream gzip=new GZIPOutputStream(byteArrayOutputStream)
                ) {
            gzip.write(bytes);
            gzip.flush();
            gzip.finish();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("gzip compress error",e);
        }
    }

    @Override
    public byte[] decompress(byte[] bytes) {
        if (bytes == null) {
            throw new NullPointerException("bytes is null");
        }
        try(
                ByteArrayOutputStream byteArrayOutputStream= new ByteArrayOutputStream();
                GZIPInputStream gzip=new GZIPInputStream(new ByteArrayInputStream(bytes))
                ) {
            int n;
            byte[] bytesA = new byte[BUFFER_SIZE];
            while((n= gzip.read(bytesA))>-1){
                byteArrayOutputStream.write(bytesA,0,n);
            }
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("gzip decompress error",e);
        }
    }
}
