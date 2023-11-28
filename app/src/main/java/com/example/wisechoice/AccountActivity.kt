package com.example.wisechoice

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AccountActivity : AppCompatActivity() {
    var name: EditText? = null
    var phone: TextView? = null
    var balance: TextView? = null
    var p_old: EditText? = null
    var p_new:EditText? = null
    var p_confirm:EditText? = null
    var databaseReference = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        name = findViewById<EditText>(R.id.name)
        balance = findViewById<EditText>(R.id.balance)
        phone = findViewById<TextView>(R.id.phone)
        p_old = findViewById(R.id.p_old)
        p_new = findViewById(R.id.p_new)
        p_confirm = findViewById(R.id.p_confirm)

        val sharedPreferences = this.getSharedPreferences("MySharedPref", Context.MODE_PRIVATE)
        val st_phone = sharedPreferences.getString("Phone", "") ?: ""

        databaseReference.child("miners").child(st_phone)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val str_uname = snapshot.child("User_Name").value as String?
                    name?.setText(str_uname)
                    val str_phone = snapshot.child("Phone").value as String?
                    phone?.setText("Phone: " + str_phone)
                    val str_balance = snapshot.child("Balance").value as String?
                    balance?.setText("Balance: " + str_balance.toString())
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun passwordPage(view: View) {
        val auth = FirebaseAuth.getInstance()

        val user = auth.currentUser

        val oldPassword = p_old?.text.toString()
        val newPassword = p_new?.text.toString()
        val confirmPassword = p_confirm?.text.toString()

        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(
                applicationContext,
                "Fill All The Fields.",
                Toast.LENGTH_SHORT
            ).show()
        }

        else {

            if (newPassword == confirmPassword) {

                val credential = EmailAuthProvider.getCredential(user?.email!!, oldPassword)
                user.reauthenticate(credential)
                    .addOnCompleteListener { reauthTask ->
                        if (reauthTask.isSuccessful) {

                            user.updatePassword(newPassword)
                                .addOnCompleteListener { updateTask ->
                                    if (updateTask.isSuccessful) {
                                        Toast.makeText(
                                            applicationContext,
                                            "Password updated successfully",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            applicationContext,
                                            "Failed to update password",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                        } else {
                            Toast.makeText(
                                applicationContext,
                                "Reauthentication failed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            } else {
                Toast.makeText(
                    applicationContext,
                    "New password and confirm password do not match",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    fun samePage(view: View) {
        val sharedPreferences = this.getSharedPreferences("MySharedPref", Context.MODE_PRIVATE)
        val st_phone = sharedPreferences.getString("Phone", "") ?: ""

        val st_uname: String = name?.getText().toString()

        databaseReference.child("miners").child(st_phone).child("User_Name").setValue(st_uname)

        Toast.makeText(
            this@AccountActivity,
            "Information Updated Successfully",
            Toast.LENGTH_SHORT
        ).show()
    }
}