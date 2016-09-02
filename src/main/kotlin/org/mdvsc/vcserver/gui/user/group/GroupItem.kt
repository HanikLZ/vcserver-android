package org.mdvsc.vcserver.gui.user.group

import org.mdvsc.vcserver.server.model.GroupInfo
import java.io.Serializable

/**
 * @author haniklz
 * @since 16/4/8.
 */
class GroupItem(var id:String, var info:GroupInfo):Serializable {
    companion object { fun empty() = GroupItem("", GroupInfo()) }
}