package com.ajl.xiaoan

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class HistoryAdapter(val data:List<HistoryBean>,val context:Context) :BaseAdapter(){
    override fun getCount(): Int {
        return data.size
    }

    override fun getItem(index: Int): Any {
        return data[index]
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getView(postion: Int, p1: View?, p2: ViewGroup?): View {
        val view = LayoutInflater.from(context).inflate(R.layout.history_item, p2, false)
        view.findViewById<TextView>(R.id.tv_start).text=data[postion].startDate.toString()
        view.findViewById<TextView>(R.id.tv_day).text=if (data[postion].day==-1) "未结束" else data[postion].day.toString()
        view.findViewById<TextView>(R.id.tv_interval).text=data[postion].interval.toString()
        return view
    }
}