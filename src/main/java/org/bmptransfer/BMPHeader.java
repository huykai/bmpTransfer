package org.bmptransfer;

public class BMPHeader {
    private int fileSize;
    private int fileDataOffset;
    private int fileInfoStructSize;
    private int width;
    private int height;
    private int plane;
    private int colorMode;
    private int imagePressMode;
    private int imageSize;
    private int pixelWidth;
    private int pixelHeight;
    private int preservedColor;
    private int preservedImpColor;
    private byte[] bmpHeaderBytes;

    public BMPHeader() {
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public int getFileDataOffset() {
        return fileDataOffset;
    }

    public void setFileDataOffset(int fileDataOffset) {
        this.fileDataOffset = fileDataOffset;
    }

    public int getFileInfoStructSize() {
        return fileInfoStructSize;
    }

    public void setFileInfoStructSize(int fileInfoStructSize) {
        this.fileInfoStructSize = fileInfoStructSize;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getPlane() {
        return plane;
    }

    public void setPlane(int plane) {
        this.plane = plane;
    }

    public int getImageSize() {
        return imageSize;
    }

    public void setImageSize(int imageSize) {
        this.imageSize = imageSize;
    }

    public int getImagePressMode() {
        return imagePressMode;
    }

    public void setImagePressMode(int imagePressMode) {
        this.imagePressMode = imagePressMode;
    }

    public int getColorMode() {
        return colorMode;
    }

    public void setColorMode(int colorMode) {
        this.colorMode = colorMode;
    }

    public int getPixelWidth() {
        return pixelWidth;
    }

    public void setPixelWidth(int pixelWidth) {
        this.pixelWidth = pixelWidth;
    }

    public int getPixelHeight() {
        return pixelHeight;
    }

    public void setPixelHeight(int pixelHeight) {
        this.pixelHeight = pixelHeight;
    }

    public int getPreservedColor() {
        return preservedColor;
    }

    public void setPreservedColor(int preservedColor) {
        this.preservedColor = preservedColor;
    }

    public int getPreservedImpColor() {
        return preservedImpColor;
    }

    public void setPreservedImpColor(int preservedImpColor){
        this.preservedImpColor = preservedImpColor;
    }

    public byte[] getBmpHeaderBytes() {
        return bmpHeaderBytes;
    }

    public void setBmpHeaderBytes(byte[] bmpHeaderBytes) {
        this.bmpHeaderBytes = bmpHeaderBytes;
    }
}