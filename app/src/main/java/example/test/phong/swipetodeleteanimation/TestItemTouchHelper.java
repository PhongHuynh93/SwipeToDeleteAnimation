package example.test.phong.swipetodeleteanimation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class TestItemTouchHelper extends ItemTouchHelper.SimpleCallback {
    private static final float CIRCLE_ACCELERATION = 3f;
    private final int mDeleteColor;
    private Drawable mDeleteIcon;
    private Paint mTopShadowPaint;
    private boolean mInitialized;
    private Paint mBottomShadowPaint;
    private Paint mRightShadowPaint;
    private float mTopShadowHeight;
    private float mBottomShadowHeight;
    private float sidemSideShadowWidth;
    private int mShadowColor;
    private int mBackgroundColor;
    private float mIconPadding;
    private int mIconColorFilter;
    private Paint mCirclePaint;
    private Matrix mMatrix = new Matrix();

    public TestItemTouchHelper(Context context) {
        super(0, ItemTouchHelper.END);
        mShadowColor = ContextCompat.getColor(context, R.color.shadow);
        mDeleteColor = ContextCompat.getColor(context, R.color.delete);
        mBackgroundColor = ContextCompat.getColor(context, R.color.background_super_dark);
        mTopShadowHeight = context.getResources().getDimension(R.dimen.spacing_micro);
        mBottomShadowHeight = mTopShadowHeight / 2f;
        sidemSideShadowWidth = mTopShadowHeight * 3f / 4f;
        mIconPadding = context.getResources().getDimension(R.dimen.spacing_normal);
        mIconColorFilter = mDeleteColor;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

    }

    @Override
    public void onChildDraw(@NonNull Canvas c,
                            @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder,
                            float dX,
                            float dY,
                            int actionState,
                            boolean isCurrentlyActive) {
        // not a swipe in horizontal
        if (dX == 0) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            return;
        }

        int saveCount = c.save();

        // clip the "revealed" area
        float right = viewHolder.itemView.getRight();
        float top = viewHolder.itemView.getTop();
        float bottom = viewHolder.itemView.getBottom();
        float left = viewHolder.itemView.getLeft();
        float width = right - left;
        float height = bottom - top;

//        c.clipRect(right + dX, top, right, bottom);
        c.clipRect(left, top, left + dX, bottom);
        c.drawColor(mBackgroundColor);

        // lazily initialize some var
        initialize(recyclerView.getContext());

        // variable dependent upon gesture progress
//        float progress = -dX / width;
        float progress = dX / width;
        float iconScale = 1f;
        float opacity = 1f;
        int iconColor = mDeleteColor;
        float circleRadius = 0f;
        float swipeThreshold = getSwipeThreshold(viewHolder);
        float thirdThreshold = swipeThreshold / 3f;
        float iconPopThreshold = swipeThreshold + 0.125f;
        float iconPopFinishedThreshold = iconPopThreshold + 0.125f;

        if (progress >= 0f && progress < thirdThreshold) {
            // fade in
            opacity = progress / thirdThreshold;
        } else if (progress >= thirdThreshold && progress < swipeThreshold) {
            // scale icon down to 0.9
            iconScale = 1f - (((progress - thirdThreshold) / (swipeThreshold - thirdThreshold)) * 0.1f);
        } else {
            // draw circle and switch icon color
            circleRadius = (progress - swipeThreshold) * width * CIRCLE_ACCELERATION;
            iconColor = Color.WHITE;
            if (progress >= swipeThreshold && progress < iconPopThreshold) {
                iconScale = 0.9f + ((progress - swipeThreshold) / (iconPopThreshold - swipeThreshold)) * 0.3f;
            } else if (progress >= iconPopThreshold && progress < iconPopFinishedThreshold) {
                iconScale = 1.2f - (((progress - iconPopThreshold) / (iconPopFinishedThreshold - iconPopThreshold)) *
                        0.2f);
            } else {
                iconScale = 1f;
            }
        }

        // draw delete icon
        if (mDeleteIcon != null) {
//            int cx = (int) (right - mIconPadding - mDeleteIcon.getIntrinsicWidth() / 2f);
            int cx = (int) (left + mIconPadding + mDeleteIcon.getIntrinsicWidth() / 2f);
            int cy = (int) (top + height / 2f);
            int halfIconSize = (int) (mDeleteIcon.getIntrinsicWidth() * iconScale / 2f);
            mDeleteIcon.setBounds(cx - halfIconSize, cy - halfIconSize, cx + halfIconSize, cy + halfIconSize);
            mDeleteIcon.setAlpha((int) (opacity * 255f));
            if (iconColor != mIconColorFilter) {
                mDeleteIcon.setColorFilter(new PorterDuffColorFilter(iconColor, PorterDuff.Mode.SRC_IN));
                mIconColorFilter = iconColor;
            }
            if (circleRadius > 0f) {
                c.drawCircle(cx, cy, circleRadius, mCirclePaint);
            }
            mDeleteIcon.draw(c);
        }

        // draw shadows to fake elevation of surrounding views
        if (mTopShadowPaint != null) {
            setTranslation(mTopShadowPaint.getShader(), 0, top);
            c.drawRect(left, top, right, top + mTopShadowHeight, mTopShadowPaint);
        }
        if (mBottomShadowPaint != null) {
            setTranslation(mBottomShadowPaint.getShader(), 0, bottom - mBottomShadowHeight);
            c.drawRect(left, bottom - mBottomShadowHeight, right, bottom, mBottomShadowPaint);
        }
        if (mRightShadowPaint != null) {
            float shadowLeft = left + dX;
            setTranslation(mRightShadowPaint.getShader(), shadowLeft, 0);
            c.drawRect(shadowLeft - sidemSideShadowWidth, top, shadowLeft, bottom, mRightShadowPaint);
        }

        c.restoreToCount(saveCount);
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    private void setTranslation(Shader shader, float x, float y) {
        mMatrix.setTranslate(x, y);
        shader.setLocalMatrix(mMatrix);
    }

    private void initialize(Context context) {
        if (!mInitialized) {
            mInitialized = true;
            mDeleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_delete);
            mTopShadowPaint = new Paint();
            mTopShadowPaint.setShader(new LinearGradient(0f,
                                                         0f,
                                                         0f,
                                                         mTopShadowHeight,
                                                         mShadowColor,
                                                         0,
                                                         Shader.TileMode.CLAMP));
            mBottomShadowPaint = new Paint();
            mBottomShadowPaint.setShader(new LinearGradient(0f,
                                                            0f,
                                                            0f,
                                                            mBottomShadowHeight,
                                                            0,
                                                            mShadowColor,
                                                            Shader.TileMode.CLAMP));
            mRightShadowPaint = new Paint();
            mRightShadowPaint.setShader(new LinearGradient(0f,
                                                           0f,
                                                           sidemSideShadowWidth,
                                                           0f,
                                                           mShadowColor,
                                                           0,
                                                           Shader.TileMode.CLAMP));
            mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mCirclePaint.setColor(mDeleteColor);
        }
    }
}
