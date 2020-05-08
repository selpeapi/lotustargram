package com.lotus.lotustargram.navigation

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.lotus.lotustargram.LoginActivity
import com.lotus.lotustargram.MainActivity
import com.lotus.lotustargram.R
import com.lotus.lotustargram.navigation.model.AlarmDTO
import com.lotus.lotustargram.navigation.model.ContentDTO
import com.lotus.lotustargram.navigation.model.FollowDTO
import kotlinx.android.synthetic.main.activity_main.*
//xml파일을 현 파일에서 수정을 위해 참조
import kotlinx.android.synthetic.main.fragment_user.view.*
class UserFragment :Fragment(){

    var fragmentView :View? =null
    var firestore :FirebaseFirestore? =null
    var uid :String? =null
    var auth :FirebaseAuth? =null

    var currentUserUid :String? =null

    companion object{
        //static 개념
        var PICK_PROFILE_FROM_ALBUM =10

    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        fragmentView =LayoutInflater.from(activity).inflate(R.layout.fragment_user,container,false)
        //이전 화면에서 넘어온 값 받아오기
        uid =arguments?.getString("destinationUid")

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        currentUserUid =auth?.currentUser?.uid
        if (currentUserUid ==uid){
            //나의 아이디(나의 페이지 My_userFragment
            fragmentView?.account_btn_follow_signout?.text =getString(R.string.signout)

            fragmentView?.account_btn_follow_signout?.setOnClickListener {
                activity?.finish()
                startActivity(Intent(activity, LoginActivity ::class.java))
                auth?.signOut()
            }
        }
        else{
            //상대방의 페이지
            //나의 페이지와 달리 보이게 설정
            fragmentView?.account_btn_follow_signout?.text =getString(R.string.follow)
            var mainactivity =(activity as MainActivity)
            mainactivity?.toolbar_username?.text =arguments?.getString("userId")

            mainactivity?.toolbar_btn_back?.setOnClickListener {
                mainactivity.bottom_navigation.selectedItemId =R.id.action_home
            }
            mainactivity?.toolbar_title_image?.visibility =View.GONE
            mainactivity?.toolbar_username?.visibility =View.VISIBLE
            mainactivity?.toolbar_btn_back?.visibility =View.VISIBLE

            fragmentView?.account_btn_follow_signout?.setOnClickListener {
                requestFollow()
            }
        }

        //리사이클러뷰에 연결
        fragmentView?.account_recyclerview?.adapter =UserFragmentRecyclerViewAdapter()
        fragmentView?.account_recyclerview?.layoutManager =GridLayoutManager(activity, 3)

        //프로필 이미지 선택
        fragmentView?.account_iv_profile?.setOnClickListener {
            var photoPickerIntent =Intent(Intent.ACTION_PICK)
            photoPickerIntent.type ="image/*"
            activity?.startActivityForResult(photoPickerIntent, PICK_PROFILE_FROM_ALBUM)
        }
        getProfileImages()
        getFollowerAndFollowing()
        return fragmentView

    }


    fun followerAlarm(destinationUid :String){
        var alarmDTO =AlarmDTO()
        alarmDTO.destinationUid =destinationUid
        alarmDTO.usedId =auth?.currentUser?.email
        alarmDTO.uid =auth?.currentUser?.uid
        alarmDTO.kind =2
        alarmDTO.timestamp =System.currentTimeMillis()
        //DB에 저장
        FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)
    }
    fun getFollowerAndFollowing(){
        //내페이지에서 클릭했을 경우 내 uid가 다른 사람의 페이지였으면 상대방의 uid가 들어옴
        firestore?.collection("users")?.document(uid!!)?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            if (documentSnapshot ==null)
                return@addSnapshotListener
            var followDTO =documentSnapshot.toObject(FollowDTO ::class.java)
            if (followDTO?.followingCount !=null){
                fragmentView?.account_tv_following_count?.text =followDTO?.followingCount?.toString()
            }
            if (followDTO?.followerCount !=null){
                fragmentView?.account_tv_follower_count?.text =followDTO?.followerCount?.toString()
                //이미 팔로워를 하고 있을 경우 변환되는 코드
                    //나의 uid가 있을 경우
                if (followDTO?.followers?.containsKey(currentUserUid!!)){
                    fragmentView?.account_btn_follow_signout?.text =getString(R.string.follow_cancel)
                    fragmentView?.account_btn_follow_signout?.background?.setColorFilter(ContextCompat.getColor(activity!!, R.color.colorLightGray),PorterDuff.Mode.MULTIPLY)
                }
                    //나의 uid가 없을 경우
                else{
                    //프로그램 안정성
                    if (uid !=currentUserUid){
                        fragmentView?.account_btn_follow_signout?.text =getString(R.string.follow)
                        //상대방 유저 페이지(프래그먼트 일때 background color 변경
                        fragmentView?.account_btn_follow_signout?.background?.colorFilter =null
                    }
                }
            }
        }
    }
    fun requestFollow(){
        //나의 계정에서 상대방 누구를 팔로워 하는지
        var tsDocFollowing =firestore?.collection("users")?.document(currentUserUid!!)
        firestore?.runTransaction { transaction ->
            //DB에 넣기
            var followDTO =transaction.get(tsDocFollowing!!).toObject(FollowDTO ::class.java)
            if (followDTO ==null){
                //값이 없을 때
                followDTO = FollowDTO()
                followDTO!!.followingCount =1
                //상대방 uid
                followDTO!!.followers[uid!!] =true

                transaction.set(tsDocFollowing, followDTO)
                return@runTransaction
            }
            //내가 팔로워를 한 상태 (상대방 키가 있을 경우
            if (followDTO.followings.containsKey(uid)){
                //팔로잉을 취소
                followDTO?.followingCount =followDTO?.followingCount -1
                //팔로워 취소를 위해 상대방 uid를 제거
                followDTO?.followings?.remove(uid)
            }
            //내가 팔로워를 하지 않은 상태
            else{
                //팔로잉을 함
                followDTO?.followingCount =followDTO?.followingCount +1
                followDTO?.followings[uid!!] =true
            }
            //DB로 저장
            transaction.set(tsDocFollowing, followDTO)
            //return으로 transaction 받기
            return@runTransaction
        }
        //상대방 계정에서 또 다른 타인이 팔로워 하는지
        //내가 팔로잉을 할 상대방 계정에 접근하는 코드
        var tsDocFollower =firestore?.collection("users")?.document(uid!!)
        firestore?.runTransaction { transaction ->
            var followDTO =transaction.get(tsDocFollower!!).toObject(FollowDTO ::class.java)
            //값이 없을 경우
            if (followDTO ==null){
                followDTO = FollowDTO()
                followDTO!!.followerCount =1
                //상대방 계정에 나의 uid를 보냄
                followDTO!!.followers[currentUserUid!!] =true
                followerAlarm(uid!!)
                //DB에 값 넣기
                transaction.set(tsDocFollower, followDTO!!)
                return@runTransaction
            }
            //상대방 계정에 팔로우를 했을 경우
            if (followDTO!!.followers.containsKey(currentUserUid)){
                //팔로우를 취소
                followDTO!!.followerCount =followDTO!!.followerCount -1
                //followers의 나의 uid 제거
                followDTO!!.followers.remove(currentUserUid!!)
            }
            else{
                //팔로우를 하지 않았을 경우
                followDTO!!.followerCount =followDTO!!.followerCount +1
                //나의 uid 추
                followDTO!!.followers[currentUserUid!!] =true
                followerAlarm(uid!!)
            }
            //DB에 값 저장
            transaction.set(tsDocFollower, followDTO!!)
            return@runTransaction

        }
    }
    //DB에서 프로필 이미지 받아오기
    fun getProfileImages(){
        firestore?.collection("profileImages")?.document(uid!!)?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            if (documentSnapshot ==null) //코드의 안정성
                return@addSnapshotListener
            if (documentSnapshot.data !=null){
                var url =documentSnapshot?.data!!["image"]
                Glide.with(activity!!).load(url).apply(RequestOptions().circleCrop()).into(fragmentView?.account_iv_profile!!)
            }
        }
    }
    //리사이클러뷰가 사용할 어댑터
    inner class UserFragmentRecyclerViewAdapter :RecyclerView.Adapter<RecyclerView.ViewHolder>(){

        var contentDTOs :ArrayList<ContentDTO> = arrayListOf()
        init {
            //생성자 생성후 DB값 읽어오기
                //내가 올린 이미지만 볼 수 있도록 설정
            firestore?.collection("images")?.whereEqualTo("uid", uid)?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (querySnapshot ==null){
                    //프로그램 안정성을 위하여 null일 경우 바로 종료
                    return@addSnapshotListener
                }
                    //null이 아닐 경우 for문에 값 담기
                //DB 받기
                for (snapshot in querySnapshot.documents){
                    contentDTOs.add(snapshot.toObject(ContentDTO ::class.java)!!)
                }
                fragmentView?.account_tv_post_count?.text =contentDTOs.size.toString()
                //데이터 교체(새로고침
                notifyDataSetChanged()
            }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            //화면의 폭 가져오기 (바로 뷰를 불러오는 것이 아님
            var width =resources.displayMetrics.widthPixels /3
            //화면 폭의 1/3값 가져오기
            //Imageview에 넣게 되면 폭의 1/3크기의 정사각형 이미지 생성됨

            var imageView =ImageView(parent.context)
            imageView.layoutParams =LinearLayoutCompat.LayoutParams(width, width)
            return CustomViewHolder(imageView)
        }

        inner class CustomViewHolder(var imageView: ImageView) : RecyclerView.ViewHolder(imageView) {

        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var imageView =(holder as CustomViewHolder).imageView
            Glide.with(holder.imageView.context).load(contentDTOs[position].imageUrl).apply(RequestOptions().centerCrop()).into(imageView)
        }

    }
}