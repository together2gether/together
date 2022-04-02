package com.techtown.matchingservice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.techtown.matchingservice.databinding.Fragment1Binding
import java.util.concurrent.locks.Condition

class Fragment1 : Fragment() {
    private var mBinding:Fragment1Binding?=null
    private val binding get() = mBinding!!
    private var mContext:Context? = null
    private val _context get() = mContext!!
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        mBinding = Fragment1Binding.inflate(inflater, container,false)
        val view = binding.root
        binding.imageButton2.setOnClickListener {
            val intent = Intent(context, SearchActivity::class.java)
            startActivity(intent)
        }
        binding.button51.setOnClickListener {
            val intent = Intent(context, ConditionActivity::class.java)
            startActivity(intent)
        }
        return view
    }

}