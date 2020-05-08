package com.lotus.lotustargram.navigation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.lotus.lotustargram.R
import com.lotus.lotustargram.navigation.model.AlarmDTO
import com.lotus.lotustargram.navigation.model.ContentDTO
import kotlinx.android.synthetic.main.activity_comment.*
import kotlinx.android.synthetic.main.item_comment.view.*

class CommentActivity : AppCompatActivity() {

    //intent로 넘길 정보를 담는 글로벌 변수 설정
    var contentUid :String? =null
    var destinationUid :String? =null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)

        contentUid =intent.getStringExtra("contentUid")
        destinationUid =intent.getStringExtra("destinationUid")

        //리사이클러뷰와 어댑터 연결
        comment_recyclerview.adapter =CommentRecyclerviewAdapter()
        comment_recyclerview.layoutManager =LinearLayoutManager(this)

        comment_btn_send.setOnClickListener {
            var comment =ContentDTO.Comment()
            //comment.정보들에 id, uid, 코맨트와 시간을 남김
            comment.userId =FirebaseAuth.getInstance().currentUser?.email
            comment.uid =FirebaseAuth.getInstance().currentUser?.uid
            comment.comment =comment_edit_message.text.toString()
            comment.timestamp =System.currentTimeMillis()

            //DB에 넣기
            FirebaseFirestore.getInstance().collection("images").document(contentUid!!).collection("comments").document().set(comment)
            commentAlarm(destinationUid!!, comment_edit_message.text.toString())
            comment_edit_message.setText("")

        }
    }
    //코맨트 작성시 알림
   fun commentAlarm(destinationUid :String, message :String){
        var alarmDTO =AlarmDTO()
        alarmDTO.destinationUid =destinationUid
        alarmDTO.usedId =FirebaseAuth.getInstance().currentUser?.email
        alarmDTO.uid =FirebaseAuth.getInstance().currentUser?.uid
        alarmDTO.kind =1
        alarmDTO.timestamp =System.currentTimeMillis()
        alarmDTO.message =message
        FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)
    }
    //어댑터 작성
    inner class CommentRecyclerviewAdapter :RecyclerView.Adapter<RecyclerView.ViewHolder>(){

        var comments :ArrayList<ContentDTO.Comment> = arrayListOf()
        init {
            //DB 읽어오기
            FirebaseFirestore.getInstance()
                .collection("images")
                .document(contentUid!!)
                .collection("comments")
                .orderBy("timestamp")
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    //값 읽어오기
                    comments.clear()
                    if (querySnapshot ==null)
                        return@addSnapshotListener

                    //스냅샷 읽어오기 (ContentDTO.COmment로 캐스팅
                    for (snapshot in querySnapshot.documents!!){
                        comments.add(snapshot.toObject(ContentDTO.Comment ::class.java)!!)
                    }
                    //새로고침
                    notifyDataSetChanged()
                }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            //리사이클러뷰 아이템에서 쓸 레이아웃 불러오기
            var view =LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
            return CustomViewHolder(view)
        }
        private inner class CustomViewHolder(view :View) :RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return comments.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            //서버에서 넘어온 메시지와 아이디 맵핑
            var view =holder.itemView
            view.commentviewitem_textview_comment.text =comments[position].comment
            view.commentviewitem_textview_profile.text =comments[position].userId

            //프로필 사진 맵핑
            FirebaseFirestore.getInstance()
                .collection("profileImages")
                    //코맨트를 단 프로필 사진의 주소가 넘어옴
                .document(comments[position].uid!!)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful){
                        var url =task.result!!["image"]
                        Glide.with(holder.itemView.context)
                            .load(url)
                            .apply(RequestOptions().circleCrop())
                            .into(view.commentviewitem_imageview_profile)
                    }
                }

        }

    }
}
