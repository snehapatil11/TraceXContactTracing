package com.example.tracexcontacttracing.Notification

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tracexcontacttracing.R

class NotificationRVAdapter:RecyclerView.Adapter<ListElement> {

    var arNotification: ArrayList<String>?= null
    var context:Context?= null
    constructor(arNotification:ArrayList<String>, context:Context){
        this.arNotification = arNotification
        this.context=context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListElement {
        val root: View = LayoutInflater.from(context)!!.inflate(R.layout.notification_list_item, parent, false)
        var listElement:ListElement=ListElement(root)
        return listElement
    }

    override fun getItemCount(): Int {
        return this.arNotification!!.size

    }

    override fun onBindViewHolder(holder: ListElement, position: Int) {
        holder.bindData(this.arNotification!!.get(position))

    }
}

class ListElement(v: View) : RecyclerView.ViewHolder(v) {
    private var view: View = v

    fun bindData(data: String){
        val tv_notification_list:TextView=view.findViewById(R.id.tv_notification_list)
        tv_notification_list.setText(data)
        }
    }