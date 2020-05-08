package com.lotus.lotustargram.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.lotus.lotustargram.R
import com.lotus.lotustargram.navigation.model.ContentDTO
import kotlinx.android.synthetic.main.fragment_grid.view.*

class GridFragment :Fragment(){

    var firestore :FirebaseFirestore? =null
    var fragmentView :View? =null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        var fragmentView =LayoutInflater.from(activity).inflate(R.layout.fragment_grid,container,false)
        //firestore 초기화
        firestore = FirebaseFirestore.getInstance()

        //리사이클러뷰에 어댑터 연결
        fragmentView?.gridfragment_recyclerview?.adapter =UserFragmentRecyclerViewAdapter()
        //Layout Manager에 GridLayoutManager 세팅
        fragmentView?.gridfragment_recyclerview?.layoutManager =GridLayoutManager(activity, 3)
        return fragmentView
    }

    //리사이클러뷰가 사용할 어댑터
    inner class UserFragmentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

        var contentDTOs :ArrayList<ContentDTO> = arrayListOf()
        init {
            //생성자 생성후 DB값 읽어오기
            //내가 올린 이미지만 볼 수 있도록 설정
            firestore?.collection("images")?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (querySnapshot ==null){
                    //프로그램 안정성을 위하여 null일 경우 바로 종료
                    return@addSnapshotListener
                }
                //null이 아닐 경우 for문에 값 담기
                //DB 받기
                for (snapshot in querySnapshot.documents){
                    contentDTOs.add(snapshot.toObject(ContentDTO ::class.java)!!)
                }
                //데이터 교체(새로고침
                notifyDataSetChanged()
            }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            //화면의 폭 가져오기 (바로 뷰를 불러오는 것이 아님
            var width =resources.displayMetrics.widthPixels /3
            //화면 폭의 1/3값 가져오기
            //Imageview에 넣게 되면 폭의 1/3크기의 정사각형 이미지 생성됨

            var imageView = ImageView(parent.context)
            imageView.layoutParams = LinearLayoutCompat.LayoutParams(width, width)
            return CustomViewHolder(imageView)
        }

        inner class CustomViewHolder(var imageView: ImageView) : RecyclerView.ViewHolder(imageView) {

        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var imageView =(holder as CustomViewHolder).imageView
            Glide.with(holder.imageView.context)
                .load(contentDTOs[position].imageUrl)
                .apply(
                    RequestOptions().centerCrop())
                .into(imageView)
        }

    }
}