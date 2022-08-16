package com.example.todolistapp.fragments

import android.app.Application
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.todolistapp.R
import com.example.todolistapp.data.models.Priority
import com.example.todolistapp.data.models.ToDoData

class SharedViewModel(application: Application) :AndroidViewModel(application) {


    //.............................List Fragment..........................//

    val emptyDatabase:MutableLiveData<Boolean> = MutableLiveData(false)

    fun  checkIfDatabaseEmpty(toDoData:List<ToDoData>){
        emptyDatabase.value= toDoData.isEmpty()

    }

    //..............................Add/UPDATEFragment.........................//


    val listener: AdapterView.OnItemSelectedListener=object :
    AdapterView.OnItemSelectedListener{
        override fun onNothingSelected(parent: AdapterView<*>?) {}
        override fun onItemSelected(
            parent: AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long
        ) {

            when(position){
                0->{(parent?.getChildAt(0)as TextView).setLinkTextColor(ContextCompat.getColor(application,R.color.red)) }
                1->{(parent?.getChildAt(0)as TextView).setLinkTextColor(ContextCompat.getColor(application, R.color.blue)) }
                2->{(parent?.getChildAt(0)as TextView).setLinkTextColor(ContextCompat.getColor(application, R.color.green)) }
            }
        }
    }


     fun verifyDataFromUser(title:String, description:String):Boolean{
        return !(title.isEmpty()||description.isEmpty())

    }

     fun parsePriority(priority:String): Priority {
        return when(priority){
            "High Priority"->{
                Priority.HIGH}
            "Medium Priority"->{
                Priority.MEDIUM}
            "Low Priority"->{
                Priority.LOW}
            else -> Priority.LOW
        }
    }
}