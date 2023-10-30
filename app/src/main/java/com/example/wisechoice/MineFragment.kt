package com.example.wisechoice

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.RequiresApi
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wisechoice.R
import com.example.wisechoice.TransactionDetailsActivity
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
            newTransactionRef.child("Status").setValue("Verified")

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

class MineFragment : Fragment(), NavigationView.OnNavigationItemSelectedListener {
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
        return inflater.inflate(R.layout.fragment_mine, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        drawerLayout = view.findViewById<DrawerLayout>(R.id.drawer)

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

        hashText = view.findViewById(R.id.hash_text)

        mineButton.setOnClickListener {
            val st_phone = sharedPreferences.getString("Phone", "") ?: ""

            val concatenatedTransactions = StringBuilder()

            for (idValue in ids) {
                val sender = senders[ids.indexOf(idValue)]
                val receiver = receivers[ids.indexOf(idValue)]
                val amount = amounts[ids.indexOf(idValue)]
                val fees = feeses[ids.indexOf(idValue)]

                val transactionInfo =
                    "Sender: $sender, Receiver: $receiver, Amount: $amount, Fees: $fees\n"
                concatenatedTransactions.append(transactionInfo)
            }

            val concatenatedString = concatenatedTransactions.toString()

            var hashedString: String
            var randomNotch: Int
            Thread {
                do {
                    randomNotch = (10000..99999).random()
                    val stringWithNotch = "$randomNotch$concatenatedString"

                    hashedString = hashString(stringWithNotch)

                    requireActivity().runOnUiThread() {
                        hashText.text = "Hash: $hashedString"
                    }

                    Thread.sleep(10)

                } while (!hashedString.startsWith("00"))

                val blockVal = FirebaseDatabase.getInstance().getReference("miners")
                    .child(st_phone).child("block_queue").push()

                blockVal.child("Block_ID").setValue(blockVal.key)
                blockVal.child("Block_Hash").setValue("$hashedString")
                blockVal.child("Nonce").setValue(randomNotch)
                blockVal.child("Miner").setValue(st_phone)
                blockVal.child("Size").setValue(((hashedString.length - 5) * 4).toString())
                blockVal.child("Previous_Hash")
                    .setValue("000000000000000000000000000000000000000000000000000000000000000000")

                val currentDateTime = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                val formattedDateTime = currentDateTime.format(formatter)

                blockVal.child("Mined_Time").setValue(formattedDateTime.toString())

                val st_phone = sharedPreferences.getString("Phone", "") ?: ""

                val minersRef = FirebaseDatabase.getInstance().getReference("miners")

                minersRef.child(st_phone).child("blockchain")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            for (childSnapshot in dataSnapshot.children) {

                                val previousBlockID = childSnapshot.key
                                val previousHash = childSnapshot.child("Block_Hash").value
                                blockVal.child("Previous_Hash").setValue(previousHash)

                                FirebaseDatabase.getInstance().getReference("miners")
                                    .child(st_phone).child("blockchain").child(previousBlockID.toString())
                                    .child("hasChild").setValue("True")

                                val minersRef = FirebaseDatabase.getInstance().getReference("miners")

                                minersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        for (childSnapshot in snapshot.children) {
                                            val phone = childSnapshot.key

                                            if (phone != null) {
                                                FirebaseDatabase.getInstance()
                                                    .getReference("miners")
                                                    .child(phone).child("main_blockchain")
                                                    .child(previousBlockID.toString())
                                                    .setValue(dataSnapshot.child(previousBlockID.toString()).value)

                                                for (grandChildSnapshot in childSnapshot
                                                    .child("transaction_details").children) {

                                                    val childKey = grandChildSnapshot.key

                                                    Toast.makeText(requireContext(), childKey.toString(), Toast.LENGTH_SHORT).show()

                                                    if (childKey != null) {
                                                        FirebaseDatabase.getInstance().getReference("miners").child(phone)
                                                            .child("transactions").child(childKey)
                                                            .child("Status").setValue("Blocked")

                                                        FirebaseDatabase.getInstance().getReference("miners").child(phone)
                                                            .child("transactions").child(childKey)
                                                            .child("Block_No").setValue(previousBlockID.toString())
                                                    }
                                                }

                                                FirebaseDatabase.getInstance().getReference("miners")
                                                    .child(phone).child("blockchain").removeValue()

                                                FirebaseDatabase.getInstance().getReference("miners")
                                                    .child(phone).child("block_queue")
                                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                                                            for (childSnapshot in dataSnapshot.children) {
                                                                val ID = childSnapshot.key
                                                                val blockHash = childSnapshot.child("Block_Hash").value

                                                                if(blockHash == previousHash) {
                                                                    FirebaseDatabase.getInstance().getReference("miners")
                                                                        .child(phone).child("block_queue")
                                                                        .child(ID.toString()).removeValue()
                                                                }
                                                            }
                                                        }

                                                        override fun onCancelled(error: DatabaseError) {

                                                        }
                                                    })
                                            }
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {

                                    }
                                })
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }
                    })

                val blockRef = blockVal.child("transaction_details")
                var noOfTransactions = 0
                var totalAmount = 0.0
                var totalFees = 0.0

                for (idValue in ids) {
                    val transactionRef = FirebaseDatabase.getInstance().getReference("miners")
                        .child(st_phone).child("transactions")

                    val query = transactionRef.orderByChild("Transaction_ID").equalTo(idValue)
                    query.addListenerForSingleValueEvent(object : ValueEventListener {

                        override fun onDataChange(transactionSnapshot: DataSnapshot) {
                            for (transactionData in transactionSnapshot.children) {
                                val transactionId = transactionData.key.toString()
                                val transactionVerifyRef = transactionRef.child(transactionId)
                                    .child("Status")

                                transactionVerifyRef.setValue("Processing...")

                                noOfTransactions++

                                val index = ids.indexOf(idValue)
                                if (index != -1) {
                                    val amount = amounts[index].toDoubleOrNull() ?: 0.0
                                    totalAmount += amount

                                    val fees = feeses[index].toDoubleOrNull() ?: 0.0
                                    totalFees += fees
                                }

                                blockRef.child(transactionId).child("Transaction_ID")
                                    .setValue(ids[ids.indexOf(idValue)])
                                blockRef.child(transactionId).child("Sender")
                                    .setValue(senders[ids.indexOf(idValue)])
                                blockRef.child(transactionId).child("Receiver")
                                    .setValue(receivers[ids.indexOf(idValue)])
                                blockRef.child(transactionId).child("Amount")
                                    .setValue(amounts[ids.indexOf(idValue)])
                                blockRef.child(transactionId).child("Fees")
                                    .setValue(feeses[ids.indexOf(idValue)])
                                blockRef.child(transactionId).child("Status")
                                    .setValue("Unrecognized")

                                blockVal.child("No_Of_Transactions").setValue(noOfTransactions)
                                blockVal.child("Total_Amount").setValue(totalAmount)
                                blockVal.child("Total_Fees").setValue(totalFees)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }
                    })

                    val tempTransactionRef = FirebaseDatabase.getInstance().getReference("miners")
                        .child(st_phone).child("temporary_blocks").child(idValue)

                    tempTransactionRef.removeValue()
                }

                val minerRef = FirebaseDatabase.getInstance().getReference("miners")

                minersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (childSnapshot in dataSnapshot.children) {
                            val childKey = childSnapshot.key

                            val blockchainReference =
                                FirebaseDatabase.getInstance().getReference("miners")
                                    .child(childKey.toString()).child("block_queue").child(blockVal.key.toString())

                            FirebaseDatabase.getInstance().getReference("miners").child(st_phone)
                                .child("block_queue").child(blockVal.key.toString())
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if (snapshot.exists()) {
                                            blockchainReference.setValue(snapshot.value)
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {

                                    }
                                })
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle errors
                    }
                })


            }.start()
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
                    val verify = dataSnapshot.child("Status").value.toString()
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
                val intent = Intent(requireContext(), SignInActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            }
        }
        return true
    }
}