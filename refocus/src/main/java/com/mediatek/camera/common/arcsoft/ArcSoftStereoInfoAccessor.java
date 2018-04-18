package com.mediatek.camera.common.arcsoft;


import com.mediatek.accessor.packer.PackInfo;
import com.mediatek.accessor.parser.IParser;
import com.mediatek.accessor.parser.SerializedInfo;
import com.mediatek.accessor.util.Log;
import com.mediatek.accessor.util.TraceHelper;

import java.io.File;

/**
 * Created by chengjian on 4/2/18.
 */
public class ArcSoftStereoInfoAccessor {
    public static boolean ENABLE_BUFFER_DUMP = false;
    public static final String DUMP_FILE_FOLDER =
             "Environment.getExternalStorageDirectory().toString()/dumpArcSoft";

    private static final String TAG = Log.Tag(ArcSoftStereoInfoAccessor.class.getSimpleName());

    static {
        File inFile = new File(DUMP_FILE_FOLDER);
        if (inFile.exists()) {
            ENABLE_BUFFER_DUMP = true;
            Log.i(TAG, "ENABLE_BUFFER_DUMP: " + ENABLE_BUFFER_DUMP);
        }
    }

    public ArcSoftStereoInfoAccessor() {
    }

    public byte[] writeStereoCaptureInfo(ArcSoftStereoCaptureInfo captureInfo) {
        TraceHelper.beginSection(">>>>ArcSoftStereoInfoAccessor-writeStereoCaptureInfo");
        Log.d(TAG, "<writeStereoCaptureInfo> captureInfo " + captureInfo);
        if (captureInfo == null) {
            Log.d(TAG, "<writeStereoCaptureInfo> captureInfo is null!");
            TraceHelper.endSection();
            return null;
        } else {
            if (ENABLE_BUFFER_DUMP) {
                dumpFileBeforeWrite(captureInfo);
            }

            ArcSoftPackerManager packerManager = new ArcSoftPackerManager();
            PackInfo packInfo = new PackInfo();
            packInfo.unpackedJpgBuf = captureInfo.jpgBuffer;
            ArcSoftStereoCaptureInfoParser stereoCaptureInfoParser =
                    new ArcSoftStereoCaptureInfoParser(packInfo.unpackedStandardXmpBuf,
                            packInfo.unpackedExtendedXmpBuf, captureInfo);
            stereoCaptureInfoParser.write();
            serialize(packInfo, stereoCaptureInfoParser);
            byte[] result = packerManager.pack(packInfo);
            TraceHelper.endSection();
            return result;
        }
    }

    public ArcSoftStereoCaptureInfo readRefocusImage(byte[] fileBuffer) {
        TraceHelper.beginSection(">>>>ArcSoftStereoInfoAccessor-readRefocusImage");
        if (fileBuffer == null) {
            Log.d(TAG, "<readRefocusImage> fileBuffer is null!");
            TraceHelper.endSection();
        } else {
            Log.d(TAG, "<readRefocusImage> fileBuffer length  = 0x" + Integer.toHexString(
                    fileBuffer.length) + "("
                    + fileBuffer.length + ")");
            ArcSoftStereoConfigInfo configInfo = new ArcSoftStereoConfigInfo();
            ArcSoftStereoCaptureInfo captureInfo = new ArcSoftStereoCaptureInfo();
            ArcSoftPackerManager packerManager = new ArcSoftPackerManager();
            PackInfo packInfo = packerManager.unpack(fileBuffer);
            ArcSoftStereoConfigInfoParser stereoConfigInfoParser =
                    new ArcSoftStereoConfigInfoParser(packInfo.unpackedStandardXmpBuf,
                            packInfo.unpackedExtendedXmpBuf, configInfo);
            stereoConfigInfoParser.read();
            captureInfo.jpgBuffer = fileBuffer;
            captureInfo.bayerBuffer = packInfo.unpackedCustomizedBufMap.get(
                    ArcSoftPackUtils.TYPE_BAYER_DATA);
            captureInfo.jpsBuffer = packInfo.unpackedCustomizedBufMap.get(
                    ArcSoftPackUtils.TYPE_JPS_DATA);
            captureInfo.configBuffer = configInfo.toJSONString().getBytes();
            captureInfo.calibrationBuffer = packInfo.unpackedCustomizedBufMap.get(
                    ArcSoftPackUtils.TYPE_CALIBRATION_DATA);
            if (ENABLE_BUFFER_DUMP) {
                dumpFileAfterRead(captureInfo);
            }
            TraceHelper.endSection();
            return captureInfo;
        }

        return null;
    }

    private void serialize(PackInfo info, IParser parser) {
        SerializedInfo serializedInfo = parser.serialize();
        info.unpackedStandardXmpBuf = serializedInfo.standardXmpBuf;
        info.unpackedExtendedXmpBuf = serializedInfo.extendedXmpBuf;
        info.unpackedCustomizedBufMap = serializedInfo.customizedBufMap;
    }

    private void dumpFileBeforeWrite(ArcSoftStereoCaptureInfo captureInfo) {
        ArcSoftPackUtils.saveFile(DUMP_FILE_FOLDER + "/jpgBuffer.data", captureInfo.jpgBuffer);
        ArcSoftPackUtils.saveFile(DUMP_FILE_FOLDER + "/bayer.data", captureInfo.bayerBuffer);
        ArcSoftPackUtils.saveFile(DUMP_FILE_FOLDER + "/depthMap.data", captureInfo.jpsBuffer);
        ArcSoftPackUtils.saveFile(DUMP_FILE_FOLDER + "/config.data", captureInfo.configBuffer);
        ArcSoftPackUtils.saveFile(DUMP_FILE_FOLDER + "/calibrationData.data",
                captureInfo.calibrationBuffer);
    }

    private void dumpFileAfterRead(ArcSoftStereoCaptureInfo captureInfo) {
        ArcSoftPackUtils.saveFile(DUMP_FILE_FOLDER + "/jpgBuffer1.data", captureInfo.jpgBuffer);
        ArcSoftPackUtils.saveFile(DUMP_FILE_FOLDER + "/bayer1.data", captureInfo.bayerBuffer);
        ArcSoftPackUtils.saveFile(DUMP_FILE_FOLDER + "/depthMap1.data", captureInfo.jpsBuffer);
        ArcSoftPackUtils.saveFile(DUMP_FILE_FOLDER + "/config1.data", captureInfo.configBuffer);
        ArcSoftPackUtils.saveFile(DUMP_FILE_FOLDER + "/calibrationData1.data",
                captureInfo.calibrationBuffer);
    }

    public static ArcSoftStereoConfigInfo parseConfigJsonData(byte[] configBuffer) {
        ArcSoftStereoInfoJsonParser stereoInfoJsonParser = new ArcSoftStereoInfoJsonParser(
                configBuffer);
        ArcSoftStereoConfigInfo configInfo = new ArcSoftStereoConfigInfo();
        configInfo.orientation = stereoInfoJsonParser.getOrientation();
        configInfo.touchCoordX1st = stereoInfoJsonParser.getTouchCoordX1st();
        configInfo.touchCoordY1st = stereoInfoJsonParser.getTouchCoordY1st();
        configInfo.dofLevel = stereoInfoJsonParser.getDofLevel();
        configInfo.i32MainWidth_CropNoScale = stereoInfoJsonParser.getMainWidth();
        configInfo.i32MainHeight_CropNoScale = stereoInfoJsonParser.getMainHeight();
        configInfo.i32AuxWidth_CropNoScale = stereoInfoJsonParser.getAuxWidth();
        configInfo.i32AuxHeight_CropNoScale = stereoInfoJsonParser.getAuxHeight();
        return configInfo;
    }
}
