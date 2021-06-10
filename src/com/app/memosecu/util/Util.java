package com.app.memosecu.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;


public class Util {

    /**
     * Left pad an integer to a given length with the given
     * character 
     * @param i The integer to pad
     * @param length The length to pad it to
     * @param c The character to do the padding with
     * @return A padded version of the integer
     */
    public static String lpad(int i, int length, char c) {
        StringBuilder buf = new StringBuilder(String.valueOf(i));
        while (buf.length() < length) {
            buf.insert(0, c);
        }
        return buf.toString();
    }


    public static byte[] getBytesFromFile(File file) throws IOException {
        return getBytesFromFile(file, file.length());
    }

    public static byte[] getBytesFromFile(File file, long numBytesToRead) throws IOException {
        InputStream is = new FileInputStream(file);
    
        // Create the byte array to hold the data
        byte[] bytes = new byte[(int) numBytesToRead];
    
        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
               && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }
    
        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            is.close();
            throw new IOException("Could not completely read file " + file.getName());
        }

        is.close();

        return bytes;
    }

    public static Charset defaultCharset() {
        return Charset.forName(
                new OutputStreamWriter(
                        new ByteArrayOutputStream()).getEncoding());
    }

}
