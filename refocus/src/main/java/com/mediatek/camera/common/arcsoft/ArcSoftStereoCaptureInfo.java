package com.mediatek.camera.common.arcsoft;

/**
 * Created by chengjian on 4/2/18.
 */

public class ArcSoftStereoCaptureInfo {
    public String debugDir;
    public byte[] jpgBuffer;
    public byte[] bayerBuffer;
    public byte[] depthBuffer;
    public byte[] configBuffer;
    public byte[] calibrationBuffer;

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("StereoBufferInfo:");
        if (jpgBuffer != null) {
            sb.append("\n    jpgBuffer length = 0x" + Integer.toHexString(jpgBuffer.length) + "("
                    + this.jpgBuffer.length + ")");
        } else {
            sb.append("\n    jpgBuffer = null");
        }

        if (this.bayerBuffer != null) {
            sb.append(
                    "\n    bayerBuffer length = 0x" + Integer.toHexString(bayerBuffer.length) + "("
                            + bayerBuffer.length + ")");
        } else {
            sb.append("\n    bayerBuffer = null");
        }

        if (this.depthBuffer != null) {
            sb.append("\n    depthBuffer length = 0x" + Integer.toHexString(depthBuffer.length) + "("
                    + depthBuffer.length + ")");
        } else {
            sb.append("\n    depthBuffer = null");
        }

        if (this.configBuffer != null) {
            sb.append("\n    configBuffer length = 0x" + Integer.toHexString(configBuffer.length)
                    + "(" + configBuffer.length + ")");
        } else {
            sb.append("\n    configBuffer = null");
        }

        if (this.calibrationBuffer != null) {
            sb.append("\n    calibrationBuffer length = 0x" + Integer.toHexString(
                    calibrationBuffer.length) + "(" + calibrationBuffer.length + ")");
        } else {
            sb.append("\n    calibrationBuffer = null");
        }

        return sb.toString();
    }
}

