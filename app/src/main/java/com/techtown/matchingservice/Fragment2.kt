package com.techtown.matchingservice


import android.content.Intent
import android.content.Intent.getIntent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.getbase.floatingactionbutton.FloatingActionsMenu
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.techtown.matchingservice.databinding.FoodItemBinding
import com.techtown.matchingservice.databinding.Fragment2Binding
import com.techtown.matchingservice.model.DeliveryDTO
import kotlinx.android.synthetic.main.fragment_2.*

class Fragment2 : Fragment() {
    private lateinit var binding: Fragment2Binding
    var firestore: FirebaseFirestore? = null
    lateinit var uid: String
    var deliverycheck : Int = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = Fragment2Binding.inflate(inflater, container, false)
        firestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().uid!!
        val drawerLayout:DrawerLayout = binding.drawerLayout
        val drawerView = binding.drawer
        var cate : String = arguments?.getString("category").toString()
        if(cate == "open"){
            drawerLayout.openDrawer(drawerView)
            cate = "close"
        }
        binding.button3.setOnClickListener {
            //val string = binding.edit.text
            /*if (string.isNullOrEmpty()) {
                Toast.makeText(context, "chip 이름을 입력해주세요", Toast.LENGTH_LONG).show()
            } else {
                binding.chipGroup2.addView(Chip(context).apply {
                    text = string
                    chipBackgroundColor = ColorStateList.valueOf(Color.parseColor("#ffffff"))

                    chipStrokeColor = ColorStateList.valueOf(Color.parseColor("#cdd9f1"))
                    chipStrokeWidth = 4f
                    setTextColor(
                        ColorStateList.valueOf(Color.parseColor("#000000"))
                    )
                    isCloseIconVisible = true
                    setOnCloseIconClickListener { binding.chipGroup2.removeView(this) }
                })
            }*/
        }
        binding.fragment2ProductRegistration.setOnClickListener {
            val intent = Intent(context, FoodActivity::class.java)
            startActivity(intent)
            Intent(context, FoodActivity::class.java).apply{
                putExtra("kind", "delivery".toString())

            }.run { context?.startActivity(this) }
        }
        binding.shop.setOnClickListener {
            val intent = Intent(context, FoodActivity::class.java)
            startActivity(intent)
            Intent(context, FoodActivity::class.java).apply{
                putExtra("kind", "shop".toString())

            }.run { context?.startActivity(this) }
        }
        binding.menu2.setOnFloatingActionsMenuUpdateListener(object: FloatingActionsMenu.OnFloatingActionsMenuUpdateListener{
            override fun onMenuExpanded() {
                binding.dark.setBackgroundColor(Color.parseColor("#80000000"))
            }

            override fun onMenuCollapsed() {
                binding.dark.setBackgroundColor(Color.parseColor("#00000000"))
            }
        })
        binding.fragment2RecyclerView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        binding.fragment2RecyclerView.adapter =Fragment2DeliveryRecyclerviewAdapter()
        binding.deliver.setOnClickListener {
            deliverycheck = 1
            binding.fragment2RecyclerView.adapter =Fragment2DeliveryRecyclerviewAdapter()
            drawerLayout.closeDrawer(drawerView)
        }
        binding.shopping.setOnClickListener {
            deliverycheck = 2
            binding.fragment2RecyclerView.adapter =Fragment2DeliveryRecyclerviewAdapter()
            drawerLayout.closeDrawer(drawerView)
        }
        binding.fragment2Rg.setOnCheckedChangeListener { radioGroup, i ->
            when(i){
                R.id.fragment2_rb_delivery -> {
                    deliverycheck = 1
                    binding.fragment2RecyclerView.adapter =Fragment2DeliveryRecyclerviewAdapter()
                    //binding.fragment2RbDelivery.buttonTintList = ColorStateList.valueOf(Color.parseColor("#ff5959"))
                    //binding.fragment2RbShopping.buttonTintList = ColorStateList.valueOf(Color.parseColor("#808080"))
                }
                R.id.fragment2_rb_shopping -> {
                    deliverycheck = 2
                    binding.fragment2RecyclerView.adapter =Fragment2DeliveryRecyclerviewAdapter()
                    //binding.fragment2RbShopping.buttonTintList = ColorStateList.valueOf(Color.parseColor("#ff5959"))
                    //binding.fragment2RbDelivery.buttonTintList = ColorStateList.valueOf(Color.parseColor("#808080"))
                }
            }
        }

        binding.fragment2RecyclerView.layoutManager = LinearLayoutManager(activity)
        return binding.root
    }

    inner class DeliveryViewHolder(var binding: FoodItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class ShoppingViewHolder(var binding: FoodItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class Fragment2DeliveryRecyclerviewAdapter() : RecyclerView.Adapter<DeliveryViewHolder>() {

        var deliveryDTOs: ArrayList<DeliveryDTO> = arrayListOf()
        var deliveryUidList: ArrayList<String> = arrayListOf()

        init {
            if (deliverycheck == 1) {
                firestore?.collection("delivery")
                    ?.orderBy("delivery_timestamp")
                    ?.addSnapshotListener { value, error ->
                        deliveryDTOs.clear()
                        deliveryUidList.clear()
                        for (snapshot in value!!.documents) {
                            var item = snapshot.toObject(DeliveryDTO::class.java)
                            if (item!!.delivery) {
                                deliveryDTOs.add(item)
                                deliveryUidList.add(snapshot.id)
                            }
                        }
                        notifyDataSetChanged()
                    }
            }else{
                firestore?.collection("delivery")
                    ?.orderBy("delivery_timestamp")
                    ?.addSnapshotListener { value, error ->
                        deliveryDTOs.clear()
                        deliveryUidList.clear()
                        for (snapshot in value!!.documents) {
                            var item = snapshot.toObject(DeliveryDTO::class.java)
                            if (!item!!.delivery) {
                                deliveryDTOs.add(item)
                                deliveryUidList.add(snapshot.id)
                            }
                        }
                        notifyDataSetChanged()
                    }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup,viewType: Int): DeliveryViewHolder {
            var view =
                FoodItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return DeliveryViewHolder(view)
        }

        override fun onBindViewHolder(holder: DeliveryViewHolder, position: Int) {
            var viewHolder = holder.binding
            //store
            viewHolder.fooditemTextviewstore.text = deliveryDTOs[position].store
            //order price
            viewHolder.fooditemTextvieworderprice.text = deliveryDTOs[position].order_price.toString()
            //delivery price
            viewHolder.fooditemTextviewdeliveryprice.text = deliveryDTOs[position].delivery_price.toString()

            //click
            viewHolder.fooditemCardView.setOnClickListener {
                Intent(context, Delivery::class.java).apply {
                    putExtra("store", deliveryDTOs[position].store.toString())
                    putExtra("delivery", deliveryDTOs[position].delivery.toString())
                    putExtra("orderPrice", deliveryDTOs[position].order_price.toString())
                    putExtra("deliveryPrice", deliveryDTOs[position].delivery_price.toString())
                    putExtra("deliveryAddress", deliveryDTOs[position].delivery_address)
                    putExtra("deliveryid", deliveryUidList[position])
                    putExtra("deliveryuid", deliveryDTOs[position].delivery_uid)
                    putExtra("detail", deliveryDTOs[position].delivery_detail)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }.run { context?.startActivity(this) }
            }
        }

        override fun getItemCount(): Int {
            return deliveryDTOs.size
        }
    }
}