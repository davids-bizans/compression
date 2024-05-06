//Dāvids Bižāns 231RDB005
//Andrejs Bistrovs 231RDB020
//Timurs Vahitovs 231RDB096
//Dominiks Stalovičs 231RDB051
//Aleksejs Vereščagins 231RDB115

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static final int WINDOW_SIZE = 60000;

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

            for (int i = searchEnd - 1; i >= searchStart; i--) {
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
                    throw new IllegalArgumentException("Invalid offset or length");
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

    public static void about(){
        System.out.println("Authors:\n" +
                "Dāvids Bižāns 231RDB005\n" +
                "Andrejs Bistrovs 231RDB020\n" +
                "Timurs Vahitovs 231RDB096\n" +
                "Dominiks Stalovičs 231RDB051\n" +
                "Aleksejs Vereščagins 231RDB115\n");
    }

    public static void writeDecompressedToFile(String decompressedContent, String outputFilePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {
            writer.write(decompressedContent);
        }
    }


    public static void size(String sourceFile) {
        try {
            FileInputStream f = new FileInputStream(sourceFile);
            System.out.println("size: " + f.available());
            f.close();
        }
        catch (IOException ex) {
            System.out.println(ex.getMessage());
        }

    }



    public static void comp(String sourceFilePath,String resultFile){
        try {
            String originalContent = new String(Files.readAllBytes(Paths.get(sourceFilePath)));
            List<Tuple> compressed = compress(originalContent);

            writeCompressedToFile(compressed, resultFile);
        }
        catch(Exception ex){
            System.out.println(ex);
        }
    }

    public static void decomp(String sourceFile,String resultFile){
        try{
            List<Tuple> compressedData = readCompressedFromFile(sourceFile);
            String decompressedContent = decompress(compressedData);
            writeDecompressedToFile(decompressedContent, resultFile);
        }catch(Exception ex){
            System.out.println(ex);
        }

    }


    public static boolean equal(String firstFile, String secondFile) {
        try {
            FileInputStream f1 = new FileInputStream(firstFile);
            FileInputStream f2 = new FileInputStream(secondFile);
            int k1, k2;
            byte[] buf1 = new byte[1000];
            byte[] buf2 = new byte[1000];
            do {
                k1 = f1.read(buf1);
                k2 = f2.read(buf2);
                if (k1 != k2) {
                    f1.close();
                    f2.close();
                    return false;
                }
                for (int i=0; i<k1; i++) {
                    if (buf1[i] != buf2[i]) {
                        f1.close();
                        f2.close();
                        return false;
                    }

                }
            } while (!(k1 == -1 && k2 == -1));
            f1.close();
            f2.close();
            return true;
        }
        catch (IOException ex) {
            System.out.println(ex.getMessage());
            return false;
        }
    }



    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String choiseStr;
        String sourceFile, resultFile, firstFile, secondFile;

        loop: while (true) {

            choiseStr = sc.next();

            switch (choiseStr) {
                case "comp":
                    System.out.print("source file name: ");
                    sourceFile = sc.next();
                    System.out.print("archive name: ");
                    resultFile = sc.next();
                    comp(sourceFile, resultFile);
                    break;
                case "decomp":
                    System.out.print("archive name: ");
                    sourceFile = sc.next();
                    System.out.print("file name: ");
                    resultFile = sc.next();
                    decomp(sourceFile, resultFile);
                    break;
                case "size":
                    System.out.print("file name: ");
                    sourceFile = sc.next();
                    size(sourceFile);
                    break;
                case "equal":
                    System.out.print("first file name: ");
                    firstFile = sc.next();
                    System.out.print("second file name: ");
                    secondFile = sc.next();
                    System.out.println(equal(firstFile, secondFile));
                    break;
                case "about":
                    about();
                    break;
                case "exit":
                    break loop;
            }
        }

        sc.close();
    }
}
