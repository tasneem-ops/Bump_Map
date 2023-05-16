package com.example.mapsdemo.broadcastReceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.mapsdemo.data.model.Bump
import com.example.mapsdemo.data.model.BumpData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UpVoteReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val latitude = intent?.getDoubleExtra("latitude", 0.0)
        val longitude = intent?.getDoubleExtra("longitude", 0.0)
        val bumpList = arrayListOf<BumpData>()
        val bumpsDatabaseReference = FirebaseDatabase.getInstance().getReference("Bumps")
        bumpsDatabaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()){
                    for (snap in snapshot.children){
                        val bump = snap.getValue(BumpData::class.java)
                        bumpList.add(bump!!)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        bumpList.forEach { bump ->
            if (bump.latitude == latitude && bump.longitude == longitude){
                val id = bump.id
                var downVotes = bump.downVotes
                var upVotes = bump.upVotes
                upVotes = upVotes?.plus(1)
                if (downVotes != null) {
                    if (downVotes - upVotes!! >= 5){
                        if (id != null) {
                            bumpsDatabaseReference.child(id).setValue(null)
                        }
                        Toast.makeText(context, "Bump is removed", Toast.LENGTH_LONG).show()
                    }
                }
                val updatedBumpData = bump
                updatedBumpData.upVotes = upVotes
                if (id != null) {
                    bumpsDatabaseReference.child(id).setValue(updatedBumpData)
                }
            }
        }
    }
}