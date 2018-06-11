package com.mediatek.camera.common.arcsoft;

import com.mediatek.accessor.data.Section;
import com.mediatek.accessor.packer.PackUtils;
import com.mediatek.accessor.util.ByteArrayInputStreamExt;
import com.mediatek.accessor.util.Log;
import com.mediatek.accessor.util.Utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Created by chengjian on 4/2/18.
 */
public class ArcSoftPackUtils {
    private static final String TAG = Log.Tag(ArcSoftPackUtils.class.getSimpleName());

    public static final String TYPE_BAYER_DATA = "BAYERBF";
    public static final String TYPE_DEPTH_DATA = "DEPTHBF";
    public static final String TYPE_CALIBRATION_DATA = "CALIBBF";
    public static final String TYPE_CONFIG_DATA = "CONFIBF";

    private ArcSoftPackUtils() {
    }

    public static ArrayList<Section> parseAppInfoFromStream(ByteArrayInputStreamExt is) {
        if (is == null) {
            Log.d(TAG, "<parseAppInfoFromStream> input stream is null!!!");
            return new ArrayList();
        } else {
            try {
                is.seek(0L);
                int value = is.readUnsignedShort();
                if (value != '\uffd8') {
                    Log.d(TAG, "<parseAppInfoFromStream> error, find no SOI");
                    return new ArrayList();
                } else {
                    Log.d(TAG, "<parseAppInfoFromStream> parse begin!!!");
                    ArrayList sections = new ArrayList();

                    while ((value = is.readUnsignedShort()) != -1 && value != 'ￚ') {
                        long offset = is.getFilePointer() - 2L;
                        int length = is.readUnsignedShort();
                        sections.add(new Section(value, offset, length));
                        is.skip((long) (length - 2));
                    }

                    for (int i = 0; i < sections.size(); ++i) {
                        checkAppSectionTypeInStream(is, (Section) sections.get(i));
                        Utils.logD(TAG, "<parseAppInfoFromStream> " + PackUtils.getSectionTag(
                                (Section) sections.get(i)));
                    }

                    is.seek(0L);
                    Log.d(TAG, "<parseAppInfoFromStream> parse end!!!");
                    return sections;
                }
            } catch (IOException var8) {
                Log.e(TAG, "<parseAppInfoFromStream> IOException ", var8);
                return new ArrayList();
            }
        }
    }

    private static void checkAppSectionTypeInStream(ByteArrayInputStreamExt is, Section section) {
        if (is != null && section != null) {
            byte[] buffer = null;
            String str = null;

            try {
                if (section.marker == '\uffef') {
                    is.seek(section.offset + 4L + 4L);
                    buffer = new byte[7];
                    is.read(buffer, 0, buffer.length);
                    str = new String(buffer);
                    if (!TYPE_BAYER_DATA.equals(str)
                            && !TYPE_DEPTH_DATA.equals(str)
                            && !TYPE_CONFIG_DATA.equals(str)
                            && !TYPE_CALIBRATION_DATA.equals(str)) {
                        section.type = "unknownApp15";
                        return;
                    }

                    section.type = str;
                    return;
                }

                if (section.marker == '￡') {
                    is.seek(section.offset + 4L);
                    buffer = new byte["http://ns.adobe.com/xmp/extension/".length()];
                    is.read(buffer, 0, buffer.length);
                    str = new String(buffer);
                    if ("http://ns.adobe.com/xmp/extension/".equals(str)) {
                        section.type = "extendedXmp";
                        return;
                    }

                    str = new String(buffer, 0, "http://ns.adobe.com/xap/1.0/\u0000".length());
                    if ("http://ns.adobe.com/xap/1.0/\u0000".equals(str)) {
                        section.type = "standardXmp";
                        return;
                    }

                    str = new String(buffer, 0, "Exif".length());
                    if ("Exif".equals(str)) {
                        section.type = "exif";
                        return;
                    }
                }
            } catch (UnsupportedEncodingException var6) {
                Log.e(TAG, "<checkAppSectionTypeInStream> UnsupportedEncodingException" + var6);
            } catch (IOException var7) {
                Log.e(TAG, "<checkAppSectionTypeInStream> IOException" + var7);
            }

        } else {
            Log.d(TAG, "<checkAppSectionTypeInStream> input stream or section is null!!!");
        }
    }

    public static void saveFile(String file, byte[] data) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            out.write(data);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}