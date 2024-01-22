package com.tp.googlesigninsample

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Telephony
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.googlesigninsample.databinding.FragmentSecondBinding

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    private val READ_SMS_PERMISSION_CODE = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonSecond.setOnClickListener {
//            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
            readMSg()
        }
    }



    fun readMSg(){
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_SMS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf<String>(Manifest.permission.READ_SMS),
                READ_SMS_PERMISSION_CODE
            )
        } else {
            readSms()
        }
    }

    private val smsList = ArrayList<String>()

    private fun readSms() {
        val contentResolver = this.requireActivity().contentResolver


        val projection = arrayOf(
            "_id", "address", "person",
            "body", "date", "type"
        )

        val cursor = contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            projection,
            null,
            null,
            "date desc"
        )


        var counter = 0


        val nameColumn = cursor!!.getColumnIndex("person")
        val phoneNumberColumn = cursor!!.getColumnIndex("address")
        val smsbodyColumn = cursor!!.getColumnIndex("body")
        val dateColumn = cursor!!.getColumnIndex("date")
        val typeColumn = cursor!!.getColumnIndex("type")


        if (cursor != null && cursor.moveToFirst()) {
            do {
                val address = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS))
                val body = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.BODY))
                smsList.add("Sender: $address\nMessage: $body")

               val name = (cursor.getString(nameColumn))
                val dateColumn = (cursor.getString(dateColumn))
                val phoneNumberColumn = (cursor.getString(phoneNumberColumn))
                val smsbodyColumn  = (cursor.getString(smsbodyColumn))
                val typeColumn  = (cursor.getString(typeColumn))



                Log.d("mvv1"," address $address  ,,,   ")

//                Log.d("mvv12"," name $name  dateColumn  $dateColumn   phoneNumberColumn   $phoneNumberColumn   smsbodyColumn  $smsbodyColumn   typeColumn  $typeColumn   ")


                counter++
            } while (cursor.moveToNext()   )
        }


        Log.d("mvv1"," smsList >>>>>  ${smsList.size}  ")
        cursor?.close()
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ_SMS_PERMISSION_CODE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                readSms()
//                val adapter = listView.getAdapter() as ArrayAdapter<String>
//                adapter.notifyDataSetChanged()
            }
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}