package com.mediatek.camera.common.arcsoft;

import com.mediatek.accessor.packer.IPacker;
import com.mediatek.accessor.packer.PackInfo;
import com.mediatek.accessor.packer.PackUtils;
import com.mediatek.accessor.util.Log;
import com.mediatek.accessor.util.TraceHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by chengjian on 4/2/18.
 */

public class ArcSoftCustomizedDataPacker implements IPacker {
    private static final String TAG = Log.Tag(ArcSoftCustomizedDataPacker.class.getSimpleName());
    private PackInfo mPackInfo;

    public ArcSoftCustomizedDataPacker(PackInfo packInfo) throws NullPointerException {
        mPackInfo = packInfo;
        if (mPackInfo == null) {
            throw new NullPointerException("mPackInfo is null!");
        }
    }

    public void pack() {
        TraceHelper.beginSection(">>>>CustomizedDataPacker-pack");
        Log.d(TAG, "<pack> begin");
        if (mPackInfo == null) {
            Log.d(TAG, "<pack> mPackInfo is null!");
            TraceHelper.endSection();
        } else if (mPackInfo.unpackedCustomizedBufMap == null) {
            Log.d(TAG, "<pack> unpackedCustomizedBufMap is null!");
            TraceHelper.endSection();
        } else {
            ArrayList<byte[]> packedCustomizedBufArray = new ArrayList();
            Iterator it = mPackInfo.unpackedCustomizedBufMap.entrySet().iterator();

            while (it.hasNext()) {
                Map.Entry<String, byte[]> entry = (Map.Entry) it.next();
                byte[] typeBuffer = ((String) entry.getKey()).getBytes();
                if (typeBuffer != null) {
                    ArrayList<byte[]> custDst = pack((byte[]) entry.getValue(), typeBuffer);
                    if (!custDst.isEmpty()) {
                        packedCustomizedBufArray.addAll(custDst);
                    }
                }
            }

            mPackInfo.packedCustomizedBufArray = packedCustomizedBufArray;
            Log.d(TAG, "<pack> end");
            TraceHelper.endSection();
        }
    }

    public void unpack() {
        TraceHelper.beginSection(">>>>CustomizedDataPacker-unpack");
        Log.d(TAG, "<unpack> begin");
        if (mPackInfo == null) {
            Log.d(TAG, "<unpack> mPackInfo is null!");
            TraceHelper.endSection();
        } else if (mPackInfo.packedCustomizedBufArray == null) {
            Log.d(TAG, "<unpack> packedCustomizedBufArray is null!");
            TraceHelper.endSection();
        } else {
            mPackInfo.unpackedCustomizedBufMap = new HashMap();
            int bufferCount = mPackInfo.packedCustomizedBufArray.size();
            byte[] bufferType = new byte[7];
            String type = "";
            Map<String, ArrayList<byte[]>> categoryCustomizedBufMap = new HashMap();

            for (int i = 0; i < bufferCount; ++i) {
                byte[] section = (byte[]) mPackInfo.packedCustomizedBufArray.get(i);
                if (section != null) {
                    byte[] bufferTemp = new byte[section.length - 12];
                    System.arraycopy(section, 12, bufferTemp, 0, bufferTemp.length);
                    System.arraycopy(section, 4, bufferType, 0, 7);
                    type = new String(bufferType);
                    if (categoryCustomizedBufMap.containsKey(type)) {
                        ((ArrayList) categoryCustomizedBufMap.get(type)).add(bufferTemp);
                    } else {
                        ArrayList<byte[]> customizedBuffer = new ArrayList();
                        customizedBuffer.add(bufferTemp);
                        categoryCustomizedBufMap.put(type, customizedBuffer);
                    }
                }
            }

            Iterator it = categoryCustomizedBufMap.entrySet().iterator();

            while (it.hasNext()) {
                Map.Entry<String, ArrayList<byte[]>> entry = (Map.Entry) it.next();
                String typeName = (String) entry.getKey();
                Log.d(TAG, "<unpack> typeName " + typeName);
                if (typeName != null && entry.getValue() != null) {
                    mPackInfo.unpackedCustomizedBufMap.put(typeName,
                            joinArraryBuffer((ArrayList) entry.getValue()));
                }
            }

            Log.d(TAG, "<unpack> end");
            TraceHelper.endSection();
        }
    }

    private byte[] joinArraryBuffer(ArrayList<byte[]> bufferArrary) {
        int bufferLength = 0;
        int count = bufferArrary.size();

        for (int i = 0; i < count; ++i) {
            bufferLength += ((byte[]) bufferArrary.get(i)).length;
        }

        byte[] buffer = new byte[bufferLength];
        int currentPos = 0;

        for (int i = 0; i < count; ++i) {
            System.arraycopy((byte[]) bufferArrary.get(i), 0, buffer, currentPos,
                    ((byte[]) bufferArrary.get(i)).length);
            currentPos += ((byte[]) bufferArrary.get(i)).length;
        }

        return buffer;
    }

    private ArrayList<byte[]> pack(byte[] bufferData, byte[] type) {
        String typeName = new String(type);
        Log.d(TAG, "<pack> type name is " + typeName);
        ArrayList custDst = new ArrayList();
        int maxBufferContentLength = 'ﾦ';

        int sectionCount = 0;
        if (bufferData.length % maxBufferContentLength == 0) {
            sectionCount = bufferData.length / maxBufferContentLength;
        } else {
            sectionCount = bufferData.length / maxBufferContentLength + 1;
        }
        byte[] section;
        byte[] bufferTotalLength;
        byte[] serialNumber;
        int bufferCurrentPos = 0;
        int sectionCurrentPos = 0;
        int sectionLen;
        for (int i = 0; i < sectionCount; ++i) {
            if (i == sectionCount - 1 && bufferData.length % maxBufferContentLength != 0) {
                sectionLen = bufferData.length % maxBufferContentLength + 12;
            } else {
                sectionLen = 'ﾲ';
            }
            section = new byte[sectionLen];
            bufferTotalLength = PackUtils.intToByteBuffer(bufferData.length, 4);
            System.arraycopy(bufferTotalLength, 0, section, sectionCurrentPos, 4);
            sectionCurrentPos += 4;
            System.arraycopy(type, 0, section, sectionCurrentPos, 7);
            sectionCurrentPos += 7;
            serialNumber = PackUtils.intToByteBuffer(i, 1);
            System.arraycopy(serialNumber, 0, section, sectionCurrentPos, 1);
            ++sectionCurrentPos;
            int copyDataLength = sectionLen - 12;
            System.arraycopy(bufferData, bufferCurrentPos, section, sectionCurrentPos,
                    copyDataLength);
            bufferCurrentPos += copyDataLength;
            sectionCurrentPos = 0;
            custDst.add(section);
        }
        return custDst;
    }
}

