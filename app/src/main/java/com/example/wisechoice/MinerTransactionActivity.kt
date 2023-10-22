package com.example.wisechoice

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView

class MinerTransactionActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    var st_phone: String? = null
    var miner_bottom: BottomNavigationView? = null

    var addToBlockFragment: AddToBlockFragment = AddToBlockFragment()
    var addTransactionFragment: AddTransactionFragment = AddTransactionFragment()
    var mineFragment: MineFragment = MineFragment()

    var username: TextView? = null
    var phone: TextView? = null
    var photo: ImageView? = null
    var home_menu: ImageView? = null

    var drawerLayout: DrawerLayout? = null
    var navigationView: NavigationView? = null
    var nView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_miner_transaction)
        val intent1 = intent
        st_phone = intent1.getStringExtra("Phone")

        miner_bottom = findViewById<BottomNavigationView>(R.id.miner_bottom)

        supportFragmentManager.beginTransaction().replace(R.id.m_frame, addTransactionFragment)
            .commit()

        miner_bottom?.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.m_add_transaction -> {
                    supportFragmentManager.beginTransaction().replace(R.id.m_frame, addTransactionFragment)
                        .commit()
                }
                R.id.m_add_to_block -> {
                    supportFragmentManager.beginTransaction().replace(R.id.m_frame, addToBlockFragment)
                        .commit()
                }
                R.id.m_mine -> {
                    supportFragmentManager.beginTransaction().replace(R.id.m_frame, mineFragment)
                        .commit()
                }
            }
            true
        }

        drawerLayout = findViewById<DrawerLayout>(R.id.drawer)
        val actionBarDrawerToggle = ActionBarDrawerToggle(
            this, drawerLayout,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        actionBarDrawerToggle.syncState()

        navigationView = findViewById<NavigationView>(R.id.navigation)
        nView = navigationView?.getHeaderView(0)
        username = nView?.findViewById<TextView>(R.id.username)
        phone = nView?.findViewById<TextView>(R.id.phone)
        photo = nView?.findViewById<ImageView>(R.id.photo)
        home_menu = findViewById<ImageView>(R.id.home_menu)

        home_menu?.setOnClickListener {
            drawerLayout?.openDrawer(GravityCompat.START)
        }

        navigationView?.setNavigationItemSelectedListener(this)
    }

    companion object {
        const val SHARED_PREF_NAME = "myPref"
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.block_queue -> {
                val intent2 = Intent(this, BlockQueueActivity::class.java)
                startActivity(intent2)
            }
            R.id.blockchain -> {
                val intent2 = Intent(this, BlockchainActivity::class.java)
                startActivity(intent2)
            }
            R.id.logout -> {
                Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        }
        return true
    }
}