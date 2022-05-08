package com.techtown.matchingservice

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.android.material.slider.RangeSlider
import com.google.android.material.slider.Slider
import com.techtown.matchingservice.databinding.ConditionBinding
import java.text.NumberFormat
import java.util.*

class ConditionDialog(context:Context) {
    //private var _binding: ConditionBinding? = null
    //private val binding get() = _binding!!
    var editPricevalue : Float = 10000.toFloat()
    var editDistancevalue : Float = 1000.toFloat()
    var editDayvalue : Float = 90.toFloat()
    private val dialog = Dialog(context)
    private lateinit var onClickListener : OnDialogClickListener
    fun setOnClickListener(listener : OnDialogClickListener){
        onClickListener = listener
    }

    fun showDialog(){
        dialog.setContentView(R.layout.condition)
        dialog.window!!.setLayout(WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)
        dialog.show()
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.findViewById<Button>(R.id.back).setOnClickListener(){
            dialog?.cancel()
        }

        dialog.findViewById<Slider>(R.id.slider).setLabelFormatter { value: Float ->
            val format = NumberFormat.getCurrencyInstance()
            format.maximumFractionDigits = 0
            format.currency = Currency.getInstance("KRW")
            format.format(value.toInt())
        }


        dialog.findViewById<Slider>(R.id.slider).addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            @SuppressLint("RestrictedApi")
            override fun onStartTrackingTouch(slider: Slider) {
                var value = slider.value.toInt()
                if(value<=30000 && editPricevalue <=30000){
                    dialog.findViewById<EditText>(R.id.edittext_condition_price).setText(value.toString())}
            }

            @SuppressLint("RestrictedApi")
            override fun onStopTrackingTouch(slider: Slider) {
                var value = slider.value.toInt()
                if(value<=30000 && editPricevalue<=30000){
                    dialog.findViewById<EditText>(R.id.edittext_condition_price).setText(value.toString())}
            }
        })

        dialog.findViewById<Slider>(R.id.slider).addOnChangeListener { slider, value, fromUser ->
            var sliderV = value.toInt()
            if(sliderV<=30000 && editPricevalue<=30000){
                dialog.findViewById<EditText>(R.id.edittext_condition_price).setText(sliderV.toString())}
        }

        //가격 edittext가 변경시 slider도 변경
        dialog.findViewById<EditText>(R.id.edittext_condition_price).addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(dialog.findViewById<EditText>(R.id.edittext_condition_price).text.toString()!="") {
                    editPricevalue = (dialog.findViewById<EditText>(R.id.edittext_condition_price).text.toString()).toFloat()
                    if(editPricevalue<=30000){
                        dialog.findViewById<Slider>(R.id.slider).setValue(editPricevalue) }
                    else if(editPricevalue>30000){
                        dialog.findViewById<Slider>(R.id.slider).setValue(30000.toFloat()) }

                }
                dialog.findViewById<EditText>(R.id.edittext_condition_price).setSelection(dialog.findViewById<EditText>(R.id.edittext_condition_price).text.length)
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })


        dialog.findViewById<Slider>(R.id.slider1).addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            @SuppressLint("RestrictedApi")
            override fun onStartTrackingTouch(slider: Slider) {
                var value = slider.value.toInt()
                if(value<=2000 && editDistancevalue<=2000){
                    dialog.findViewById<EditText>(R.id.edittext_condition_distance).setText(value.toString())}
            }

            @SuppressLint("RestrictedApi")
            override fun onStopTrackingTouch(slider: Slider) {
                var value = slider.value.toInt()
                if(value<=2000 && editDistancevalue<=2000){
                    dialog.findViewById<EditText>(R.id.edittext_condition_distance).setText(value.toString())}
            }
        })

        dialog.findViewById<Slider>(R.id.slider1).addOnChangeListener { slider, value, fromUser ->
            var sliderV = value.toInt()
            if(sliderV<=2000 && editDistancevalue<=2000){
                dialog.findViewById<EditText>(R.id.edittext_condition_distance).setText(sliderV.toString())}
        }

        //거리 edittext가 변경시 slider도 변경
        dialog.findViewById<EditText>(R.id.edittext_condition_distance).addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(dialog.findViewById<EditText>(R.id.edittext_condition_distance).text.toString()!="") {
                    editDistancevalue = (dialog.findViewById<EditText>(R.id.edittext_condition_distance).text.toString()).toFloat()
                    if(editDistancevalue<=2000){
                        dialog.findViewById<Slider>(R.id.slider1).setValue(editDistancevalue) }
                    else if(editDistancevalue>2000){
                        dialog.findViewById<Slider>(R.id.slider1).setValue(2000.toFloat()) }

                }
                dialog.findViewById<EditText>(R.id.edittext_condition_distance).setSelection(dialog.findViewById<EditText>(R.id.edittext_condition_distance).text.length)
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })

        dialog.findViewById<Slider>(R.id.slider2).addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            @SuppressLint("RestrictedApi")
            override fun onStartTrackingTouch(slider: Slider) {
                var value = slider.value.toInt()
                if(value<=180 && editDayvalue<=180){
                    dialog.findViewById<EditText>(R.id.edittext_condition_day).setText(value.toString())}
            }

            @SuppressLint("RestrictedApi")
            override fun onStopTrackingTouch(slider: Slider) {
                var value = slider.value.toInt()
                if(value<=180 && editDayvalue<=180){
                    dialog.findViewById<EditText>(R.id.edittext_condition_day).setText(value.toString())}
            }
        })

        dialog.findViewById<Slider>(R.id.slider2).addOnChangeListener { slider, value, fromUser ->
            var valueV = value.toInt()
            if(valueV<=180 && editDayvalue<=180){
                dialog.findViewById<EditText>(R.id.edittext_condition_day).setText(valueV.toString())}
        }

        //주기 edittext가 변경시 slider도 변경
        dialog.findViewById<EditText>(R.id.edittext_condition_day).addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(dialog.findViewById<EditText>(R.id.edittext_condition_day).text.toString()!="") {
                    editDayvalue = (dialog.findViewById<EditText>(R.id.edittext_condition_day).text.toString()).toFloat()
                    if(editDayvalue<=180){
                        dialog.findViewById<Slider>(R.id.slider2).setValue(editDayvalue) }
                    else if(editDayvalue>180){
                        dialog.findViewById<Slider>(R.id.slider2).setValue(180.toFloat()) }

                }
                dialog.findViewById<EditText>(R.id.edittext_condition_day).setSelection(dialog.findViewById<EditText>(R.id.edittext_condition_day).text.length)
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })
        dialog.findViewById<Button>(R.id.btn_search).setOnClickListener {
            onClickListener.onClicked(editPricevalue, editDayvalue, editDistancevalue)
            dialog.dismiss()
        }
    }

    interface OnDialogClickListener{
        fun onClicked(price : Float, day : Float, distance : Float)
    }
}