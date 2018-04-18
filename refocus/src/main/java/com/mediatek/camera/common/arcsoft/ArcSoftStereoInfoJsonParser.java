package com.mediatek.camera.common.arcsoft;

import com.mediatek.accessor.util.JsonParser;
import com.mediatek.accessor.util.Log;

/**
 * Created by chengjian on 4/2/18.
 */
public class ArcSoftStereoInfoJsonParser {
    private static final String TAG = Log.Tag(ArcSoftStereoInfoJsonParser.class.getSimpleName());
    public static final String ORIENTATION_TAG = "Orientation";
    public static final String TOUCHCOORDX1ST_TAG = "TouchCoordX1st";
    private static final String TOUCHCOORDY1ST_TAG = "TouchCoordY1st";
    private static final String DOF_LEVEL_TAG = "DOF";

    private static final String I32MAIN_WIDTH = "i32MainWidth_CropNoScale";
    private static final String I32MAIN_HEIGHT = "i32MainHeight_CropNoScale";
    private static final String I32AUX_WIDTH = "i32AuxWidth_CropNoScale";
    private static final String I32AUX_HEIGHT = "i32AuxHeight_CropNoScale";

    private int mOrientation = -1;
    private int mTouchCoordX1st = -1;
    private int mTouchCoordY1st = -1;
    private int mDofLevel = -1;

    private int mI32MainWidth_CropNoScale = -1;
    private int mI32MainHeight_CropNoScale = -1;
    private int mI32AuxWidth_CropNoScale = -1;
    private int mI32AuxHeight_CropNoScale = -1;

    private JsonParser mParser;

    public ArcSoftStereoInfoJsonParser(String jsonString) {
        mParser = new JsonParser(jsonString);
    }

    public ArcSoftStereoInfoJsonParser(byte[] jsonBuffer) {
        mParser = new JsonParser(jsonBuffer);
    }

    public int getOrientation() {
        if (mOrientation != -1) {
            return mOrientation;
        } else {
            mOrientation = mParser.getValueIntFromObject((String) null, (String) null, ORIENTATION_TAG);
            return mOrientation;
        }
    }

    public int getTouchCoordX1st() {
        if (mTouchCoordX1st != -1) {
            return mTouchCoordX1st;
        } else {
            mTouchCoordX1st = mParser.getValueIntFromObject((String) null, (String) null,
                    TOUCHCOORDX1ST_TAG);
            return mTouchCoordX1st;
        }
    }

    public int getTouchCoordY1st() {
        if (mTouchCoordY1st != -1) {
            return mTouchCoordY1st;
        } else {
            mTouchCoordY1st = mParser.getValueIntFromObject((String) null, (String) null,
                    TOUCHCOORDY1ST_TAG);
            return mTouchCoordY1st;
        }
    }

    public int getDofLevel() {
        if (mDofLevel != -1) {
            return mDofLevel;
        } else {
            mDofLevel = mParser.getValueIntFromObject((String) null, (String) null, DOF_LEVEL_TAG);
            return mDofLevel;
        }
    }

    public int getMainHeight() {
        if (mI32MainHeight_CropNoScale != -1) {
            return mI32MainHeight_CropNoScale;
        } else {
            mI32MainHeight_CropNoScale = mParser.getValueIntFromObject((String) null, (String) null,
                    I32MAIN_HEIGHT);
            return mI32MainHeight_CropNoScale;
        }
    }

    public int getMainWidth() {
        if (mI32MainWidth_CropNoScale != -1) {
            return mI32MainWidth_CropNoScale;
        } else {
            mI32MainWidth_CropNoScale = mParser.getValueIntFromObject((String) null, (String) null,
                    I32MAIN_WIDTH);
            return mI32MainWidth_CropNoScale;
        }
    }

    public int getAuxHeight() {
        if (mI32AuxHeight_CropNoScale != -1) {
            return mI32AuxHeight_CropNoScale;
        } else {
            mI32AuxHeight_CropNoScale = mParser.getValueIntFromObject((String) null, (String) null,
                    I32AUX_WIDTH);
            return mI32AuxHeight_CropNoScale;
        }
    }

    public int getAuxWidth() {
        if (mI32AuxWidth_CropNoScale != -1) {
            return mI32AuxWidth_CropNoScale;
        } else {
            mI32AuxWidth_CropNoScale = mParser.getValueIntFromObject((String) null, (String) null,
                    I32AUX_HEIGHT);
            return mI32AuxWidth_CropNoScale;
        }
    }
}

