package com.example.wisechoice

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
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
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AddTransactionFragment : Fragment(), NavigationView.OnNavigationItemSelectedListener{
    private lateinit var receiver: EditText
    private lateinit var amount: EditText
    private lateinit var fees: EditText
    private lateinit var transact: Button

    private lateinit var st_phone: String

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var databaseReference: DatabaseReference

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
        return inflater.inflate(R.layout.fragment_add_transaction, container, false)
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
                } else if (st_amount.isEmpty()) {
                    amount.error = "Please Enter The Amount."
                } else if (st_fees.isEmpty()) {
                    fees.error = "Please Enter The Phone Number."
                } else {
                    val minersRef = databaseReference.child("miners")

                    val transactionKey = databaseReference.child("transactions").push().key

                    minersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (childSnapshot in snapshot.children) {
                                val phone = childSnapshot.key

                                if (phone != null) {
                                    val phoneRef = minersRef.child(phone)

                                    if (transactionKey != null) {
                                        val newTransactionRef =
                                            phoneRef.child("transactions").child(transactionKey)
                                        val refString = newTransactionRef.key

                                        newTransactionRef.child("Amount").setValue(st_amount)
                                        newTransactionRef.child("Block_No").setValue(unrecognized)
                                        newTransactionRef.child("Fees").setValue(st_fees)
                                        newTransactionRef.child("Receiver").setValue(st_receiver)
                                        newTransactionRef.child("Sender").setValue(st_phone)
                                        newTransactionRef.child("Transaction_ID")
                                            .setValue(refString.toString())
                                        newTransactionRef.child("Transaction_Time")
                                            .setValue(formattedDateTime.toString())
                                        newTransactionRef.child("Verify").setValue(unrecognized)
                                    }
                                }
                            }

                            Toast.makeText(
                                requireContext(),
                                "The Transaction Has Occurred.",
                                Toast.LENGTH_SHORT
                            ).show()

                            receiver.text.clear()
                            amount.text.clear()
                            fees.text.clear()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Handle database error if needed
                        }
                    })
                }
            }
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