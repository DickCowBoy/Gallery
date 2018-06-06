package com.bm.library.tile;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.util.Log;
import android.util.LongSparseArray;

import java.util.ArrayList;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;

import static android.content.ContentValues.TAG;

public class TileProvider {

    private Application application;
    protected BitmapRegionDecoder mRegionDecoder;
    private Uri mImageUri;

    private final Rect mSourceRect = new Rect();
    private final Rect mTargetRect = new Rect();

    private RenderTarget renderTarget;

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

    protected int mCenterX;
    protected int mCenterY;
    protected float mScale;
    protected int mRotation;

    private boolean mBackgroundTileUploaded;

    protected int mLevelCount;  // cache the value of mScaledBitmaps.length
    // The mLevel variable indicates which level of bitmap we should use.
    // Level 0 means the original full-sized bitmap, and a larger value means
    // a smaller scaled bitmap (The width and height of each scaled bitmap is
    // half size of the previous one). If the value is in [0, mLevelCount), we
    // use the bitmap in mScaledBitmaps[mLevel] for display, otherwise the value
    // is mLevelCount, and that means we use mScreenNail for display.
    private int mLevel = 0;

    private final LongSparseArray<Tile> mActiveTiles = new LongSparseArray<Tile>();

    public TileProvider(Application application) {
        this.application = application;
        mTileDecoderThread = new ArrayList<>();
        for (int i = 0; i < TILE_DECODER_NUM; i++) {
            Thread t = new TileDecoder();
            t.setName("TileDecoder-" + i);
            t.start();
            Log.i(TAG, "<TileImageView> create Thread-" + i + ", id = "
                    + t.getId());
            mTileDecoderThread.add(t);
        }
    }

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
            mOffsetX = Math.round(renderTarget.getWidth() / 2f + (range.left - centerX) * scale);
            mOffsetY = Math.round(renderTarget.getHeight() / 2f + (range.top - centerY) * scale);
            fromLevel = scale * (1 << mLevel) > 0.75f ? mLevel - 1 : mLevel;
        } else {
            // Activate the tiles of the smallest two levels.
            fromLevel = mLevel - 2;
            mOffsetX = Math.round(renderTarget.getWidth() / 2f - centerX * scale);
            mOffsetY = Math.round(renderTarget.getHeight() / 2f - centerY * scale);
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
            mBackgroundTileUploaded = false;

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
        renderTarget.onTileContentUpdate();
    }


    public void setRenderTarget(RenderTarget renderTarget) {
        this.renderTarget = renderTarget;
    }

    public synchronized void initRegionDecoder(Uri imageUri) {

        if (imageUri.equals(mImageUri)) {
            return;
        }
        if (mRegionDecoder != null) {
            mRegionDecoder.recycle();
        }
        this.mImageUri = imageUri;
        Flowable.create(new FlowableOnSubscribe<BitmapRegionDecoder>(){

            private Uri currentDecodeUri;
            @Override
            public void subscribe(FlowableEmitter<BitmapRegionDecoder> flowableEmitter) throws Exception {
                BitmapRegionDecoder regionDecoder = null;
                do {
                    if (regionDecoder != null) {
                        regionDecoder.recycle();
                    }
                    synchronized (TileProvider.this) {
                        currentDecodeUri = TileProvider.this.mImageUri;
                    }
                    regionDecoder = DecodeUtils.createBitmapRegionDecoder(
                            application.getContentResolver().openFileDescriptor(currentDecodeUri, "r")
                            .getFileDescriptor(),
                            false);
                } while (!currentDecodeUri.equals(TileProvider.this.mImageUri));

                flowableEmitter.onNext(regionDecoder);
                flowableEmitter.onComplete();
            }
        },BackpressureStrategy.LATEST) .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSubscriber<BitmapRegionDecoder>() {
                    @Override
                    public void onNext(BitmapRegionDecoder regionDecoder) {
                        setRegionDecoder(regionDecoder);
                        if (renderTarget != null) {
                            renderTarget.setTileProvider(TileProvider.this);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    public void setRegionDecoder(BitmapRegionDecoder regionDecoder) {
        this.mRegionDecoder = regionDecoder;
        mImageWidth = regionDecoder.getWidth();
        mImageHeight = regionDecoder.getHeight();
        mLevelCount = calculateLevelCount();
    }

    private int calculateLevelCount() {
        return Math.max(0, Utils.ceilLog2(
                (float) mImageWidth / renderTarget.getScreenNailWidth()));
    }

    // Set the position of the tile view
    public void setTileViewPosition(RectF imageRect,
                                     int viewW, int viewH) {
        float cx = imageRect.centerX();
        float cy = imageRect.centerY();
        float scale = imageRect.width() / 1.0F / mImageWidth;
        // Find out the bitmap coordinates of the center of the view
        int centerX = (int) (mImageWidth / 2f + (viewW / 2f - cx) / scale + 0.5f);
        int centerY = (int) (mImageHeight / 2f + (viewH / 2f - cy) / scale + 0.5f);

        int inverseX = mImageWidth - centerX;
        int inverseY = mImageHeight - centerY;
        int x, y;
        switch (mRotation) {
            case 0: x = centerX; y = centerY; break;
            case 90: x = centerY; y = inverseX; break;
            case 180: x = inverseX; y = inverseY; break;
            case 270: x = inverseY; y = centerX; break;
            default:
                throw new RuntimeException(String.valueOf(mRotation));
        }
        setPosition(x, y, scale, mRotation);
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
        int getScreenNailWidth();
        int getScreenNailHeight();
        void setTileProvider(TileProvider provider);
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

    public void setPosition(int centerX, int centerY, float scale, int rotation) {
        if (mCenterX == centerX && mCenterY == centerY
                && mScale == scale && mRotation == rotation) return;
        mCenterX = centerX;
        mCenterY = centerY;
        mScale = scale;
        mRotation = rotation;
        layoutTiles(centerX, centerY, scale, rotation);
        renderTarget.onTileContentUpdate();
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
                Math.abs(cos * renderTarget.getWidth() - sin * renderTarget.getHeight()),
                Math.abs(cos * renderTarget.getWidth() + sin * renderTarget.getHeight())));
        int height = (int) Math.ceil(Math.max(
                Math.abs(sin * renderTarget.getWidth() + cos * renderTarget.getHeight()),
                Math.abs(sin * renderTarget.getWidth() - cos * renderTarget.getHeight())));

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

    private class TileDecoder extends Thread {
        @Override
        public void run() {
            while (!isInterrupted()) {
                Tile tile = null;
                synchronized (TileProvider.this) {
                    tile = mDecodeQueue.pop();
                    if (tile == null && !isInterrupted()) {
                        Log.d(TAG, "<TileDecoder.run> wait, this = " + TileDecoder.this);
                        try {
                            TileProvider.this.wait();
                        } catch (InterruptedException e) {
                            interrupt();
                        }
                    }
                }
                if (tile == null) {
                    continue;
                }
                Log.d(TAG, "<TileDecoder.run> decodeTile, this = " + TileDecoder.this
                        + ", tile = " + tile);
                if (decodeTile(tile) && renderTarget != null) {
                    renderTarget.onTileContentUpdate();
                }
            }
            Log.d(TAG, "<TileDecoder.run> exit, this = " + TileDecoder.this);
        }
    }

    boolean decodeTile(Tile tile) {
        synchronized (this) {
            if (tile.mTileState !=  Tile.STATE_IN_QUEUE) return false;
            tile.mTileState = Tile.STATE_DECODING;
        }
        boolean decodeComplete = decode(tile);
        synchronized (this) {
            if (tile.mTileState == Tile.STATE_RECYCLING) {
                tile.mTileState = Tile.STATE_RECYCLED;
                if (tile.mDecodedTile != null) {
                    tile.mDecodedTile.recycle();
                    tile.mDecodedTile = null;
                }
                mRecycledQueue.push(tile);
                return false;
            }
            tile.mTileState = decodeComplete ? Tile.STATE_DECODED : Tile.STATE_DECODE_FAIL;
            return decodeComplete;
        }
    }
    /// @}

    //********************************************************************
    //*                              MTK                                 *
    //********************************************************************
    // Do region decode in multi-thread
    private static final int TILE_DECODER_NUM = 2;
    private ArrayList<Thread> mTileDecoderThread;

    private Bitmap getTileWithoutReusingBitmap(
            int level, int x, int y, int tileSize) {
        int t = tileSize << level;

        Rect wantRegion = new Rect(x, y, x + t, y + t);

        boolean needClear;
        BitmapRegionDecoder regionDecoder = null;

        synchronized (this) {
            regionDecoder = mRegionDecoder;
            if (regionDecoder == null) return null;

            // We need to clear a reused bitmap, if wantRegion is not fully
            // within the image.
            needClear = !new Rect(0, 0, mImageWidth, mImageHeight)
                    .contains(wantRegion);
        }

        Bitmap bitmap =null;
        if (bitmap != null) {
            if (needClear) bitmap.eraseColor(0);
        } else {
            bitmap = Bitmap.createBitmap(tileSize, tileSize, Bitmap.Config.ARGB_8888);
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inSampleSize =  (1 << level);
        options.inBitmap = bitmap;

        try {
            // In CropImage, we may call the decodeRegion() concurrently.
            synchronized (regionDecoder) {
                bitmap = regionDecoder.decodeRegion(wantRegion, options);
            }
            Log.e(TAG, "getTile: "+  options.inSampleSize +" :"  + bitmap.getWidth() + ":" + bitmap.getHeight());
        } finally {
            if (options.inBitmap != bitmap && options.inBitmap != null) {
                options.inBitmap = null;
            }
        }

        if (bitmap == null) {
            Log.w(TAG, "fail in decoding region");
        }
        /// M: [BUG.ADD] Some bitmaps have transparent areas, so clear alpha value @{
        return bitmap;
    }

    boolean decode(Tile tile) {
        // Get a tile from the original image. The tile is down-scaled
        // by (1 << mTilelevel) from a region in the original image.
        try {
            tile.mDecodedTile = getTileWithoutReusingBitmap(
                    tile.mTileLevel, tile.mX, tile.mY, sTileSize);
        } catch (Throwable t) {
            Log.w(TAG, "fail to decode tile", t);
        }
        return tile.mDecodedTile != null;
    }

    public synchronized void queueForDecode(Tile tile) {
        if (tile.mTileState == Tile.STATE_ACTIVATED) {
            tile.mTileState = Tile.STATE_IN_QUEUE;
            if (mDecodeQueue.push(tile)) notifyAll();
        }
    }

    // View need to check whether need to render tile
    public void renderTile(Canvas canvas) {
        if (mLevel != mLevelCount) {
            Rect source = mSourceRect;
            Rect target = mTargetRect;
            int size = (sTileSize << mLevel);
            float scale = mScale;
            float length = size * scale;
            Rect r = mTileRange;


            int index = 0;
            int notAll = 0;
            for (int ty = r.top, i = 0; ty < r.bottom; ty += size, i++) {
                float y = mOffsetY + i * length;
                for (int tx = r.left, j = 0; tx < r.right; tx += size, j++) {
                    float x = mOffsetX + j * length;
                    Tile tile = getTile(tx, ty, mLevel);
                    if (tile != null) {
                        target.set((int)x, (int)y, (int)(x + length), (int)(y + length));
                        if (tile.mTileState == Tile.STATE_DECODED) {
                            // render
                            source.set(0, 0, sTileSize, sTileSize);
                            if (target.left < 0) {
                                source.left -= (target.left / scale);
                                target.left = 0;
                                notAll++;
                            }
                            if (target.right >  renderTarget.getWidth()) {
                                source.right -= ((target.right - renderTarget.getWidth()) / scale);
                                target.right = renderTarget.getWidth();
                                notAll++;
                            }

                            if (target.top < 0) {
                                source.top -= (target.top / scale);
                                target.top = 0;
                                notAll++;
                            }
                            if (target.bottom >  renderTarget.getHeight()) {
                                source.bottom -= ((target.bottom - renderTarget.getHeight()) / scale);
                                target.bottom = renderTarget.getHeight();
                                notAll++;
                            }
                            index ++;
                            Log.e(TAG, "recct: " + rectString(source) + ":" + rectString(target));
                            canvas.drawBitmap(tile.mDecodedTile, source, target, null);
                        } else if (tile.mTileState != Tile.STATE_DECODE_FAIL){
                            queueForDecode(tile);
                        }
                    }
                }
            }
            Log.e("LJL", "drawCount" +":"+index +" :" + mTileRange.toString() +" :"+notAll +":" + scale);
        }
    }

    private String rectString(Rect rect) {
        return "[" + rect.left +"," + rect.top+"," + rect.right+"," + rect.bottom +"]";
    }

    private Tile getTile(int x, int y, int level) {
        return mActiveTiles.get(makeTileKey(x, y, level));
    }

    public void freeTextures() {
        if (mTileDecoderThread != null) {
            for (int i = 0; i < TILE_DECODER_NUM; i++) {
                if (mTileDecoderThread.get(i) != null) {
                    mTileDecoderThread.get(i).interrupt();
                }
            }
            mTileDecoderThread.clear();
            mTileDecoderThread = null;
        }
        /// @}

        int n = mActiveTiles.size();
        for (int i = 0; i < n; i++) {
            Tile texture = mActiveTiles.valueAt(i);
            texture.recycle();
        }
        mActiveTiles.clear();
        mTileRange.set(0, 0, 0, 0);

        synchronized (this) {
            mDecodeQueue.clean();
            Tile tile = mRecycledQueue.pop();
            while (tile != null) {
                tile.recycle();
                tile = mRecycledQueue.pop();
            }
        }
    }
}
