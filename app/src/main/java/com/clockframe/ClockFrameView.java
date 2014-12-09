package com.clockframe;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class ClockFrameView extends View {
    private static final String TAG = "ClockFrameView";

    private Context context;
    private TextPaint textPaint;
    private Paint linePaint;
    private Paint blockPaint;

    private GestureDetector gestureDetector;
    private GestureDetector.SimpleOnGestureListener simpleOnGestureListener;

    private int paddingTop;
    private int paddingBottom;

    private int contentWidth;

    private static final int columns = 11;
    private static final int rows = 10;
    private float textSize;

    private onColorChangedListener onColorChangedListener;

    private int color;
    private List<FrameBlock> blocks;

    public ClockFrameView(Context context) {
        super(context);
        this.context = context;
        init(null, 0);
    }

    public ClockFrameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init(attrs, 0);
    }

    public ClockFrameView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        init(attrs, defStyle);
    }

    private void colorBlock(MotionEvent e)
    {
        FrameBlock block = getBlock(e);
        if(block != null)
        {
            block.setColor(color);
            invalidate();
        }
    }

    private void init(AttributeSet attrs, int defStyle) {
        initBlocks();

        simpleOnGestureListener = new GestureDetector.SimpleOnGestureListener()
        {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                colorBlock(e);
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                colorBlock(e2);
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                FrameBlock block = getBlock(e);
                if(block.getColor()!=0)
                setColor(block.getColor());
                return true;
            }
        };
        gestureDetector = new GestureDetector(this.context, simpleOnGestureListener);

        textSize =TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14, getResources().getDisplayMetrics());

        textPaint = new TextPaint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(textSize);

        linePaint = new Paint();
        linePaint.setColor(Color.WHITE);

        blockPaint = new Paint();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(width, getResources().getDisplayMetrics().widthPixels);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        paddingTop = getPaddingTop();
        paddingBottom = getPaddingBottom();
        contentWidth = (int) (getWidth()-textSize-1);

        int realBottom = contentWidth;

        //horizontal
        for (int i = 0; i < columns; i++) {
            canvas.drawText(Integer.toString(i), 0 +textSize+ i * contentWidth / columns, paddingTop+textSize, textPaint);
        }
        //vertical
        for (int i = 0; i < rows; i++) {
            canvas.drawText(Integer.toString(i), 0, paddingTop +textSize+ i * contentWidth / rows, textPaint);
        }
        canvas.translate(textSize,textSize);

        //vertical
        for (int i = 0; i <= columns; i++) {
            canvas.drawLine(i * contentWidth / columns, paddingTop, i * contentWidth / columns, realBottom, linePaint);
        }
        //horizontal
        for (int i = 0; i <= rows; i++) {
            canvas.drawLine(0, paddingTop + i * contentWidth / rows, contentWidth, paddingTop + i * contentWidth / rows, linePaint);
        }

        //Coloring
        for (int i = 0; i <= columns; i++) {
            for (int j = 0; j <= rows; j++) {
                if(blocks.size() > columns * j + i) {
                    blockPaint.setColor(blocks.get(columns * j + i).getColor());
                    canvas.drawRect(i * contentWidth / columns, paddingTop + j * contentWidth / rows, (i + 1) * contentWidth / columns, paddingTop + (j + 1) * contentWidth / rows, blockPaint);
                }
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return true;
    }

    private FrameBlock getBlock(MotionEvent event)
    {
        int x = (int) ((event.getX()-textSize) / (contentWidth/columns));
        int y = (int) ((event.getY()-textSize) / (contentWidth/rows));
        if(x < 0)
            x=0;
        else if(x>10)
            x=10;
        int position = y * columns + x;
        if(position >= 0 && blocks.size() > position) {
            return blocks.get(position);
        }
        return null;
    }

    public String generateCode()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("uint8_t frame[5][] = {");
        int x = 0;
        int y = 0;
        for (FrameBlock block : blocks)
        {
            if(block.getColor() != 0) {
                sb.append("{");
                sb.append(Integer.toString(x));
                sb.append(",");
                sb.append(Integer.toString(y));
                sb.append(",");
                sb.append(Integer.toString(Color.red(block.getColor())));
                sb.append(",");
                sb.append(Integer.toString(Color.green(block.getColor())));
                sb.append(",");
                sb.append(Integer.toString(Color.blue(block.getColor())));
                sb.append("}");
                if(y != rows)
                {
                    sb.append(",");
                }
            }
            x++;
            if(x == columns)
            {
                x=0;
                y++;
            }
        }
        sb.append("}");
        return sb.toString();
    }

    public void setColor(int color) {
        this.color = color;
        if(this.onColorChangedListener!=null)
        this.onColorChangedListener.onColorChanged(color);
    }

    public void clear()
    {
        initBlocks();
        invalidate();
    }

    private void initBlocks()
    {
        blocks = new ArrayList<FrameBlock>(columns * rows);
        for (int i = 0; i < columns*rows; i++)
            blocks.add(new FrameBlock());
    }

    private class FrameBlock {
        private int color;

        public int getColor() {
            return color;
        }

        public void setColor(int color) {
            this.color = color;
        }
    }

    public interface onColorChangedListener
    {
        void onColorChanged(int color);
    }

    public void setOnColorChangedListener(onColorChangedListener listener)
    {
        this.onColorChangedListener = listener;
    }
}
