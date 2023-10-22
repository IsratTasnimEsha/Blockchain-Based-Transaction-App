package com.example.wisechoice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wisechoice.R
import com.example.wisechoice.TransactionDetailsActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.security.MessageDigest

class MineBlockAdapter(
    private val context: Context,
    private val senders: List<String>,
    private val receivers: List<String>,
    private val amounts: List<String>,
    private val feeses: List<String>,
    private val ids: List<String>,
    private val transaction_times: List<String>,
) : RecyclerView.Adapter<MineBlockAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.mine_block, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.sender.text = "${senders[position]}"
        holder.receiver.text = "${receivers[position]}"
        holder.amount.text = "${amounts[position]}"
        holder.fees.text = "${feeses[position]}"
        val idValue = ids[position]

        holder.remove_button.setOnClickListener {
            val sharedPreferences =
                context.getSharedPreferences("MySharedPref", Context.MODE_PRIVATE)
            val st_phone = sharedPreferences.getString("Phone", "") ?: ""

            // Update the "Verify" field of the corresponding transaction to "Verified"
            val newTransactionRef = FirebaseDatabase.getInstance().getReference("miners")
                .child(st_phone).child("transactions").child(idValue)
            newTransactionRef.child("Verify").setValue("Verified")

            // Remove the corresponding item from temporary_blocks
            val tempTransactionRef = FirebaseDatabase.getInstance().getReference("miners")
                .child(st_phone).child("temporary_blocks").child(idValue)
            tempTransactionRef.removeValue()
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
        var remove_button: Button = itemView.findViewById(R.id.remove_button)
        var transaction_card = itemView.findViewById<CardView>(R.id.transaction_card)
    }
}

class MineFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapterClass: MineBlockAdapter
    private lateinit var mineButton: Button
    private lateinit var hashText: TextView

    private val senders = mutableListOf<String>()
    private val receivers = mutableListOf<String>()
    private val amounts = mutableListOf<String>()
    private val feeses = mutableListOf<String>()
    private val ids = mutableListOf<String>()
    private val transaction_times = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mine, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = requireContext().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE)
        val st_phone = sharedPreferences.getString("Phone", "") ?: ""

        val tempBlocksReference = FirebaseDatabase.getInstance().getReference("miners")
            .child(st_phone).child("temporary_blocks")

        recyclerView = view.findViewById(R.id.recycler)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapterClass = MineBlockAdapter(
            requireContext(),
            senders,
            receivers,
            amounts,
            feeses,
            ids,
            transaction_times
        )
        recyclerView.adapter = adapterClass

        mineButton = view.findViewById(R.id.mine_button)

        // Initialize the TextView for hash text
        hashText = view.findViewById(R.id.hash_text)

        mineButton.setOnClickListener {
            val st_phone = sharedPreferences.getString("Phone", "") ?: ""

            // 1. Concatenate all the transaction rows into a single string
            val concatenatedTransactions = StringBuilder()

            for (idValue in ids) {
                val sender = senders[ids.indexOf(idValue)]
                val receiver = receivers[ids.indexOf(idValue)]
                val amount = amounts[ids.indexOf(idValue)]
                val fees = feeses[ids.indexOf(idValue)]

                val transactionInfo = "Sender: $sender, Receiver: $receiver, Amount: $amount, Fees: $fees\n"
                concatenatedTransactions.append(transactionInfo)
            }

            val concatenatedString = concatenatedTransactions.toString()

            var hashedString: String
            var randomNotch: Int
            Thread {
                do {
                    // 2. Add a 5-digit random notch to the concatenated string
                    randomNotch = (10000..99999).random()
                    val stringWithNotch = "$randomNotch$concatenatedString"

                    // 3. Create a hash of the final string
                    hashedString = hashString(stringWithNotch)

                    // Update the TextView with the changing hash
                    requireActivity().runOnUiThread() {
                        hashText.text = "$hashedString"
                    }

                    // Add a delay of 1 second (adjust as needed)
                    Thread.sleep(10)

                } while (!hashedString.startsWith("00"))

                val blockVal = FirebaseDatabase.getInstance().getReference("miners")
                    .child(st_phone).child("blocks").push()


                blockVal.child("Block_Hash").setValue("$hashedString")
                blockVal.child("Nonce").setValue(randomNotch)
                blockVal.child("Miner").setValue(st_phone)

                val blockRef = blockVal.child("transaction_details")

                for (idValue in ids) {
                    val transactionRef = FirebaseDatabase.getInstance().getReference("miners")
                        .child(st_phone).child("transactions")

                    val query = transactionRef.orderByChild("Transaction_ID").equalTo(idValue)
                    query.addListenerForSingleValueEvent(object : ValueEventListener {

                        override fun onDataChange(transactionSnapshot: DataSnapshot) {
                            for (transactionData in transactionSnapshot.children) {
                                val transactionId = transactionData.key.toString()
                                val transactionVerifyRef = transactionRef.child(transactionId)
                                    .child("Verify")

                                transactionVerifyRef.setValue("Processing...")
                                blockRef.child(transactionId).child("Transaction_ID").setValue(ids[ids.indexOf(idValue)])
                                blockRef.child(transactionId).child("Sender").setValue(senders[ids.indexOf(idValue)])
                                blockRef.child(transactionId).child("Receiver").setValue(receivers[ids.indexOf(idValue)])
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {
                        // Handle the error
                        }
                    })
                    // Remove the corresponding item from temporary_blocks
                    val tempTransactionRef = FirebaseDatabase.getInstance().getReference("miners")
                        .child(st_phone).child("temporary_blocks").child(idValue)

                    tempTransactionRef.removeValue()
                }

            }.start()

            // Use 'hashedString' as needed for your application
        }

        tempBlocksReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                senders.clear()
                receivers.clear()
                amounts.clear()
                feeses.clear()
                ids.clear()
                transaction_times.clear()

                for (dataSnapshot in snapshot.children) {
                    val sender = dataSnapshot.child("Sender").value.toString()
                    val receiver = dataSnapshot.child("Receiver").value.toString()
                    val amount = dataSnapshot.child("Amount").value.toString()
                    val fees = dataSnapshot.child("Fees").value.toString()
                    val verify = dataSnapshot.child("Verify").value.toString()
                    val transaction_time = dataSnapshot.child("Transaction_Time").value.toString()
                    val id = dataSnapshot.child("Transaction_ID").value.toString()

                    senders.add(sender)
                    receivers.add(receiver)
                    amounts.add(amount)
                    feeses.add(fees)
                    ids.add(id)
                    transaction_times.add(transaction_time)
                }
                adapterClass.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun hashString(input: String): String {
        val bytes = input.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)

        val hexString = StringBuilder()
        for (byte in digest) {
            hexString.append(String.format("%02x", byte))
        }

        return hexString.toString()
    }
}