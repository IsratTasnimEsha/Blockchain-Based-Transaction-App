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

                val transactionInfo =
                    "Sender: $sender, Receiver: $receiver, Amount: $amount, Fees: $fees\n"
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
                        hashText.text = "Hash: $hashedString"
                    }

                    // Add a delay of 1 second (adjust as needed)
                    Thread.sleep(10)

                } while (!hashedString.startsWith("00"))

                val blockVal = FirebaseDatabase.getInstance().getReference("miners")
                    .child(st_phone).child("block_queue").push()

                blockVal.child("Block_ID").setValue(blockVal.key)
                blockVal.child("Block_Hash").setValue("$hashedString")
                blockVal.child("Nonce").setValue(randomNotch)
                blockVal.child("Miner").setValue(st_phone)

                val currentDateTime = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                val formattedDateTime = currentDateTime.format(formatter)

                blockVal.child("Mined_Time").setValue(formattedDateTime.toString())
                blockVal.child("Status").setValue("In Queue")

                val st_phone = sharedPreferences.getString("Phone", "") ?: ""

                val minersRef = FirebaseDatabase.getInstance().getReference("miners")
                val blockQueueRef = minersRef.child(st_phone).child("block_queue")

                blockQueueRef.orderByKey().limitToLast(2).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val blockList = dataSnapshot.children.toList()

                        if (blockList.size == 1) {
                            // Only one block in the block_queue
                            val previousHash = "000000000000000000000000000000000000000000000000000000000000000000"
                            blockList.firstOrNull()?.let { blockData ->
                                val blockHash = blockData.child("Block_Hash").value.toString()

                                blockVal.child("Previous_Hash").setValue(previousHash)
                            }
                        } else if (blockList.size >= 2) {
                            // Two or more blocks in the block_queue
                            val lastBlock = blockList.last()
                            val previousBlock = blockList[blockList.size - 2]

                            val lastBlockHash = lastBlock.child("Block_Hash").value.toString()
                            val previousHash = previousBlock.child("Block_Hash").value.toString()

                            blockVal.child("Previous_Hash").setValue(previousHash)
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle the error
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
                                    .child("Verify")

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
                                blockRef.child(transactionId).child("Amount")
                                    .setValue(amounts[ids.indexOf(idValue)])
                                blockRef.child(transactionId).child("Fees")
                                    .setValue(feeses[ids.indexOf(idValue)])

                                blockVal.child("No_Of_Transactions").setValue(noOfTransactions)
                                blockVal.child("Total_Amount").setValue(totalAmount)
                                blockVal.child("Total_Fees").setValue(totalFees)
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

                val sourceMinerBlocksRef = FirebaseDatabase.getInstance().getReference("miners")
                    .child(st_phone)
                    .child("block_queue")

                sourceMinerBlocksRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(sourceSnapshot: DataSnapshot) {
                        if (sourceSnapshot.exists()) {
                            val blockData = sourceSnapshot.value

                            val minersRef = FirebaseDatabase.getInstance().getReference("miners")
                            minersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(minersSnapshot: DataSnapshot) {
                                    for (minerSnapshot in minersSnapshot.children) {
                                        val minerId = minerSnapshot.key
                                        if (minerId != st_phone) {
                                            val minerBlocksRef = minersRef.child(minerId.toString()).child("block_queue")
                                            val newBlockRef = minerBlocksRef
                                            newBlockRef.setValue(blockData)
                                        }
                                    }
                                }

                                override fun onCancelled(minersError: DatabaseError) {
                                    // Handle the error
                                }
                            })
                        }
                    }

                    override fun onCancelled(sourceError: DatabaseError) {
                        // Handle the error
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