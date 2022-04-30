package com.techtown.matchingservice

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.GsonBuilder
import okhttp3.*
import java.io.IOException
import java.net.URL
import java.net.URLEncoder

class RecommendActivity : AppCompatActivity() {
    val clientId = "J1nu_UfuVjUnvnvQFjCG"
    val clientSecret = "xzgFjMymr7"
    private var recyclerView : RecyclerView? = null
    var btn_search : ImageView? = null
    var edit_search : EditText? = null

    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recommend)

        recyclerView = findViewById(R.id.result_list)
        btn_search = findViewById(R.id.btn_search)
        edit_search = findViewById(R.id.edit_search)

        val btn_back = findViewById<Button>(R.id.button50)
        btn_back.setOnClickListener {
            setResult(RESULT_CANCELED, intent)
            finish()
        }

        btn_search?.setOnClickListener {
            //키워드 없으면
            if(edit_search?.text!!.isEmpty()){
                return@setOnClickListener
            }

            recyclerView?.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
            recyclerView?.setHasFixedSize(true)

            fetchJson(edit_search?.text.toString())

            val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(edit_search?.windowToken, 0)
        }

        recyclerView?.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

    }
    fun fetchJson(vararg p0: String){
        val text = URLEncoder.encode("${p0[0]}", "UTF-8")
        println(text)
        val url = URL("https://openapi.naver.com/v1/search/shop.json?query=${text}&display=20&start=1")
        val formBody = FormBody.Builder()
            .add("query", "${text}")
            .add("display", "20")
            .add("start", "1")
            .build()
        val request = Request.Builder()
            .url(url)
            .addHeader("X-Naver-Client-Id", clientId)
            .addHeader("X-Naver-Client-Secret", clientSecret)
            .method("GET", null)
            .build()
        val client = OkHttpClient()

        client.newCall(request).enqueue(object : Callback {

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                println("Success to execute request : $body")

                //Gson을 Kotlin에서 사용 가능한 object로 만든다.
                val gson = GsonBuilder().create()
                //아! 이렇게 하는구나
                val itemfeed = gson.fromJson(body, Itemfeed::class.java)
                //println(homefeed)

                for(i in 0..19){
                    var title = itemfeed.items[i].title
                    title = title.replace("<b>","")
                    title = title.replace("</b>","")
                    itemfeed.items[i].title = title
                }

                //어답터를 연결하자. 메인쓰레드 변경하기 위해 이 메소드 사용
                runOnUiThread {
                    recyclerView?.adapter = RecyclerViewAdapter(itemfeed)
                    edit_search?.setText("")
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                println("Failed to execute request")
            }
        })
    }
    data class Itemfeed(val items: List<Item>)
    data class Item(
        var title : String,
        val link : String,
        val image : String,
        val lprice : String,
        val hprice : String,
        val mallName : String,
        val productId : String,
        val productType : String,
        val maker : String,
        val brand : String,
        val category1 : String,
        val category2 : String,
        val category3 : String,
        val category4 : String
    )
    inner class RecyclerViewAdapter(val itemfeed: Itemfeed): RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {
        override fun getItemCount(): Int {
            return itemfeed.items.count()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.recommend_item, parent,false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bindItems(itemfeed.items.get(position))
        }
        inner class ViewHolder(val view: View): RecyclerView.ViewHolder(view){
            fun bindItems(data: Item){
                var imageView = view.findViewById<ImageView>(R.id.item_image)
                Glide.with(view.context).load(data.image)
                    .apply(RequestOptions().override(300,450))
                    .apply(RequestOptions.centerCropTransform())
                    .into(imageView)
                var btn_regist = itemView.findViewById<Button>(R.id.btn_regist)
                var textView_title = itemView.findViewById<TextView>(R.id.text_title)
                var textView_lprice = itemView.findViewById<TextView>(R.id.text_lowprice)
                textView_title.text = data.title
                textView_lprice.text = "최저가 ${data.lprice}원"

                btn_regist.setOnClickListener {
                    Intent(applicationContext, ProductActivity::class.java).apply {
                        putExtra("title", data.title)
                        putExtra("lprice", data.lprice)
                        putExtra("imageURL", data.image)
                        putExtra("link", data.link)
                        putExtra("bol", "shop")

                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        setResult(RESULT_OK, intent)
                    }.run {applicationContext?.startActivity(this)}
                }

                itemView.setOnClickListener({
                    val webpage = Uri.parse("${data.link}")
                    val webIntent = Intent(Intent.ACTION_VIEW, webpage)
                    view.getContext().startActivity(webIntent)
                })
            }
        }
    }


}