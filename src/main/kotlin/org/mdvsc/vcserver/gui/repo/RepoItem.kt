package org.mdvsc.vcserver.gui.repo

import org.mdvsc.vcserver.server.model.RepoInfo
import java.io.Serializable

/**
 * @author haniklz
 * @since 16/4/6.
 */
class RepoItem(var path:String, var info: RepoInfo):Serializable {
    companion object { fun empty() = RepoItem("", RepoInfo()) }
}

