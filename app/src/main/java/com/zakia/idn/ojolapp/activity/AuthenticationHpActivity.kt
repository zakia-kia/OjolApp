package com.zakia.idn.ojolapp.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.database.FirebaseDatabase
import com.zakia.idn.ojolapp.R
import com.zakia.idn.ojolapp.utils.Constan
import kotlinx.android.synthetic.main.activity_authentication_hp.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

class AuthenticationHpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication_hp)

        val key = intent.getStringExtra(Constan.key)
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference(Constan.tb_uaser)

        //update realtime database
        tv_authentication_submit.onClick {
            if (et_authentication_hp.text.toString().isNotEmpty()){
                myRef.child(key!!).child("hp")
                    .setValue(et_authentication_hp.text.toString())
                startActivity<MainActivity>()
            }

            else toast("tidak boleh kosong")
        }
    }
}