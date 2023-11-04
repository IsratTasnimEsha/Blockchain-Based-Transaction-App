package com.example.wisechoice
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
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
import java.util.concurrent.TimeUnit

class SignUpActivity : AppCompatActivity() {

    private lateinit var r_name: EditText
    private lateinit var r_phone: EditText
    private lateinit var r_pass: EditText
    private lateinit var r_signup: Button
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        r_name = findViewById(R.id.r_name)
        r_phone = findViewById(R.id.r_phone)
        r_pass = findViewById(R.id.r_pass)
        r_signup = findViewById(R.id.bur_signup)
        databaseReference = FirebaseDatabase.getInstance().reference

        r_signup.setOnClickListener(View.OnClickListener {
            val st_name = r_name.text.toString()
            val st_phone = r_phone.text.toString()
            val st_pass = r_pass.text.toString()

            if (st_name.isEmpty()) {
                r_name.setError("User Name Is Required.")
                Toast.makeText(this@SignUpActivity, "User Name Is Required.", Toast.LENGTH_SHORT).show()
            } else if (st_phone.isEmpty()) {
                r_phone.setError("Phone Number Is Required.")
                Toast.makeText(this@SignUpActivity, "Phone Number Is Required.", Toast.LENGTH_SHORT).show()
            } else if (st_pass.isEmpty()) {
                r_pass.setError("Password Is Required.")
                Toast.makeText(this@SignUpActivity, "Password Is Required.", Toast.LENGTH_SHORT).show()
            } else {
                val keyPair = generateKeyPair() // Generate public and private key pair

                val publicKey = keyPair.public
                val privateKey = keyPair.private

                val signature = generateSignature(st_name, st_phone, privateKey) // Generate signature

                val initialBalance = 50.0

                databaseReference.child("miners").addListenerForSingleValueEvent(object : ValueEventListener {
                    @RequiresApi(Build.VERSION_CODES.O)
                    override fun onDataChange(@NonNull snapshot: DataSnapshot) {
                        if (snapshot.hasChild(st_phone)) {
                            Toast.makeText(this@SignUpActivity,
                                "Phone Number Is Already Registered.", Toast.LENGTH_SHORT).show()
                        } else {
                            databaseReference.child("miners").child(st_phone).child("User_Name").setValue(st_name)
                            databaseReference.child("miners").child(st_phone).child("Phone").setValue(st_phone)
                            databaseReference.child("miners").child(st_phone).child("Password").setValue(st_pass)
                            databaseReference.child("miners").child(st_phone).child("Public_Key").setValue(Base64.getEncoder().encodeToString(publicKey.encoded))
                            databaseReference.child("miners").child(st_phone).child("Signature").setValue(Base64.getEncoder().encodeToString(signature))
                            databaseReference.child("miners").child(st_phone).child("Balance").setValue(initialBalance)

                            // Storing user data under "PublicKeys" node under "miners"
                            val userReference = databaseReference.child("miners").child("PublicKeys").child(st_phone)
                            userReference.child("User_Name").setValue(st_name)
                            userReference.child("Phone").setValue(st_phone)
                            userReference.child("Public_Key").setValue(Base64.getEncoder().encodeToString(publicKey.encoded))

                            val intent = Intent(this@SignUpActivity, SignInActivity::class.java)
                            startActivity(intent)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle errors
                    }
                })
            }
        })
    }

    // Generate Key Pair
    private fun generateKeyPair(): KeyPair {
        val keyGen = KeyPairGenerator.getInstance("RSA")
        keyGen.initialize(2048)
        return keyGen.generateKeyPair()
    }

    // Generate Signature
    private fun generateSignature(name: String, phone: String, privateKey: PrivateKey): ByteArray {
        val data = "$name$phone".toByteArray()
        val signature = Signature.getInstance("SHA256withRSA")
        signature.initSign(privateKey)
        signature.update(data)
        return signature.sign()
    }
}