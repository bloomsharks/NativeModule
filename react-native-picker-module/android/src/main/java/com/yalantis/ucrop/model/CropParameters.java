package com.yalantis.ucrop.model;

import android.graphics.Bitmap;

/**
 * Created by Oleksii Shliama [https://github.com/shliama] on 6/21/16.
 */
public class CropParameters {

    private int mMaxResultImageSizeX, mMaxResultImageSizeY;

    private Bitmap.CompressFormat mCompressFormat;
    private int mCompressQuality;
    private int mCompressionMaxSizeBytes;
    private String mImageInputPath, mImageOutputPath;
    private ExifInfo mExifInfo;


    public CropParameters(
            int maxResultImageSizeX,
            int maxResultImageSizeY,
            Bitmap.CompressFormat compressFormat,
            int compressQuality,
            int compressionMaxSizeBytes,
            String imageInputPath,
            String imageOutputPath,
            ExifInfo exifInfo
    ) {
        mMaxResultImageSizeX = maxResultImageSizeX;
        mMaxResultImageSizeY = maxResultImageSizeY;
        mCompressFormat = compressFormat;
        mCompressQuality = compressQuality;
        mImageInputPath = imageInputPath;
        mImageOutputPath = imageOutputPath;
        mExifInfo = exifInfo;
        mCompressionMaxSizeBytes = compressionMaxSizeBytes;
    }

    public int getMaxResultImageSizeX() {
        return mMaxResultImageSizeX;
    }

    public int getMaxResultImageSizeY() {
        return mMaxResultImageSizeY;
    }

    public Bitmap.CompressFormat getCompressFormat() {
        return mCompressFormat;
    }

    public int getCompressQuality() {
        return mCompressQuality;
    }

    public int getmCompressionMaxSizeBytes() {
        return mCompressionMaxSizeBytes;
    }

    public String getImageInputPath() {
        return mImageInputPath;
    }

    public String getImageOutputPath() {
        return mImageOutputPath;
    }

    public ExifInfo getExifInfo() {
        return mExifInfo;
    }

}
