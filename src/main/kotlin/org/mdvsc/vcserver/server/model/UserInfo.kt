package org.mdvsc.vcserver.server.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 *
 * @author  haniklz
 * @since   16/4/2
 * @version 1.0.0
 */
class UserInfo : Serializable {
    @SerializedName("name") var name:String? = null
    @SerializedName("email") var email:String? = null
    @SerializedName("description") var description:String? = null
    @SerializedName("password") var password:String? = null
    @SerializedName("enablePasswordAuthentication") var enablePasswordAuthentication = false
    @SerializedName("isAdmin") var isAdmin = false
}

