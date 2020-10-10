package com.cyq.jetpack.event

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.Toast

/**
 *    @author : ChenYangQi
 *    date   : 2020/10/10 14:41
 *    desc   :
 */
class EventHandleListener(private var context: Context) {
    fun onButtonClicked(view: View) {
        Toast.makeText(context, "click:" + (view as Button).text, Toast.LENGTH_SHORT).show()
    }

    fun openActivity(view: View, activity: Class<Activity>) {
        context.startActivity(Intent(context, activity))
    }
}

