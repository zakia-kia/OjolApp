package com.zakia.idn.ojolapp.ui.dashboard.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zakia.idn.ojolapp.R
import com.zakia.idn.ojolapp.model.Booking
import kotlinx.android.synthetic.main.history_item.view.*

class HistoryAdapter(private val mValues: List<Booking>
): RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.history_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = mValues.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]

        holder.mAwal.text = item.lokasiAwal
        holder.mTanggal.text = item.tanggal
        holder.mTujuan.text = item.lokasiTujuan
    }

    //menginisialisasi
    inner class ViewHolder(mView : View): RecyclerView.ViewHolder(mView) {
        var mAwal: TextView = mView.tv_item_awal
        var mTujuan: TextView = mView.tv_item_tujuan
        var mTanggal: TextView = mView.tv_item_tanggal

    }
}