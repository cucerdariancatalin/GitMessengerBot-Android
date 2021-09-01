/*
 * GitMessengerBot © 2021 지성빈 & 구환. all rights reserved.
 * GitMessengerBot license is under the GPL-3.0.
 *
 * [CommitItem.kt] created by Ji Sungbin on 21. 9. 2. 오전 12:06
 *
 * Please see: https://github.com/GitMessengerBot/GitMessengerBot-Android/blob/master/LICENSE.
 */

package io.github.jisungbin.gitmessengerbot.data.github.model.commit

import com.fasterxml.jackson.annotation.JsonProperty

data class CommitItem(
    @field:JsonProperty("sha")
    val sha: String?,

    @field:JsonProperty("commit")
    val commit: Commit?,
)
