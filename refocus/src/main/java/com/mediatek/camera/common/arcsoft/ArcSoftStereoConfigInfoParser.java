package com.mediatek.camera.common.arcsoft;

import com.mediatek.accessor.meta.data.DataItem;
import com.mediatek.accessor.operator.IMetaOperator;
import com.mediatek.accessor.operator.MetaOperatorFactory;
import com.mediatek.accessor.parser.SerializedInfo;
import com.mediatek.accessor.util.Log;
import com.mediatek.accessor.util.TraceHelper;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by chengjian on 4/2/18.
 */
public class ArcSoftStereoConfigInfoParser {
    private static final String TAG = Log.Tag(ArcSoftStereoConfigInfoParser.class.getSimpleName());
    private IMetaOperator mStandardMetaOperator;
    private IMetaOperator mExtendedMetaOperator;
    private DataItem.DataCollections mStandardDataCollections = new DataItem.DataCollections();
    private DataItem.DataCollections mExtendardDataCollections = new DataItem.DataCollections();

    private ArrayList<DataItem.SimpleItem> mListOfExtendedItem = new ArrayList();
    private ArrayList<DataItem.SimpleItem> mListOfStandardItem = new ArrayList();
    private ArcSoftStereoConfigInfo mStereoConfigInfo;
    private static final String NAMESPACE_PREFIX = "TPlinkRefocus";//XMPInfo

    public ArcSoftStereoConfigInfoParser(byte[] standardBuffer, byte[] extendedBuffer,
            ArcSoftStereoConfigInfo info) {
        mStandardDataCollections.dest = 0;
        mExtendardDataCollections.dest = 0;
        mStereoConfigInfo = info;
        initStandardItem();
        initExtendedValue();
        mStandardDataCollections.listOfSimpleValue = mListOfStandardItem;
        mStandardMetaOperator = MetaOperatorFactory.getOperatorInstance(0, standardBuffer,
                (Map) null);

        try {
            mStandardMetaOperator.setData(mStandardDataCollections);
        } catch (NullPointerException var8) {
            var8.printStackTrace();
        }

        mExtendardDataCollections.listOfSimpleValue = mListOfExtendedItem;
        mExtendedMetaOperator = MetaOperatorFactory.getOperatorInstance(0, extendedBuffer,
                (Map) null);

        try {
            mExtendedMetaOperator.setData(mExtendardDataCollections);
        } catch (NullPointerException var7) {
            var7.printStackTrace();
        }
    }

    public ArcSoftStereoConfigInfoParser(IMetaOperator standardMetaOperator,
            IMetaOperator extendedMetaOperator, ArcSoftStereoConfigInfo info) {
        mStandardDataCollections.dest = 0;
        mExtendardDataCollections.dest = 0;
        mStereoConfigInfo = info;
        initStandardItem();
        initExtendedValue();
        mStandardDataCollections.listOfSimpleValue = mListOfStandardItem;
        mStandardMetaOperator = standardMetaOperator;

        try {
            mStandardMetaOperator.setData(mStandardDataCollections);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        mExtendardDataCollections.listOfSimpleValue = mListOfExtendedItem;
        mExtendedMetaOperator = extendedMetaOperator;

        try {
            mExtendedMetaOperator.setData(mExtendardDataCollections);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void read() {
        TraceHelper.beginSection(">>>>StereoConfigInfoParser-read");
        Log.d(TAG, "<read>");
        if (mStandardMetaOperator != null) {
            mStandardMetaOperator.read();
        }

        if (mExtendedMetaOperator != null) {
            mExtendedMetaOperator.read();
        }

        if (mStereoConfigInfo == null) {
            Log.d(TAG, "<read> mStereoConfigInfo is null!");
            TraceHelper.endSection();
        } else {
            readExtendedValue();
            TraceHelper.endSection();
        }
    }

    public void write() {
        TraceHelper.beginSection(">>>>StereoConfigInfoParser-write");
        Log.d(TAG, "<write>");
        if (mStereoConfigInfo == null) {
            Log.d(TAG, "<write> mStereoConfigInfo is null!");
            TraceHelper.endSection();
        } else {
            writeExtendedValue();
            if (mStandardMetaOperator != null) {
                mStandardMetaOperator.write();
            }

            if (mExtendedMetaOperator != null) {
                mExtendedMetaOperator.write();
            }

            TraceHelper.endSection();
        }
    }

    public SerializedInfo serialize() {
        TraceHelper.beginSection(">>>>StereoConfigInfoParser-serialize");
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

        TraceHelper.endSection();
        return info;
    }

    private void writeExtendedValue() {
        int simpleValueItemCount = mListOfExtendedItem.size();

        for (int i = 0; i < simpleValueItemCount; ++i) {
            if (mListOfExtendedItem.get(i) == null) {
                Log.d(TAG, "mListOfExtendedItem.get(i) is null!");
            } else if ("Orientation".equals(
                    ((DataItem.SimpleItem) mListOfExtendedItem.get(i)).name)) {
                ((DataItem.SimpleItem) mListOfExtendedItem.get(i)).value = String.valueOf(
                        mStereoConfigInfo.orientation);
            } else if ("TouchCoordX1st".equals(
                    ((DataItem.SimpleItem) mListOfExtendedItem.get(i)).name)) {
                ((DataItem.SimpleItem) mListOfExtendedItem.get(i)).value = String.valueOf(
                        mStereoConfigInfo.touchCoordX1st);
            } else if ("TouchCoordY1st".equals(
                    ((DataItem.SimpleItem) mListOfExtendedItem.get(i)).name)) {
                ((DataItem.SimpleItem) mListOfExtendedItem.get(i)).value = String.valueOf(
                        mStereoConfigInfo.touchCoordY1st);
            } else if ("DOF".equals(((DataItem.SimpleItem) mListOfExtendedItem.get(i)).name)) {
                ((DataItem.SimpleItem) mListOfExtendedItem.get(i)).value = String.valueOf(
                        mStereoConfigInfo.dofLevel);
            } else if ("I32MainWidth_CropNoScale".equals(
                    ((DataItem.SimpleItem) mListOfExtendedItem.get(i)).name)) {
                ((DataItem.SimpleItem) mListOfExtendedItem.get(i)).value = String.valueOf(
                        mStereoConfigInfo.i32MainWidth_CropNoScale);
            } else if ("I32MainHeight_CropNoScale".equals(
                    ((DataItem.SimpleItem) mListOfExtendedItem.get(i)).name)) {
                ((DataItem.SimpleItem) mListOfExtendedItem.get(i)).value = String.valueOf(
                        mStereoConfigInfo.i32MainHeight_CropNoScale);
            } else if ("I32AuxWidth_CropNoScale".equals(
                    ((DataItem.SimpleItem) mListOfExtendedItem.get(i)).name)) {
                ((DataItem.SimpleItem) mListOfExtendedItem.get(i)).value = String.valueOf(
                        mStereoConfigInfo.i32AuxWidth_CropNoScale);
            } else if ("I32AuxHeight_CropNoScale".equals(
                    ((DataItem.SimpleItem) mListOfExtendedItem.get(i)).name)) {
                ((DataItem.SimpleItem) mListOfExtendedItem.get(i)).value = String.valueOf(
                        mStereoConfigInfo.i32AuxHeight_CropNoScale);
            }
        }

        mExtendardDataCollections.listOfSimpleValue = mListOfExtendedItem;
    }

    private void readExtendedValue() {
        DataItem.SimpleItem simpleValue = null;
        int simpleValueItemCount = mListOfExtendedItem.size();

        for (int i = 0; i < simpleValueItemCount; ++i) {
            simpleValue = (DataItem.SimpleItem) this.mListOfExtendedItem.get(i);
            if (simpleValue != null && simpleValue.value != null
                    && simpleValue.value.length() != 0) {
                if ("Orientation".equals(simpleValue.name)) {
                    mStereoConfigInfo.orientation = Integer.parseInt(simpleValue.value);
                } else if ("TouchCoordX1st".equals(simpleValue.name)) {
                    mStereoConfigInfo.touchCoordX1st = Integer.parseInt(simpleValue.value);
                } else if ("TouchCoordY1st".equals(simpleValue.name)) {
                    mStereoConfigInfo.touchCoordY1st = Integer.parseInt(simpleValue.value);
                } else if ("DOF".equals(simpleValue.name)) {
                    mStereoConfigInfo.dofLevel = Integer.parseInt(simpleValue.value);
                } else if ("I32MainWidth_CropNoScale".equals(simpleValue.name)) {
                    mStereoConfigInfo.i32MainWidth_CropNoScale = Integer.parseInt(
                            simpleValue.value);
                } else if ("I32MainHeight_CropNoScale".equals(simpleValue.name)) {
                    mStereoConfigInfo.i32MainHeight_CropNoScale = Integer.parseInt(
                            simpleValue.value);
                } else if ("I32AuxWidth_CropNoScale".equals(simpleValue.name)) {
                    mStereoConfigInfo.i32AuxWidth_CropNoScale = Integer.parseInt(simpleValue.value);
                } else if ("I32AuxHeight_CropNoScale".equals(simpleValue.name)) {
                    mStereoConfigInfo.i32AuxHeight_CropNoScale = Integer.parseInt(
                            simpleValue.value);
                }
            }
        }

        mExtendardDataCollections.listOfSimpleValue = mListOfExtendedItem;
    }

    private void initExtendedValue() {
        DataItem.SimpleItem simpleValue = getSimpleValueInstance();
        simpleValue.name = "Orientation";
        mListOfExtendedItem.add(simpleValue);
        simpleValue = getSimpleValueInstance();
        simpleValue.name = "TouchCoordX1st";
        mListOfExtendedItem.add(simpleValue);
        simpleValue = getSimpleValueInstance();
        simpleValue.name = "TouchCoordY1st";
        mListOfExtendedItem.add(simpleValue);
        simpleValue = getSimpleValueInstance();
        simpleValue.name = "DOF";
        mListOfExtendedItem.add(simpleValue);
        simpleValue = getSimpleValueInstance();
        simpleValue.name = "I32MainWidth_CropNoScale";
        mListOfExtendedItem.add(simpleValue);
        simpleValue = getSimpleValueInstance();
        simpleValue.name = "I32MainHeight_CropNoScale";
        mListOfExtendedItem.add(simpleValue);
        simpleValue = getSimpleValueInstance();
        simpleValue.name = "I32AuxWidth_CropNoScale";
        mListOfExtendedItem.add(simpleValue);
        simpleValue = getSimpleValueInstance();
        simpleValue.name = "I32AuxHeight_CropNoScale";
        mListOfExtendedItem.add(simpleValue);
    }

    private void initStandardItem() {
        DataItem.SimpleItem simpleValue = new DataItem.SimpleItem();
        simpleValue.dest = 0;
        simpleValue.nameSpaceItem = new DataItem.NameSpaceItem();
        simpleValue.nameSpaceItem.dest = 0;
        simpleValue.nameSpaceItem.nameSpace = "http://ns.google.com/photos/1.0/depthmap/";
        simpleValue.nameSpaceItem.nameSpacePrifix = NAMESPACE_PREFIX;
        simpleValue.name = "RoiX";
        simpleValue.value = "1000";
        mListOfStandardItem.add(simpleValue);
        simpleValue = new DataItem.SimpleItem();
        simpleValue.dest = 0;
        simpleValue.nameSpaceItem = new DataItem.NameSpaceItem();
        simpleValue.nameSpaceItem.dest = 0;
        simpleValue.nameSpaceItem.nameSpace = "http://ns.adobe.com/xmp/note/";
        simpleValue.nameSpaceItem.nameSpacePrifix = "xmpNote";
        simpleValue.name = "HasExtendedXMP";
        simpleValue.value = "BB6E7F4B665491381B0E22B855CE46F2";
        mListOfStandardItem.add(simpleValue);
    }

    private DataItem.SimpleItem getSimpleValueInstance() {
        DataItem.SimpleItem simpleValue = new DataItem.SimpleItem();
        simpleValue.dest = 0;
        simpleValue.nameSpaceItem = new DataItem.NameSpaceItem();
        simpleValue.nameSpaceItem.dest = 0;
        simpleValue.nameSpaceItem.nameSpace = "http://ns.google.com/photos/1.0/depthmap/";
        simpleValue.nameSpaceItem.nameSpacePrifix = NAMESPACE_PREFIX;
        return simpleValue;
    }
}
