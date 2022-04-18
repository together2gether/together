package com.techtown.matchingservice

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Exception

class RecommandLocation : AppCompatActivity() {
    companion object {
        const val BASE_URL = "http://dapi.kakao.com/"
        const val API_KEY = "KakaoAK 39c205c0e521e0d112dcefa5460592d8"
    }
    private val listItems = arrayListOf<ListLayout>()   // 리사이클러 뷰 아이템
    private val listAdapter = ListAdapter(listItems)    // 리사이클러 뷰 어댑터
    private var pageNumber = 1      // 검색 페이지 번호
    private var keyword = ""        // 검색 키워드
    lateinit var mapView : MapView
    lateinit var button : Button
    lateinit var button1 : Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mapView = findViewById(R.id.mapView)
        //val kakao = findViewById<ConstraintLayout>(R.id.mapView)
        //kakao.addView(mapView)
        val layout = findViewById<RecyclerView>(R.id.rv_list)
        try{
            // 리사이클러 뷰
            layout.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            layout.adapter = listAdapter
            // 리스트 아이템 클릭 시 해당 위치로 이동
            listAdapter.setItemClickListener(object: ListAdapter.OnItemClickListener {
                override fun onClick(v: View, position: Int) {
                    val mapPoint = MapPoint.mapPointWithGeoCoord(listItems[position].y, listItems[position].x)
                    mapView.setMapCenterPointAndZoomLevel(mapPoint, 1, true)
                }
            })
        }catch (e: Exception){

        }
        searchKeyword("편의점",1)
    }

    // 키워드 검색 함수
    private fun searchKeyword(keyword: String, page: Int) {
        val retrofit = Retrofit.Builder()          // Retrofit 구성
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(KakaoAPI::class.java)            // 통신 인터페이스를 객체로 생성
        //val call = api.getSearchCategory(API_KEY, "CS2", "126.972526" ,"37.560452", 50000)
        val call = api.getSearchKeyword(API_KEY, "CS2","126.972526", "37.560452",1000 ,1, "distance")
        // 검색 조건 입력

        //val call = api.getSearchKeyword(API_KEY, keyword, "126.972526" ,"37.560452", 50000 )
        // API 서버에 요청
        call.enqueue(object: Callback<ResultSearchKeyword> {
            override fun onResponse(call: Call<ResultSearchKeyword>, response: Response<ResultSearchKeyword>) {
                // 통신 성공
                addItemsAndMarkers(response.body())
                val call1 = api.getSearchKeyword(API_KEY, "SW8", "126.972526", "37.560452", 1000, 1, "distance")
                call1.enqueue(object : Callback<ResultSearchKeyword> {
                    override fun onResponse(
                        call: Call<ResultSearchKeyword>,
                        response: Response<ResultSearchKeyword>
                    ) {
                        addItemsAndMarkers(response.body())
                    }

                    override fun onFailure(call: Call<ResultSearchKeyword>, t: Throwable) {
                        Log.w("LocalSearch", "통신 실패 : ${t.message}")
                    }
                })
            }

            override fun onFailure(call: Call<ResultSearchKeyword>, t: Throwable) {
                // 통신 실패
                Log.w("LocalSearch", "통신 실패: ${t.message}")
            }
        })

    }

    // 검색 결과 처리 함수
    private fun addItemsAndMarkers(searchResult: ResultSearchKeyword?) {
        if (!searchResult?.documents.isNullOrEmpty()) {
            // 검색 결과 있음
            //listItems.clear()                   // 리스트 초기화
            //mapView.removeAllPOIItems() // 지도의 마커 모두 제거
            for (document in searchResult!!.documents) {
                // 결과를 리사이클러 뷰에 추가
                val item = ListLayout(document.place_name,
                    document.road_address_name,
                    document.address_name,
                    document.x.toDouble(),
                    document.y.toDouble())
                listItems.add(item)

                // 지도에 마커 추가
                val point = MapPOIItem()
                point.apply {
                    itemName = document.place_name
                    mapPoint = MapPoint.mapPointWithGeoCoord(document.y.toDouble(),
                        document.x.toDouble())
                    markerType = MapPOIItem.MarkerType.BluePin
                    selectedMarkerType = MapPOIItem.MarkerType.RedPin
                }
                mapView.addPOIItem(point)
            }
            listAdapter.notifyDataSetChanged()
        } else {
            // 검색 결과 없음
            Toast.makeText(this, "검색 결과가 없습니다", Toast.LENGTH_SHORT).show()
        }
    }

}