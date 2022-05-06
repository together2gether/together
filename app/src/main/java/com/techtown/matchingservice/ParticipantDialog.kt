package com.techtown.matchingservice

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.techtown.matchingservice.databinding.ParticipantBinding
import com.techtown.matchingservice.databinding.ParticipantItemBinding
import com.techtown.matchingservice.model.ContentDTO
import com.techtown.matchingservice.model.DeliveryDTO
import kotlinx.android.synthetic.main.food_info.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ParticipantDialog : DialogFragment() {
    private var _binding: ParticipantBinding? = null
    private val binding get() = _binding!!
    var firestore: FirebaseFirestore? = null
    var productid : String? = null
    var item = ContentDTO()
    val db = Firebase.firestore
    val docRef = db.collection("images")

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

        binding.participantRecyclerview.adapter = RecyclerviewAdapter()
        binding.participantRecyclerview.layoutManager = LinearLayoutManager(activity)
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class ViewHolder(var binding: ParticipantItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class RecyclerviewAdapter() : RecyclerView.Adapter<ViewHolder>() {

        var contentUidList: ArrayList<String> = arrayListOf()

        init {
            firestore?.collection("images")?.document("$productid")?.get()
                ?.addOnSuccessListener { document ->
                    if(document != null){
                        item = document.toObject(ContentDTO::class.java)!!
                        for((key, value) in item.Participation){
                            if(value == true){
                                contentUidList.add(key)
                            }
                        }
                    }
                }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            var view = ParticipantItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            var viewHolder = holder.binding
            viewHolder.nickname.text = "oki"
        }

        override fun getItemCount(): Int {
            return contentUidList.size
        }
    }
}
