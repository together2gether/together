package com.techtown.matchingservice

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.techtown.matchingservice.databinding.ParticipantBinding
import com.techtown.matchingservice.model.ContentDTO
import com.techtown.matchingservice.model.UsersInfo

class ParticipantDialog : DialogFragment() {
    private var contentUidList = ArrayList<String>()
    private var _binding: ParticipantBinding? = null
    private val binding get() = _binding!!
    var firestore: FirebaseFirestore? = null
    var productid : String? = null
    //var item = ContentDTO()
    val db = Firebase.firestore
    val docRef = db.collection("images")
    private var database = Firebase.database("https://matchingservice-ac54b-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private val usersRef = database.getReference("usersInfo")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        firestore = FirebaseFirestore.getInstance()
        _binding = ParticipantBinding.inflate(inflater, container, false)
        val view = binding.root

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.back.setOnClickListener() {
            dialog?.cancel()
        }

        productid = arguments?.getString("productid").toString()

        binding.participantRecyclerview.adapter = RecyclerViewAdapter()
        binding.participantRecyclerview.layoutManager = LinearLayoutManager(activity)
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class RecyclerViewAdapter : RecyclerView.Adapter<RecyclerViewAdapter.ParticipantViewHolder>() {
        private var user : UsersInfo? = null


        init {
            getParticipantList()
            Log.d("participant",contentUidList.size.toString())
        }
        fun getParticipantList(){
            docRef.document("$productid")?.get()
                ?.addOnSuccessListener { document ->
                    contentUidList.clear()
                    if(document != null){
                        var item = document.toObject(ContentDTO::class.java)!!
                        for((key, value) in item.Participation){
                            if(value == true){

                                contentUidList.add(key)
                                Log.d("participant",contentUidList.size.toString())

                            }
                        }
                        notifyDataSetChanged()
                    }
                }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewAdapter.ParticipantViewHolder {
            val view : View = LayoutInflater.from(parent.context).inflate(R.layout.participant_item, parent, false)
            return ParticipantViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerViewAdapter.ParticipantViewHolder, position: Int) {
            usersRef.child(contentUidList[position]).addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    user = snapshot.getValue<UsersInfo>()
                    Glide.with(holder.itemView.context)
                        .load(user?.profileImageUrl)
                        .apply(RequestOptions().circleCrop())
                        .into(holder.profile)
                    holder.nickname.text = user?.nickname
                }
            })
        }

        override fun getItemCount(): Int {
            return contentUidList.size
        }
        inner class ParticipantViewHolder(view : View) : RecyclerView.ViewHolder(view){
            val profile : ImageView = view.findViewById(R.id.participantitem_img)
            val nickname : TextView = view.findViewById(R.id.nickname)
            val del : Button = view.findViewById(R.id.participantitem_garbage)
        }
    }
}
