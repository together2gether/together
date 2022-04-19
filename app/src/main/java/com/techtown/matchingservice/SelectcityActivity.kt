package com.techtown.matchingservice

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity

class SelectcityActivity : AppCompatActivity() {
    var spinnerCity: Spinner? = null
    var spinnerSigungu: Spinner? = null
    var spinnerDong: Spinner? = null

    var arrayAdapter: ArrayAdapter<String>? = null
    val EXTRA_ADDRESS: String = "address"

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selectcity)

        spinnerCity = findViewById(R.id.city)
        spinnerSigungu = findViewById(R.id.sigungu)
        spinnerDong = findViewById(R.id.dong)

        ArrayAdapter.createFromResource(
            this,
            R.array.spinner_region,
            R.layout.spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item)
            spinnerCity!!.adapter = adapter
        }

        initAddressSpinner()



    }


    fun initAddressSpinner(){
        spinnerCity?.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if(position == 0){
                    spinnerSigungu?.adapter = null
                } else if(position == 1){
                    setSigunguSpinnerAdapterItem(R.array.spinner_region_seoul)
                    spinnerSigungu?.onItemSelectedListener = object :
                        AdapterView.OnItemSelectedListener{
                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            if(position == 0){
                                setDongSpinnerAdapterItem(R.array.spinner_region_seoul_gangnam)
                            } else if(position == 1){
                                setDongSpinnerAdapterItem(R.array.spinner_region_seoul_gangdong)
                            } else if(position == 2){
                                setDongSpinnerAdapterItem(R.array.spinner_region_seoul_gangbuk)
                            } else if(position == 3){
                                setDongSpinnerAdapterItem(R.array.spinner_region_seoul_gangseo)
                            } else if(position == 4){
                                setDongSpinnerAdapterItem(R.array.spinner_region_seoul_gwanak)
                            } else if(position == 5){
                                setDongSpinnerAdapterItem(R.array.spinner_region_seoul_gwangjin)
                            } else if(position == 6){
                                setDongSpinnerAdapterItem(R.array.spinner_region_seoul_guro)
                            } else if(position == 7){
                                setDongSpinnerAdapterItem(R.array.spinner_region_seoul_geumcheon)
                            } else if(position == 8){
                                setDongSpinnerAdapterItem(R.array.spinner_region_seoul_nowon)
                            } else if(position == 9){
                                setDongSpinnerAdapterItem(R.array.spinner_region_seoul_dobong)
                            } else if(position == 10){
                                setDongSpinnerAdapterItem(R.array.spinner_region_seoul_dongdaemun)
                            } else if(position == 11){
                                setDongSpinnerAdapterItem(R.array.spinner_region_seoul_dongjag)
                            } else if(position == 12){
                                setDongSpinnerAdapterItem(R.array.spinner_region_seoul_mapo)
                            } else if(position == 13){
                                setDongSpinnerAdapterItem(R.array.spinner_region_seoul_seodaemun)
                            } else if(position == 14){
                                setDongSpinnerAdapterItem(R.array.spinner_region_seoul_seocho)
                            } else if(position == 15){
                                setDongSpinnerAdapterItem(R.array.spinner_region_seoul_seongdong)
                            } else if(position == 16){
                                setDongSpinnerAdapterItem(R.array.spinner_region_seoul_seongbuk)
                            } else if(position == 17){
                                setDongSpinnerAdapterItem(R.array.spinner_region_seoul_songpa)
                            } else if(position == 18){
                                setDongSpinnerAdapterItem(R.array.spinner_region_seoul_yangcheon)
                            } else if(position == 19){
                                setDongSpinnerAdapterItem(R.array.spinner_region_seoul_yongsan)
                            } else if(position == 20){
                                setDongSpinnerAdapterItem(R.array.spinner_region_seoul_yeongdeungpo)
                            } else if(position == 21){
                                setDongSpinnerAdapterItem(R.array.spinner_region_seoul_eunpyeong)
                            } else if(position == 22){
                                setDongSpinnerAdapterItem(R.array.spinner_region_seoul_jongno)
                            } else if(position == 23){
                                setDongSpinnerAdapterItem(R.array.spinner_region_seoul_jung)
                            } else if(position == 24){
                                setDongSpinnerAdapterItem(R.array.spinner_region_seoul_jungnanggu)
                            }
                        }

                        override fun onNothingSelected(p0: AdapterView<*>?) {
                        }
                    }

                } else if(position == 2){
                    setSigunguSpinnerAdapterItem(R.array.spinner_region_busan)
                } else if(position == 3){
                    setSigunguSpinnerAdapterItem(R.array.spinner_region_daegu)
                } else if(position == 4){
                    setSigunguSpinnerAdapterItem(R.array.spinner_region_incheon)
                } else if(position == 5){
                    setSigunguSpinnerAdapterItem(R.array.spinner_region_gwangju)
                } else if(position == 6){
                    setSigunguSpinnerAdapterItem(R.array.spinner_region_daejeon)
                } else if(position == 7){
                    setSigunguSpinnerAdapterItem(R.array.spinner_region_ulsan)
                } else if(position == 8){
                    setSigunguSpinnerAdapterItem(R.array.spinner_region_sejong)
                } else if(position == 9){
                    setSigunguSpinnerAdapterItem(R.array.spinner_region_gyeonggi)
                } else if(position == 10){
                    setSigunguSpinnerAdapterItem(R.array.spinner_region_gangwon)
                } else if(position == 11){
                    setSigunguSpinnerAdapterItem(R.array.spinner_region_chung_buk)
                } else if(position == 12){
                    setSigunguSpinnerAdapterItem(R.array.spinner_region_chung_nam)
                } else if(position == 13){
                    setSigunguSpinnerAdapterItem(R.array.spinner_region_jeon_buk)
                } else if(position == 14){
                    setSigunguSpinnerAdapterItem(R.array.spinner_region_jeon_nam)
                } else if(position == 15){
                    setSigunguSpinnerAdapterItem(R.array.spinner_region_gyeong_buk)
                } else if(position == 16){
                    setSigunguSpinnerAdapterItem(R.array.spinner_region_gyeong_nam)
                } else if(position == 17){
                    setSigunguSpinnerAdapterItem(R.array.spinner_region_jeju)
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }


    }

    fun setSigunguSpinnerAdapterItem(array_resource: Int){
        if(arrayAdapter != null){
            spinnerSigungu?.adapter = null
            arrayAdapter = null
        }
        if(spinnerCity?.selectedItemPosition!! > 1){
            spinnerDong?.adapter = null
        }

        arrayAdapter = ArrayAdapter<String>(this, R.layout.spinner_item, resources.getStringArray(array_resource))
        arrayAdapter!!.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item)
        spinnerSigungu?.adapter = arrayAdapter
    }

    fun setDongSpinnerAdapterItem(array_resource: Int){
        if(arrayAdapter != null){
            spinnerDong?.adapter = null
            arrayAdapter = null
        }

        arrayAdapter = ArrayAdapter<String>(this, R.layout.spinner_item, resources.getStringArray(array_resource))
        arrayAdapter!!.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item)
        spinnerDong?.adapter = arrayAdapter
    }
}