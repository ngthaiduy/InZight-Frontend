package com.example.inzightapp.view;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.interfaces.datasets.IPieDataSet;
import com.github.mikephil.charting.renderer.PieChartRenderer;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.List;

public class CustomPieChartRenderer extends PieChartRenderer {

    public CustomPieChartRenderer(PieChart chart, ChartAnimator animator, ViewPortHandler viewPortHandler) {
        super(chart, animator, viewPortHandler);
    }

    @Override
    public void drawValues(Canvas c) {
        super.drawValues(c);

        MPPointF center = mChart.getCenterCircleBox();

        float radius = mChart.getRadius();
        float rotationAngle = mChart.getRotationAngle();
        float[] drawAngles = mChart.getDrawAngles();
        float[] absoluteAngles = mChart.getAbsoluteAngles();

        float phaseY = mAnimator.getPhaseY();

        final float holeRadiusPercent = mChart.getHoleRadius() / 100f;
        
        PieData data = mChart.getData();
        List<IPieDataSet> dataSets = data.getDataSets();

        int xIndex = 0;

        // **THÊM OFFSET CHO ICON Ở ĐÂY**
        // Điều chỉnh giá trị 10f (10dp) nếu bạn cần khoảng cách lớn hơn hoặc nhỏ hơn.
        final float iconOffset = Utils.convertDpToPixel(10f); 

        for (int i = 0; i < dataSets.size(); i++) {
            IPieDataSet dataSet = dataSets.get(i);

            if (dataSet.getYValuePosition() != PieDataSet.ValuePosition.OUTSIDE_SLICE) {
                xIndex += dataSet.getEntryCount();
                continue;
            }

            final float valueLinePart1OffsetPercentage = dataSet.getValueLinePart1OffsetPercentage() / 100.f;
            final float valueLinePart1Length = dataSet.getValueLinePart1Length();
            final float valueLinePart2Length = dataSet.getValueLinePart2Length();
            
            final float valueLinePart1Radius = (radius - (radius * holeRadiusPercent)) * valueLinePart1OffsetPercentage
                    + (radius * holeRadiusPercent);
            
            // Bán kính cuối cùng của part 1
            final float valueLinePart2Radius = (radius - (radius * holeRadiusPercent)) * valueLinePart1Length
                    + valueLinePart1Radius;
                    
            // Chiều dài của part 2
            final float valueLineLength2 = (radius - (radius * holeRadiusPercent)) * valueLinePart2Length;

            for (int j = 0; j < dataSet.getEntryCount(); j++) {
                PieEntry entry = dataSet.getEntryForIndex(j);

                // Calculate angles
                final float sliceAngle = drawAngles[xIndex];
                final float transformedAngle = rotationAngle + (absoluteAngles[xIndex] - sliceAngle / 2f) * phaseY;
                
                // Calculate position
                float sliceXBase = (float) Math.cos(transformedAngle * Utils.FDEG2RAD);
                float sliceYBase = (float) Math.sin(transformedAngle * Utils.FDEG2RAD);
                
                // **ĐIỂM SỬA CHỮA**: Thêm iconOffset vào bán kính cuối cùng
                final float pt2x = center.x + (valueLinePart2Radius + valueLineLength2 + iconOffset) * sliceXBase;
                final float pt2y = center.y + (valueLinePart2Radius + valueLineLength2 + iconOffset) * sliceYBase;
                
                // Draw Icon
                Drawable icon = entry.getIcon();
                if (icon != null) {
                    
                    int iconSize = 90; 
                    
                    icon.setBounds(
                        (int)(pt2x - iconSize / 2),
                        (int)(pt2y - iconSize / 2),
                        (int)(pt2x + iconSize / 2),
                        (int)(pt2y + iconSize / 2)
                    );
                    icon.draw(c);
                }

                xIndex++;
            }
        }
        MPPointF.recycleInstance(center);
    }
}