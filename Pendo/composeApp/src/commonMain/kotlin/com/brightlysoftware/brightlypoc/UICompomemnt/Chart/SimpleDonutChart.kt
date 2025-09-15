package com.brightlysoftware.brightlypoc.UICompomemnt.Chart
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brightlysoftware.brightlypoc.UICompomemnt.Chart.Util.getDonutChartData
import network.chaintech.cmpcharts.common.components.Legends
import network.chaintech.cmpcharts.common.model.LegendLabel
import network.chaintech.cmpcharts.common.model.LegendsConfig
import network.chaintech.cmpcharts.ui.piechart.charts.DonutPieChart
import network.chaintech.cmpcharts.ui.piechart.models.PieChartConfig
import network.chaintech.cmpcharts.ui.piechart.models.PieChartData
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun SimpleDonutChart()  {
    val data = getDonutChartData()
    val pieChartConfig =
        PieChartConfig(
            labelVisible = true,
            strokeWidth = 300f,
            labelColor = font_color,
            activeSliceAlpha = .9f,
            isEllipsizeEnabled = true,
            labelFontWeight = FontWeight.Bold,
            isAnimationEnable = true,
            chartPadding = 50,
            labelFontSize = 32.sp,
        )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(500.dp)
    ) {

        DonutPieChart(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp),
            data,
            pieChartConfig
        ) { slice ->

        }

        Legends(
            legendsConfig = getLegendsConfigFromPieChartDataForDonutChart(
                pieChartData = data,
                3
            )
        )
    }
}

@Composable
fun getLegendsConfigFromPieChartDataForDonutChart(
    pieChartData: PieChartData,
    gridSize: Int
): LegendsConfig {
    val legendsList = mutableListOf<LegendLabel>()
    pieChartData.slices.forEach { slice ->
        legendsList.add(LegendLabel(slice.color, "${slice.label} (${slice.value.toInt()})"))
    }
    return LegendsConfig(
        legendLabelList = legendsList,
        colorBoxSize = 10.dp,
        gridColumnCount = gridSize,
        legendsArrangement = Arrangement.Start
    )
}