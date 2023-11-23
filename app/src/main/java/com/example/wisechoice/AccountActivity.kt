package com.example.wisechoice

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
                    phone?.setText(str_phone)
                    val str_balance = snapshot.child("Users_Balance").child(st_phone).child("Initial").value
                    balance?.setText(str_balance.toString())
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun passwordPage(view: View) {
        val sharedPreferences = this.getSharedPreferences("MySharedPref", Context.MODE_PRIVATE)
        val st_phone = sharedPreferences.getString("Phone", "") ?: ""

        databaseReference.child("miners").child(st_phone)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val str_old = snapshot.child("Password").value as String?
                    val st_old = p_old!!.text.toString()
                    val st_new = p_new!!.text.toString()
                    val st_confirm = p_confirm!!.text.toString()
                    if (str_old != st_old) {
                        p_old!!.error = "Password Is Incorrect."
                        Toast.makeText(
                            this@AccountActivity, "Password Didn't Matched.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        // if (st_new.length < 6) {
                        //    p_new!!.error = "Password Must Be At Least 6 Characters."
                        //    Toast.makeText(
                        //        this@AccountActivity,
                        //        "Password Must Be At Least 6 Characters.",
                        //        Toast.LENGTH_SHORT
                        //    ).show()
                        //}
                        if (st_new != st_confirm) {
                            p_confirm!!.error = "Password Didn't Matched."
                            Toast.makeText(
                                this@AccountActivity, "Password Didn't Matched.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            databaseReference.child("miners").child(st_phone).child("Password")
                                .setValue(st_new)
                            Toast.makeText(
                                this@AccountActivity, "Password Has Been Changed.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
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