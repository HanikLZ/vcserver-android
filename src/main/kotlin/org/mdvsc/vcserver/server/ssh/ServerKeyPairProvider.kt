package org.mdvsc.vcserver.server.ssh

import org.apache.sshd.common.keyprovider.AbstractKeyPairProvider
import org.apache.sshd.common.util.SecurityUtils
import org.mdvsc.vcserver.server.PathProvider
import org.mdvsc.vcserver.util.FileUtils
import java.io.*
import java.security.KeyPair
import java.util.*

/**
 * @author haniklz
 * @since 16/3/20.
 */
class ServerKeyPairProvider(val pathProvider: PathProvider) : AbstractKeyPairProvider() {

    private val hostKeyMap = mapOf("ECDSA" to "host.ecdsa", "RSA" to "host.rsa", "DSA" to "host.dsa")

    private fun getKeyPair(hostKeyFile:File, key:String) = try {
        FileInputStream(hostKeyFile).use {
            ObjectInputStream(it).readObject() as KeyPair
        }
    } catch (ignored : Exception) {null} ?: try {
        val newKeyPair = SecurityUtils.getKeyPairGenerator(key).generateKeyPair()
        FileOutputStream(FileUtils.makeSureNewFile(hostKeyFile)).use { ObjectOutputStream(it).writeObject(newKeyPair) }
        newKeyPair
    } catch (ignored : Exception) {null}

    override fun loadKeys(): MutableIterable<KeyPair>? {
        val folder = pathProvider.getPathFile(PathProvider.Folder.KEY)
        return ArrayList<KeyPair>(hostKeyMap.size).apply {
            hostKeyMap.forEach { getKeyPair(File(folder, it.value), it.key)?.run { add(this) } }
        }
    }
}

