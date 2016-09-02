package org.mdvsc.vcserver.util

import com.google.gson.Gson

/**
 *
 * @author  haniklz
 * @since   16/4/2
 * @version 1.0.0
 */
object JsonUtils {

    private val gson: Gson by lazy { Gson() }

    fun toJson(o: Any): String {
        return gson.toJson(o) ?: ""
    }

    fun <T> fromJson(json: String, clazz: Class<T>): T? {
        return gson.fromJson(json, clazz)
    }

}
