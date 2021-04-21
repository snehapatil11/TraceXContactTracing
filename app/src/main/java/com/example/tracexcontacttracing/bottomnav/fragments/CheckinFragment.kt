package com.example.tracexcontacttracing.bottomnav.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.tracexcontacttracing.R
import com.example.tracexcontacttracing.data.CheckinRecordEntity
import com.example.tracexcontacttracing.data.DeviceEntity
import com.example.tracexcontacttracing.database.RoomDb
import com.example.tracexcontacttracing.fragment.CheckinDetailFragment
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_checkin.view.*
import java.util.*


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
    private val TAG = "CheckinFragment"
    private lateinit var database: DatabaseReference

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

        view.checkIn.setOnClickListener {
            checkIn()
        }

        view.uploadData.setOnClickListener {
            getConsent()
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

    private fun getConsent() {
        val builder = AlertDialog.Builder(context)
        builder.setMessage("Confirm to share exposed devices data that you have been in close contact with?")
        builder.setTitle("TraceX")

        builder.setPositiveButton("I consent") { dialog, which ->
            dialog.dismiss()
            Log.d(TAG, "Yes on consent. Uploading data...")
            uploadData()
        }
        builder.setNegativeButton(
            "No"
        ) { dialog, which ->
            dialog.dismiss()
            Log.d(TAG, "Clicked No")
        }

        val d = builder.create()
        d.show()
    }

    private fun uploadData() {
        database = Firebase.database.reference

        simulateDeviceUpload() // testing, comment out if not needed

        uploadExposedDevices() // actual upload
    }

    private fun uploadDevice(device: DeviceEntity) {
        database.child("exposed_devices").child(device.deviceId).setValue(device)

        Toast.makeText(context, "Device ${device.deviceId} Data Uploaded", Toast.LENGTH_SHORT).show()
    }

    private fun simulateDeviceUpload() {
        val device = generateRandomDevice()
        uploadDevice(device)
    }

    private fun generateRandomDevice(): DeviceEntity {
        val deviceId = UUID.randomUUID().toString().substring(0, 7)
        return DeviceEntity(
            "token1",
            deviceId,
            System.currentTimeMillis(),
            System.currentTimeMillis()
        )
    }

    private fun uploadExposedDevices() {
        val deviceDao = RoomDb.getAppDatabase(this.requireContext())?.deviceDao()
        val devices = deviceDao?.getDeviceData()

        devices?.forEach {
            uploadDevice(it)
            Log.d(TAG, "uploaded $it")
        }
    }


    private fun checkIn() {
        Log.d(TAG, "moving to check in details fragment")

        val checkBox1 = view?.symptom1 as CheckBox
        val checkBox2 = view?.symptom2 as CheckBox
        val checkBox3 = view?.symptom3 as CheckBox
        val checkBox4 = view?.symptom4 as CheckBox

        if (!checkBox1.isChecked() && !checkBox2.isChecked() && !checkBox3.isChecked() && !checkBox4.isChecked()) {
            Toast.makeText(context, "Glad you feel okay. Stay safe!", Toast.LENGTH_SHORT).show()

            replaceFragment(CheckinDetailFragment())

            return
        }

        val isFever = checkBox1.isChecked()
        val isCough = checkBox2.isChecked()
        val isTasteLoss = checkBox3.isChecked()
        val isSoreThroat = checkBox4.isChecked()

        val checkinRecord = CheckinRecordEntity(
            0,
            isFever,
            isCough,
            isTasteLoss,
            isSoreThroat,
            System.currentTimeMillis(),
            System.currentTimeMillis()
        )

        val checkinRecordDao = RoomDb.getAppDatabase(this.context!!)?.checkinRecordDao()
        val id = checkinRecordDao?.insert(checkinRecord)

        Log.d(TAG, "Record $id was saved $checkinRecord")

        // Reset
        checkBox1.setChecked(false);
        checkBox2.setChecked(false);
        checkBox3.setChecked(false);
        checkBox4.setChecked(false);

        replaceFragment(CheckinDetailFragment())
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentTransaction = activity?.supportFragmentManager?.beginTransaction()!!
        fragmentTransaction.replace(R.id.f1_wrapper, fragment)
        fragmentTransaction.disallowAddToBackStack()
        fragmentTransaction.commit()
    }

}