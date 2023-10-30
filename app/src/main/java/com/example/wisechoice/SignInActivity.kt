package com.example.wisechoice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class SignInActivity : AppCompatActivity() {
    private lateinit var l_phone: EditText
    private lateinit var l_pass: EditText
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        l_phone = findViewById(R.id.l_phone)
        l_pass = findViewById(R.id.l_pass)
        databaseReference = FirebaseDatabase.getInstance().reference
    }

    fun signup(view: View) {
        startActivity(Intent(this, SignUpActivity::class.java))
    }

    fun updatePage(view: View) {
        val st_phone = l_phone.text.toString()
        val st_pass = l_pass.text.toString()

        if (st_phone.isEmpty()) {
            l_phone.error = "Please Enter The Phone Number."
        } else if (st_pass.isEmpty()) {
            l_pass.error = "Please Enter The Password."
        } else {
            databaseReference.child("miners").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.hasChild(st_phone)) {
                        val str_pass = snapshot.child(st_phone).child("Password").value as? String

                        if (str_pass == st_pass) {
                            val sharedPreferences = getSharedPreferences("MySharedPref", Context.MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            editor.putString("Phone", st_phone)
                            editor.apply()

                            val intent = Intent(this@SignInActivity, MinerTransactionActivity::class.java)
                            startActivity(intent)
                        } else {
                            Toast.makeText(this@SignInActivity, "Password Is Wrong.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@SignInActivity, "Phone Number Is Not Registered Yet.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        }
    }
}