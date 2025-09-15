package com.brightlysoftware.brightlypoc.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Alignment

import com.brightlysoftware.brightlypoc.UICompomemnt.SegmentedButtons
import com.brightlysoftware.brightlypoc.UICompomemnt.SegmentedButtonItem
import com.brightlysoftware.brightlypoc.UICompomemnt.Chart.GradientBarChart
import com.brightlysoftware.brightlypoc.UICompomemnt.Chart.SimpleDonutChart
import org.jetbrains.compose.ui.tooling.preview.Preview

enum class ChatSegment(
    val title: String
) {
    BarChat("BarChat"),
    PieChart("PieChart")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun ChartScreen() {
    var selectedSegment by remember { mutableStateOf(ChatSegment.BarChat) }

    Scaffold(

    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        )  {
            // Segmented control
            Box(modifier = Modifier.padding(top = 10.dp)) {
                SegmentedButtons {
                    SegmentedButtonItem(
                        selected = selectedSegment == ChatSegment.BarChat,
                        onClick = { selectedSegment = ChatSegment.BarChat },
                        label = { Text(text = "BarChat") }
                    )
                    SegmentedButtonItem(
                        selected = selectedSegment == ChatSegment.PieChart,
                        onClick = { selectedSegment = ChatSegment.PieChart },
                        label = { Text(text = "PieChart") }
                    )
                }
            }

            // Placeholder space or padding, replace with your content as necessary
            Spacer(modifier = Modifier.height(16.dp))

            // Chart view based on selected segment
            Box(modifier = Modifier.weight(1f)) {
                when (selectedSegment) {
                    ChatSegment.BarChat -> BarChartView()
                    ChatSegment.PieChart -> PieChartView()
                }
            }
        }
    }
}


@Composable
fun BarChartView() {
    // Logic for drawing a Bar chart goes here
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center) {
        // Example content, replace with actual Bar chart
        GradientBarChart()
    }
}

@Composable
fun PieChartView() {
    Box(modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center) {
        SimpleDonutChart()
    }
}