package com.example.wisechoice

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AddTransactionFragment : Fragment(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var receiverField: EditText
    private lateinit var amountField: EditText
    private lateinit var feesField: EditText
    private lateinit var transact: Button
    private lateinit var signatureField: EditText
    private lateinit var signatureButton: Button
    private lateinit var st_phone: String
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var databaseReference: DatabaseReference

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_transaction, container, false)

        receiverField = view.findViewById(R.id.receiver)
        amountField = view.findViewById(R.id.amount)
        feesField = view.findViewById(R.id.fees)
        transact = view.findViewById(R.id.transact)
        signatureField = view.findViewById(R.id.signatureField)
        signatureButton = view.findViewById(R.id.signatureButton)

        databaseReference = FirebaseDatabase.getInstance().getReference()

        sharedPreferences = requireActivity().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE)
        st_phone = sharedPreferences.getString("Phone", "").toString()

        signatureButton.setOnClickListener {
            fetchSignatureFromFirebase()
        }

        transact.setOnClickListener {
            performTransaction()
        }

        return view
    }

    private fun fetchSignatureFromFirebase() {
        databaseReference.child("miners").child(st_phone)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.child("Signature").exists()) {
                        val signature = snapshot.child("Signature").value.toString()
                        signatureField.setText(signature)
                    } else {
                        Toast.makeText(requireContext(), "No signature found for the user.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle database error if needed
                }
            })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun performTransaction() {
        val st_receiver = receiverField.text.toString()
        val st_amount = amountField.text.toString().toDoubleOrNull() ?: 0.0
        val st_fees = feesField.text.toString().toDoubleOrNull() ?: 0.0
        val st_signature = signatureField.text.toString()
        val unrecognized: String = "Unrecognized"
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val formattedDateTime = currentDateTime.format(formatter)

        if (st_receiver.isEmpty() || st_amount == 0.0 || st_fees == 0.0 || st_signature.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all fields.", Toast.LENGTH_SHORT).show()
            return
        }

        val minersRef = databaseReference.child("miners")

        minersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var receiverExists = false
                var senderBalance = 0.0

                for (childSnapshot in snapshot.children) {
                    val phone = childSnapshot.key
                    if (phone == st_receiver) {
                        receiverExists = true
                    }
                    if (phone == st_phone) {
                        senderBalance = childSnapshot.child("Balance").value.toString().toDoubleOrNull() ?: 0.0
                    }
                }

                if (!receiverExists) {
                    Toast.makeText(requireContext(), "Receiver does not exist.", Toast.LENGTH_SHORT).show()
                    return
                }

                if (st_amount + st_fees > senderBalance) {
                    Toast.makeText(requireContext(), "Insufficient balance to perform the transaction.", Toast.LENGTH_SHORT).show()
                    return
                }

                // Transaction is ready to be performed...
                val newBalance = senderBalance - (st_amount + st_fees)

                // Proceed with updating the sender's balance and the transaction details...
                updateSenderBalance(newBalance, st_amount, st_fees, st_receiver, st_signature,formattedDateTime)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error if needed
            }
        })
    }

    private fun updateSenderBalance(newBalance: Double, amount: Double, fees: Double, receiver: String, signature: String ,formattedDateTime:String) {
        val senderRef = databaseReference.child("miners").child(st_phone)
        senderRef.child("Balance").setValue(newBalance)

        val transactionKey = databaseReference.child("transactions").push().key
        val newTransactionRef = senderRef.child("transactions").child(transactionKey!!)
        val refString = newTransactionRef.key

        newTransactionRef.apply {
            child("Amount").setValue(amount)
            child("Fees").setValue(fees)
            child("Receiver").setValue(receiver)
            child("Sender").setValue(st_phone)
            child("Signature").setValue(signature)
            child("Transaction_ID").setValue(refString.toString())
            newTransactionRef.child("Transaction_Time")
                .setValue(formattedDateTime.toString())
            child("Status").setValue("Unrecognized")
        }

        Toast.makeText(requireContext(), "The Transaction Has Occurred.", Toast.LENGTH_SHORT).show()

        receiverField.text.clear()
        amountField.text.clear()
        feesField.text.clear()
        signatureField.text.clear()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.block_queue -> {
                val intent = Intent(requireContext(), BlockQueueActivity::class.java)
                startActivity(intent)
            }
            R.id.blockchain -> {
                val intent = Intent(requireContext(), BlockchainActivity::class.java)
                startActivity(intent)
            }
            R.id.logout -> {
                val intent = Intent(requireContext(), SignInActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            }
        }
        return true
    }
}