package com.techtown.matchingservice

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.techtown.matchingservice.databinding.Fragment4Binding
import com.techtown.matchingservice.model.UsersInfo

class Fragment4 : Fragment() {
    companion object{
        fun newInstance() : Fragment4{
            return Fragment4()
        }
    }
    val database = Firebase.database("https://matchingservice-ac54b-default-rtdb.asia-southeast1.firebasedatabase.app/")
    val infoRef = database.getReference("usersInfo")

    var uid = Firebase.auth.currentUser?.uid.toString()

    private var mBinding: Fragment4Binding?=null
    private val binding get() = mBinding!!
    private var mContext: Context? = null
    private val _context get() = mContext!!
    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        mBinding = Fragment4Binding.inflate(inflater, container,false)
        val view = binding.root

        infoRef.child(uid).addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val userProfile = snapshot.getValue<UsersInfo>()
                binding.textViewNickname.text = userProfile?.nickname+" 님"
                if(userProfile?.profileImageUrl != ""){
                    Glide.with(requireContext()).load(userProfile?.profileImageUrl)
                        .apply(RequestOptions().circleCrop())
                        .into(binding.profileImg!!)
                }
            }
        })

        binding.button5.setOnClickListener {
            val intent = Intent(context, ModifyInfo::class.java)
            startActivity(intent)

        }
        binding.button8.setOnClickListener {
            val intent = Intent(context, TradeActivity::class.java)
            startActivity(intent)
        }
        binding.button7.setOnClickListener {
            val intent = Intent(context, GroupActivity::class.java)
            startActivity(intent)
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        infoRef.child(uid).addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val userProfile = snapshot.getValue<UsersInfo>()
                binding.textViewNickname.text = userProfile?.nickname+" 님"
                if(userProfile?.profileImageUrl != ""){
                    Glide.with(requireContext()).load(userProfile?.profileImageUrl)
                        .apply(RequestOptions().circleCrop())
                        .into(binding.profileImg!!)
                }
            }
        })
    }

    /*@SuppressLint("UseRequireInsteadOfGet")
    fun refresh() {
        var ft: FragmentTransaction = getFragmentManager()!!.beginTransaction()
        ft.detach(this).attach(this).commit()
    }*/



}