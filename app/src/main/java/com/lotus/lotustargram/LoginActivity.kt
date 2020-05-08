package com.lotus.lotustargram

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Base64
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*
import java.security.MessageDigest


class LoginActivity : AppCompatActivity() {
    //회원가입 코드
    var auth :FirebaseAuth? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        email_loging_button.setOnClickListener {
            signinAndsignup()

        }

//        login_edit1.setOnClickListener {
//            //아이디 변경하기
//        }

        login_edit2.setOnClickListener {
            //비밀번호 찾기
            var editTextNewPassword = EditText(this)
            //비밀번호를 바꿀 때 ***로 가릴 때 사용 기존엔 1234 다 보임
            //입력된 글자기 자동으로 ***로 바뀜
            editTextNewPassword.transformationMethod = PasswordTransformationMethod.getInstance()

            var alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle("비밀번호 찾기")
            alertDialog.setMessage("비밀번호를 재설정할 이메일을 입력해주세요.")
            alertDialog.setView(editTextNewPassword)
            //                                         변경 버튼을 눌렀을 때 작동하는 코드를 넣으면 됨
            alertDialog.setPositiveButton("확인", {dialog, which ->
                //비밀번호 재설정 하는 fun
                    FirebaseAuth.getInstance().sendPasswordResetEmail(editTextNewPassword.text.toString())
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful)
                                Toast.makeText(this, "메일을 보냈습니.", Toast.LENGTH_SHORT).show()
                            else
                                Toast.makeText(this, task.exception.toString(), Toast.LENGTH_SHORT).show()

                }
            })
            //                                                          dialog를 종료시키는 코드
            //비밀번호를 변경하면 (로그아웃 후)다시 로그인 한 다음에, 비밀번호를 변경해 달라고 함 (보안상의 이유로)

            alertDialog.setNegativeButton("취소", {dialog, which ->  dialog.dismiss()})
            alertDialog.show()


        }
    }

    override fun onStart() {
        super.onStart()
        moveMainPage(auth?.currentUser)
    }

    //일반 이메일 로그인 부분
   fun signinAndsignup(){
        auth?.createUserWithEmailAndPassword(email_edittext.text.toString(), password_edittext.text.toString())
            ?.addOnCompleteListener {
                task ->
                //아이디가 생성되었을 때 작동하는 구문
                if (task.isSuccessful){
                    //아이디가 생성되었을 때 필요한 코드를 입력하는 구문
                    moveMainPage(task.result?.user)
                }
                else if (task.exception?.message.isNullOrEmpty()){
                    //에러 발생시 구문
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                }
                //이미 아이디가 있을 경우 작동하는 구문
                else{
                    signinEmail()
                }
        }
    }
    //로그인 코드
    fun signinEmail(){
        auth?.signInWithEmailAndPassword(email_edittext.text.toString(), password_edittext.text.toString())
            ?.addOnCompleteListener {
                    task ->
                //아이디가 생성되었을 때 작동하는 구문
                if (task.isSuccessful){
                    //아이디와 비밀번호가 일치했을 때 로그인 성공
                    moveMainPage(task.result?.user)
                }
                else{
                    //실패시 구현
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
    }
    //로그인 성공 시 다른 화면으로 전환
    fun moveMainPage(user :FirebaseUser?){
        if (user !=null){
            //유저 상태가 맞을 경우
            //다음 페이지로 이동
            startActivity(Intent(this, MainActivity ::class.java))
            finish()
        }
    }
}
