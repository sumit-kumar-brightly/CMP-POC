package com.brightlysoftware.brightlypoc.UICompomemnt.Chart.Util

import androidx.compose.ui.graphics.Color
import com.brightlysoftware.brightlypoc.UICompomemnt.Chart.purple_dark
import com.brightlysoftware.brightlypoc.UICompomemnt.Chart.blue_dark
import com.brightlysoftware.brightlypoc.UICompomemnt.Chart.green_dark
import com.brightlysoftware.brightlypoc.UICompomemnt.Chart.yellow_dark
import com.brightlysoftware.brightlypoc.UICompomemnt.Chart.red_dark
import network.chaintech.cmpcharts.common.model.PlotType
import network.chaintech.cmpcharts.common.model.Point
import network.chaintech.cmpcharts.ui.barchart.config.BarData
import network.chaintech.cmpcharts.ui.piechart.models.PieChartData
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.random.Random

data class Movie(val title: String, val year: String)

fun generateMovieData(): List<Movie> {
    return listOf(
        Movie("Crimson Tide", "2020"),
        Movie("Golden Hour", "2021"),
        Movie("Last Signal", "2022"),
        Movie("Echo Valley", "2023"),
        Movie("Echo Valley", "2023"),
        Movie("Night Parade", "2025"),
        Movie("Night Parade", "2025"),
        Movie("Night Parade", "2025")
    )
}

fun generateYearlyMovieCount(movies: List<Movie>): Map<String, Int> {
    return movies.groupingBy { it.year }.eachCount()
}

fun generateBarData(yearlyMovieCount: Map<String, Int>): List<BarData> {
    return yearlyMovieCount.entries.mapIndexed { index, (year, count) ->
        BarData(
            point = Point(x = index.toFloat(), y = count.toFloat()),
            color = getColorForYear(year = year),
            label = year
        )
    }
}

fun getGradientBarChartData(): List<BarData> {
    val list = generateYearlyMovieCount(movies =  generateMovieData())
    return  generateBarData(yearlyMovieCount = list)
}

fun getDonutChartData(): PieChartData {
    val yearlyMovieCount = generateYearlyMovieCount(movies =  generateMovieData())
    val pieChartData =  yearlyMovieCount.entries.mapIndexed { index, (year, count) ->
        PieChartData.Slice(year, count.toFloat(), getColorForYear(year = year))
    }
    return PieChartData(
        slices = pieChartData,
        plotType = PlotType.Donut
    )
}



fun getColorForYear(year: String): Color {
    return when (year) {
        "2020" -> blue_dark
        "2021" -> red_dark
        "2022" -> green_dark
        "2023" -> yellow_dark
        "2025" -> purple_dark
        else -> Color.Gray // Default color if year doesn't match
    }
}

