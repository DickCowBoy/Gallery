package com.mediatek.camera.common.arcsoft;

import com.mediatek.accessor.data.Section;
import com.mediatek.accessor.packer.IPacker;
import com.mediatek.accessor.packer.PackInfo;
import com.mediatek.accessor.packer.PackUtils;
import com.mediatek.accessor.util.ByteArrayInputStreamExt;
import com.mediatek.accessor.util.ByteArrayOutputStreamExt;
import com.mediatek.accessor.util.Log;
import com.mediatek.accessor.util.TraceHelper;
import com.mediatek.accessor.util.Utils;

import java.io.IOException;
import java.util.ArrayList;

public class ArcSoftJpgPacker implements IPacker {
    private static final String TAG = Log.Tag(ArcSoftJpgPacker.class.getSimpleName());
    private PackInfo mPackInfo;

    public ArcSoftJpgPacker(PackInfo packInfo) throws NullPointerException {
        mPackInfo = packInfo;
        if (mPackInfo == null) {
            throw new NullPointerException("mPackInfo is null!");
        }
    }

    public void pack() {
        TraceHelper.beginSection(">>>>JpgPacker-pack");
        Log.d(TAG, "<pack> begin");
        if (mPackInfo == null) {
            Log.d(TAG, "<pack> mPackInfo is null!");
            TraceHelper.endSection();
        } else if (mPackInfo.unpackedJpgBuf == null) {
            Log.d(TAG, "<pack> unpackedJpgBuf is null!");
        } else {
            Section standardSection = null;
            ArrayList<Section> extendedSections = new ArrayList();
            ArrayList<Section> customizedSections = new ArrayList();
            if (mPackInfo.packedStandardXmpBuf != null) {
                standardSection = new Section('￡', 0L, mPackInfo.packedStandardXmpBuf.length + 2);
                standardSection.buffer = mPackInfo.packedStandardXmpBuf;
                standardSection.type = "standardXmp";
            }

            if (mPackInfo.packedExtendedXmpBufArray != null) {
                extendedSections = makeJpgSections('￡', mPackInfo.packedExtendedXmpBufArray);
            }

            if (mPackInfo.packedCustomizedBufArray != null) {
                customizedSections = makeJpgSections('\uffef', mPackInfo.packedCustomizedBufArray);
            }

            ByteArrayInputStreamExt is = new ByteArrayInputStreamExt(mPackInfo.unpackedJpgBuf);
            ByteArrayOutputStreamExt os = new ByteArrayOutputStreamExt();
            pack(is, os, standardSection, extendedSections, customizedSections);
            mPackInfo.packedJpgBuf = os.toByteArray();

            try {
                is.close();
                os.close();
            } catch (IOException var7) {
                var7.printStackTrace();
            }

            Log.d(TAG, "<pack> end");
            TraceHelper.endSection();
        }
    }

    public void unpack() {
        TraceHelper.beginSection(">>>>JpgPacker-unpack");
        Log.d(TAG, "<unpack> begin");
        if (mPackInfo == null) {
            Log.d(TAG, "<unpack> mPackInfo is null!");
            TraceHelper.endSection();
        } else if (mPackInfo.packedJpgBuf == null) {
            Log.d(TAG, "<unpack> packedJpgBuf is null!");
        } else {
            ByteArrayInputStreamExt inputStream = new ByteArrayInputStreamExt(
                    mPackInfo.packedJpgBuf);
            ArrayList<Section> srcJpgSections = ArcSoftPackUtils.parseAppInfoFromStream(
                    inputStream);
            byte[] standardXmp = null;
            ArrayList<byte[]> extendedXmp = new ArrayList();
            ArrayList<byte[]> custDataBuffer = new ArrayList();
            int srcJpgSectionsSize = srcJpgSections.size();

            try {
                for (int i = 0; i < srcJpgSectionsSize; ++i) {
                    Section sec = (Section) srcJpgSections.get(i);
                    if ("standardXmp".equals(sec.type)) {
                        inputStream.seek(sec.offset + 4L);
                        standardXmp = new byte[sec.length - 2];
                        inputStream.read(standardXmp);
                    }

                    byte[] customXmp;
                    if ("extendedXmp".equals(sec.type)) {
                        inputStream.seek(sec.offset + 4L);
                        customXmp = new byte[sec.length - 2];
                        inputStream.read(customXmp);
                        extendedXmp.add(customXmp);
                    }

                    if (ArcSoftPackUtils.TYPE_BAYER_DATA.equals(sec.type)
                            || ArcSoftPackUtils.TYPE_JPS_DATA.equals(sec.type)
                            || ArcSoftPackUtils.TYPE_CONFIG_DATA.equals(sec.type)
                            || ArcSoftPackUtils.TYPE_CALIBRATION_DATA.equals(sec.type)) {
                        inputStream.seek(sec.offset + 4L);
                        customXmp = new byte[sec.length - 2];
                        inputStream.read(customXmp);
                        custDataBuffer.add(customXmp);
                    }
                }
            } catch (IOException var10) {
                var10.printStackTrace();
            }

            mPackInfo.packedStandardXmpBuf = standardXmp;
            mPackInfo.packedExtendedXmpBufArray = extendedXmp;
            mPackInfo.packedCustomizedBufArray = custDataBuffer;
            Log.d(TAG, "<unpack> end");
            TraceHelper.endSection();
        }
    }

    private ArrayList<Section> makeJpgSections(int marker, ArrayList<byte[]> sections) {
        Log.d(TAG, "<makeJpgSections>");
        ArrayList<Section> jpgSections = new ArrayList();
        int bufferCount = sections.size();

        for (int i = 0; i < bufferCount; ++i) {
            byte[] buffer = (byte[]) sections.get(i);
            if (buffer != null) {
                Section section = new Section(marker, 0L, buffer.length + 2);
                if (marker == '￡') {
                    Utils.logD(TAG, "<makeJpgSections> type is TYPE_EXTENDED_XMP");
                    section.type = "extendedXmp";
                } else {
                    String typename = PackUtils.getCustomTypeName(buffer);
                    Utils.logD(TAG, "<makeJpgSections> type is " + typename);
                    section.type = typename;
                }

                section.buffer = buffer;
                jpgSections.add(section);
            }
        }

        return jpgSections;
    }

    private void pack(ByteArrayInputStreamExt is, ByteArrayOutputStreamExt os,
            Section standardSection, ArrayList<Section> extendedSections,
            ArrayList<Section> customizedSections) {
        Log.d(TAG, "<pack> write begin!!!");
        ArrayList<Section> srcJpgSections = PackUtils.parseAppInfoFromStream(is);
        os.writeShort('\uffd8');
        int writenLocation = PackUtils.findProperLocationForXmp(srcJpgSections);
        boolean hasWritenXmp = false;
        boolean hasWritenCustomizedData = false;
        boolean hasWritenBlurImage = false;
        if (writenLocation == 0) {
            Log.d(TAG, "<pack> No APP1 information!");
            writeXmp(os, standardSection, extendedSections);
            hasWritenXmp = true;
        }

        for (int i = 0; i < srcJpgSections.size(); ++i) {
            Section sec = (Section) srcJpgSections.get(i);
            if ("exif".equals(sec.type)) {
                Log.d(TAG, "<pack> write exif, " + PackUtils.getSectionTag(sec));
                PackUtils.writeSectionToStream(is, os, sec);
                if (!hasWritenXmp) {
                    writeXmp(os, standardSection, extendedSections);
                    hasWritenXmp = true;
                }
            } else {
                if (!hasWritenXmp) {
                    Log.d(TAG, "<pack> write xmp, " + PackUtils.getSectionTag(sec));
                    writeXmp(os, standardSection, extendedSections);
                    hasWritenXmp = true;
                }

                if (!hasWritenCustomizedData && (sec.marker == 'ￛ' || sec.marker == 'ￄ')) {
                    Log.d(TAG, "<pack> write custom, " + PackUtils.getSectionTag(sec));
                    writeCust(os, customizedSections);
                    hasWritenCustomizedData = true;
                }

                if (!ArcSoftPackUtils.TYPE_BAYER_DATA.equals(sec.type)
                        && !ArcSoftPackUtils.TYPE_JPS_DATA.equals(sec.type)
                        && !ArcSoftPackUtils.TYPE_CONFIG_DATA.equals(sec.type)
                        && !ArcSoftPackUtils.TYPE_CALIBRATION_DATA.equals(sec.type)
                        && !"standardXmp".equals(sec.type)
                        && !"extendedXmp".equals(sec.type)) {
                    Utils.logD(TAG, "<pack> write other info, " + PackUtils.getSectionTag(sec));
                    PackUtils.writeSectionToStream(is, os, sec);
                } else {
                    is.skip((long) (sec.length + 2));
                    Utils.logD(TAG, "<pack> skip old data, type: " + sec.type);
                }
            }
        }

        if (!hasWritenCustomizedData) {
            writeCust(os, customizedSections);
        }

        if (!hasWritenBlurImage) {
            Log.d(TAG, "<pack> write remain whole file (from SOS)");
            PackUtils.copyToStreamWithFixBuffer(is, os);
        }

        Log.d(TAG, "<pack> write end!!!");
    }

    private void writeCust(ByteArrayOutputStreamExt os, ArrayList<Section> customizedSections) {
        int customizedSectionsSize = customizedSections.size();
        Log.d(TAG, "<writeCust> customizedSections size " + customizedSectionsSize);
        if (customizedSectionsSize != 0) {
            for (int i = 0; i < customizedSectionsSize; ++i) {
                PackUtils.writeSectionToStream(os, (Section) customizedSections.get(i));
            }

        }
    }

    private void writeXmp(ByteArrayOutputStreamExt os, Section standardSection,
            ArrayList<Section> extendedSections) {
        if (standardSection != null) {
            Log.d(TAG, "<writeXmp> standardxmp");
            PackUtils.writeSectionToStream(os, standardSection);
        }

        int extendedSectionsSize = extendedSections.size();
        Log.d(TAG, "<writeXmp> extendedSectionsSize size " + extendedSectionsSize);
        if (extendedSectionsSize != 0) {
            for (int i = 0; i < extendedSectionsSize; ++i) {
                PackUtils.writeSectionToStream(os, (Section) extendedSections.get(i));
            }

        }
    }
}
