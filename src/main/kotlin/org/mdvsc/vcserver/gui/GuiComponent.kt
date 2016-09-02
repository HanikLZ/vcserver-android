package org.mdvsc.vcserver.gui

import dagger.Subcomponent
import org.mdvsc.vcserver.ActivityScope
import org.mdvsc.vcserver.gui.repo.RepoListAdapter
import org.mdvsc.vcserver.gui.user.UserListAdapter
import org.mdvsc.vcserver.gui.user.group.GroupListAdapter

/**
 * @author haniklz
 * *
 * @since 16/3/28.
 */
@Subcomponent(modules = arrayOf(GuiModule::class))
@ActivityScope
interface GuiComponent {
    fun inject(model: EntryViewModel): EntryViewModel
    fun inject(adapter: RepoListAdapter): RepoListAdapter
    fun inject(adapter: UserListAdapter): UserListAdapter
    fun inject(adapter: GroupListAdapter): GroupListAdapter
}

