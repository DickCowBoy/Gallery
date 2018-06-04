package com.bm.library.tile;

import android.util.Log;

import static android.content.ContentValues.TAG;

public class TileQueue {private Tile mHead;

    public synchronized Tile pop() {
        Tile tile = mHead;
        if (tile != null){
            mHead = tile.mNext;
            //pop出来的元素 需要和当前链表断开
            tile.mNext = null;
        }
        return tile;
    }

    public synchronized boolean push(Tile tile) {
        boolean wasEmpty = mHead == null;
        /// M: [BUG.ADD] @{
        // If tile is same as head, it will lead a dead circle,
        // pop method can not pop up tile successfully.
        if (tile == mHead) {
            Log.e(TAG, "<TileQueue.push> push tile same as head, return, tile = " + tile);
            return wasEmpty;
        }
        /// @}
        tile.mNext = mHead;
        mHead = tile;
        return wasEmpty;
    }

    public synchronized void clean() {
        mHead = null;
    }
}