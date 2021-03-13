package com.example.tracexcontacttracing.bottomnav.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.tracexcontacttracing.GetData
import com.example.tracexcontacttracing.R
import com.example.tracexcontacttracing.data.DeviceEntity
import com.example.tracexcontacttracing.database.RoomDb
import kotlinx.android.synthetic.main.fragment_checkin.*
import kotlinx.android.synthetic.main.fragment_checkin.view.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CheckinFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CheckinFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_checkin, container, false)

        view.saveButton.setOnClickListener {
            saveData()
        }

        // Inflate the layout for this fragment
        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CheckinFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CheckinFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    fun saveData() {
//        val deviceToken: EditText = findViewById(R.id.editViewDeviceToken)
//        val deviceId: EditText = findViewById(R.id.editViewDeviceId)

        val deviceTokenText = editViewDeviceToken.text.toString().trim()
        val deviceIdText = editViewDeviceId.text.toString().trim()

        // TODO check input
        val device = DeviceEntity(0, deviceTokenText, deviceIdText, System.currentTimeMillis(), System.currentTimeMillis())

        val deviceDao = RoomDb.getAppDatabase(this.context!!)?.deviceDao()
        val id = deviceDao?.insert(device)

        println("saved user $device with id=$id")

//        Toast.makeText(this, "Data Saved", Toast.LENGTH_SHORT).show()

        val intent: Intent = Intent(getActivity(), GetData::class.java);
        startActivity(intent);
//        startActivity(Intent(this, GetData::class.java))
    }
}