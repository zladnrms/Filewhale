package filewhalewebhard.defytech.wmqkem.filewhalewebhard.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * TODO: document your custom view class.
 */
public class GraphView extends View {

    Paint paint = new Paint();
    int Graph_width = 0;
    String color = "#47C83E";

    public GraphView(Context context) {
        super(context);
    }

    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GraphView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setWidth(Double count){
        Log.d("GraphView LOG", "넘어온 값"+count);
        Double width = 300 * count;
        Log.d("GraphView LOG", "최종 너비(소수점)"+width);
        Graph_width = Integer.valueOf(width.intValue());
        Log.d("GraphView LOG", "최종 너비(정수)"+Graph_width);

        invalidate();
    }

    public void setColor(String _color) {
        color = _color;

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        paint.setColor(Color.parseColor(color)); //선색면색
        paint.setStyle(Paint.Style.FILL); //면이 채워진 사각형 설정
        Rect graph = new Rect(0, 0, Graph_width, 20); // 시작 x좌표, 시작 y좌표, 끝x 좌표(너비), 끝y 좌표(높이)
        canvas.drawRect(graph, paint); //화면에 사격형 그리기.(사각형 좌표가 저장된 객체,사각형 모양설정 paint 객체 )
    }
}