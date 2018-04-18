package com.mediatek.camera.common.arcsoft;

import com.mediatek.accessor.operator.IMetaOperator;
import com.mediatek.accessor.operator.MetaOperatorFactory;
import com.mediatek.accessor.parser.IParser;
import com.mediatek.accessor.parser.SerializedInfo;
import com.mediatek.accessor.util.Log;
import com.mediatek.accessor.util.TraceHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chengjian on 4/2/18.
 */
public class ArcSoftStereoCaptureInfoParser implements IParser {
    private static final String TAG = Log.Tag(ArcSoftStereoCaptureInfoParser.class.getSimpleName());
    private ArcSoftStereoCaptureInfo mStereoCaptureInfo;
    private ArcSoftStereoConfigInfoParser mStereoConfigInfoParser;
    private ArcSoftStereoConfigInfo mConfigInfo;
    private IMetaOperator mStandardMetaOperator;
    private IMetaOperator mExtendedMetaOperator;

    public ArcSoftStereoCaptureInfoParser(byte[] standardBuffer, byte[] extendedBuffer,
            ArcSoftStereoCaptureInfo info) {
        mStereoCaptureInfo = info;
        mConfigInfo = new ArcSoftStereoConfigInfo();
        mExtendedMetaOperator = MetaOperatorFactory.getOperatorInstance(0, extendedBuffer,
                (Map) null);
        mStandardMetaOperator = MetaOperatorFactory.getOperatorInstance(0, standardBuffer,
                (Map) null);
    }

    public void read() {
    }

    public void write() {
        TraceHelper.beginSection(">>>>StereoCaptureInfoParser-write");
        Log.d(TAG, "<write>");
        if (mStereoCaptureInfo == null) {
            Log.d(TAG, "<write> mStereoCaptureInfo is null!");
            TraceHelper.endSection();
        } else {
            writeInfo();
            mStereoConfigInfoParser = new ArcSoftStereoConfigInfoParser(mStandardMetaOperator,
                    mExtendedMetaOperator, mConfigInfo);
            mStereoConfigInfoParser.write();
            TraceHelper.endSection();
        }
    }

    public SerializedInfo serialize() {
        TraceHelper.beginSection(">>>>StereoCaptureInfoParser-serialize");
        Log.d(TAG, "<serialize>");
        SerializedInfo info = new SerializedInfo();
        if (mStandardMetaOperator != null) {
            Map customizedData = mStandardMetaOperator.serialize();
            info.standardXmpBuf = (byte[]) customizedData.get("XMP");
        }

        if (mExtendedMetaOperator != null) {
            Map customizedData = mExtendedMetaOperator.serialize();
            info.extendedXmpBuf = (byte[]) customizedData.get("XMP");
        }

        info.customizedBufMap = writeCustomMap();

        TraceHelper.endSection();
        return info;
    }

    private void writeInfo() {
        TraceHelper.beginSection(">>>>StereoCaptureInfoParser-writeInfo");
        if (mStereoCaptureInfo.configBuffer == null) {
            TraceHelper.endSection();
        } else {
            ArcSoftStereoInfoJsonParser stereoInfoJsonParser = new ArcSoftStereoInfoJsonParser(
                    mStereoCaptureInfo.configBuffer);
            mConfigInfo.orientation = stereoInfoJsonParser.getOrientation();
            mConfigInfo.touchCoordX1st = stereoInfoJsonParser.getTouchCoordX1st();
            mConfigInfo.touchCoordY1st = stereoInfoJsonParser.getTouchCoordY1st();
            mConfigInfo.dofLevel = stereoInfoJsonParser.getDofLevel();
            mConfigInfo.i32MainWidth_CropNoScale = stereoInfoJsonParser.getMainWidth();
            mConfigInfo.i32MainHeight_CropNoScale = stereoInfoJsonParser.getMainHeight();
            mConfigInfo.i32AuxWidth_CropNoScale = stereoInfoJsonParser.getAuxWidth();
            mConfigInfo.i32AuxHeight_CropNoScale = stereoInfoJsonParser.getAuxHeight();
            TraceHelper.endSection();
        }
    }

    private Map<String, byte[]> writeCustomMap() {
        Map<String, byte[]> unpackedCustomizedBufMap = new HashMap<String, byte[]>();
        unpackedCustomizedBufMap.put(ArcSoftPackUtils.TYPE_BAYER_DATA,
                mStereoCaptureInfo.bayerBuffer);
        unpackedCustomizedBufMap.put(ArcSoftPackUtils.TYPE_JPS_DATA, mStereoCaptureInfo.jpsBuffer);
        unpackedCustomizedBufMap.put(ArcSoftPackUtils.TYPE_CONFIG_DATA,
                mStereoCaptureInfo.configBuffer);
        unpackedCustomizedBufMap.put(ArcSoftPackUtils.TYPE_CALIBRATION_DATA,
                mStereoCaptureInfo.calibrationBuffer);
        return unpackedCustomizedBufMap;
    }
}