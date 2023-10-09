package com.example.wisechoice

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AddToBlockFragment : Fragment() {
    var recyclerView: RecyclerView? = null
    var arrayList: ArrayList<TempBlock>? = null
    var adapterClass: TempBlockAdapter? = null

    private lateinit var st_phone: String
    private lateinit var sharedPreferences: SharedPreferences

    var databaseReference = FirebaseDatabase.getInstance().reference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_to_block, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recycler)
        recyclerView?.setHasFixedSize(true)
        recyclerView?.layoutManager = LinearLayoutManager(context)
        arrayList = ArrayList()
        adapterClass = context?.let { TempBlockAdapter(it, arrayList!!) }
        recyclerView?.adapter = adapterClass

        sharedPreferences = requireContext().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE)
        st_phone =
            sharedPreferences.getString("Phone", null).toString()

        //databaseReference.child("transactions").child(st_phone)
        //    .addValueEventListener(object : ValueEventListener {
        //        override fun onDataChange(snapshot: DataSnapshot) {
        //            arrayList!!.clear()
        //            for (dataSnapshot in snapshot.children) {
        //                val tempBlock: TempBlock? = dataSnapshot.getValue(TempBlock::class.java)
        //                if (tempBlock != null) {
        //                    arrayList!!.add(tempBlock)
        //                }
        //            }
        //            adapterClass!!.notifyDataSetChanged()
        //        }
//
        //        override fun onCancelled(error: DatabaseError) {}
        //    })
    }
}