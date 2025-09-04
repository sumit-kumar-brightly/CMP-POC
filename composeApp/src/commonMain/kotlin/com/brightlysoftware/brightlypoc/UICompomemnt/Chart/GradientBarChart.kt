package com.brightlysoftware.brightlypoc.UICompomemnt.Chart

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import network.chaintech.cmpcharts.axis.AxisProperties
import network.chaintech.cmpcharts.ui.barchart.BarChart
import network.chaintech.cmpcharts.ui.barchart.config.BarChartConfig
import network.chaintech.cmpcharts.ui.barchart.config.BarChartStyle
import network.chaintech.cmpcharts.ui.barchart.config.SelectionHighlightData

import com.brightlysoftware.brightlypoc.UICompomemnt.Chart.Util.getGradientBarChartData
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun GradientBarChart() {
    val maxRange = 5
    val barData = getGradientBarChartData()
    val yStepSize = 5
    val xAxisData = AxisProperties(
        stepSize = 5.dp,
        stepCount = barData.size-1,
        bottomPadding = 20.dp,
        initialDrawPadding = 50.dp,
        labelColor = font_color,
        lineColor = font_color,
        shouldExtendLineToEnd = true,
        labelFormatter = { index -> barData[index].label }
    )

    val yAxisData = AxisProperties(
        stepCount = yStepSize,
        labelPadding = 20.dp,
        offset = 10.dp,
        labelColor = font_color,
        lineColor = font_color,
        labelFormatter = { index -> (index * (maxRange / yStepSize)).toString() }
    )
    val barChartData = BarChartConfig(
        chartData = barData,
        xAxisData = xAxisData,
        yAxisData = yAxisData,
        barStyle = BarChartStyle(paddingBetweenBars = 30.dp,
            barWidth = 20.dp,
            selectionHighlightData = SelectionHighlightData(
                highlightBarColor = gray_light,
                highlightTextColor = white_color,
                highlightTextTypeface = FontWeight.Bold,
                highlightTextBackgroundColor = magenta_dark,
                popUpLabel = { _, y -> " Value : $y " }
            ))
    )
    BarChart(modifier = Modifier.height(350.dp), barChartData = barChartData)
}