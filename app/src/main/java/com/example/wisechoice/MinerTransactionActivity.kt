package com.example.wisechoice

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MinerTransactionActivity : AppCompatActivity() {
    var st_phone: String? = null
    var st_username: String? = null

    var miner_bottom: BottomNavigationView? = null

    var addToBlockFragment: AddToBlockFragment = AddToBlockFragment()
    var addTransactionFragment: AddTransactionFragment = AddTransactionFragment()
    var mineFragment: MineFragment = MineFragment()

    var sharedPreferences: SharedPreferences? = null
    var databaseReference = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_miner_transaction)
        val intent1 = intent
        st_phone = intent1.getStringExtra("Phone")

        miner_bottom = findViewById<BottomNavigationView>(R.id.miner_bottom)

        supportFragmentManager.beginTransaction().replace(R.id.m_frame, addToBlockFragment)
            .commit()

        miner_bottom?.setOnItemSelectedListener(NavigationBarView.OnItemSelectedListener { item ->
            if (item.itemId == R.id.m_add_transaction) {
                supportFragmentManager.beginTransaction().replace(R.id.m_frame, addTransactionFragment)
                    .commit()
            }
            if (item.itemId == R.id.m_add_to_block) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.m_frame, addToBlockFragment).commit()
            }
            if (item.itemId == R.id.m_mine) {
                supportFragmentManager.beginTransaction().replace(R.id.m_frame, mineFragment)
                    .commit()
            }
            true
        })
    }

    companion object {
        const val SHARED_PREF_NAME = "myPref"
    }
}