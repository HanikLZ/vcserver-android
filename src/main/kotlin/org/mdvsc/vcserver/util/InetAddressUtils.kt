package org.mdvsc.vcserver.util

import java.net.NetworkInterface
import java.util.regex.Pattern

/**
 * @author haniklz
 * @since 16/4/5.
 */
object InetAddressUtils {

    private val ipv4Pattern = Pattern.compile("^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$")
    private val ipv6StdPattern = Pattern.compile("^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$")
    private val ipv6HexCompressedPattern = Pattern.compile("^((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)::((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)$")

    fun isIPv4Address(input:String) = ipv4Pattern.matcher(input).matches();
    fun isIPv6StdAddress(input:String) = ipv6StdPattern.matcher(input).matches();
    fun isIPv6HexCompressedAddress(input:String) = ipv6HexCompressedPattern.matcher(input).matches();

    fun getLocalV4Address():String {
        var net = NetworkInterface.getNetworkInterfaces()
        while (net.hasMoreElements()) {
            var addresessEnumeration = net.nextElement().inetAddresses
            while (addresessEnumeration.hasMoreElements()) {
                var address = addresessEnumeration.nextElement()
                var host = address.hostAddress
                if (!address.isLoopbackAddress && isIPv4Address(host)) {
                    return host
                }
            }
        }
        return ""
    }
}
