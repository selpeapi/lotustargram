package com.lotus.lotustargram.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.lotus.lotustargram.R
import com.lotus.lotustargram.navigation.model.ContentDTO
import kotlinx.android.synthetic.main.activity_add_photo.*
import java.text.SimpleDateFormat
import java.util.*

class AddPhotoActivity : AppCompatActivity() {

    var PICK_IMAGE_FROM_ALBUM =0
    var storage :FirebaseStorage? =null
    var photoUri :Uri? =null

    //유저의 정보를 가져오는
    var auth :FirebaseAuth? =null
    //데이터베이스 사용을 위해
    var firestore :FirebaseFirestore? =null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_photo)

        //스토리지,auth,firestore 초기화
        storage = FirebaseStorage.getInstance()

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        //액티비티를 실행하고 화면이 열리게
        var photoPickerIntent =Intent(Intent.ACTION_PICK)
        photoPickerIntent.type ="image/*"
        startActivityForResult(photoPickerIntent, PICK_IMAGE_FROM_ALBUM)

        //버튼에 이벤트 적용
        addphoto_btn_upload.setOnClickListener {
            contentUpload()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode ==PICK_IMAGE_FROM_ALBUM){
            if (resultCode ==Activity.RESULT_OK){
                //사진을 선택했을 때의 이미지의 경로
                photoUri =data?.data
                addphoto_image.setImageURI(photoUri)
            }
            else{
                //취소를 했을 때 작동하는 코드
                finish()
            }
        }
    }

    fun contentUpload(){
        //파일이름 만들기
        //이름이 중복 생성되지 않도록 변수 설정
        var timestamp =SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imageFileName ="IMAGE_" +timestamp +"_.png"

        //스토리지 래퍼런스를 만들어서 이미지를 업로드 (폴더명. 파일명
        var storageRef =storage?.reference?.child("images")?.child(imageFileName)

        //데이터베이스 입력 코드

        //업로드 (promise {구글 권장 방식
        storageRef?.putFile(photoUri!!)?.continueWithTask { task: Task<UploadTask.TaskSnapshot> ->
            return@continueWithTask  storageRef.downloadUrl
        }?.addOnSuccessListener { uri ->
            //이미지 주소를 받고 데이터 모델을 만들고
            var contentDTO =ContentDTO()
            //downloadUrl을 contentDTO의 imageUrl에 넣기
            contentDTO.imageUrl =uri.toString()

            //유저의 uid 넣기
            contentDTO.uid =auth?.currentUser?.uid

            //유저 아이디 넣기
            contentDTO.userId =auth?.currentUser?.email

            //설명 넣기
            contentDTO.explain =addphoto_edit_explain.text.toString()

            //시간 넣기
            contentDTO.timestamp =System.currentTimeMillis()

            //images 컬렉션 안에 데이터 입력
            firestore?.collection("images")?.document()?.set(contentDTO)

            //정상적으로 닫혔다고 값을 넘겨주기 위해 RESULT_OK 사용

            setResult(Activity.RESULT_OK)

            finish()
        }

//        //업로드 (Callback 방식
//        storageRef?.putFile(photoUri!!)?.addOnSuccessListener {
//            Toast.makeText(this,"이미지 업로드에 성공했습니다.",Toast.LENGTH_SHORT).show()
//            //이미지 업로드 완료시에 이미지 주소를 받아오는
//            storageRef.downloadUrl.addOnSuccessListener { uri ->
//                //이미지 주소를 받고 데이터 모델을 만들고
//                var contentDTO =ContentDTO()
//                //downloadUrl을 contentDTO의 imageUrl에 넣기
//                contentDTO.imageUrl =uri.toString()
//
//                //유저에 uid 넣기
//                contentDTO.uid =auth?.currentUser?.uid
//
//                //유저 아이디 넣기
//                contentDTO.userId =auth?.currentUser?.email
//
//                //설명 넣기
//                contentDTO.explain =addphoto_edit_explain.text.toString()
//
//                //시간 넣기
//                contentDTO.timestamp =System.currentTimeMillis()
//
//                //images 컬렉션 안에 데이터 입력
//                firestore?.collection("images")?.document()?.set(contentDTO)
//
//                //정상적으로 닫혔다고 값을 넘겨주기 위해 RESULT_OK 사용
//
//                setResult(Activity.RESULT_OK)
//
//                finish()
//            }
//        }


    }
}
