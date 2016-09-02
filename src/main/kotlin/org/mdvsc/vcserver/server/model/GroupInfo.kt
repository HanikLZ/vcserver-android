package org.mdvsc.vcserver.server.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 *
 * @author  haniklz
 * @since   16/4/2
 * @version 1.0.0
 */
class GroupInfo : Serializable {
    @SerializedName("name") var name:String? = null
    @SerializedName("description") var description:String? = null
    @SerializedName("users") var users:Array<String>? = null
}

