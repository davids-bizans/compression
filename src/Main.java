import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static final int WINDOW_SIZE = 5000;

    public static class Tuple {
        private final int offset;
        private final int length;
        private final char nextChar;

        public Tuple(int offset, int length, char nextChar) {
            this.offset = offset;
            this.length = length;
            this.nextChar = nextChar;
        }

        @Override
        public String toString() {
            return "(" + offset + ", " + length + ", " + nextChar + ")";
        }

        public void writeToStream(DataOutputStream out) throws IOException {
            out.writeShort(offset);
            out.writeShort(length);
            out.writeChar(nextChar);
        }
    }

    public static List<Tuple> compress(String text) {
        List<Tuple> compressed = new ArrayList<>();
        int pos = 0;

        while (pos < text.length()) {
            int maxLen = 0;
            int bestOffset = 0;
            int searchStart = Math.max(0, pos - WINDOW_SIZE);
            int searchEnd = pos;

            // Search for the longest match within the window
            for (int i = searchStart; i < searchEnd; i++) {
                int len = 0;
                while (i + len < searchEnd && pos + len < text.length() && text.charAt(i + len) == text.charAt(pos + len)) {
                    len++;
                }
                if (len > maxLen) {
                    maxLen = len;
                    bestOffset = pos - i;
                }
            }

            if (maxLen > 0 && pos + maxLen < text.length()) {
                compressed.add(new Tuple(bestOffset, maxLen, text.charAt(pos + maxLen)));
                pos += maxLen + 1;
            } else {
                compressed.add(new Tuple(0, 0, text.charAt(pos)));
                pos++;
            }
        }

        return compressed;
    }


    public static String decompress(List<Tuple> compressed) {
        StringBuilder builder = new StringBuilder();
        for (Tuple tuple : compressed) {
            if (tuple.offset > 0 && tuple.length > 0) {
                int start = builder.length() - tuple.offset;
                if (start < 0 || start + tuple.length > builder.length()) {
                    throw new IllegalArgumentException("Invalid offset/length in tuple.");
                }
                for (int i = 0; i < tuple.length; i++) {
                    builder.append(builder.charAt(start + i));
                }
            }
            builder.append(tuple.nextChar);
        }
        return builder.toString();
    }

    public static void writeCompressedToFile(List<Tuple> compressed, String filePath) throws IOException {
        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(filePath)))) {
            for (Tuple tuple : compressed) {
                tuple.writeToStream(out);
            }
        }
    }

    public static List<Tuple> readCompressedFromFile(String filePath) throws IOException {
        List<Tuple> compressed = new ArrayList<>();
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(filePath)))) {
            while (in.available() > 0) {
                int offset = in.readUnsignedShort();
                int length = in.readUnsignedShort();
                char nextChar = in.readChar();
                compressed.add(new Tuple(offset, length, nextChar));
            }
        }
        return compressed;
    }

    public static void writeDecompressedToFile(String decompressedContent, String outputFilePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {
            writer.write(decompressedContent);
        }
    }

    public static void printStatistics(long originalSize, long compressedSize, long decompressedSize){
        double ratio = (double) originalSize / compressedSize;
        System.out.println("Original File Size: " + originalSize + " bytes");
        System.out.println("Compressed File Size: " + compressedSize + " bytes");
        System.out.println("Decompressed File Size: " + decompressedSize + " bytes");
        System.out.println("Compression Ratio: " + ratio);
    }

    public static void compressDecompressFiles(){
        String sourceFilePath = "C:\\Users\\david\\OneDrive\\Desktop\\txt_testing\\input.txt"; // Update with your file path
        String compressedFilePath = "C:\\Users\\david\\OneDrive\\Desktop\\txt_testing\\compressed_file.bin";
        String decompressedFilePath = "C:\\Users\\david\\OneDrive\\Desktop\\txt_testing\\decompressed_output.txt";

        try {
            String originalContent = new String(Files.readAllBytes(Paths.get(sourceFilePath)));
            List<Tuple> compressed = compress(originalContent);

            writeCompressedToFile(compressed, compressedFilePath);
            List<Tuple> compressedData = readCompressedFromFile(compressedFilePath);

            String decompressedContent = decompress(compressedData);
            writeDecompressedToFile(decompressedContent, decompressedFilePath);

            // Display comparison
            long originalSize = new File(sourceFilePath).length();
            long compressedSize = new File(compressedFilePath).length();
            long decompressedSize = new File(decompressedFilePath).length();

            printStatistics(originalSize, compressedSize, decompressedSize);


        } catch (IOException e) {
            System.err.println("Error during processing: " + e.getMessage());
        }
    }

    public static void checkString(){
        String input = "Hello, Hello, Hello!";
        List<Tuple> compressed = compress(input);
        String decompressed = decompress(compressed);

        System.out.println("Original: " + input);
        System.out.println("Compressed: " + compressed);
        System.out.println("Decompressed: " + decompressed);

    }

    public static void main(String[] args) {
        compressDecompressFiles();
    }
}