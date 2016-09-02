package org.mdvsc.vcserver.server.ssh

import android.util.Base64
import org.apache.sshd.server.PasswordAuthenticator
import org.apache.sshd.server.PublickeyAuthenticator
import org.apache.sshd.server.session.ServerSession
import org.mdvsc.vcserver.server.PathProvider
import org.mdvsc.vcserver.server.ServerHelper
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.math.BigInteger
import java.security.KeyFactory
import java.security.PublicKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.RSAPublicKeySpec

/**
 * @author haniklz
 * @since 16/3/25.
 */
class ServerPasswordAuthenticator(val pathProvider: PathProvider) : PasswordAuthenticator {

    private var sessionId:Long = 0
    private var sessionPassword:String? = null
    private var sessionPasswordEnable = false

    override fun authenticate(username: String?, password: String?, session: ServerSession?): Boolean {
        if (username != null) {
            val id = session?.id
            if (id != sessionId && id != null) {
                sessionId = id
                val user = ServerHelper.getUserInfo(pathProvider, username)
                sessionPasswordEnable = user?.enablePasswordAuthentication ?: false
                sessionPassword = user?.password
            }
            return sessionPasswordEnable && ServerHelper.stringDigest(password) == sessionPassword
        }
        return false
    }
}

class ServerPublicKeyAuthenticator(val pathProvider: PathProvider) : PublickeyAuthenticator {

    private var sessionId:Long = 0
    private var sessionKeyModulus = BigInteger.ZERO

    override fun authenticate(username: String?, key: PublicKey?, session: ServerSession?) = if (key is RSAPublicKey) {
        val id = session?.id
        if (id != sessionId && id != null) {
            sessionId = id
            val ssh = pathProvider.getPathFile(PathProvider.Folder.SSH, username + ".pub")
            if (ssh.exists() && ssh.isFile && ssh.canRead()) {
                val loadKey = RSAKeyDecoder.decodePublicKey(ssh.readText())
                if (loadKey != null) {
                    sessionKeyModulus = loadKey.modulus
                } else {
                    sessionKeyModulus = BigInteger.ZERO
                }
            } else {
                sessionKeyModulus = BigInteger.ZERO
            }
        }
        sessionKeyModulus != BigInteger.ZERO && sessionKeyModulus == key.modulus
    } else false
}

object RSAKeyDecoder {

    private val keyType = "ssh-rsa"
    private val key = "RSA"

    fun DataInputStream.readBytes(len: Int) = ByteArray(len).apply { read(this) }
    fun DataInputStream.readString() = String(readBytes(readInt()))
    fun DataInputStream.readBigInt() = BigInteger(readBytes(readInt()))

    private fun decodePublicKeyInner(part: String): RSAPublicKey {
        val dataInputStream = DataInputStream(ByteArrayInputStream(Base64.decode(part.toByteArray(), Base64.DEFAULT)));
        val type = dataInputStream.readString()
        if (type == keyType) {
            val e = dataInputStream.readBigInt()
            val m = dataInputStream.readBigInt()
            return KeyFactory.getInstance(key).generatePublic(RSAPublicKeySpec(m, e)) as RSAPublicKey
        } else {
            throw IllegalArgumentException("Unknown type: $type")
        }
    }

    fun decodePublicKey(keyString: String): RSAPublicKey? {
        for (part in keyString.split(' ').filter { it.startsWith("AAAA") }) {
            try {return decodePublicKeyInner(part)} catch (ignored: Exception) {ignored.printStackTrace()}
        }
        return null
    }
}

