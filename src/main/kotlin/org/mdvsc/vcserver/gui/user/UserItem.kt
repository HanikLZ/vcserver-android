package org.mdvsc.vcserver.gui.user

import org.mdvsc.vcserver.server.model.UserInfo
import java.io.Serializable

/**
 * @author haniklz
 * @since 16/4/6.
 */
class UserItem(var id:String, var info: UserInfo):Serializable {
    companion object {val empty = UserItem("", UserInfo())}
}

