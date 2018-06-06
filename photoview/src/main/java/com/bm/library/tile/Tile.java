package com.bm.library.tile;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;

import junit.framework.Assert;

import static android.content.ContentValues.TAG;

public class Tile {
    /*
     *  This is the tile state in the CPU side.
     *  Life of a Tile:
     *      ACTIVATED (initial state)
     *              --> IN_QUEUE - by queueForDecode()
     *              --> RECYCLED - by recycleTile()
     *      IN_QUEUE --> DECODING - by decodeTile()
     *               --> RECYCLED - by recycleTile)
     *      DECODING --> RECYCLING - by recycleTile()
     *               --> DECODED  - by decodeTile()
     *               --> DECODE_FAIL - by decodeTile()
     *      RECYCLING --> RECYCLED - by decodeTile()
     *      DECODED --> ACTIVATED - (after the decoded bitmap is uploaded)
     *      DECODED --> RECYCLED - by recycleTile()
     *      DECODE_FAIL -> RECYCLED - by recycleTile()
     *      RECYCLED --> ACTIVATED - by obtainTile()
     */
    public static final int STATE_ACTIVATED = 0x01;
    public static final int STATE_IN_QUEUE = 0x02;
    public static final int STATE_DECODING = 0x04;
    public static final int STATE_DECODED = 0x08;
    public static final int STATE_DECODE_FAIL = 0x10;
    public static final int STATE_RECYCLING = 0x20;
    public static final int STATE_RECYCLED = 0x40;


    public int mX;
    public int mY;
    public int mTileLevel;
    public Tile mNext;
    public Bitmap mDecodedTile;
    public volatile int mTileState = STATE_ACTIVATED;

    public Tile(int x, int y, int level) {
        mX = x;
        mY = y;
        mTileLevel = level;
    }

    public void update(int x, int y, int level) {
        mX = x;
        mY = y;
        mTileLevel = level;
    }

    public void recycle() {
        synchronized (this) {
            if (mDecodedTile != null) freeBitmap();
        }
    }

    private void freeBitmap() {
        if (mDecodedTile != null) {
            mDecodedTile.recycle();
        }
        mDecodedTile = null;
    }
}