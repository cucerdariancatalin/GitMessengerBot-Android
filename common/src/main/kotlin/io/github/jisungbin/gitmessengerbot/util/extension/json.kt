/*
 * GitMessengerBot © 2021 지성빈 & 구환. all rights reserved.
 * GitMessengerBot license is under the GPL-3.0.
 *
 * [json.kt] created by Ji Sungbin on 21. 8. 28. 오후 3:59
 *
 * Please see: https://github.com/GitMessengerBot/GitMessengerBot-Android/blob/master/LICENSE.
 */

package io.github.jisungbin.gitmessengerbot.util.extension

import com.google.gson.Gson
import io.github.jisungbin.gitmessengerbot.util.exception.CommonException

inline fun <reified T : Any> String.toModel() = Gson().fromJson(this, T::class.java)!!

fun Any.toJsonString() = Gson().toJson(this)
    ?: throw CommonException("Error occur when convert json-object to string. ($this)")
