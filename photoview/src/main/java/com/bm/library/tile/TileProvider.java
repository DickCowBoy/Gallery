package com.bm.library.tile;

import android.graphics.Rect;
import android.util.LongSparseArray;

public class TileProvider {

    private RenderTarget mRecycleTile;

    public static final int SIZE_UNKNOWN = -1;

    // TILE_SIZE must be 2^N
    private static int sTileSize = 1024;

    // The offsets of the (left, top) of the upper-left tile to the (left, top)
    // of the view.
    private int mOffsetX;
    private int mOffsetY;

    // The width and height of the full-sized bitmap
    protected int mImageWidth = SIZE_UNKNOWN;
    protected int mImageHeight = SIZE_UNKNOWN;

    // Temp variables to avoid memory allocation
    private final Rect mTileRange = new Rect();
    private final Rect mActiveRange[] = {new Rect(), new Rect()};

    // The following three queue is guarded by TileImageView.this
    private final TileQueue mRecycledQueue = new TileQueue();
    private final TileQueue mDecodeQueue = new TileQueue();

    protected int mLevelCount;  // cache the value of mScaledBitmaps.length
    // The mLevel variable indicates which level of bitmap we should use.
    // Level 0 means the original full-sized bitmap, and a larger value means
    // a smaller scaled bitmap (The width and height of each scaled bitmap is
    // half size of the previous one). If the value is in [0, mLevelCount), we
    // use the bitmap in mScaledBitmaps[mLevel] for display, otherwise the value
    // is mLevelCount, and that means we use mScreenNail for display.
    private int mLevel = 0;

    private final LongSparseArray<Tile> mActiveTiles = new LongSparseArray<Tile>();

    // Prepare the tiles we want to use for display.
    //
    // 1. Decide the tile level we want to use for display.
    // 2. Decide the tile levels we want to keep as texture (in addition to
    //    the one we use for display).
    // 3. Recycle unused tiles.
    // 4. Activate the tiles we want.
    private void layoutTiles(int centerX, int centerY, float scale, int rotation) {
        // The width and height of this view.

        // The tile levels we want to keep as texture is in the range
        // [fromLevel, endLevel).
        int fromLevel;
        int endLevel;

        // We want to use a texture larger than or equal to the display size.
        mLevel = Utils.clamp(Utils.floorLog2(1f / scale), 0, mLevelCount);

        // We want to keep one more tile level as texture in addition to what
        // we use for display. So it can be faster when the scale moves to the
        // next level. We choose a level closer to the current scale.
        if (mLevel != mLevelCount) {
            Rect range = mTileRange;
            getRange(range, centerX, centerY, mLevel, scale, rotation);
            mOffsetX = Math.round(mRecycleTile.getWidth() / 2f + (range.left - centerX) * scale);
            mOffsetY = Math.round(mRecycleTile.getHeight() / 2f + (range.top - centerY) * scale);
            fromLevel = scale * (1 << mLevel) > 0.75f ? mLevel - 1 : mLevel;
        } else {
            // Activate the tiles of the smallest two levels.
            fromLevel = mLevel - 2;
            mOffsetX = Math.round(mRecycleTile.getWidth() / 2f - centerX * scale);
            mOffsetY = Math.round(mRecycleTile.getHeight() / 2f - centerY * scale);
        }

        fromLevel = Math.max(0, Math.min(fromLevel, mLevelCount - 2));
        endLevel = Math.min(fromLevel + 2, mLevelCount);

        Rect range[] = mActiveRange;
        for (int i = fromLevel; i < endLevel; ++i) {
            getRange(range[i - fromLevel], centerX, centerY, i, rotation);
        }

        // If rotation is transient, don't update the tile.
        if (rotation % 90 != 0) return;

        synchronized (this) {
            mDecodeQueue.clean();

            // Recycle unused tiles: if the level of the active tile is outside the
            // range [fromLevel, endLevel) or not in the visible range.
            int n = mActiveTiles.size();
            for (int i = 0; i < n; i++) {
                Tile tile = mActiveTiles.valueAt(i);
                int level = tile.mTileLevel;
                if (level < fromLevel || level >= endLevel
                        || !range[level - fromLevel].contains(tile.mX, tile.mY)) {
                    mActiveTiles.removeAt(i);
                    i--;
                    n--;
                    recycleTile(tile);
                }
            }
        }

        for (int i = fromLevel; i < endLevel; ++i) {
            int size = sTileSize << i;
            Rect r = range[i - fromLevel];
            for (int y = r.top, bottom = r.bottom; y < bottom; y += size) {
                for (int x = r.left, right = r.right; x < right; x += size) {
                    activateTile(x, y, i);
                }
            }
        }
        mRecycleTile.onTileContentUpdate();
    }



    /**
     * load the tile of the  range by the scale
     * @param range render range
     * @param scale scale of the tile
     */
    private void loadTileByRenge(Rect range, float scale) {

    }


    public interface RenderTarget {
        void onTileContentUpdate();
        int getWidth();
        int getHeight();
    }

    /**
     * get the tile of the  range by the scale and load the unload tile
     * @param range render range
     * @param scale scale of the tile
     */
    public Tile[] getTile(Rect range, float scale) {
        int level = scaleToLevel(scale);

        return null;
    }

    /**
     * transfer view scale to tile level
     * @param scale the scale of the tile target
     * @return
     */
    private int scaleToLevel(float scale) {
        return 0;
    }

    private Tile getActivedTile(int x, int y, int level) {
        return mActiveTiles.get(makeTileKey(x, y, level));
    }

    private static long makeTileKey(int x, int y, int level) {
        long result = x;
        result = (result << 16) | y;
        result = (result << 16) | level;
        return result;
    }

    private void getRange(Rect out, int cX, int cY, int level, int rotation) {
        getRange(out, cX, cY, level, 1f / (1 << (level + 1)), rotation);
    }

    // If the bitmap is scaled by the given factor "scale", return the
    // rectangle containing visible range. The left-top coordinate returned is
    // aligned to the tile boundary.
    //
    // (cX, cY) is the point on the original bitmap which will be put in the
    // center of the ImageViewer.
    private void getRange(Rect out,
                          int cX, int cY, int level, float scale, int rotation) {

        double radians = Math.toRadians(-rotation);

        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        int width = (int) Math.ceil(Math.max(
                Math.abs(cos * mRecycleTile.getWidth() - sin * mRecycleTile.getHeight()),
                Math.abs(cos * mRecycleTile.getWidth() + sin * mRecycleTile.getHeight())));
        int height = (int) Math.ceil(Math.max(
                Math.abs(sin * mRecycleTile.getWidth() + cos * mRecycleTile.getHeight()),
                Math.abs(sin * mRecycleTile.getWidth() - cos * mRecycleTile.getHeight())));

        int left = (int) Math.floor(cX - width / (2f * scale));
        int top = (int) Math.floor(cY - height / (2f * scale));
        int right = (int) Math.ceil(left + width / scale);
        int bottom = (int) Math.ceil(top + height / scale);

        // align the rectangle to tile boundary
        int size = sTileSize << level;
        left = Math.max(0, size * (left / size));
        top = Math.max(0, size * (top / size));
        right = Math.min(mImageWidth, right);
        bottom = Math.min(mImageHeight, bottom);

        out.set(left, top, right, bottom);
    }

    // TODO think about reuse the bitmap of tile
    synchronized void recycleTile(Tile tile) {
        if (tile.mTileState == Tile.STATE_DECODING) {
            tile.mTileState = Tile.STATE_RECYCLING;
            return;
        }
        tile.mTileState = Tile.STATE_RECYCLED;
        if (tile.mDecodedTile != null) {
            tile.mDecodedTile.recycle();
            tile.mDecodedTile = null;
        }
        mRecycledQueue.push(tile);
    }

    private void activateTile(int x, int y, int level) {
        long key = makeTileKey(x, y, level);
        Tile tile = mActiveTiles.get(key);
        if (tile != null) {
            if (tile.mTileState == Tile.STATE_IN_QUEUE) {
                tile.mTileState = Tile.STATE_ACTIVATED;
            }
            return;
        }
        tile = obtainTile(x, y, level);
        mActiveTiles.put(key, tile);
    }

    private synchronized Tile obtainTile(int x, int y, int level) {
        Tile tile = mRecycledQueue.pop();
        if (tile != null) {
            tile.mTileState = Tile.STATE_ACTIVATED;
            tile.update(x, y, level);
            return tile;
        }
        return new Tile(x, y, level);
    }
}
