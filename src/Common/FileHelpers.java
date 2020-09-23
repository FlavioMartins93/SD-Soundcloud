package Common;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileHelpers {
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
    
    public static byte[] toByteArray(File file, int start, int count) throws FileNotFoundException, IOException {
        long length = file.length();
        if (start >= length) return new byte[0];
        int countAux = (int)Math.min(count, length - start);
        byte[] array = new byte[countAux];
        InputStream in = new FileInputStream(file);
        in.skip(start);
        int offset = 0;
        while (offset < countAux) {
            int tmp = in.read(array, offset, countAux); //(int)(length - offset));
            offset += tmp;
        }
        in.close();
        return array;
    }
    
    public static void writeBytesToFile(FileOutputStream out, String hex) throws IOException
    {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) 
        {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                             + Character.digit(hex.charAt(i+1), 16));
        }
        out.write(data);
    }
}
