package com.example.wisechoice

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.example.wisechoice.R

class TransactionDetailsActivity : AppCompatActivity() {

    private lateinit var st_id: String
    private lateinit var transactionIDTextView: TextView
    private lateinit var senderTextView: TextView
    private lateinit var receiverTextView: TextView
    private lateinit var amountTextView: TextView
    private lateinit var feesTextView: TextView
    private lateinit var statusTextView: TextView
    private lateinit var transactionTimeTextView: TextView

    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_details)

        // Initialize views
        transactionIDTextView = findViewById(R.id.transaction_id)
        senderTextView = findViewById(R.id.sender)
        receiverTextView = findViewById(R.id.receiver)
        amountTextView = findViewById(R.id.amount)
        feesTextView = findViewById(R.id.fees)
        statusTextView = findViewById(R.id.status)
        transactionTimeTextView = findViewById(R.id.transaction_time)

        st_id = intent.getStringExtra("transaction_id") ?: ""

        databaseReference = FirebaseDatabase.getInstance().getReference("transactions").child(st_id)

        // Fetch and set data from Firebase
        fetchTransactionDetails()
    }

    private fun fetchTransactionDetails() {
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val transactionID = snapshot.child("Transaction_ID").value.toString()
                    val sender = snapshot.child("Sender").value.toString()
                    val receiver = snapshot.child("Receiver").value.toString()
                    val amount = snapshot.child("Amount").value.toString()
                    val fees = snapshot.child("Fees").value.toString()
                    val status = snapshot.child("Verify").value.toString()
                    val transactionTime = snapshot.child("Transaction_Time").value.toString()

                    transactionIDTextView.text = transactionID
                    senderTextView.text = sender
                    receiverTextView.text = receiver
                    amountTextView.text = amount
                    feesTextView.text = fees
                    statusTextView.text = status
                    transactionTimeTextView.text = transactionTime
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error if needed
            }
        })
    }
}
