package com.techtown.matchingservice

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.chip.Chip
import com.techtown.matchingservice.databinding.Fragment1Binding
import com.techtown.matchingservice.databinding.RegisterProductBinding
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
        binding.button52.setOnClickListener {
            val intent = Intent(context, ProductActivity::class.java)
            startActivity(intent)
        }
        binding.button3.setOnClickListener {
            val string = binding.edit.text
            if(string.isNullOrEmpty()){
                Toast.makeText(context, "chip 이름을 입력해주세요", Toast.LENGTH_LONG).show()
            } else{
                binding.chipGroup.addView(Chip(context).apply{
                    text = string
                    chipBackgroundColor = ColorStateList.valueOf(Color.parseColor("#ffffff"))

                    chipStrokeColor = ColorStateList.valueOf(Color.parseColor("#cdd9f1"))
                    chipStrokeWidth = 4f
                    setTextColor(
                        ColorStateList.valueOf(Color.parseColor("#000000"))
                    )
                    isCloseIconVisible = true
                    setOnCloseIconClickListener { binding.chipGroup.removeView(this) }
                })
            }
        }
        return view
    }

}