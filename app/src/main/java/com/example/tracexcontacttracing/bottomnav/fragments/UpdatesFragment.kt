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
import com.example.tracexcontacttracing.fragment.StateDataTableFragment
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
import kotlinx.android.synthetic.main.fragment_updates.*
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
        val textViewCases = view.findViewById<TextView>(R.id.cases);
        val textViewDeaths = view.findViewById<TextView>(R.id.deaths);
        val textViewNewCases = view.findViewById<TextView>(R.id.newCases);
        val textViewNewDeaths = view.findViewById<TextView>(R.id.newDeaths);
        val textViewTodayDate = view.findViewById<TextView>(R.id.datetoday);
        val textViewVaccineInitiated = view.findViewById<TextView>(R.id.vaccine1);
        val textViewVaccineCompleted = view.findViewById<TextView>(R.id.vaccine2);
       // val viewTableButton = view.findViewById<TextView>(R.id.viewTableButton);
        StrictMode.enableDefaults();
        StrictMode.allowThreadDiskReads();
        StrictMode.allowThreadDiskWrites();
        val stateCovidDataDao = RoomDb.getAppDatabase(this.context!!)?.stateCovidDataDao()
        val timeSeriesCovidDataDao = RoomDb.getAppDatabase(this.context!!)?.timeSeriesCovidDataDao()

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

        // Tabs
        val tabLayout =  view.findViewById<TabLayout>(R.id.tabLayout);
        val viewPager =  view.findViewById<ViewPager2>(R.id.viewpg);
        viewPager.isSaveEnabled = false

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

        //viewTableButton.setOnClickListener {
        //    replaceFragment(StateDataTableFragment())
        //}

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

    private fun replaceFragment(fragment: Fragment) {
        val fragmentTransaction = activity?.supportFragmentManager?.beginTransaction()!!
        fragmentTransaction.replace(R.id.f1_wrapper, fragment)
        fragmentTransaction.addToBackStack("tag")
        fragmentTransaction.commit()
    }


}