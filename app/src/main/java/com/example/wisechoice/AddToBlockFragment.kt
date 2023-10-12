package com.example.wisechoice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class TempBlockAdapter(
    private val context: Context,
    private val senders: List<String>,
    private val receivers: List<String>,
    private val amounts: List<String>,
    private val feeses: List<String>,
    private val verifies: List<String>,
    private val ids: List<String>,

    private var databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference()

) : RecyclerView.Adapter<TempBlockAdapter.MyViewHolder>() {

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

        if(holder.verify.text == "Verified") {
            holder.verify_button.text = "Add To Block"
        }

        holder.verify_button.setOnClickListener {
            val idValue = ids[position]

            val sharedPreferences =
                context.getSharedPreferences("MySharedPref", Context.MODE_PRIVATE)
            val st_phone = sharedPreferences.getString("Phone", "") ?: ""

            val newTransactionRef = databaseReference.child("miners").child(st_phone)
                .child("transactions").child(idValue)
            newTransactionRef.child("Verify").setValue("Verified")

            holder.verify.text = "Verified"
            holder.verify_button.text = "Add To Block"

        }

        holder.transaction_card.setOnClickListener {
            val idValue = ids[position]

            val intent = Intent(context, TransactionDetailsActivity::class.java)
            intent.putExtra("transaction_id", idValue.toString())
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

class AddToBlockFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapterClass: TempBlockAdapter

    private lateinit var databaseReference: DatabaseReference

    private val senders = mutableListOf<String>()
    private val receivers = mutableListOf<String>()
    private val amounts = mutableListOf<String>()
    private val feeses = mutableListOf<String>()
    private val verifies = mutableListOf<String>()
    private val ids = mutableListOf<String>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_to_block, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var sharedPreferences =
            requireContext().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE)
        var st_phone = sharedPreferences.getString("Phone", "") ?: ""

        databaseReference = FirebaseDatabase.getInstance().getReference("miners").child(st_phone)
            .child("transactions")

        recyclerView = view.findViewById(R.id.recycler)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapterClass = TempBlockAdapter(
            requireContext(),
            senders,
            receivers,
            amounts,
            feeses,
            verifies,
            ids
        )
        recyclerView.adapter = adapterClass

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                senders.clear()
                receivers.clear()
                amounts.clear()
                feeses.clear()
                verifies.clear()

                for (dataSnapshot in snapshot.children) {
                    val sender = dataSnapshot.child("Sender").value.toString()
                    val receiver = dataSnapshot.child("Receiver").value.toString()
                    val amount = dataSnapshot.child("Amount").value.toString()
                    val fees = dataSnapshot.child("Fees").value.toString()
                    val verify = dataSnapshot.child("Verify").value.toString()
                    val id = dataSnapshot.child("Transaction_ID").value.toString()

                    senders.add(sender)
                    receivers.add(receiver)
                    amounts.add(amount)
                    feeses.add(fees)
                    verifies.add(verify)
                    ids.add(id)
                }
                adapterClass.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}