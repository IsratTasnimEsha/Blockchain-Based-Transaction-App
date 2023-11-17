package com.example.wisechoice

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
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
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Base64

class AddTransactionFragment : Fragment(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var receiverField: EditText
    private lateinit var amountField: EditText
    private lateinit var feesField: EditText


    private lateinit var signatureButton: Button
    private lateinit var st_phone: String
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var databaseReference: DatabaseReference
    private lateinit var senderPrivateKey: String
    var drawerLayout: DrawerLayout? = null
    var navigationView: NavigationView? = null
    var nView: View? = null

    var username: TextView? = null
    var phone: TextView? = null
    var photo: ImageView? = null
    var home_menu: ImageView? = null
    private lateinit var formattedDateTime: String
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_transaction, container, false)

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

        phone?.text = st_phone
        FirebaseDatabase.getInstance().getReference("miners").child(st_phone)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userName = snapshot.child("User_Name").getValue().toString()

                    username?.text = userName
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

        receiverField = view.findViewById(R.id.receiver)
        amountField = view.findViewById(R.id.amount)
        feesField = view.findViewById(R.id.fees)


        signatureButton = view.findViewById(R.id.signatureButton)

        databaseReference = FirebaseDatabase.getInstance().getReference()

        signatureButton.setOnClickListener {
            fetchSignatureFromFirebase()
        }


        return view
    }

    private fun fetchSignatureFromFirebase() {
        val sharedPreferences = requireContext().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE)
        val st_phone = sharedPreferences.getString("Phone", "") ?: ""

        databaseReference.child("miners").child(st_phone)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.child("Private_Key").exists()) {
                        senderPrivateKey = snapshot.child("Private_Key").value.toString()
                        val st_receiver = receiverField.text.toString()
                        val st_amount = amountField.text.toString()
                        val st_fees = feesField.text.toString()
                        formattedDateTime = getFormattedDateTime()
                        val st_transactionData = "$st_receiver$st_amount$st_fees$formattedDateTime"

                        val signature = Base64.getEncoder().encodeToString(createSignature(senderPrivateKey, st_transactionData))
                        Toast.makeText(requireContext(), "Signature created successfully.", Toast.LENGTH_SHORT).show()
                        performTransaction(signature)



                    } else {
                        Toast.makeText(requireContext(), "No private key found for the user.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle database error if needed
                }
            })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun performTransaction(signature :String) {
        val sharedPreferences = requireContext().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE)
        val st_phone = sharedPreferences.getString("Phone", "") ?: ""

        val st_receiver = receiverField.text.toString()
        val st_amount = amountField.text.toString().toDoubleOrNull() ?: 0.0
        val st_fees = feesField.text.toString().toDoubleOrNull() ?: 0.0
        val st_signature = signature
        val unrecognized: String = "Unrecognized"


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
                        senderBalance = childSnapshot.child("Users_Balance").child(st_phone)
                            .child("Total").value.toString().toDoubleOrNull() ?: 0.0
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
                val newBalance = st_amount + st_fees

                // Proceed with updating the sender's balance and the transaction details...
                updateSenderBalance(newBalance, st_amount, st_fees, st_receiver, st_signature,formattedDateTime)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error if needed
            }
        })
    }

    private fun updateSenderBalance(newBalance: Double, amount: Double, fees: Double, receiver: String, signature: String, formattedDateTime: String) {
        val sharedPreferences = requireContext().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE)
        val st_phone = sharedPreferences.getString("Phone", "") ?: ""

        val senderRef = databaseReference.child("miners")

        val transactionKey = databaseReference.child("transactions").push().key

        senderRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (childSnapshot in snapshot.children) {
                    val phone = childSnapshot.key

                    if (phone != null) {
                        senderRef.child(phone).child("Users_Balance").child(st_phone)
                            .child("Sent")

                        senderRef.child(phone).child("Users_Balance").child(st_phone)
                            .child("Sent").addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val prevBalance = snapshot.getValue()
                                    val totalBalance = prevBalance.toString().toFloat() + newBalance.toFloat()
                                    senderRef.child(phone).child("Users_Balance").child(st_phone)
                                        .child("Sent").setValue(totalBalance)
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    TODO("Not yet implemented")
                                }

                            })

                        val newTransactionRef = senderRef.child(phone).child("transactions").child(transactionKey!!)
                        val refString = newTransactionRef.key

                        newTransactionRef.apply {
                            child("Amount").setValue(amount)
                            child("Fees").setValue(fees)
                            child("Receiver").setValue(receiver)
                            child("Sender").setValue(st_phone)
                            child("Signature").setValue(signature)
                            child("Transaction_ID").setValue(refString.toString())
                            newTransactionRef.child("Transaction_Time").setValue(formattedDateTime)
                            child("Status").setValue("Unrecognized")

                            Toast.makeText(requireContext(), "The Transaction Has Occurred.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        receiverField.text.clear()
        amountField.text.clear()
        feesField.text.clear()

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createSignature(senderPrivateKey: String, dataToSign: String): ByteArray {
        val privateBytes = Base64.getDecoder().decode(senderPrivateKey)
        Log.d("SignatureVerification", "data: $dataToSign")

        val privateKey = KeyFactory.getInstance("RSA").generatePrivate(PKCS8EncodedKeySpec(privateBytes))

        val signature = Signature.getInstance("SHA256withRSA")
        signature.initSign(privateKey)
        signature.update(dataToSign.toByteArray())

        return signature.sign()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getFormattedDateTime(): String {
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return currentDateTime.format(formatter)
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
            R.id.transaction -> {
                val intent = Intent(requireContext(), TransactionDetailsActivity::class.java)
                startActivity(intent)
            }
            R.id.rejected -> {
                val intent = Intent(requireContext(), RejectedBlocksActivity::class.java)
                startActivity(intent)
            }
            R.id.notifications -> {
                val intent = Intent(requireContext(), NotificationActivity::class.java)
                startActivity(intent)
            }
            R.id.account -> {
                val intent = Intent(requireContext(), AccountActivity::class.java)
                startActivity(intent)
            }
            R.id.logout -> {
                val sharedPrefs = context?.getSharedPreferences(SignInActivity.PREFS_NAME, Context.MODE_PRIVATE)
                val editor = sharedPrefs?.edit()
                editor?.putBoolean("hasSignedIn", false)
                editor?.apply()

                val intent = Intent(requireContext(), SignInActivity::class.java)
                startActivity(intent)
                requireActivity().finish() // Finish the current activity
            }
        }
        return true
    }

    companion object {
        const val PREFS_NAME = "MySharedPref"
    }
}