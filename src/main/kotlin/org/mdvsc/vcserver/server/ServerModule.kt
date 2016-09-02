package org.mdvsc.vcserver.server

import android.content.res.Resources
import dagger.Module
import dagger.Provides
import org.apache.sshd.common.Factory
import org.apache.sshd.common.KeyPairProvider
import org.apache.sshd.common.NamedFactory
import org.apache.sshd.common.Random
import org.apache.sshd.common.file.FileSystemFactory
import org.apache.sshd.common.random.JceRandom
import org.apache.sshd.common.random.SingletonRandomFactory
import org.apache.sshd.server.Command
import org.apache.sshd.server.CommandFactory
import org.apache.sshd.server.PasswordAuthenticator
import org.apache.sshd.server.PublickeyAuthenticator
import org.mdvsc.vcserver.ServiceScope
import org.mdvsc.vcserver.server.ssh.*

/**
 * @author haniklz
 * *
 * @since 16/3/28.
 */
@Module
class ServerModule {
    @ServiceScope @Provides fun serverPathProvider(serverPreference: ServerPreference): PathProvider = serverPreference
    @ServiceScope @Provides fun shellFactory(resources: Resources, pathProvider: PathProvider): Factory<Command> = ServerShellFactory(resources, pathProvider)
    @ServiceScope @Provides fun commandFactory(pathProvider: PathProvider): CommandFactory = ServerCommandFactory(pathProvider)
    @ServiceScope @Provides fun fileSystemFactory(pathProvider: PathProvider): FileSystemFactory = ServerFileSystemFactory(pathProvider)
    @ServiceScope @Provides fun keyPairProvider(pathProvider: PathProvider): KeyPairProvider = ServerKeyPairProvider(pathProvider)
    @ServiceScope @Provides fun publicKeyAuthenticator(pathProvider: PathProvider): PublickeyAuthenticator = ServerPublicKeyAuthenticator(pathProvider)
    @ServiceScope @Provides fun passwordAuthenticator(pathProvider: PathProvider): PasswordAuthenticator = ServerPasswordAuthenticator(pathProvider)
    @ServiceScope @Provides fun randomFactory(): NamedFactory<Random> = SingletonRandomFactory(JceRandom.Factory())
}

