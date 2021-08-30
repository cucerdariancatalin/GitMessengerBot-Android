/*
 * GitMessengerBot © 2021 지성빈 & 구환. all rights reserved.
 * GitMessengerBot license is under the GPL-3.0.
 *
 * [DebugActivty.kt] created by Ji Sungbin on 21. 7. 18. 오전 3:01.
 *
 * Please see: https://github.com/GitMessengerBot/GitMessengerBot-Android/blob/master/LICENSE.
 */

package io.github.jisungbin.gitmessengerbot.activity.debug

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import io.github.jisungbin.gitmessengerbot.common.config.IntentConfig
import io.github.jisungbin.gitmessengerbot.common.exception.PresentationException
import io.github.jisungbin.gitmessengerbot.theme.MaterialTheme
import io.github.jisungbin.gitmessengerbot.theme.SystemUiController
import io.github.jisungbin.gitmessengerbot.theme.colors
import io.github.jisungbin.gitmessengerbot.theme.twiceLightGray
import io.github.sungbin.gitmessengerbot.core.bot.Bot

class DebugActivty : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scriptId = intent.getIntExtra(IntentConfig.DebugScriptId, -1)
        val script = try {
            Bot.getAllScripts().first { it.id == scriptId }
        } catch (exception: Exception) {
            throw PresentationException("DebugItem script it not exist. (${exception.message})")
        }

        SystemUiController(window).run {
            setStatusBarColor(colors.primary)
            setNavigationBarColor(twiceLightGray)
        }
        setContent {
            MaterialTheme {
                Debug(activity = this, script = script)
            }
        }
    }
}
