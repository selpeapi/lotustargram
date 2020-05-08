package com.lotus.lotustargram.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.lotus.lotustargram.R
import com.lotus.lotustargram.navigation.model.AlarmDTO
import kotlinx.android.synthetic.main.fragment_alarm.view.*
import kotlinx.android.synthetic.main.item_comment.view.*

class AlarmFragment :Fragment(){

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        var view =LayoutInflater.from(activity).inflate(R.layout.fragment_alarm, container,false)
        //리사이클러뷰에 어댑터 연결
        view.alarmfragment_recyclerview.adapter =AlarmRecyclerviewAdapter()
        //리사이클러뷰 배치 방향
        view.alarmfragment_recyclerview.layoutManager =LinearLayoutManager(activity)
        return view
    }
    inner class AlarmRecyclerviewAdapter :RecyclerView.Adapter<RecyclerView.ViewHolder>(){

        //알림 저장 list 변수
        var alarmDTOList :ArrayList<AlarmDTO> = arrayListOf()

        //생성자
        init {
            //Adapter가 생성될 때 DB 읽어오기
            var uid =FirebaseAuth.getInstance().currentUser?.uid

            //DB에서 쿼리를 이용해 나에게 도착한 메시지만 필터링
            FirebaseFirestore.getInstance().collection("alarms").whereEqualTo("destinationUid", uid).addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                //값을 가져와 리스트에 담기
                alarmDTOList.clear()
                if (querySnapshot ==null)
                    return@addSnapshotListener
                for (snapshop in querySnapshot.documents){
                    //스냅샷 값을 AlarmDTO로 캐스팅 후 넣기
                    alarmDTOList.add(snapshop.toObject(AlarmDTO ::class.java)!!)
                }
                //새로고침
                notifyDataSetChanged()
            }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view =LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)

            return CustonViewHolder(view)
        }
        inner class CustonViewHolder(view :View) :RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return alarmDTOList.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            //종류에 떄라 메시지를 다르게 표시
            var view =holder.itemView

            //프로필 이미지 DB에 접근해서 프로필 이미지 가져오기     (코맨트를 단 상대방의 uid를 입력해 상대방의 프로필 이미지를 가져옴
            FirebaseFirestore.getInstance().collection("profileImages").document(alarmDTOList[position].uid!!).get().addOnCompleteListener { task ->
                if (task.isSuccessful){
                    val url =task.result!!["image"]
                    Glide.with(view.context)
                        .load(url)
                        .apply(RequestOptions().circleCrop())
                        .into(view.commentviewitem_imageview_profile)
                }
            }
            when(alarmDTOList[position].kind){
                0 ->{
                    //좋아요
                    val str_0 =alarmDTOList[position].usedId + "\n "+getString(R.string.alarm_favorite)
                    view.commentviewitem_textview_profile.text =str_0
                }
                1 ->{
                    //코맨트
                    val str_1 =alarmDTOList[position].usedId +"\n "+getString(R.string.alarm_comment) +" of " +alarmDTOList[position].message
                    view.commentviewitem_textview_profile.text =str_1
                }
                2 ->{
                    //팔로우
                    val str_2 =alarmDTOList[position].usedId + "\n "+getString(R.string.alarm_follow)
                    view.commentviewitem_textview_profile.text =str_2
                }

            }
            //코맨트 가리기
            view.commentviewitem_textview_comment.visibility =View.INVISIBLE
        }

    }
}