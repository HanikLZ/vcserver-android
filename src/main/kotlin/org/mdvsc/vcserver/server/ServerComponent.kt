package org.mdvsc.vcserver.server

import dagger.Subcomponent
import org.apache.sshd.common.Factory
import org.apache.sshd.common.KeyPairProvider
import org.apache.sshd.common.NamedFactory
import org.apache.sshd.common.Random
import org.apache.sshd.common.file.FileSystemFactory
import org.apache.sshd.server.Command
import org.apache.sshd.server.CommandFactory
import org.apache.sshd.server.PasswordAuthenticator
import org.apache.sshd.server.PublickeyAuthenticator
import org.mdvsc.vcserver.ServiceScope

/**
 * @author haniklz
 * *
 * @since 16/3/28.
 */
@Subcomponent(modules = arrayOf(ServerModule::class))
@ServiceScope
interface ServerComponent {
    fun getServerPathProvider(): PathProvider
    fun getShellFactory(): Factory<Command>
    fun getCommandFactory(): CommandFactory
    fun getFileSystemFactory(): FileSystemFactory
    fun getKeyPairProvider(): KeyPairProvider
    fun getPublicKeyAuthenticator(): PublickeyAuthenticator
    fun getPasswordAuthenticator(): PasswordAuthenticator
    fun getRandomFactory(): NamedFactory<Random>
}

