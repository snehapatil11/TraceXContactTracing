package com.example.tracexcontacttracing.fragment

import android.graphics.Color.red
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.tracexcontacttracing.R
import com.example.tracexcontacttracing.dao.TimeSeriesCovidDataDao
import com.example.tracexcontacttracing.database.RoomDb
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import org.joda.time.DateTime

class LineChart2MonthsDataFragment : Fragment(){
    private var lineChart: LineChart? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_linechart_2monthsdata, container, false);
        lineChart = view.findViewById<LineChart>(R.id.lineChart2monthsdata)
        val timeSeriesCovidDataDao = RoomDb.getAppDatabase(this.context!!)?.timeSeriesCovidDataDao()
        setLineChart(timeSeriesCovidDataDao);
        return view;
    }

    private fun setLineChart(timeSeriesCovidDataDao: TimeSeriesCovidDataDao?) {

        print("in line chart")
        val dataOf2Months =  timeSeriesCovidDataDao?.get2MonthsTimeSeriesData()
        var count:Float = 1f;
        val lineEntries = ArrayList<Entry>()
        val labels = ArrayList<String>()
        dataOf2Months?.map { i->
            lineEntries.add(BarEntry(count, i.newCases.toFloat()))
            val d =  DateTime.parse(i.date);
            val day = d.dayOfMonth;
            val month = d.monthOfYear;
            labels.add("$month/$day")
            count++;
        }

        val dataSet =  LineDataSet(lineEntries, "Time series");
        val lineData = LineData(dataSet);
        lineChart?.setData(lineData);
        dataSet.setDrawValues(false)
        dataSet.setDrawFilled(true)
        dataSet.lineWidth = 3f
        dataSet.setDrawCircles(false)
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.color = ContextCompat.getColor(context!!, R.color.orange)
        dataSet.fillColor = ContextCompat.getColor(context!!, R.color.lightOrange)

        lineChart?.getAxisRight()?.setDrawGridLines(false);
        lineChart?.getAxisLeft()?.setDrawGridLines(false);
        lineChart?.getXAxis()?.setDrawGridLines(false);
        lineChart?.setTouchEnabled(true)
        lineChart?.description?.isEnabled = false
        lineChart?.getLegend()?.setEnabled(false);
        val xAxis = lineChart?.xAxis
        xAxis?.valueFormatter =  IndexAxisValueFormatter(labels)
        xAxis?.position = XAxis.XAxisPosition.BOTTOM

        val rightYAxis = lineChart!!.axisRight
        rightYAxis.isEnabled = false


        lineChart?.animateY(500)
        lineChart?.invalidate();
    }
}