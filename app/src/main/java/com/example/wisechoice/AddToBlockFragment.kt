package com.example.wisechoice

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
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
    private val transaction_times: List<String>,

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
        val idValue = ids[position]

        if(verifies[position] == "Unrecognized") {
            holder.verify_button.isEnabled = true
            holder.verify_button.text = "Verify"
            holder.transaction_card.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
        }

        else if(verifies[position] == "Verified") {
            holder.verify_button.isEnabled = true
            holder.verify_button.text = "Add To Block"
            holder.transaction_card.setBackgroundColor(ContextCompat.getColor(context, R.color.green))
        }

        else if(verifies[position] == "Not Verified") {
            holder.verify_button.isEnabled = false
            holder.verify_button.text = "Denied"
            holder.transaction_card.setBackgroundColor(ContextCompat.getColor(context, R.color.dark_green ))
        }

        else if(verifies[position] == "Temporary Blocked") {
            holder.verify_button.isEnabled = false
            holder.verify_button.text = "Wait"
            holder.transaction_card.setBackgroundColor(ContextCompat.getColor(context, R.color.yellow))
        }

        else if(verifies[position] == "Processing...") {
            holder.verify_button.isEnabled = false
            holder.verify_button.text = "Wait"
            holder.transaction_card.setBackgroundColor(ContextCompat.getColor(context, R.color.orange))
        }

        holder.verify_button.setOnClickListener {
            val sharedPreferences =
                context.getSharedPreferences("MySharedPref", Context.MODE_PRIVATE)
            val st_phone = sharedPreferences.getString("Phone", "") ?: ""

            if(holder.verify.text == "Unrecognized") {
                val newTransactionRef = databaseReference.child("miners").child(st_phone)
                    .child("transactions").child(idValue)
                newTransactionRef.child("Verify").setValue("Verified")

                holder.verify.text = "Verified"
                holder.verify_button.text = "Add To Block"
                holder.verify_button.isEnabled = true
                holder.transaction_card.setBackgroundColor(ContextCompat.getColor(context, R.color.green))
            }

            else if(holder.verify.text == "Verified") {
                val newTransactionRef = databaseReference.child("miners").child(st_phone)
                    .child("transactions").child(idValue)
                newTransactionRef.child("Verify").setValue("Temporary Blocked")

                holder.verify.text = ""
                holder.verify_button.text = "Temporary Blocked"
                holder.verify_button.isEnabled = false
                holder.transaction_card.setBackgroundColor(ContextCompat.getColor(context, R.color.yellow))

                val tempTransactionRef = databaseReference.child("miners").child(st_phone)
                    .child("temporary_blocks").child(idValue)

                tempTransactionRef.child("Transaction_ID").setValue("${ids[position]}")
                tempTransactionRef.child("Sender").setValue("${senders[position]}")
                tempTransactionRef.child("Receiver").setValue("${receivers[position]}")
                tempTransactionRef.child("Amount").setValue("${amounts[position]}")
                tempTransactionRef.child("Fees").setValue("${feeses[position]}")
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

class AddToBlockFragment : Fragment(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapterClass: TempBlockAdapter

    private lateinit var databaseReference: DatabaseReference

    private val senders = mutableListOf<String>()
    private val receivers = mutableListOf<String>()
    private val amounts = mutableListOf<String>()
    private val feeses = mutableListOf<String>()
    private val verifies = mutableListOf<String>()
    private val ids = mutableListOf<String>()
    private val transaction_times = mutableListOf<String>()

    var drawerLayout: DrawerLayout? = null
    var navigationView: NavigationView? = null
    var nView: View? = null

    var username: TextView? = null
    var phone: TextView? = null
    var photo: ImageView? = null
    var home_menu: ImageView? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_to_block, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        drawerLayout = view.findViewById<DrawerLayout>(R.id.drawer)
        // Use the activity context to initialize ActionBarDrawerToggle
        val actionBarDrawerToggle = ActionBarDrawerToggle(
            requireActivity(), drawerLayout,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        actionBarDrawerToggle.syncState()

        navigationView = view.findViewById<NavigationView>(R.id.navigation)
        nView = navigationView?.getHeaderView(0)
        username = nView?.findViewById<TextView>(R.id.username)
        phone = nView?.findViewById<TextView>(R.id.phone)
        photo = nView?.findViewById<ImageView>(R.id.photo)
        home_menu = view.findViewById<ImageView>(R.id.home_menu)

        home_menu?.setOnClickListener {
            drawerLayout?.openDrawer(GravityCompat.START)
        }

        navigationView?.setNavigationItemSelectedListener(this)

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
            ids,
            transaction_times
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
                    val verify = dataSnapshot.child("Verify").value.toString()
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

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.block_queue -> {
                val intent2 = Intent(requireContext(), BlockQueueActivity::class.java)
                startActivity(intent2)
            }
            R.id.blockchain -> {
                val intent2 = Intent(requireContext(), BlockchainActivity::class.java)
                startActivity(intent2)
            }
            R.id.logout -> {
                val intent = Intent(requireContext(), MainActivity::class.java)
                startActivity(intent)
                requireActivity().finish() // Finish the current activity
            }
        }
        return true
    }
}