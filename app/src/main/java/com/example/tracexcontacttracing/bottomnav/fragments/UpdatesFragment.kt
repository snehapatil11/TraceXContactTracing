package com.example.tracexcontacttracing.bottomnav.fragments

import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.tracexcontacttracing.R
import com.example.tracexcontacttracing.data.CovidMonthlyStatsTuple
import com.example.tracexcontacttracing.database.RoomDb
import com.example.tracexcontacttracing.fragment.BarChartFragment
import com.example.tracexcontacttracing.fragment.LineChart2MonthsDataFragment
import com.example.tracexcontacttracing.fragment.LineChartAllDataFragment
import com.example.tracexcontacttracing.fragment.tabs.ChartTabsAdapterV2
import com.example.tracexcontacttracing.service.CovidDataService
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import org.joda.time.DateTime

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [UpdatesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UpdatesFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var barChart: BarChart? = null
    private var lineChart: LineChart? = null
    private var param2: String? = null
    private var covidDataService:CovidDataService = CovidDataService();
    val TAG = "UpdateFragment"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        //setContentView(R.layout.activity_main);

       // barChart = findViewById(R.id.barChart) as BarChart;
       // barChart = findViewById<View>(R.id.barChart) as BarChart
        //setBarChart()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_updates, container, false);
        barChart =  view.findViewById<BarChart>(R.id.barChart);
        lineChart = view.findViewById<LineChart>(R.id.lineChart)
        val textViewCases = view.findViewById<TextView>(R.id.cases);
        val textViewDeaths = view.findViewById<TextView>(R.id.deaths);
        val textViewNewCases = view.findViewById<TextView>(R.id.newCases);
        val textViewNewDeaths = view.findViewById<TextView>(R.id.newDeaths);
        val textViewTodayDate = view.findViewById<TextView>(R.id.datetoday);
        val textViewVaccineInitiated = view.findViewById<TextView>(R.id.vaccine1);
        val textViewVaccineCompleted = view.findViewById<TextView>(R.id.vaccine2);
        StrictMode.enableDefaults();
        StrictMode.allowThreadDiskReads();
        StrictMode.allowThreadDiskWrites();
        val stateCovidDataDao = RoomDb.getAppDatabase(this.context!!)?.stateCovidDataDao()
        val timeSeriesCovidDataDao = RoomDb.getAppDatabase(this.context!!)?.timeSeriesCovidDataDao()
        //TODO add logic to call store


        val currentTs = System.currentTimeMillis()
        val lastUpdatedTsOfData = stateCovidDataDao?.getLastUpdatedTsForData()
        if(lastUpdatedTsOfData!=null) {
            val lastUpdatedDate = DateTime(lastUpdatedTsOfData).dayOfMonth
            val currentDate = DateTime(currentTs).dayOfMonth
            Log.i(TAG, "Last Updated Date is: "+lastUpdatedDate);
            Log.i(TAG, "Current Date is: "+currentDate);

            //if (currentTs- lastUpdatedTsOfData!! >= 1000*60) { // fetch after 1 min
            if (currentDate != lastUpdatedDate) { // fetch once date changes
                Log.i(TAG, "Fetching new data from Covid API")
                covidDataService.fetchAndStoreStateCovidData(stateCovidDataDao)
                covidDataService.fetchAndStoreTimeSeriesCovidData(timeSeriesCovidDataDao)
            }
        }

        val cases = stateCovidDataDao?.getTotalCases()
        val deaths = stateCovidDataDao?.getTotalDeaths()
        val newCases = stateCovidDataDao?.getTotalNewCases()
        val newDeaths = stateCovidDataDao?.getTotalNewDeaths()
        val vaccineInitiated = stateCovidDataDao?.getTotalVaccinationInitiated();
        val vaccineCompleted = stateCovidDataDao?.getTotalVaccinationCompleted();

        Log.i(TAG, "Total Cases: ${cases}, Total Deaths: ${deaths}")

        val monthlyStats = timeSeriesCovidDataDao?.getMonthlyData();

        val dateToday = DateTime.now()
        val formatter = org.joda.time.format.DateTimeFormat.fullDate()
        val formatted = formatter.print(dateToday)
        textViewCases.setText(cases.toString())
        textViewDeaths.setText(deaths.toString())
        textViewNewCases.setText(newCases.toString())
        textViewNewDeaths.setText(newDeaths.toString())
        textViewTodayDate.setText(formatted.toString())
        textViewVaccineInitiated.setText(vaccineInitiated.toString())
        textViewVaccineCompleted.setText(vaccineCompleted.toString())
        setBarChart()
        setLineChart(monthlyStats!!)

        // Tabs
        val tabLayout =  view.findViewById<TabLayout>(R.id.tabLayout);
        val viewPager =  view.findViewById<ViewPager2>(R.id.viewpg);

        //val chartTabsAdapter = ChartTabsAdapter(fragmentManager!!)
        val chartTabsAdapter = ChartTabsAdapterV2(this)
        chartTabsAdapter.addFragment(BarChartFragment(), "Last 2 Weeks")
        chartTabsAdapter.addFragment(LineChart2MonthsDataFragment(), "Last 2 Months")
        chartTabsAdapter.addFragment(LineChartAllDataFragment(), "All")
        //chartTabsAdapter.addFragment(BarChartFragment(), "Bar Chart2")
        viewPager.adapter = chartTabsAdapter;
        //tabLayout.setupWithViewPager(viewPager, true);
        tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {}
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        TabLayoutMediator(tabLayout, viewPager,
            TabLayoutMediator.TabConfigurationStrategy { tab, position ->
                tab.text = chartTabsAdapter.getTitle(position)
                viewPager.setCurrentItem(tab.position, true)
            }).attach()

        return view;
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment UpdatesFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            UpdatesFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun setBarChart() {

        print("in bar chart")

        val entries = ArrayList<BarEntry>()
        entries.add(BarEntry(1f, 0.5f))
        entries.add(BarEntry(1f, 1f))
        entries.add(BarEntry(2f, 2f))
        entries.add(BarEntry(3f, 2f))
        entries.add(BarEntry(4f, 1.5f))
        entries.add(BarEntry(5f, 1f))
        entries.add(BarEntry(6f, 1f))

        val barDataSet = BarDataSet(entries, "Cells")

        val labels = ArrayList<String>()
        labels.add("15-April")
        labels.add("16-April")
        labels.add("17-April")
        labels.add("18-April")
        labels.add("19-April")
        labels.add("20-April")
        labels.add("21-April")
        val data = BarData(barDataSet)
        barChart?.data = data // set the data and list of lables into chart

        barChart?.description  // set the description

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

    private fun setLineChart(monthlyStats:List<CovidMonthlyStatsTuple>) {

        print("in line chart")

        val lineEntries = ArrayList<Entry>()
        val labels = ArrayList<String>()
        for (i in 0 until monthlyStats?.size) {
            //val x:Float = (System.currentTimeMillis()/1000000).toFloat()-(i * 100)
            val x:Float = (i).toFloat()
            val y:Float = monthlyStats.get(i).newCases!!
            Log.i(TAG, "(${x}, ${y})")
            if (x>0) {
                lineEntries.add(Entry(x,y))
            }
            labels.add(monthlyStats.get(i).monthGroup!!)
        }

        val dataSet =  LineDataSet(lineEntries, "Time series");
        val lineData = LineData(dataSet);
        lineChart?.setData(lineData);

        lineChart?.setTouchEnabled(true)
        lineChart?.description?.isEnabled = false
        //barChart?.xAxis?.axisMinimum = 0f

        // barChart?.groupBars(0f, groupSpace, barSpace)
        val xAxis = lineChart?.xAxis
        xAxis?.valueFormatter =  IndexAxisValueFormatter(labels)
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