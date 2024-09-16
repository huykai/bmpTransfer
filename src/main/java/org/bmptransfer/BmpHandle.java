package org.bmptransfer;

import javax.sound.midi.Soundbank;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class BmpHandle {
    private String bmpFile;
    private String sourceFile;
    private String outputTxtFileName = "out.txt";
    private String targetFile;
    private BMPHeader bmpHeader = new BMPHeader();

    public static void main(String args[]) {
        if (args.length < 1) {
            System.out.printf("参数不够，请补全！\n");
            System.out.printf("Usage： java -jar xxxx.jar transform bmp source target\n"
                    + " java -jar xxxx.jar fallback bmp source target\n"
                    + " java -jar xxxx.jar *.bmp // kind of fallback\n"
                    + " java -jar xxxx.jar *.zip/7z/any // kind of tranform\n"
                    + " java -jar xxxx.jar *.zip/7z/any txt// kind of tranform to unicode\n");
            return;
        }
        //System.out.println("args oper: " + args[0] + " bmp:" + args[1] + " source: " + args[2] + " target: " + args[3]);

        String operMode = args[0];
        BmpHandle bp = null;
        boolean res = false;
        if (operMode.equalsIgnoreCase("transform")) {
            bp = new BmpHandle(args[1], args[2], args[3]);
            res = bp.transformMap();
        } else if (operMode.equalsIgnoreCase("fallback")) {
            bp = new BmpHandle(args[1], args[2], args[3]);
            res = bp.fallbackMap();
        } else if (operMode.contains(".bmp")) {
            bp = new BmpHandle(args[0], args[0], args[0].replace(".bmp",""));
            res = bp.fallbackMap();
        } else {
            bp = new BmpHandle("", args[0], "");
            if (args.length > 1) {
                String operTarget = args[1];
                if (operTarget.equalsIgnoreCase("txt")) {
                    res = bp.transformUnicode();
                }
            } else {
                res = bp.transformMap();
            }
        }
        if (!res) {
            System.out.printf("Something is wrong!");
        }
        return;
//        if ( !res ) {
//            if (operMode.equalsIgnoreCase("transform")) {
//                System.out.println("BMP transform failed!");
//            } else if (operMode.equalsIgnoreCase("fallback")) {
//                System.out.println("BMP fallback failed!");
//            }
//        } else {
//            if (operMode.equalsIgnoreCase("transform")) {
//                System.out.println("BMP tranform sucessfully!");
//            } else if (operMode.equalsIgnoreCase("fallback")) {
//                System.out.println("BMP fallback sucessfully!");
//            };
//        }

    }

    public boolean transformUnicode() {
        boolean res = false;
        try {
            File file = new File(sourceFile);

            // 如果文件不存在则创建该文件
            if (!file.exists()) {
                System.out.printf("Source File not Exist!");
                res = false;
                return res;
            }
            long sourceFileSize = file.length();
            System.out.println("Souce File Size: " + sourceFileSize);
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));

            FileWriter writer = new FileWriter("output.txt");

            byte[] bisContent = new byte[10240];
            int count = bis.read(bisContent);
            byte low, high;
            while (count > 0) {
                int i = 0;
                while (i < count){
                    low = bisContent[i];
                    if ((i+1) < count) {
                        high = bisContent[i+1];
                        i+=2;
                    } else {
                        i+=1;
                    }

                    int gbCode = 20013;

                    // 将国标码转换为Unicode字符
                    String unicodeStr = String.format("\\u%04x", gbCode);
                    writer.write(unicodeStr);
                    // 解析Unicode字符串并打印汉字
                    System.out.println(unicodeStr); // 输出：好
                }
            }
            writer.close();
            res = true;
            return res;
        } catch(Exception e){
            System.err.println("transformUnicode get exception: " + e.getMessage());
            return res;
        }
    }
    public BmpHandle(String bmpFile, String sFile, String tFile) {
        this.bmpFile = bmpFile;
        this.sourceFile = sFile;
        this.targetFile = tFile;
    }

    private void writeBufferToTargetFile(byte[] content, int bufferLength, FileOutputStream fou) {
        try {
            fou.write(content, 0, bufferLength);
        } catch (Exception e) {
            System.out.println("writeBufferToTargetFile get exception: " + e.getMessage());
        }
    }

    private boolean writeTarget(String sourceFileName, FileOutputStream fou) {
        try {
            FileInputStream fin = new FileInputStream(sourceFileName);
            byte[] header = new byte[54];
            fin.read(header, 0, 54);
            int realLineNumberBytes0 = Byte.toUnsignedInt(header[6]);
            int realLineNumberBytes1 = Byte.toUnsignedInt(header[7]);
            int lastLineWidthBytes0 = Byte.toUnsignedInt(header[8]);
            int lastLineWidthBytes1 = Byte.toUnsignedInt(header[9]);

            int realLineNumber = (realLineNumberBytes1 << 8) + (realLineNumberBytes0 << 0);
            int lastLineWidth = (lastLineWidthBytes1 << 8) + (lastLineWidthBytes0 << 0);
            if (realLineNumber <= 0) {
                realLineNumber = bmpHeader.getHeight();
                lastLineWidth = bmpHeader.getWidth() * 3;
            } else {
                System.out.println(realLineNumber);
            }

            int width = bmpHeader.getWidth();
            int lineWidth = width * 3;
            int lineByteFillWidth = (4 - ((width * 3) % 4)) % 4;
            int bmpLineWidth = lineWidth + lineByteFillWidth;
            byte[] bufferForLine = new byte[bmpLineWidth];
            int lineReadLength = fin.read(bufferForLine);
            int lineNumber = 1;
            byte[] bufferForFile = new byte[bmpHeader.getHeight() * (lineWidth + lineByteFillWidth)];
            int bufferForFileCurr = 0;
            boolean isLast = false;
            while ((lineReadLength == bmpLineWidth) && (lineNumber <= realLineNumber)) {
                if (lineNumber == realLineNumber) {
                    for (int i = 0; i < lastLineWidth; i++) {
                        bufferForFile[bufferForFileCurr++] = bufferForLine[i];
                    }
                    break;
                } else {
                    for (int i = 0; i < lineWidth; i++) {
                        bufferForFile[bufferForFileCurr++] = bufferForLine[i];
                    }
                }
                lineReadLength = fin.read(bufferForLine);
                if (lineNumber == 767) {
                    System.out.println("767");
                }
                lineNumber++;
            }
            writeBufferToTargetFile(bufferForFile, bufferForFileCurr, fou);
            fou.flush();
            //fou.close();
            return true;
        } catch (Exception e) {
            System.out.println("writeMap get exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private List<String> findExistSources(String sourceFile) {
        List<String> res = new ArrayList<String>();
        try {
            String sourcePathName = sourceFile.substring(0, sourceFile.lastIndexOf("\\"));
            String sourceFileName = sourceFile.substring(sourceFile.lastIndexOf("\\") + 1);
            String sourceFileNameFirst = sourceFileName.split("\\.")[0];
            String sourceFileNameLast = sourceFileName.split("\\.")[1];
            File dir = new File(sourcePathName);
            if (Objects.isNull(dir) || !dir.exists() || dir.isFile()) {
                return res;
            }
            File[] files = dir.listFiles();
            String cursourceFileName = sourceFileName;
            int i = 1;
            while (fileInDir(files, cursourceFileName)) {
                res.add(sourcePathName + "\\" + cursourceFileName);
                cursourceFileName = sourceFileNameFirst + String.valueOf(i++) + "." + sourceFileNameLast;
            }
        } catch (Exception e) {
            System.out.println("findExistsources get exception: " + e.getMessage());
            e.printStackTrace();
        }
        return res;
    }

    private boolean fileInDir(File[] files, String sFile) {
        for (File file : files) {
            if (file.getName().equalsIgnoreCase(sFile)) {
                return true;
            }
        }
        return false;
    }

    public boolean fallbackMap() {
        if (!readBMPMap(bmpFile)) {
            System.out.println("请更换BMP文件！");
            return false;
        } else {
            try {
                List<String> sourceFiles = findExistSources(sourceFile);
                FileOutputStream fou = new FileOutputStream(targetFile);
                for (String sourceFile : sourceFiles) {
                    System.out.println("fallback file: " + sourceFile);
                    writeTarget(sourceFile, fou);
                }
                fou.close();
                return true;
            } catch (Exception e) {
                System.out.println("fallbackMap get exception: " + e.getMessage());
                e.printStackTrace();
                return false;
            }

        }
    }

    public boolean transformMap() {
        if ("".equalsIgnoreCase(bmpFile)) {
            // transfer file without bmpFile
            try {
                File file = new File(sourceFile);

                // 如果文件不存在则创建该文件
                if (!file.exists()) {
                    System.out.printf("Source File not Exist!");
                    return false;
                }
                long sourceFileSize = file.length();
                System.out.println("Souce File Size: " + sourceFileSize);
                long pixelNumber = (long) Math.ceil(sourceFileSize / 3);
                //long width = (long)Math.ceil(Math.sqrt(pixelNumber / 9 / 16)) / 1024 / 1024 * 1024 ;
                int width = (int) Math.ceil(Math.sqrt(pixelNumber / 9 / 16) * 16 / 1024) * 1024;
                int height = (int) (pixelNumber / width + 1);
                System.out.printf("PixelNumber: " + pixelNumber + "\n");
                System.out.printf("width: " + width + "\n");
                System.out.printf("height: " + height + "\n");
                targetFile = file.getName() + ".bmp";
                System.out.printf("targetFile: " + targetFile + "\n");
                setMapHeader(width, height);
                writeMap(bmpHeader, sourceFile, targetFile);
                return true;
            } catch (Exception e) {
                System.out.printf("Source File open failed!");
                return false;
            }
        } else if (!readBMPMap(bmpFile)) {
            System.out.println("请更换BMP文件！");
            return false;
        } else {
            return writeMap(bmpHeader, sourceFile, targetFile);
        }

    }

    private FileOutputStream changeNewFou(BMPHeader bmpHeader, String fileName, int fileNumber) {
        try {
            String[] fileNameSplit = fileName.split("\\.");
            String fileDirFileNamePart = fileName.substring(0, fileName.lastIndexOf("."));
            //String fileNamePrefix = fileNameSplit[fileNameSplit.length - 2];
            String fileNamePostfix = fileNameSplit[fileNameSplit.length - 1];
            String newFileName = fileDirFileNamePart + String.valueOf(fileNumber) + "." + fileNamePostfix;
            System.out.println("New target filename: " + newFileName);
            FileOutputStream fou = new FileOutputStream(newFileName);
            //fou.write(this.bmpHeader.getBmpHeaderBytes());
            return fou;
        } catch (Exception e) {
            System.out.println("Change New targer File exception: " + e.getMessage());
            return null;
        }
    }

    private void writeBufferToBMPFile(BMPHeader bmpH, boolean isLast, int height, int width, byte[] content, FileOutputStream fou) {
        try {
            byte[] bmpHeader = new byte[54];
            byte[] bmpOrigin = bmpH.getBmpHeaderBytes();
            for (int i = 0; i < 54; i++) {
                bmpHeader[i] = bmpOrigin[i];
            }
            //write real line number and last line offset
            if (isLast) {
            //int lastFileLength = height * bmpHeader.getWidth() + lineReadLength;
                bmpHeader[6] = (byte) (height % 256);
                bmpHeader[7] = (byte) (height / 256);
                bmpHeader[8] = (byte) (width % 256);
                bmpHeader[9] = (byte) (width / 256);
            }
            fou.write(bmpHeader);
            fou.write(content);
            fou.close();
        } catch (Exception e) {
            System.out.println("writeBufferToFile get Exception: " + e.getMessage());
        }
    }

    public boolean writeMap(BMPHeader bmpHeader, String sourceFileName, String targetFileName) {
        try {
            FileInputStream fin = new FileInputStream(sourceFileName);
            FileOutputStream fou = new FileOutputStream(targetFileName);

            //fou.write(bmpHeader.getBmpHeaderBytes());

            int width = bmpHeader.getWidth();
            int lineWidth = width * 3;
            int lineByteFillWidth = (4 - ((width * 3) % 4)) % 4;
            byte[] bufferForLine = new byte[lineWidth];
            int lineReadLength = fin.read(bufferForLine);
            int lineNumber = 0;
            int fileNumber = 0;
            byte[] bufferForFile = new byte[bmpHeader.getHeight() * (lineWidth + lineByteFillWidth)];
            int bufferForFileCurr = 0;
            boolean isLast = false;
            //int bufferForFileLength = fin.read(bufferForFile);
            while (lineReadLength == lineWidth) {
                lineNumber++;
                if (lineNumber > bmpHeader.getHeight()) {
                    System.out.println("Source Data spill.");

                    writeBufferToBMPFile(bmpHeader, isLast, bmpHeader.getHeight(), bmpHeader.getWidth(), bufferForFile, fou);
                    //fou.close();
                    fileNumber++;
                    fou = changeNewFou(bmpHeader, targetFileName, fileNumber);
                    bufferForFileCurr = 0;
                    lineNumber = 1;
                    if (fou == null)
                        break;
                }
                //fou.write(bufferForLine);
                for (int i = 0; i < lineReadLength; i++) {
                    bufferForFile[bufferForFileCurr++] = bufferForLine[i];
                }
                for (int i = 0; i < lineByteFillWidth; i++) {
                    bufferForFile[bufferForFileCurr++] = 0;
                }
                lineReadLength = fin.read(bufferForLine);
//                if (lineNumber == 767) {
//                    System.out.println("768");
//                }
            }
            lineNumber++;
            if (lineReadLength > 0) {
                for (int i = 0; i < bufferForLine.length; i++) {
                    if (i < lineReadLength) {
                        //fou.write(bufferForLine[i]);
                        bufferForFile[bufferForFileCurr++] = bufferForLine[i];
                    } else {
                        //fou.write(0);
                        bufferForFile[bufferForFileCurr++] = 0;
                    }
                }
            }
            if (lineNumber < bmpHeader.getHeight()) {
                System.out.println("Source Data not enough.");
                for (int i = (lineNumber + 1); i < bmpHeader.getHeight(); i++) {
                    for (int j = 0; j < (lineWidth + lineByteFillWidth); j++) {
                        //fou.write(0);
                        bufferForFile[bufferForFileCurr++] = 0;
                    }
                }
            }
            isLast = true;
            writeBufferToBMPFile(bmpHeader, isLast, lineNumber, lineReadLength, bufferForFile, fou);
            return true;
        } catch (Exception e) {
            System.out.println("writeMap get exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void setMapHeader(int width, int height) {
        byte[] res = new byte[54];
        res[0] = 'B'; res[1] = 'M';

        int fileSize = width * height + 54;
        writeIntToHeader(fileSize, res, 2);
        this.bmpHeader.setFileSize(fileSize);
        // reserve
        res[6] = (byte) 0; res[7] = (byte) 0; res[8] = (byte) 0; res[9] = (byte) 0;
        int fileOffset = 54;
        writeIntToHeader(fileOffset, res, 10);
        this.bmpHeader.setFileDataOffset(fileOffset);
        int fileInfoLength = 40;
        writeIntToHeader(fileInfoLength, res, 14);
        this.bmpHeader.setFileInfoStructSize(fileInfoLength);
        writeIntToHeader(width, res, 18);
        this.bmpHeader.setWidth(width);
        writeIntToHeader(height, res, 22);
        this.bmpHeader.setHeight(height);
        writeShortToHeader(1, res, 26); //plane
        this.bmpHeader.setPlane(1);
        writeShortToHeader(24, res, 28); //color bits
        this.bmpHeader.setColorMode(24);
        writeIntToHeader(0, res, 30); //press mode
        this.bmpHeader.setImagePressMode(0);
        int imageSize = width * height * 3;
        writeIntToHeader(imageSize, res, 34); //Image size
        this.bmpHeader.setImageSize(imageSize);
        writeIntToHeader(50190, res, 38); //pixel width per meter
        this.bmpHeader.setPixelWidth(50190);
        writeIntToHeader(50190, res, 42); //pixel height per meter
        this.bmpHeader.setPixelHeight(50190);
        writeIntToHeader(0, res, 46); //preservedColor
        writeIntToHeader(0, res, 50); //preservedImpColor
        this.bmpHeader.setPreservedImpColor(0);
        this.bmpHeader.setBmpHeaderBytes(res);
    }
    private void writeShortToHeader(int data, byte[] res, int begin) {
        res[begin] = (byte) (data % (256));
        res[begin + 1] = (byte) ((data - (int)(res[begin] & 0xff)) / 256);
    }
    private void writeIntToHeader(int data, byte[] res, int begin) {
        res[begin] = (byte) (data % (256));
        res[begin + 1] = (byte) (((data - (int)(res[begin] & 0xff)) / 256) % (256));
        res[begin + 2] = (byte) (((data - ((int)(res[begin] & 0xff)) - (int)(res[begin + 1] & 0xff) * 256) / (256 * 256)) % (256));
        res[begin + 3] = (byte) (data / (256*256*256));
    }
    public boolean readMapHeader(DataInputStream dis) {
        try {
            Date tt = new Date();

            byte[] res = new byte[54];
            byte[] b = new byte[2];
            b[0] = dis.readByte();
            //System.out.println("File Desc: " + b);
            b[1] = dis.readByte();
            res[0] = b[0];
            res[1] = b[1];
            //System.out.println("File Desc: " + b[0] + " " + b[1]);
            if (!(b[0] == 'B' && b[1] == 'M')) {
                System.out.println("BMP template file not correct.");
                return false;
            }
            // file size
            int ch1 = dis.read();
            res[2] = (byte) ch1;
            int ch2 = dis.read();
            res[3] = (byte) ch2;
            int ch3 = dis.read();
            res[4] = (byte) ch3;
            int ch4 = dis.read();
            res[5] = (byte) ch4;
            int fileSize = ((ch4 << 24) + (ch3 << 16) + (ch2 << 8) + (ch1 << 0));
            //System.out.println("File Size: " + fileSize);
            this.bmpHeader.setFileSize(fileSize);
            // file reserved
            ch1 = dis.read();
            res[6] = (byte) ch1;
            ch2 = dis.read();
            res[7] = (byte) ch2;
            ch3 = dis.read();
            res[8] = (byte) ch3;
            ch4 = dis.read();
            res[9] = (byte) ch4;
            // file data offset
            ch1 = dis.read();
            res[10] = (byte) ch1;
            ch2 = dis.read();
            res[11] = (byte) ch2;
            ch3 = dis.read();
            res[12] = (byte) ch3;
            ch4 = dis.read();
            res[13] = (byte) ch4;
            int fileDataOffset = ((ch4 << 24) + (ch3 << 16) + (ch2 << 8) + (ch1 << 0));
            //System.out.println("File Data Offset: " + fileDataOffset);
            this.bmpHeader.setFileDataOffset(fileDataOffset);
            // file info struct size
            ch1 = dis.read();
            res[14] = (byte) ch1;
            ch2 = dis.read();
            res[15] = (byte) ch2;
            ch3 = dis.read();
            res[16] = (byte) ch3;
            ch4 = dis.read();
            res[17] = (byte) ch4;
            int fileInfoStructSize = ((ch4 << 24) + (ch3 << 16) + (ch2 << 8) + (ch1 << 0));
            //System.out.println("File Info Struct Size: " + fileInfoStructSize);
            this.bmpHeader.setFileInfoStructSize(fileInfoStructSize);
            // file info width
            ch1 = dis.read();
            res[18] = (byte) ch1;
            ch2 = dis.read();
            res[19] = (byte) ch2;
            ch3 = dis.read();
            res[20] = (byte) ch3;
            ch4 = dis.read();
            res[21] = (byte) ch4;
            int width = ((ch4 << 24) + (ch3 << 16) + (ch2 << 8) + (ch1 << 0));
            //System.out.println("width: " + width);
            this.bmpHeader.setWidth(width);
            ch1 = dis.read();
            res[22] = (byte) ch1;
            ch2 = dis.read();
            res[23] = (byte) ch2;
            ch3 = dis.read();
            res[24] = (byte) ch3;
            ch4 = dis.read();
            res[25] = (byte) ch4;
            int height = ((ch4 << 24) + (ch3 << 16) + (ch2 << 8) + (ch1 << 0));
            //System.out.println("height: " + height);
            this.bmpHeader.setHeight(height);
            ch1 = dis.read();
            res[26] = (byte) ch1;
            ch2 = dis.read();
            res[27] = (byte) ch2;
            int plane = ((ch2 << 8) + (ch1 << 0));
            //System.out.println("plane: " + plane);
            this.bmpHeader.setPlane(plane);
            ch1 = dis.read();
            res[28] = (byte) ch1;
            ch2 = dis.read();
            res[29] = (byte) ch2;
            int colorMode = ((ch2 << 8) + (ch1 << 0));
            //System.out.println("colorMode: " + colorMode);
            this.bmpHeader.setColorMode(colorMode);
            ch1 = dis.read();
            res[30] = (byte) ch1;
            ch2 = dis.read();
            res[31] = (byte) ch2;
            ch3 = dis.read();
            res[32] = (byte) ch3;
            ch4 = dis.read();
            res[33] = (byte) ch4;
            int imagePressMode = ((ch4 << 24) + (ch3 << 16) + (ch2 << 8) + (ch1 << 0));
            //System.out.println("imagePressMode: " + imagePressMode);
            this.bmpHeader.setImagePressMode(imagePressMode);
            ch1 = dis.read();
            res[34] = (byte) ch1;
            ch2 = dis.read();
            res[35] = (byte) ch2;
            ch3 = dis.read();
            res[36] = (byte) ch3;
            ch4 = dis.read();
            res[37] = (byte) ch4;
            int imageSize = ((ch4 << 24) + (ch3 << 16) + (ch2 << 8) + (ch1 << 0));
            //System.out.println("imageSize: " + imageSize);
            this.bmpHeader.setImageSize(imageSize);
            ch1 = dis.read();
            res[38] = (byte) ch1;
            ch2 = dis.read();
            res[39] = (byte) ch2;
            ch3 = dis.read();
            res[40] = (byte) ch3;
            ch4 = dis.read();
            res[41] = (byte) ch4;
            int pixelWidth = ((ch4 << 24) + (ch3 << 16) + (ch2 << 8) + (ch1 << 0));
            //System.out.println("pixelWidth: " + pixelWidth);
            this.bmpHeader.setPixelWidth(pixelWidth);
            ch1 = dis.read();
            res[42] = (byte) ch1;
            ch2 = dis.read();
            res[43] = (byte) ch2;
            ch3 = dis.read();
            res[44] = (byte) ch3;
            ch4 = dis.read();
            res[45] = (byte) ch4;
            int pixelHeight = ((ch4 << 24) + (ch3 << 16) + (ch2 << 8) + (ch1 << 0));
            //System.out.println("pixelHeight: " + pixelHeight);
            this.bmpHeader.setPixelHeight(pixelHeight);
            ch1 = dis.read();
            res[46] = (byte) ch1;
            ch2 = dis.read();
            res[47] = (byte) ch2;
            ch3 = dis.read();
            res[48] = (byte) ch3;
            ch4 = dis.read();
            res[49] = (byte) ch4;
            int preservedColor = ((ch4 << 24) + (ch3 << 16) + (ch2 << 8) + (ch1 << 0));
            //System.out.println("preservedColor: " + preservedColor);
            this.bmpHeader.setPreservedColor(preservedColor);
            ch1 = dis.read();
            res[50] = (byte) ch1;
            ch2 = dis.read();
            res[51] = (byte) ch2;
            ch3 = dis.read();
            res[52] = (byte) ch3;
            ch4 = dis.read();
            res[53] = (byte) ch4;
            int preservedImpColor = ((ch4 << 24) + (ch3 << 16) + (ch2 << 8) + (ch1 << 0));
            //System.out.println("preservedImpColor: " + preservedImpColor);
            this.bmpHeader.setPreservedImpColor(preservedImpColor);
            this.bmpHeader.setBmpHeaderBytes(res);
            if (colorMode != 24) {
                System.out.println("Now we only support 24bit bmp file.");
                return false;
            }
            return true;
        } catch (Exception e) {
            System.out.println("readMapHead get exception: " + e.getMessage());
            return false;
        }

    }

    public boolean readBMPMap(String filename) {
        try {
            FileInputStream fis = new FileInputStream(filename);
            java.io.BufferedInputStream bis = new BufferedInputStream(fis);
            DataInputStream dis = new DataInputStream(bis);

            if (!readMapHeader(dis)) {
                System.out.println("BMP template file get header failed!");
                return false;
            }

            return true;
        } catch (Exception e) {
            System.out.println("readMap get Exception: " + e.getMessage());
            return false;
        }
    }
}


