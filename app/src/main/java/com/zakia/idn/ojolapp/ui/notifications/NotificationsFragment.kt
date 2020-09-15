package com.zakia.idn.ojolapp.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.zakia.idn.ojolapp.R
import com.zakia.idn.ojolapp.activity.LoginActivity
import com.zakia.idn.ojolapp.model.Users
import com.zakia.idn.ojolapp.utils.Constan
import kotlinx.android.synthetic.main.fragment_notifications.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.startActivity

class NotificationsFragment : Fragment() {

    var auth: FirebaseAuth? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_notifications, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference(Constan.tb_uaser)
        val query = myRef.orderByChild("uid")
            .equalTo(auth?.uid)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                for (issue in snapshot?.children){
                    val data = issue?.getValue(Users::class.java)
                    showProfile(data)
                }
            }

        })
    }

    private fun showProfile(data: Users?) {
        tv_profile_email.text = data?.email
        tv_profile_name.text = data?.name
        tv_profile_hp.text = data?.hp

        btn_profile_signout.onClick {
            auth?.signOut()
            startActivity<LoginActivity>()
        }
    }
}