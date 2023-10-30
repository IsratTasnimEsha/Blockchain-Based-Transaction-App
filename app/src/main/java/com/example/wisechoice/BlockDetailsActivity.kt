package com.example.wisechoice

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class BlockDetailsAdapter(
    private val context: Context,
    private val senders: List<String>,
    private val receivers: List<String>,
    private val amounts: List<String>,
    private val feeses: List<String>,
    private val verifies: List<String>,
    private val ids: List<String>,
    private val transaction_times: List<String>,
    private val st_id: String,

    private var databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference()

) : RecyclerView.Adapter<BlockDetailsAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.temp_block_xml, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        holder.sender.text = "${senders[position]}"
        holder.receiver.text = "${receivers[position]}"
        holder.amount.text = "${amounts[position]}"
        holder.fees.text = "${feeses[position]}"
        holder.verify.text = "${verifies[position]}"
        val idValue = ids[position]

        if(verifies[position] == "Unrecognized") {
            holder.verify_button.isEnabled = true
            holder.verify_button.text = "Verify"
            holder.transaction_card.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
        }

        else if(verifies[position] == "Verified") {
            holder.verify_button.isEnabled = false
            holder.transaction_card.setBackgroundColor(ContextCompat.getColor(context, R.color.green))
        }

        else if(verifies[position] == "Not Verified") {
            holder.verify_button.isEnabled = false
            holder.transaction_card.setBackgroundColor(ContextCompat.getColor(context, R.color.dark_green ))
        }

        holder.verify_button.setOnClickListener {
            val sharedPreferences =
                context.getSharedPreferences("MySharedPref", Context.MODE_PRIVATE)
            val st_phone = sharedPreferences.getString("Phone", "") ?: ""

            if(holder.verify.text == "Unrecognized") {
                val newTransactionRef = databaseReference.child("miners").child(st_phone)
                    .child("block_queue").child(st_id).child("transaction_details").child(idValue)
                newTransactionRef.child("Status").setValue("Verified")

                holder.verify.text = "Verified"
                holder.verify_button.isEnabled = true
                holder.transaction_card.setBackgroundColor(ContextCompat.getColor(context, R.color.green))
            }
        }

        holder.transaction_card.setOnClickListener {

            val intent = Intent(context, TransactionDetailsActivity::class.java)

            intent.putExtra("transaction_id", idValue)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return senders.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var sender: TextView = itemView.findViewById(R.id.sender)
        var receiver: TextView = itemView.findViewById(R.id.receiver)
        var amount: TextView = itemView.findViewById(R.id.amount)
        var fees: TextView = itemView.findViewById(R.id.fees)
        var verify: TextView = itemView.findViewById(R.id.verify)
        var verify_button: Button = itemView.findViewById(R.id.verify_button)
        var transaction_card = itemView.findViewById<CardView>(R.id.transaction_card)
    }
}

class BlockDetailsActivity : AppCompatActivity() {

    private lateinit var st_id: String
    private lateinit var blockIDTextView: TextView
    private lateinit var blockHashTextView: TextView
    private lateinit var previousHashTextView: TextView
    private lateinit var nonceTextView: TextView
    private lateinit var minerTextView: TextView
    private lateinit var noOfTransactionsTextView: TextView
    private lateinit var totalSentTextView: TextView
    private lateinit var sizeTextView: TextView
    private lateinit var totalFeesTextView: TextView
    private lateinit var minedTextView: TextView
    private lateinit var acceptButton: Button

    private lateinit var databaseReference: DatabaseReference

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapterClass: BlockDetailsAdapter

    private val senders = mutableListOf<String>()
    private val receivers = mutableListOf<String>()
    private val amounts = mutableListOf<String>()
    private val feeses = mutableListOf<String>()
    private val verifies = mutableListOf<String>()
    private val ids = mutableListOf<String>()
    private val transaction_times = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_block_details)

        blockIDTextView = findViewById(R.id.block_id)
        blockHashTextView = findViewById(R.id.block_hash)
        previousHashTextView= findViewById(R.id.previous_hash)
        nonceTextView= findViewById(R.id.nonce)
        minerTextView= findViewById(R.id.miner)
        noOfTransactionsTextView= findViewById(R.id.no_of_transactions)
        totalSentTextView= findViewById(R.id.total_sent)
        sizeTextView= findViewById(R.id.size)
        totalFeesTextView= findViewById(R.id.total_fees)
        minedTextView= findViewById(R.id.mined)
        acceptButton = findViewById(R.id.accept_button)

        st_id = intent.getStringExtra("block_id") ?: ""

        val sharedPreferences = getSharedPreferences("MySharedPref", Context.MODE_PRIVATE)
        val st_phone = sharedPreferences.getString("Phone", "") ?: ""

        fetchTransactionDetails(st_phone)

        databaseReference = FirebaseDatabase.getInstance().getReference("miners").child(st_phone)
            .child("block_queue").child(st_id).child("transaction_details")

        recyclerView = findViewById(R.id.recycler)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapterClass = BlockDetailsAdapter(
            this,
            senders,
            receivers,
            amounts,
            feeses,
            verifies,
            ids,
            transaction_times,
            st_id
        )
        recyclerView.adapter = adapterClass

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                senders.clear()
                receivers.clear()
                amounts.clear()
                feeses.clear()
                verifies.clear()
                ids.clear()
                transaction_times.clear()

                for (dataSnapshot in snapshot.children) {
                    val sender = dataSnapshot.child("Sender").value.toString()
                    val receiver = dataSnapshot.child("Receiver").value.toString()
                    val amount = dataSnapshot.child("Amount").value.toString()
                    val fees = dataSnapshot.child("Fees").value.toString()
                    val verify = dataSnapshot.child("Status").value.toString()
                    val transaction_time = dataSnapshot.child("Transaction_Time").value.toString()
                    val id = dataSnapshot.child("Transaction_ID").value.toString()

                    senders.add(sender)
                    receivers.add(receiver)
                    amounts.add(amount)
                    feeses.add(fees)
                    verifies.add(verify)
                    ids.add(id)
                    transaction_times.add(transaction_time)
                }
                adapterClass.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun fetchTransactionDetails(st_phone: String) {
        databaseReference = FirebaseDatabase.getInstance().getReference("miners").child(st_phone)
            .child("block_queue").child(st_id)

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val blockID = st_id
                    val blockHash = snapshot.child("Block_Hash").value.toString()
                    val previousHash = snapshot.child("Previous_Hash").value.toString()
                    val nonce = snapshot.child("Nonce").value.toString()
                    val miner = snapshot.child("Miner").value.toString()
                    val noOfTransactions = snapshot.child("No_Of_Transactions").value.toString()
                    val totalSent = snapshot.child("Total_Amount").value.toString()
                    val size = snapshot.child("Size").value.toString()
                    val totalFees = snapshot.child("Total_Fees").value.toString()
                    val minedTime = snapshot.child("Mined_Time").value.toString()

                    blockIDTextView.text = blockID
                    blockHashTextView.text = blockHash
                    previousHashTextView.text = previousHash
                    nonceTextView.text = nonce
                    minerTextView.text = miner
                    noOfTransactionsTextView.text = noOfTransactions
                    totalSentTextView.text = totalSent
                    sizeTextView.text = size
                    totalFeesTextView.text = totalFees
                    minedTextView.text = minedTime

                    acceptButton.setOnClickListener {
                        val blockchainReference = FirebaseDatabase.getInstance().getReference("miners")
                            .child(st_phone).child("blockchain").child(st_id)

                        FirebaseDatabase.getInstance().getReference("miners").child(st_phone)
                            .child("block_queue").child(st_id)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.exists()) {
                                        blockchainReference.setValue(snapshot.value)

                                        for (childSnapshot in snapshot.child("transaction_details").children) {
                                            val childKey = childSnapshot.key

                                            if (childKey != null) {
                                                FirebaseDatabase.getInstance().getReference("miners").child(st_phone)
                                                    .child("transactions").child(childKey)
                                                    .child("Status").setValue("Processing...")
                                            }
                                        }

                                        FirebaseDatabase.getInstance().getReference("miners").child(st_phone)
                                            .child("block_queue").removeValue()
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {

                                }
                            })


                        FirebaseDatabase.getInstance().getReference("miners").child(st_phone)
                            .child("block_queue").child(st_id).removeValue()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}