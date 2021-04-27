package com.example.tracexcontacttracing.bottomnav.fragments

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.tracexcontacttracing.R
import com.example.tracexcontacttracing.blemodule.BLEService

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ContacttracingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ContacttracingFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var inflatedView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_contacttracing, container, false)
        val isServiceRunning = BLEService.isAppInForeground
        inflatedView = inflater.inflate(R.layout.fragment_contacttracing, container, false)

        val serviceIntent = Intent(context, BLEService::class.java)
        serviceIntent.putExtra("inputExtra", "Foreground Service in Android")

        val button = inflatedView.findViewById(R.id.scan_ble) as Button
        if (!isServiceRunning) {

            button.setBackgroundColor(Color.parseColor("#6200EE"))
            button.setText("Enable Contact Tracing")
            button.setTextColor(Color.parseColor("#ffffff"))

            button.setOnClickListener {
                Log.d(null, "Initiated background task")
                Toast.makeText(context, "Contact Tracing Service Started", Toast.LENGTH_SHORT)
                    .show()
                //context?.startForegroundService(serviceIntent)

                button.setBackgroundColor(Color.RED)
                button.setText("Disable Contact Tracing")
                button.setTextColor(Color.parseColor("#ffffff"))

                context?.startService(serviceIntent)

                BLEService.isAppInForeground = true
                //parentFragmentManager.beginTransaction().detach(this).attach(this).commit()
            }
        } else {
            button.setBackgroundColor(Color.RED)
            button.setText("Disable Contact Tracing")
            button.setTextColor(Color.parseColor("#ffffff"))

            button.setOnClickListener {
                button.setBackgroundColor(Color.parseColor("#6200EE"))
                button.setText("Enable Contact Tracing")
                button.setTextColor(Color.parseColor("#ffffff"))

                context?.stopService(serviceIntent)
                BLEService.isAppInForeground = false
                //parentFragmentManager.beginTransaction().detach(this).attach(this).commit()
            }

        }
        return inflatedView
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ContacttracingFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ContacttracingFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}