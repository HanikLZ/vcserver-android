package org.mdvsc.vcserver.gui.repo

import org.mdvsc.vcserver.server.model.PermissionInfo

/**
 *
 * @author  haniklz
 * @since   16/4/11
 * @version 1.0.0
 */
class RemoveRepoEvent(val path:String)
class AddRepoEvent(val item: RepoItem)
class ModifyRepoEvent(val item: RepoItem)
class RemovePermissionEvent(val id: String)
class AddPermissionEvent(val id: String, val info:PermissionInfo)
class ModifyPermissionEvent(val id:String, val info:PermissionInfo)
