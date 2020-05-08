package com.lotus.lotustargram.navigation

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.lotus.lotustargram.R
import com.lotus.lotustargram.navigation.model.AlarmDTO
import com.lotus.lotustargram.navigation.model.ContentDTO
import kotlinx.android.synthetic.main.fragment_detail.view.*
import kotlinx.android.synthetic.main.item_detail.view.*

class detailViewFragment :Fragment(){

    //DB에 접근할 수 있도록 firestore변수 생성
    var firestore :FirebaseFirestore? =null
    //uid정보를 글로벌 변수로 올림
    var uid :String? =null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        var view =LayoutInflater.from(activity).inflate(R.layout.fragment_detail,container,false)
        //여기서 초기화
        firestore = FirebaseFirestore.getInstance()
        uid =FirebaseAuth.getInstance().currentUser?.uid

        view.detailviewfragment_recyclerview.adapter =DetailViewRecyclerViewAdapter()
        //화면을 세로로 배치하기 위하여 (activity로 context를 넘기기
        //역순 배치를 위해 주석 처리
//        view.detailviewfragment_recyclerview.layoutManager =LinearLayoutManager(activity)
        val lll =LinearLayoutManager(activity)
        lll.stackFromEnd =true
        view.detailviewfragment_recyclerview.layoutManager =lll
        return view
    }

    inner class DetailViewRecyclerViewAdapter :RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var contentDTOs :ArrayList<ContentDTO> = arrayListOf()
        var contentUidList :ArrayList<String> = arrayListOf()
        //생성자
        init {
            //DB에 접근해서 데이터를 받아오는 쿼리 생성
            //시간순으로 받는 코드(orderBy,
            firestore?.collection("images")?.orderBy("timestamp")?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                contentDTOs.clear()
                contentUidList.clear()
                if (querySnapshot ==null){
                    //없을 경우 firestore의 스냅샷에서 에러 발생
                    return@addSnapshotListener
                }
                //스냅샷에 넘어오는 데이터들을 하나하나 읽어들임
                for (snapshot in querySnapshot!!.documents){
                    //ContentDTO로 캐스팅후 contentDTOs에 담기
                    var item =snapshot.toObject(ContentDTO::class.java)
                    contentDTOs.add(item!!)
                    contentUidList.add(snapshot.id)
                }
                //값이 새로고침 되도록
                notifyDataSetChanged()
            }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view =LayoutInflater.from(parent.context).inflate(R.layout.item_detail,parent, false)
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        }

        override fun getItemCount(): Int {
            //리사이클러뷰 개수 넘기기
            return contentDTOs.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            //서버에서 넘어온 데이터들을 맵핑 시켜주기
            var viewHolder =(holder as CustomViewHolder).itemView
            //유저 아이디 맵핑
            viewHolder.detailviewitem_profile_textview.text =contentDTOs!![position].userId
            //이미지 맵핑
            Glide.with(holder.itemView.context)
                .load(contentDTOs!![position].imageUrl)
                .into(viewHolder.detailviewitem_imageview_content)
            //설명글(내 맵핑
            viewHolder.detailviewitem_explain_textview.text =contentDTOs!![position].explain
            //좋아요 맵핑
            viewHolder.detailviewitem_favoritecounter_textview.text ="Likes " +contentDTOs!![position].favoriteCount
            //유저 프로필 이미지 맵핑
            Glide.with(holder.itemView.context)
                .load(contentDTOs!![position].imageUrl)
                .into(viewHolder.detailviewitem_profile_image)

            //like버튼에 이벤트 설정
            viewHolder.detailviewitem_favorite_imageview.setOnClickListener {
                favoriteEvent(position)
            }
            //좋아요 카운트와 하트 표시 연동
            if (contentDTOs!![position].favorites.containsKey(uid)){
                //카운트에 내 uid가 포함되어 있을 경우 (좋아요를 클릭함
                viewHolder.detailviewitem_favorite_imageview.setImageResource(R.drawable.ic_favorite_red)
            }
            else{
                viewHolder.detailviewitem_favorite_imageview.setImageResource(R.drawable.ic_favorite_border)
            }
            //프로필 이미지 클릭 시 해당 유저의 페이지로 이동
            viewHolder.detailviewitem_profile_image.setOnClickListener {
                var fragment =UserFragment()
                var bundle =Bundle()
                //해당 유저의 uid와 id를 UserFragment로 넘김
                bundle.putString("destinationUid", contentDTOs[position].uid)
                bundle.putString("userId", contentDTOs[position].userId)
                fragment.arguments =bundle
                activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.main_content, fragment)?.commit()
            }
            //댓글 아이콘 클릭 시
            viewHolder.detailviewitem_comment_imageview.setOnClickListener { v ->
                var intent =Intent(v.context, CommentActivity ::class.java)
                //게시글의 정보를 가지고 해당 게시글의 Comment페이지로 넘김
                intent.putExtra("contentUid", contentUidList[position])
                intent.putExtra("destinationUid", contentDTOs[position].uid)
                startActivity(intent)
            }
        }
        //좋아요 이벤트 구현
        fun favoriteEvent(position: Int){
            //선택한 컨텐츠의 Uid를 받아와서 좋아요를 하는 이벤트
            var tsDoc =firestore?.collection("images")?.document(contentUidList[position])
            //데이터 입력을 위해 transaction 불러오기
            firestore?.runTransaction { transaction ->
                //transaction을 하기 위해 Uid값을 받고 (글로벌 변수로 올림
                // var uid =FirebaseAuth.getInstance().currentUser?.uid

                // transaction의 데이터를 contentDTO로 캐스팅
                var contentDTO =transaction.get(tsDoc!!).toObject(ContentDTO::class.java)

                //좋아요가 이미 활성화되어 있을 경우 구별
                if (contentDTO!!.favorites.containsKey(uid)){
                    //활성화 된것을 취소
                    contentDTO?.favoriteCount =contentDTO?.favoriteCount -1
                    contentDTO?.favorites.remove(uid)
                }
                else{
                    //빈것을 활성화
                    contentDTO?.favoriteCount =contentDTO?.favoriteCount +1
                    contentDTO?.favorites[uid!!] =true
                    favoriteAlarm(contentDTOs[position].uid!!)
                }
                //transaction을 서버로 되돌려 보냄
                transaction.set(tsDoc, contentDTO)
            }
        }
        //좋아요시 알림 이벤트
        fun favoriteAlarm(destinationUid :String){
            //메시지를 보낼 alarmDTO 생성
            var alarmDTO =AlarmDTO()
            alarmDTO.destinationUid =destinationUid
            alarmDTO.usedId =FirebaseAuth.getInstance().currentUser?.email
            alarmDTO.uid =FirebaseAuth.getInstance().currentUser?.uid
            alarmDTO.kind =0
            alarmDTO.timestamp =System.currentTimeMillis()
            FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)
        }
    }
}