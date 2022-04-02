package com.techtown.matchingservice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.techtown.matchingservice.databinding.Fragment2Binding

class Fragment2 : Fragment() {
    private var mBinding: Fragment2Binding?=null
    private val binding get() = mBinding!!
    private var mContext:Context? = null
    private val _context get() = mContext!!
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        mBinding = Fragment2Binding.inflate(inflater, container,false)
        val view = binding.root
        binding.imageButton2.setOnClickListener {
            val intent = Intent(context, SearchActivity::class.java)
            startActivity(intent)
        }
        return view
    }
}