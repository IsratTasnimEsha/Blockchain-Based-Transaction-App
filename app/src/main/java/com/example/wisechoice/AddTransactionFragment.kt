package com.example.wisechoice

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AddTransactionFragment : Fragment() {
    private lateinit var receiver: EditText
    private lateinit var amount: EditText
    private lateinit var fees: EditText
    private lateinit var transact: Button

    private lateinit var st_phone: String

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var databaseReference: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_transaction, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        databaseReference = FirebaseDatabase.getInstance().getReference()

        receiver = view.findViewById(R.id.receiver)
        amount = view.findViewById(R.id.amount)
        fees = view.findViewById(R.id.fees)
        transact = view.findViewById(R.id.transact)

        sharedPreferences = requireContext().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE)

        st_phone = sharedPreferences.getString("Phone", "").toString()

        transact.setOnClickListener(object : View.OnClickListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onClick(view: View) {
                val st_receiver = receiver.text.toString()
                val st_amount = amount.text.toString()
                val st_fees = fees.text.toString()
                val unrecognized: String = "Unrecognized"

                val currentDateTime = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                val formattedDateTime = currentDateTime.format(formatter)

                if (st_receiver.isEmpty()) {
                        receiver.error = "Please Enter The Phone Number."
                }
                else if (st_amount.isEmpty()) {
                    amount.error = "Please Enter The Amount."
                }
                else if (st_fees.isEmpty()) {
                    fees.error = "Please Enter The Phone Number."
                }
                else {
                    databaseReference.child("transactions").child(st_phone).child("Amount").setValue(st_amount)
                    databaseReference.child("transactions").child(st_phone).child("Block_No").setValue(unrecognized)
                    databaseReference.child("transactions").child(st_phone).child("Fees").setValue(st_fees)
                    databaseReference.child("transactions").child(st_phone).child("Receiver").setValue(st_receiver)
                    databaseReference.child("transactions").child(st_phone).child("Sender").setValue(st_phone)
                    databaseReference.child("transactions").child(st_phone).child("Temp_Block").setValue(unrecognized)
                    databaseReference.child("transactions").child(st_phone).child("Transaction_Time").setValue(formattedDateTime.toString())
                    databaseReference.child("transactions").child(st_phone).child("Verify").setValue(unrecognized)

                    Toast.makeText(requireContext(), "The Transaction Has Occurred.", Toast.LENGTH_SHORT).show()
                    receiver.text.clear()
                    amount.text.clear()
                    fees.text.clear()
                }
            }
        })
    }
}