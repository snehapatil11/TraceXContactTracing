package com.example.tracexcontacttracing.fragment

import android.os.Bundle
import android.os.StrictMode
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.tracexcontacttracing.R
import com.example.tracexcontacttracing.dao.TimeSeriesCovidDataDao
import com.example.tracexcontacttracing.database.RoomDb
import com.example.tracexcontacttracing.service.CovidDataService
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import org.joda.time.DateTime

class BarChartFragment : Fragment(){
    private var barChart: BarChart? = null;
    private var covidDataService: CovidDataService = CovidDataService();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_barchart, container, false);
        barChart =  view.findViewById<BarChart>(R.id.barChart);
        StrictMode.enableDefaults();
        StrictMode.allowThreadDiskReads();
        StrictMode.allowThreadDiskWrites();
        val timeSeriesCovidDataDao = RoomDb.getAppDatabase(this.context!!)?.timeSeriesCovidDataDao()
        setBarChart(timeSeriesCovidDataDao);
        return view;
    }

    private fun setBarChart(timeSeriesCovidDataDao: TimeSeriesCovidDataDao?) {

        print("in bar chart")
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        val dataOf2Weeks =  timeSeriesCovidDataDao?.get2WeeksTimeSeriesData()
        var count:Float = 1f;

        dataOf2Weeks?.map { i->
            entries.add(BarEntry(count, i.newCases.toFloat()))
            val d =  DateTime.parse(i.date);
            val day = d.dayOfMonth;
            val month = d.monthOfYear;
            labels.add("$month/$day")
            count++;
        }

        val barDataSet = BarDataSet(entries, "Cells")

        val data = BarData(barDataSet)
        barChart?.data = data // set the data and list of lables into chart

        barChart?.description  // set the description
        barDataSet.setDrawValues(false)
        barChart?.getLegend()?.setEnabled(false);

        //barDataSet.setColors(ColorTemplate.COLORFUL_COLORS)
        barDataSet.color = resources.getColor(R.color.colorAccent)

        val xAxis = barChart?.xAxis
        xAxis?.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis?.setLabelCount(6)
        xAxis?.position = XAxis.XAxisPosition.BOTTOM

        //xAxis?.granularity = 2f
        // xAxis?.setCenterAxisLabels(true)
        // xAxis?.isGranularityEnabled = true

        val barSpace = 0.02f
        val groupSpace = 0.3f
        val groupCount = 4f

        data.barWidth = 0.45f
        barChart?.setNoDataTextColor(R.color.colorPrimaryDark)
        barChart?.setTouchEnabled(true)
        barChart?.description?.isEnabled = false
        //barChart?.xAxis?.axisMinimum = 0f

        // barChart?.groupBars(0f, groupSpace, barSpace)

        barChart?.animateY(500)
        barChart?.invalidate();
    }
}

