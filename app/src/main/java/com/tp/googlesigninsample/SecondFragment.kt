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
import androidx.lifecycle.lifecycleScope
import com.example.googlesigninsample.databinding.FragmentSecondBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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



    private fun readMSg(){
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

        lifecycleScope.launch(Dispatchers.IO) {
            val cursor = contentResolver.query(
                Telephony.Sms.CONTENT_URI,
                projection,
                null,
                null,
                "date desc"
            )

            val nameColumn = cursor!!.getColumnIndex("person")
            val phoneNumberColumn = cursor!!.getColumnIndex("address")
            val smsbodyColumn = cursor!!.getColumnIndex("body")
            val dateColumn = cursor!!.getColumnIndex("date")
            val typeColumn = cursor!!.getColumnIndex("type")

            val TAG = "SMS_PARSER"
            val listBankCodes = arrayListOf("ICICIT", "ICICIB", "HDFCBK", "HDFCBN")
            val listCardLastDigits = arrayListOf("XX5018", "XX1407")
            val listKeyWords = arrayListOf("statement", "bill")

            val finalFilteredListMessages = ArrayList<String>()
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    val address =
                        cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS))
                    val body = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.BODY))
                    smsList.add("Sender: $address\nMessage: $body")

//               val name = (cursor.getString(nameColumn))
//                val dateColumn = (cursor.getString(dateColumn))
//                val phoneNumberColumn = (cursor.getString(phoneNumberColumn))
//                val smsbodyColumn  = (cursor.getString(smsbodyColumn))
//                val typeColumn  = (cursor.getString(typeColumn))

                    Log.d(TAG, " address $address")
                    if (isValidBank(address, listBankCodes) && isKeyWordPresent(
                            body,
                            listKeyWords
                        ) && isCardLastDigitsPresent(body, listCardLastDigits)
                    ) {
                        Log.d(TAG, "isValidSMS = true")
                        finalFilteredListMessages.add(body)
                    }

//                Log.d("mvv12"," name $name  dateColumn  $dateColumn   phoneNumberColumn   $phoneNumberColumn   smsbodyColumn  $smsbodyColumn   typeColumn  $typeColumn   ")


                } while (cursor.moveToNext())

            }




            Log.d(TAG, " finalFilteredListMessages size : ${finalFilteredListMessages.size}")
            Log.d(TAG, "smsList size >>>>>  ${smsList.size}  ")

            launch(Dispatchers.Main) {
                val showStr =
                    "Total SMS List size :  ${smsList.size} \n\n\nFinal Filtered SMS List size : ${finalFilteredListMessages.size}"
                binding.textviewSecond.text = showStr
            }

            cursor.close()
        }
    }

    private fun isValidBank(smsSenderStr : String, listBankCodes : ArrayList<String>) : Boolean {
        for(bankCode in listBankCodes) {
            if(smsSenderStr.lowercase().contains(bankCode.lowercase())) {
                return true
            }
        }
        return false
    }

    private fun isKeyWordPresent(smsBodyStr : String, listKeywords : ArrayList<String>) : Boolean {
        for(keyword in listKeywords) {
            if(smsBodyStr.lowercase().contains(keyword.lowercase())) {
                return true
            }
        }
        return false
    }

    private fun isCardLastDigitsPresent(smsBodyStr : String, listCardLastDigits : ArrayList<String>) : Boolean {
        for(cardNumberLastDigits in listCardLastDigits) {
            if(smsBodyStr.lowercase().contains(cardNumberLastDigits.lowercase())) {
                return true
            }
        }
        return false
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