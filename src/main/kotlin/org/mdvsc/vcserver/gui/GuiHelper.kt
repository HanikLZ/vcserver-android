package org.mdvsc.vcserver.gui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.greenrobot.eventbus.EventBus

/**
 * @author haniklz
 * @since 16/4/6.
 */
object GuiHelper {

    val argumentFragmentName = "fragmentName"
    val argumentFragmentArgs = "fragmentArgs"
    val argumentWithActionBar = "withActionBar"
    val argumentTitle = "title"
    val argumentWorkPath = "workPath"
    val argumentUserItem = "userItem"
    val argumentRepoItem = "repoItem"
    val argumentPermissionId = "permissionId"
    val argumentPermissionInfo = "permissionInfo"
    val argumentGroupItem = "groupItem"

    private val eventBus by lazy { EventBus.builder().apply { sendNoSubscriberEvent(false) }.build() }

    fun startComponentActivity(context: Context, fragmentClazz: Class<*>, args:Bundle? = null, title:CharSequence? = null) = context.startActivity(Intent(context, ComponentActivity::class.java)
            .putExtra(argumentFragmentName, fragmentClazz.name)
            .putExtra(argumentFragmentArgs, args)
            .putExtra(argumentWithActionBar, title != null)
            .putExtra(argumentTitle, title)
        )

    fun postEvent(event:Any, isStick:Boolean = false) = if (isStick) eventBus.postSticky(event) else eventBus.post(event)
    fun registerEventBus(receiver:Any) = eventBus.register(receiver)
    fun unregisterEventBus(receiver: Any) = eventBus.unregister(receiver)
}

