package com.techtown.matchingservice

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.android.material.slider.RangeSlider
import com.google.android.material.slider.Slider
import com.techtown.matchingservice.databinding.ConditionBinding
import java.text.NumberFormat
import java.util.*

class ConditionDialog : DialogFragment() {
    private var _binding: ConditionBinding? = null
    private val binding get() = _binding!!
    var editPricevalue: Float = 0.toFloat()
    var editDistancevalue: Float = 0.toFloat()
    var editDayvalue: Float = 0.toFloat()

    interface OnDataPassListener{
        fun onDataPass(price : Float?, distance : Float?, day : Float?)
    }
    lateinit var dataPassListener: OnDataPassListener
    override fun onAttach(context: Context) {
        super.onAttach(context)
        dataPassListener = context as OnDataPassListener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val pass = getView()?.findViewById<Button>(R.id.btn_search)
        pass?.setOnClickListener {
            dataPassListener.onDataPass(editPricevalue, editDistancevalue, editDayvalue)
            dialog?.cancel()
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ConditionBinding.inflate(inflater, container, false)
        val view = binding.root
        // 레이아웃 배경을 투명하게 해줌, 필수 아님
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.back.setOnClickListener() {
            dialog?.cancel()
        }
        binding.btnSearch.setOnClickListener() {
            dialog?.cancel()
        }
        binding.slider.setLabelFormatter { value: Float ->
            val format = NumberFormat.getCurrencyInstance()
            format.maximumFractionDigits = 0
            format.currency = Currency.getInstance("KRW")
            format.format(value.toInt())
        }


        binding.slider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            @SuppressLint("RestrictedApi")
            override fun onStartTrackingTouch(slider: Slider) {
                var value = slider.value.toInt()
                if (value <= 30000 && editPricevalue <= 30000) {
                    binding.edittextConditionPrice.setText(value.toString())
                }
            }

            @SuppressLint("RestrictedApi")
            override fun onStopTrackingTouch(slider: Slider) {
                var value = slider.value.toInt()
                if (value <= 30000 && editPricevalue <= 30000) {
                    binding.edittextConditionPrice.setText(value.toString())
                }
            }
        })

        binding.slider.addOnChangeListener { slider, value, fromUser ->
            var sliderV = value.toInt()
            if (sliderV <= 30000 && editPricevalue <= 30000) {
                binding.edittextConditionPrice.setText(sliderV.toString())
            }
        }

        //가격 edittext가 변경시 slider도 변경
        binding.edittextConditionPrice.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (binding.edittextConditionPrice.text.toString() != "") {
                    editPricevalue = (binding.edittextConditionPrice.text.toString()).toFloat()
                    if (editPricevalue <= 30000) {
                        binding.slider.setValue(editPricevalue)
                    } else if (editPricevalue > 30000) {
                        binding.slider.setValue(30000.toFloat())
                    }

                }
                binding.edittextConditionPrice.setSelection(binding.edittextConditionPrice.text.length)
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })


        binding.slider1.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            @SuppressLint("RestrictedApi")
            override fun onStartTrackingTouch(slider: Slider) {
                var value = slider.value.toInt()
                if (value <= 2000 && editDistancevalue <= 2000) {
                    binding.edittextConditionDistance.setText(value.toString())
                }
            }

            @SuppressLint("RestrictedApi")
            override fun onStopTrackingTouch(slider: Slider) {
                var value = slider.value.toInt()
                if (value <= 2000 && editDistancevalue <= 2000) {
                    binding.edittextConditionDistance.setText(value.toString())
                }
            }
        })

        binding.slider1.addOnChangeListener { slider, value, fromUser ->
            var sliderV = value.toInt()
            if (sliderV <= 2000 && editDistancevalue <= 2000) {
                binding.edittextConditionDistance.setText(sliderV.toString())
            }
        }

        //거리 edittext가 변경시 slider도 변경
        binding.edittextConditionDistance.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (binding.edittextConditionDistance.text.toString() != "") {
                    editDistancevalue =
                        (binding.edittextConditionDistance.text.toString()).toFloat()
                    if (editDistancevalue <= 2000) {
                        binding.slider1.setValue(editDistancevalue)
                    } else if (editDistancevalue > 2000) {
                        binding.slider1.setValue(2000.toFloat())
                    }

                }
                binding.edittextConditionDistance.setSelection(binding.edittextConditionDistance.text.length)
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })

        binding.slider2.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            @SuppressLint("RestrictedApi")
            override fun onStartTrackingTouch(slider: Slider) {
                var value = slider.value.toInt()
                if (value <= 180 && editDayvalue <= 180) {
                    binding.edittextConditionDay.setText(value.toString())
                }
            }

            @SuppressLint("RestrictedApi")
            override fun onStopTrackingTouch(slider: Slider) {
                var value = slider.value.toInt()
                if (value <= 180 && editDayvalue <= 180) {
                    binding.edittextConditionDay.setText(value.toString())
                }
            }
        })

        binding.slider2.addOnChangeListener { slider, value, fromUser ->
            var valueV = value.toInt()
            if (valueV <= 180 && editDayvalue <= 180) {
                binding.edittextConditionDay.setText(valueV.toString())
            }
        }

        //주기 edittext가 변경시 slider도 변경
        binding.edittextConditionDay.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (binding.edittextConditionDay.text.toString() != "") {
                    editDayvalue = (binding.edittextConditionDay.text.toString()).toFloat()
                    if (editDayvalue <= 180) {
                        binding.slider2.setValue(editDayvalue)
                    } else if (editDayvalue > 180) {
                        binding.slider2.setValue(180.toFloat())
                    }

                }
                binding.edittextConditionDay.setSelection(binding.edittextConditionDay.text.length)
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })
        return view
    }
}