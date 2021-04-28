package com.example.tracexcontacttracing.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.tracexcontacttracing.R
import com.example.tracexcontacttracing.data.CovidMonthlyStatsTuple
import com.example.tracexcontacttracing.database.RoomDb
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class LineChartAllDataFragment : Fragment(){
    private var lineChart: LineChart? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_linechart_alldata, container, false);
        lineChart = view.findViewById<LineChart>(R.id.lineChartAllData)
        val timeSeriesCovidDataDao = RoomDb.getAppDatabase(this.context!!)?.timeSeriesCovidDataDao()
        val monthlyStats = timeSeriesCovidDataDao?.getMonthlyData();
        setLineChart(monthlyStats!!)
        return view;
    }

    private fun setLineChart(monthlyStats:List<CovidMonthlyStatsTuple>) {

        print("in line chart")

        val lineEntries = ArrayList<Entry>()
        val labels = ArrayList<String>()
        for (i in 0 until monthlyStats?.size) {
            //val x:Float = (System.currentTimeMillis()/1000000).toFloat()-(i * 100)
            val x:Float = (i).toFloat()
            val y:Float = monthlyStats.get(i).newCases!!
            //Log.i(TAG, "(${x}, ${y})")
            if (x>0) {
                lineEntries.add(Entry(x,y))
            }
            labels.add(monthlyStats.get(i).monthGroup!!)
        }

        val dataSet =  LineDataSet(lineEntries, "Time series");
        val lineData = LineData(dataSet);
        lineChart?.setData(lineData);

        dataSet.setDrawValues(false)
        dataSet.setDrawFilled(true)
        dataSet.lineWidth = 3f
        dataSet.fillColor = R.color.colorPrimaryDark
        dataSet.fillAlpha = R.color.colorPrimary

        lineChart?.setTouchEnabled(true)
        lineChart?.description?.isEnabled = false
        lineChart?.getLegend()?.setEnabled(false);
        lineChart?.getAxisRight()?.setDrawGridLines(false);
        lineChart?.getAxisLeft()?.setDrawGridLines(false);
        lineChart?.getXAxis()?.setDrawGridLines(false);
        val xAxis = lineChart?.xAxis
        xAxis?.valueFormatter =  IndexAxisValueFormatter(labels)
        xAxis?.position = XAxis.XAxisPosition.BOTTOM

        val rightYAxis = lineChart!!.axisRight
        rightYAxis.isEnabled = false

        //xAxis?.setDrawGridLines(false);

        //val yAxis = lineChart?.y

        /*
        object : ValueFormatter() {
        override fun getFormattedValue(value: Float, axis: AxisBase): String {
            return
        }
    }
         */

        lineChart?.animateY(500)
        lineChart?.invalidate();
    }
}