package org.mdvsc.vcserver.server.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 *
 * @author  haniklz
 * @since   16/4/2
 * @version 1.0.0
 */
class PermissionInfo : Serializable {
    @SerializedName("branch") var branch:String? = null
    @SerializedName("rule") var rule:String? = null
    @SerializedName("users") var users:Array<String>? = null
    @SerializedName("groups") var groups:Array<String>? = null
    @SerializedName("excludeUsers") var excludeUsers:Array<String>? = null
}

