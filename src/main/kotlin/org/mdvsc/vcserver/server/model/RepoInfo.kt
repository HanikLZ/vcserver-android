package org.mdvsc.vcserver.server.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 *
 * @author  haniklz
 * @since   16/4/2
 * @version 1.0.0
 */
class RepoInfo : Serializable {
    @SerializedName("name") var name:String? = null
    @SerializedName("description") var description:String? = null
    @SerializedName("permissions") var permissions:Array<PermissionInfo>? = null
}

