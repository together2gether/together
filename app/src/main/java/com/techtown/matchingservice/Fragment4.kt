package com.techtown.matchingservice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.techtown.matchingservice.databinding.Fragment1Binding
import com.techtown.matchingservice.databinding.Fragment4Binding

class Fragment4 : Fragment() {
    private var mBinding: Fragment4Binding?=null
    private val binding get() = mBinding!!
    private var mContext: Context? = null
    private val _context get() = mContext!!
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        mBinding = Fragment4Binding.inflate(inflater, container,false)
        val view = binding.root
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
}