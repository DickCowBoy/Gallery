package com.mediatek.camera.common.arcsoft;

/**
 * Created by chengjian on 4/2/18.
 */

public class ArcSoftStereoConfigInfo {
    public int orientation;
    public int touchCoordX1st;
    public int touchCoordY1st;
    public int dofLevel;
    public int i32MainWidth_CropNoScale;
    public int i32MainHeight_CropNoScale;
    public int i32AuxWidth_CropNoScale;
    public int i32AuxHeight_CropNoScale;

    public ArcSoftStereoConfigInfo cloneConfig() {
        ArcSoftStereoConfigInfo ret = new ArcSoftStereoConfigInfo();
        ret.orientation = orientation;
        ret.touchCoordX1st = touchCoordX1st;
        ret.touchCoordY1st = touchCoordY1st;
        ret.dofLevel = dofLevel;
        ret.i32MainWidth_CropNoScale = i32MainWidth_CropNoScale;
        ret.i32MainHeight_CropNoScale = i32MainHeight_CropNoScale;
        ret.i32AuxWidth_CropNoScale = i32AuxWidth_CropNoScale;
        ret.i32AuxHeight_CropNoScale = i32AuxHeight_CropNoScale;
        return ret;
    }

    public void updateData(ArcSoftStereoConfigInfo ret) {
        this.orientation = ret.orientation;
        this.touchCoordX1st = ret.touchCoordX1st;
        this.touchCoordY1st = ret.touchCoordY1st;
        this.dofLevel = ret.dofLevel;
        this.i32MainWidth_CropNoScale = ret.i32MainWidth_CropNoScale;
        this.i32MainHeight_CropNoScale = ret.i32MainHeight_CropNoScale;
        this.i32AuxWidth_CropNoScale = ret.i32AuxWidth_CropNoScale;
        this.i32AuxHeight_CropNoScale = ret.i32AuxHeight_CropNoScale;
    }

    public ArcSoftStereoConfigInfo() {
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("StereoConfigInfo:");
        sb.append("\n    orientation  = 0x" + Integer.toHexString(orientation) + "(" + orientation
                + ")");
        sb.append("\n    touchCoordX1st = 0x" + Integer.toHexString(touchCoordX1st) + "("
                + touchCoordX1st + ")");
        sb.append("\n    touchCoordY1st = 0x" + Integer.toHexString(touchCoordY1st) + "("
                + touchCoordY1st + ")");
        sb.append("\n    dofLevel = 0x" + Integer.toHexString(dofLevel) + "(" + dofLevel + ")");

        sb.append("\n    i32MainWidth_CropNoScale = 0x" + Integer.toHexString(
                i32MainWidth_CropNoScale) + "(" + i32MainWidth_CropNoScale + ")");
        sb.append("\n    i32MainHeight_CropNoScale = 0x" + Integer.toHexString(
                i32MainHeight_CropNoScale) + "(" + i32MainHeight_CropNoScale + ")");
        sb.append(
                "\n    i32AuxWidth_CropNoScale = 0x" + Integer.toHexString(i32AuxWidth_CropNoScale)
                        + "(" + i32AuxWidth_CropNoScale + ")");
        sb.append("\n    i32AuxHeight_CropNoScale = 0x" + Integer.toHexString(
                i32AuxHeight_CropNoScale) + "(" + i32AuxHeight_CropNoScale + ")");
        return sb.toString();
    }

    public String toJSONString() {
        return "{" +
                "Orientation=" + orientation +
                ", TouchCoordX1st=" + touchCoordX1st +
                ", TouchCoordY1st=" + touchCoordY1st +
                ", DOF=" + dofLevel +
                ", i32MainWidth_CropNoScale=" + i32MainWidth_CropNoScale +
                ", i32MainHeight_CropNoScale=" + i32MainHeight_CropNoScale +
                ", i32AuxWidth_CropNoScale=" + i32AuxWidth_CropNoScale +
                ", i32AuxHeight_CropNoScale=" + i32AuxHeight_CropNoScale +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ArcSoftStereoConfigInfo that = (ArcSoftStereoConfigInfo) o;

        if (orientation != that.orientation) return false;
        if (touchCoordX1st != that.touchCoordX1st) return false;
        if (touchCoordY1st != that.touchCoordY1st) return false;
        if (dofLevel != that.dofLevel) return false;
        if (i32MainWidth_CropNoScale != that.i32MainWidth_CropNoScale) return false;
        if (i32MainHeight_CropNoScale != that.i32MainHeight_CropNoScale) return false;
        if (i32AuxWidth_CropNoScale != that.i32AuxWidth_CropNoScale) return false;
        return i32AuxHeight_CropNoScale == that.i32AuxHeight_CropNoScale;
    }
}
