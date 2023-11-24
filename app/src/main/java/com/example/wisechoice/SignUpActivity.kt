package com.example.wisechoice

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.util.Base64

class SignUpActivity : AppCompatActivity() {

    private lateinit var r_name: EditText
    private lateinit var r_phone: EditText
    private lateinit var r_email: EditText
    private lateinit var r_pass: EditText
    private lateinit var r_signup: Button
    private lateinit var databaseReference: DatabaseReference
    private lateinit var r_privateKey: String

    private lateinit var auth: FirebaseAuth

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        r_name = findViewById(R.id.r_name)
        r_phone = findViewById(R.id.r_phone)
        r_email = findViewById(R.id.r_email)
        r_pass = findViewById(R.id.r_pass)
        r_signup = findViewById(R.id.bur_signup)
        databaseReference = FirebaseDatabase.getInstance().reference

        r_signup.setOnClickListener(View.OnClickListener {
            val st_name = r_name.text.toString()
            val st_phone = r_phone.text.toString()
            val st_email = r_email.text.toString()
            val st_pass = r_pass.text.toString()

            if (st_name.isEmpty()) {
                r_name.setError("User Name Is Required.")
                Toast.makeText(this@SignUpActivity, "User Name Is Required.",
                    Toast.LENGTH_SHORT).show()
            }
            else if (st_phone.isEmpty()) {
                r_email.setError("Phone Number Is Required.")
                Toast.makeText(this@SignUpActivity, "Phone Number Is Required.",
                    Toast.LENGTH_SHORT).show()
            }
            else if (st_email.isEmpty()) {
                    r_email.setError("Email Is Required.")
                    Toast.makeText(this@SignUpActivity, "Email Is Required.",
                        Toast.LENGTH_SHORT).show()
            } else if (st_pass.isEmpty()) {
                r_pass.setError("Password Is Required.")
                Toast.makeText(this@SignUpActivity, "Password Is Required.",
                    Toast.LENGTH_SHORT).show()
            }
            else if (st_pass.length < 6) {
                r_pass.setError("Password Should Be Of At Least 6 Characters.")
                Toast.makeText(this@SignUpActivity, "Password Should Be Of At Least 6 Characters.",
                    Toast.LENGTH_SHORT).show()
            }
            else {
                auth = Firebase.auth

                auth.createUserWithEmailAndPassword(st_email, st_pass)
                    .addOnCompleteListener(this) { task -> if (task.isSuccessful) {
                        val user = auth.currentUser
                        val userID = user?.uid.toString()

                        databaseReference.child("users").child(userID).setValue(st_phone)

                        val keyPair = generateKeyPair()

                        val publicKey = keyPair.public
                        val privateKey = keyPair.private

                        r_privateKey =
                            Base64.getEncoder().encodeToString(privateKey.encoded)

                        databaseReference.child("miners")
                            .addListenerForSingleValueEvent(object :
                                ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val phoneNumbers =
                                        snapshot.children.mapNotNull { it.key }

                                    if (phoneNumbers.isEmpty()) {
                                        saveNewUser(
                                            st_name, st_phone, st_pass,
                                            Base64.getEncoder()
                                                .encodeToString(publicKey.encoded))

                                    } else if (phoneNumbers.size >= 1) {
                                        val random1 = phoneNumbers.random()
                                        val random2 = phoneNumbers.random()
                                        val random3 = phoneNumbers.random()
                                        val random4 = phoneNumbers.random()

                                        val balance1 = snapshot.child(random1).child("Users_Balance")
                                        val balance2 = snapshot.child(random2).child("Users_Balance")
                                        val blockchain1 = snapshot.child(random3).child("main_blockchain")
                                        val blockchain2 = snapshot.child(random4).child("main_blockchain")

                                        if (balance1.exists() && balance2.exists() && balance1.value == balance2.value) {

                                            if (blockchain1.exists() && blockchain2.exists() &&
                                                blockchain1.value != blockchain2.value
                                                ) {
                                                Toast.makeText(this@SignUpActivity,
                                                    "Someone Has Corrupted Data. Please Try Again To Sign Up",
                                                    Toast.LENGTH_SHORT).show()
                                            } else {
                                                databaseReference.child("miners").child(st_phone)
                                                    .child("Users_Balance").setValue(balance1.value)

                                                val transactions = snapshot.child(random3).child("transactions")

                                                databaseReference.child("miners")
                                                    .child(st_phone).child("transactions")
                                                    .setValue(transactions.value)

                                                databaseReference.child("miners")
                                                    .child(st_phone).child("transactions")
                                                    .addListenerForSingleValueEvent(object :
                                                        ValueEventListener {
                                                        override fun onDataChange(snapshot: DataSnapshot) {
                                                            for (childSnapshot in snapshot.children) {
                                                                val id = childSnapshot.key
                                                                if (childSnapshot.child("Block_No")
                                                                    .exists()) {
                                                                    databaseReference.child("miners")
                                                                        .child(st_phone).child("transactions")
                                                                        .child(id.toString()).child("Status")
                                                                        .setValue("Blocked")
                                                                } else {
                                                                    databaseReference.child("miners")
                                                                        .child(st_phone).child("transactions")
                                                                        .child(id.toString()).child("Status")
                                                                        .setValue("Unrecognized")
                                                                }
                                                            }
                                                        }

                                                        override fun onCancelled(error: DatabaseError) {
                                                        }
                                                        })


                                                if (blockchain1.exists() && blockchain2.exists() &&
                                                    blockchain1.value == blockchain2.value) {
                                                    databaseReference.child("miners").child(st_phone)
                                                        .child("main_blockchain").setValue(blockchain1.value)

                                                    val lastChild = blockchain2.children.last()
                                                    databaseReference.child("miners").child(st_phone)
                                                        .child("blockchain").child(lastChild.key.toString())
                                                        .setValue(lastChild.value)
                                                }

                                                databaseReference.child("miners").child(st_phone)
                                                    .child("User_Name").setValue(st_name)
                                                databaseReference.child("miners").child(st_phone)
                                                    .child("Phone").setValue(st_phone)
                                                databaseReference.child("miners").child(st_phone)
                                                    .child("Balance").setValue("100")
                                                databaseReference.child("miners").child(st_phone)
                                                    .child("Password").setValue(st_pass)
                                                databaseReference.child("miners").child(st_phone)
                                                    .child("Public_Key")
                                                    .setValue(Base64.getEncoder().encodeToString(publicKey.encoded))
                                                databaseReference.child("miners").child(st_phone)
                                                    .child("Private_Key").setValue(r_privateKey)

                                                databaseReference.child("miners")
                                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                                        override fun onDataChange(snapshot: DataSnapshot) {
                                                            for (childSnapshot in snapshot.children) {
                                                                val phone = childSnapshot.key

                                                                databaseReference.child("miners")
                                                                    .child(phone.toString())
                                                                    .child("Users_Balance")
                                                                    .child(st_phone).child("Initial")
                                                                    .setValue(100.0)
                                                                databaseReference.child("miners")
                                                                    .child(phone.toString())
                                                                    .child("Users_Balance")
                                                                    .child(st_phone).child("Sent")
                                                                    .setValue(0.0) }
                                                        }

                                                        override fun onCancelled(error: DatabaseError) {

                                                        }
                                                    })

                                                val userReference = databaseReference.child("PublicKeys")
                                                    .child(st_phone)
                                                userReference.child("User_Name")
                                                    .setValue(st_name)
                                                userReference.child("Phone")
                                                    .setValue(st_phone)
                                                userReference.child("Public_Key")
                                                    .setValue(Base64.getEncoder().encodeToString(publicKey.encoded))

                                                val intent = Intent(this@SignUpActivity,
                                                    SignInActivity::class.java)
                                                startActivity(intent)
                                                finish()
                                            }
                                        } else {
                                            Toast.makeText(
                                                this@SignUpActivity,
                                                "Someone Has Corrupted Data. Please Try Again To Sign Up",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {

                                }
                                })

                    }
                    else {
                        Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveNewUser(st_name: String, st_phone: String, st_pass: String, publicKey: Any) {
        databaseReference.child("miners").child(st_phone).child("User_Name").setValue(st_name)
        databaseReference.child("miners").child(st_phone).child("Phone").setValue(st_phone)
        databaseReference.child("miners").child(st_phone).child("Password").setValue(st_pass)
        databaseReference.child("miners").child(st_phone).child("Public_Key")
            .setValue(publicKey)
        databaseReference.child("miners").child(st_phone).child("Private_Key").setValue(r_privateKey)

        databaseReference.child("miners").child(st_phone).child("Users_Balance")
            .child(st_phone).child("Initial").setValue(100.0)
        databaseReference.child("miners").child(st_phone).child("Users_Balance")
            .child(st_phone).child("Sent").setValue(0.0)

        val userReference = databaseReference.child("PublicKeys").child(st_phone)
        userReference.child("User_Name").setValue(st_name)
        userReference.child("Phone").setValue(st_phone)
        userReference.child("Public_Key").setValue(publicKey)

        val intent = Intent(this@SignUpActivity, SignInActivity::class.java)
        startActivity(intent)
    }

    private fun generateKeyPair(): KeyPair {
        val keyGen = KeyPairGenerator.getInstance("RSA")
        keyGen.initialize(2048)
        return keyGen.generateKeyPair()
    }

}