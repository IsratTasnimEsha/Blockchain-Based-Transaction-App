package com.example.wisechoice

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase

class TempBlockAdapter(var context: Context, arrayList: ArrayList<TempBlock>) :
    RecyclerView.Adapter<com.example.wisechoice.TempBlockAdapter.MyViewHolder>() {
    var arrayList: ArrayList<TempBlock>
    var databaseReference = FirebaseDatabase.getInstance().reference

    init {
        this.arrayList = arrayList
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): com.example.wisechoice.TempBlockAdapter.MyViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.temp_block_xml, parent, false)
        return com.example.wisechoice.TempBlockAdapter.MyViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: com.example.wisechoice.TempBlockAdapter.MyViewHolder,
        position: Int
    ) {
        val tempBlock: TempBlock = arrayList[position]

        //holder.sender.setText(tempBlock.getSender())
        //holder.receiver.setText(tempBlock.getReceiver())
        //holder.amount.setText(tempBlock.getAmount())
        //holder.fees.setText(tempBlock.getFees())
        //holder.verify.setText(tempBlock.getVerify())
        //holder.time.setText(tempBlock.getTransaction_Time())
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var sender: EditText
        var receiver: EditText
        var amount: EditText
        var fees: EditText
        var verify: TextView
        var time: TextView
        
        init {
            sender = itemView.findViewById(R.id.sender)
            receiver = itemView.findViewById(R.id.receiver)
            amount = itemView.findViewById(R.id.amount)
            fees = itemView.findViewById(R.id.fees)
            verify = itemView.findViewById(R.id.verify)
            time = itemView.findViewById(R.id.time)
        }
    }
}