package com.zakia.idn.ojolapp.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.zakia.idn.ojolapp.R
import com.zakia.idn.ojolapp.model.Users
import com.zakia.idn.ojolapp.utils.Constan
import kotlinx.android.synthetic.main.activity_register.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.startActivity

class RegisterActivity : AppCompatActivity() {
    private var auth : FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        btn_signup.onClick {
            if (et_signup_email.text.isNotEmpty()&&
                    et_signup_name.text.isNotEmpty()&&
                    et_signup_hp.text.isNotEmpty()&&
                    et_signup_password.text.isNotEmpty()&&
                    et_signup_confirm_password.text.isNotEmpty()
            ) {
                authUserSignUp (
                    et_signup_email.text.toString(),
                    et_signup_password.text.toString()
                )
            }
        }
    }

    private fun authUserSignUp(email: String, pass: String): Boolean? {
        auth = FirebaseAuth.getInstance()

        var status : Boolean? = null
        var TAG = "tag"

        auth?.createUserWithEmailAndPassword(email,pass)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                if (insertUser(
                        et_signup_name.text.toString(),
                        et_signup_email.text.toString(),
                        et_signup_hp.text.toString(),
                        task.result?.user!!
                    )) {
                    startActivity<LoginActivity>()
                }
            }else{
                status = false
            }
        }
        return status
    }

    private fun insertUser(name: String, email: String, hp: String, users: FirebaseUser): Boolean {
        var user = Users()
        user.uid = user.uid
        user.name = user.name
        user.email = user.email
        user.hp = user.hp

        val database = FirebaseDatabase.getInstance()
        val key = database.reference.push().key
        val myRef = database.getReference(Constan.tb_uaser)
        myRef.child(key!!).setValue(user)

        return true

    }
}