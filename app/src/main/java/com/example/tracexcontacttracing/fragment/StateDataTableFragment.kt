package com.example.tracexcontacttracing.fragment

import android.os.Bundle
import android.os.StrictMode
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.AppCompatImageButton
import androidx.fragment.app.Fragment
import com.example.tracexcontacttracing.R
import com.example.tracexcontacttracing.bottomnav.fragments.UpdatesFragment
import com.example.tracexcontacttracing.database.RoomDb


class StateDataTableFragment : Fragment(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_statedata_table, container, false);
        StrictMode.enableDefaults();
        StrictMode.allowThreadDiskReads();
        StrictMode.allowThreadDiskWrites();
        val stateCovidDataDao = RoomDb.getAppDatabase(this.context!!)?.stateCovidDataDao()
        val backButton = view.findViewById<AppCompatImageButton>(R.id.backButton);
        backButton.setOnClickListener {
            replaceFragment(UpdatesFragment())
        }
        val tableLayout =  view.findViewById<LinearLayout>(R.id.innerTable);
        val stateData = stateCovidDataDao?.getAllStateData();
        var i = 0;
        val totalWidth = this.activity?.windowManager?.defaultDisplay?.width

        stateData?.map {
            val row = TableRow(view.context)
            val lp = TableLayout.LayoutParams()
            row.setLayoutParams(lp)
            row.setPadding(50, 20, 20, 15);
            val stateName = it.state;
            val stateView = TextView(context)
            stateView.setText(stateName)
            stateView.width = totalWidth!!/3
            stateView.textAlignment = View.TEXT_ALIGNMENT_TEXT_START;

            val stateCases = it.cases.toString();
            val casesView = TextView(context)
            casesView.text = stateCases
            casesView.width = totalWidth!!/3;
            casesView.textAlignment = View.TEXT_ALIGNMENT_TEXT_START;

            val stateNewCases = it.newCases.toString();
            val newCasesView = TextView(context)
            newCasesView.text = stateNewCases
            newCasesView.width = totalWidth!!/3
            newCasesView.textAlignment = View.TEXT_ALIGNMENT_TEXT_START;

            row.addView(stateView)
            row.addView(casesView)
            row.addView(newCasesView)
            tableLayout.addView(row, i)
            i++;
        }
        return view;
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentTransaction = activity?.supportFragmentManager?.beginTransaction()!!
        fragmentTransaction.replace(R.id.f1_wrapper, fragment)
        fragmentTransaction.addToBackStack("tag")
        fragmentTransaction.commit()
    }

}