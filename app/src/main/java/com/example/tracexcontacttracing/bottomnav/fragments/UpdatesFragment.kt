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
import com.example.tracexcontacttracing.R
import com.example.tracexcontacttracing.database.RoomDb
import com.example.tracexcontacttracing.service.CovidDataService
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import org.joda.time.DateTime
import kotlin.collections.ArrayList

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
        val textViewCases = view.findViewById<TextView>(R.id.cases);
        val textViewDeaths = view.findViewById<TextView>(R.id.deaths);
        val textViewTodayDate = view.findViewById<TextView>(R.id.datetoday);
        val textViewVaccineInitiated = view.findViewById<TextView>(R.id.vaccine1);
        val textViewVaccineCompleted = view.findViewById<TextView>(R.id.vaccine2);
        StrictMode.enableDefaults();
        StrictMode.allowThreadDiskReads();
        val stateCovidDataDao = RoomDb.getAppDatabase(this.context!!)?.stateCovidDataDao()
        val timeSeriesCovidDataDao = RoomDb.getAppDatabase(this.context!!)?.TimeSeriesCovidDataDao()
        //TODO add logic to call store

        covidDataService.fetchAndStoreStateCovidData(stateCovidDataDao)
        covidDataService.fetchAndStoreTimeSeriesCovidData((timeSeriesCovidDataDao))

        val cases = stateCovidDataDao?.getTotalCases()
        val deaths = stateCovidDataDao?.getTotalDeaths()
        val vaccineInitiated = stateCovidDataDao?.getTotalVaccinationInitiated();
        val vaccineCompleted = stateCovidDataDao?.getTotalVaccinationCompleted();

        Log.i(TAG, "Total Cases: ${cases}, Total Deaths: ${deaths}")

        val dateToday = DateTime.now()
        val formatter = org.joda.time.format.DateTimeFormat.fullDate()
        val formatted = formatter.print(dateToday)
        textViewCases.setText(cases.toString())
        textViewDeaths.setText(deaths.toString())
        textViewTodayDate.setText(formatted.toString())
        textViewVaccineInitiated.setText(vaccineInitiated.toString())
        textViewVaccineCompleted.setText(vaccineCompleted.toString())
        setBarChart()
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
        entries.add(BarEntry(0f, 5f))
        entries.add(BarEntry(1f, 1f))
        entries.add(BarEntry(2f, 2f))
        entries.add(BarEntry(3f, 3f))
        entries.add(BarEntry(4f, 4f))
        entries.add(BarEntry(5f, 5f))

        val barDataSet = BarDataSet(entries, "Cells")

        val labels = ArrayList<String>()
        labels.add("18-Jan")
        labels.add("19-Jan")
        labels.add("20-Jan")
        labels.add("21-Jan")
        labels.add("22-Jan")
        labels.add("23-Jan")
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


}