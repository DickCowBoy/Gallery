package com.mediatek.camera.common.arcsoft;

import com.mediatek.accessor.packer.IPacker;
import com.mediatek.accessor.packer.PackInfo;
import com.mediatek.accessor.packer.PackerManager;
import com.mediatek.accessor.packer.XmpPacker;
import com.mediatek.accessor.util.Log;

/**
 * Created by chengjian on 3/31/18.
 */
public class ArcSoftPackerManager {
    private static final String TAG = Log.Tag(PackerManager.class.getSimpleName());
    private IPacker mCustDataPacker;
    private IPacker mJpgPacker;
    private IPacker mXmpPacker;

    public ArcSoftPackerManager() {
    }

    public byte[] pack(PackInfo packInfo) {
        Log.d(TAG, "<pack>");
        this.mXmpPacker = new XmpPacker(packInfo);
        this.mXmpPacker.pack();

        this.mCustDataPacker = new ArcSoftCustomizedDataPacker(packInfo);
        this.mCustDataPacker.pack();

        this.mJpgPacker = new ArcSoftJpgPacker(packInfo);
        this.mJpgPacker.pack();

        return packInfo.packedJpgBuf;
    }

    public PackInfo unpack(byte[] src) {
        Log.d(TAG, "<unpack>");
        PackInfo packInfo = new PackInfo();
        packInfo.packedJpgBuf = src;
        this.mJpgPacker = new ArcSoftJpgPacker(packInfo);
        this.mJpgPacker.unpack();
        this.mXmpPacker = new XmpPacker(packInfo);
        this.mXmpPacker.unpack();
        this.mCustDataPacker = new ArcSoftCustomizedDataPacker(packInfo);
        this.mCustDataPacker.unpack();
        return packInfo;
    }
}
