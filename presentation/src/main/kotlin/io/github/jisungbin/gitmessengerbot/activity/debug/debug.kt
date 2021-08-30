/*
 * GitMessengerBot © 2021 지성빈 & 구환. all rights reserved.
 * GitMessengerBot license is under the GPL-3.0.
 *
 * [debug.kt] created by Ji Sungbin on 21. 6. 19. 오후 11:52.
 *
 * Please see: https://github.com/GitMessengerBot/GitMessengerBot-Android/blob/master/LICENSE.
 */

package io.github.jisungbin.gitmessengerbot.activity.debug

import android.app.Activity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import gun0912.tedkeyboardobserver.TedKeyboardObserver
import io.github.jisungbin.gitmessengerbot.R
import io.github.jisungbin.gitmessengerbot.common.config.ScriptConfig
import io.github.jisungbin.gitmessengerbot.common.core.Util
import io.github.jisungbin.gitmessengerbot.common.extension.toast
import io.github.jisungbin.gitmessengerbot.theme.colors
import io.github.jisungbin.gitmessengerbot.theme.orange
import io.github.jisungbin.gitmessengerbot.theme.transparentTextFieldColors
import io.github.jisungbin.gitmessengerbot.theme.twiceLightGray
import io.github.sungbin.gitmessengerbot.core.bot.Bot
import io.github.sungbin.gitmessengerbot.core.bot.Sender
import io.github.sungbin.gitmessengerbot.core.bot.debug.DebugItem
import io.github.sungbin.gitmessengerbot.core.bot.debug.DebugStore
import io.github.sungbin.gitmessengerbot.core.bot.debug.createDebugItem
import io.github.sungbin.gitmessengerbot.core.bot.script.ScriptItem
import io.github.sungbin.gitmessengerbot.core.setting.AppConfig
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun Debug(activity: Activity, script: ScriptItem? = null) {
    val settingDialogVisible = remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            DebugToolbar(
                activity = activity,
                script = script,
                evalMode = AppConfig.appValue.evalMode,
                settingDialogVisible = settingDialogVisible
            )
        },
        content = {
            DebugContent(
                activity = activity,
                script = script,
                evalMode = AppConfig.appValue.evalMode,
                settingDialogVisible = settingDialogVisible
            )
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DebugSettingDialog(visible: MutableState<Boolean>, debugId: Int) {
    val context = LocalContext.current

    if (visible.value) {
        AlertDialog(
            onDismissRequest = { visible.value = false },
            buttons = {},
            title = { Text(text = stringResource(R.string.composable_debug_setting)) },
            text = {
                Surface(
                    modifier = Modifier.combinedClickable(
                        onClick = {
                            toast(
                                context,
                                context.getString(R.string.composable_debug_toast_confirm_remove)
                            )
                        },
                        onLongClick = {
                            toast(
                                context,
                                context.getString(R.string.composable_debug_toast_removed)
                            )
                            DebugStore.removeAll(debugId)
                        },
                    ),
                    elevation = 1.dp,
                    color = orange,
                    shape = RoundedCornerShape(5.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_round_warning_24),
                            contentDescription = null,
                            tint = Color.White
                        )
                        Text(
                            text = stringResource(R.string.composable_debug_remove_all_history),
                            color = Color.White,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        )
    }
}

@Composable
private fun DebugToolbar(
    activity: Activity,
    script: ScriptItem?,
    evalMode: Boolean,
    settingDialogVisible: MutableState<Boolean>,
) {
    TopAppBar(
        backgroundColor = colors.primary,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp)
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            val (back, title, setting, switchDescription, modeSwitch) = createRefs()

            if (script != null) {
                Icon(
                    painter = painterResource(R.drawable.ic_round_arrow_left_24),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(15.dp)
                        .clickable {
                            activity.finish()
                        }
                        .constrainAs(back) {
                            start.linkTo(parent.start)
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                        }
                )
                Text(
                    text = script.name,
                    color = Color.White,
                    modifier = Modifier.constrainAs(title) {
                        start.linkTo(back.end, 10.dp)
                        top.linkTo(back.top)
                        bottom.linkTo(back.bottom)
                    }
                )
            } else {
                Text(
                    text = stringResource(R.string.composable_debug_title),
                    color = Color.White,
                    modifier = Modifier.constrainAs(title) {
                        start.linkTo(parent.start)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    }
                )
            }
            Icon(
                painter = painterResource(R.drawable.ic_round_settings_24),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .clickable {
                        settingDialogVisible.value = true
                    }
                    .constrainAs(setting) {
                        end.linkTo(parent.end)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    }
            )
            Switch(
                checked = evalMode,
                onCheckedChange = { evalMode ->
                    AppConfig.update { app ->
                        app.copy(evalMode = evalMode)
                    }
                },
                modifier = Modifier.constrainAs(modeSwitch) {
                    end.linkTo(setting.start, 16.dp)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = colors.primaryVariant,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color.Black
                ),
            )
            Text(
                text = stringResource(R.string.composable_debug_eval_mode),
                color = Color.White,
                fontSize = 13.sp,
                modifier = Modifier.constrainAs(switchDescription) {
                    end.linkTo(modeSwitch.start, 5.dp)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }
            )
        }
    }
}

@Composable
private fun DebugContent(
    activity: Activity,
    script: ScriptItem?,
    evalMode: Boolean,
    settingDialogVisible: MutableState<Boolean>,
) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(twiceLightGray)
    ) {
        val (chats, textfield) = createRefs()
        val lazyListState = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()
        var inputField by remember { mutableStateOf(TextFieldValue()) }

        val items: List<DebugItem>
        val debugId: Int

        when {
            evalMode -> {
                items = DebugStore.getByScriptId(ScriptConfig.EvalId)
                debugId = ScriptConfig.EvalId
            }
            script == null -> {
                items = DebugStore.itemsValue.filterNot { it.scriptId == ScriptConfig.EvalId }
                debugId = ScriptConfig.DebugAllBot
            }
            else -> {
                items = DebugStore.getByScriptId(script.id)
                debugId = script.id
            }
        }

        fun scrollDown() {
            coroutineScope.launch {
                lazyListState.animateScrollToItem(items.size)
            }
        }

        scrollDown()
        DebugSettingDialog(visible = settingDialogVisible, debugId = debugId)

        TedKeyboardObserver(activity).listen {
            scrollDown()
        }

        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .constrainAs(chats) {
                    top.linkTo(parent.top)
                    bottom.linkTo(textfield.top, 8.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    height = Dimension.fillToConstraints
                    width = Dimension.fillToConstraints
                },
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(items = items) { index, _ ->
                ChatBubble(
                    prevItem = items.getOrNull(index - 1),
                    item = items[index],
                    nextItem = items.getOrNull(index + 1)
                )
            }
        }
        TextField(
            value = inputField,
            onValueChange = { inputField = it },
            trailingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_round_send_24),
                    contentDescription = null,
                    modifier = Modifier.clickable {
                        val message = inputField.text
                        DebugStore.add(
                            createDebugItem(
                                debugId,
                                message,
                                "null", // TODO: Profile image
                                Sender.User() // TODO: Sender name
                            )
                        )
                        inputField = TextFieldValue()
                        when {
                            evalMode -> {
                                Bot.callJsResponder(
                                    script = ScriptItem(
                                        id = ScriptConfig.EvalId,
                                        name = "",
                                        lang = 0,
                                        power = false,
                                        compiled = false,
                                        lastRun = ""
                                    ),
                                    room = "",
                                    message = message,
                                    sender = Sender.User(),
                                    isGroupChat = false,
                                    isDebugMode = true
                                )
                            }
                            script == null -> {
                                Bot.getCompiledScripts().forEach { script ->
                                    Bot.callJsResponder(
                                        script = script,
                                        room = "DebugRoom", // TODO
                                        message = message,
                                        sender = Sender.User(), // TODO
                                        isGroupChat = false, // TODO
                                        isDebugMode = true
                                    )
                                }
                            }
                            else -> {
                                Bot.callJsResponder(
                                    script = script,
                                    room = "DebugRoom", // TODO
                                    message = message,
                                    sender = Sender.User(), // TODO
                                    isGroupChat = false, // TODO
                                    isDebugMode = true
                                )
                            }
                        }
                        scrollDown()
                    }
                )
            },
            colors = transparentTextFieldColors(backgroundColor = Color.White),
            modifier = Modifier
                .padding(16.dp)
                .requiredHeightIn(min = Dp.Unspecified, max = 150.dp)
                .constrainAs(textfield) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                    width = Dimension.fillToConstraints
                },
            shape = RoundedCornerShape(30.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ChatBubble(prevItem: DebugItem?, item: DebugItem, nextItem: DebugItem?) {
    val context = LocalContext.current
    val date = SimpleDateFormat("a hh:mm", Locale.KOREA).format(Date().apply { time = item.time })
    val nextDate = SimpleDateFormat("a hh:mm", Locale.KOREA).format(
        Date().apply { time = nextItem?.time ?: 0L }
    )
    val visibleTime = if (nextItem != null) {
        date != nextDate
    } else {
        true
    }
    val visibleProfileImage = if (prevItem != null) {
        item.sender != prevItem.sender
    } else {
        true
    }

    when (item.sender) {
        Sender.Bot -> { // 왼쪽 (봇이 답장)
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                val (message, time) = createRefs()

                Surface(
                    modifier = Modifier
                        .combinedClickable(
                            onClick = {},
                            onLongClick = {
                                Util.copy(context, item.message)
                            }
                        )
                        .constrainAs(message) {
                            start.linkTo(parent.start)
                        },
                    elevation = 1.dp,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = item.message,
                        color = Color.Black,
                        fontSize = 15.sp,
                        modifier = Modifier
                            .requiredWidthIn(min = Dp.Unspecified, max = 200.dp)
                            .padding(top = 4.dp, bottom = 4.dp, start = 8.dp, end = 8.dp)
                    )
                }
                if (visibleTime) {
                    Text(
                        text = date,
                        color = Color.Gray,
                        fontSize = 10.sp,
                        modifier = Modifier.constrainAs(time) {
                            end.linkTo(message.end)
                            top.linkTo(message.bottom, 5.dp)
                        }
                    )
                }
            }
        }
        else -> { // 오른쪽 (사용자가 입력)
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                val (profileImage, name, message, time) = createRefs()

                if (visibleProfileImage) {
                    Icon(
                        painter = painterResource(R.drawable.ic_round_account_circle_24),
                        contentDescription = null,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .constrainAs(profileImage) {
                                end.linkTo(parent.end)
                            }
                    )
                    Text(
                        text = item.sender,
                        fontSize = 13.sp,
                        modifier = Modifier.constrainAs(name) {
                            end.linkTo(parent.end, 60.dp)
                        }
                    )
                    Surface(
                        modifier = Modifier
                            .combinedClickable(
                                onClick = {},
                                onLongClick = {
                                    Util.copy(context, item.message)
                                }
                            )
                            .constrainAs(message) {
                                top.linkTo(name.bottom, 10.dp)
                                end.linkTo(name.end)
                            },
                        elevation = 1.dp,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = item.message,
                            color = Color.Black,
                            fontSize = 15.sp,
                            modifier = Modifier
                                .requiredWidthIn(min = Dp.Unspecified, max = 200.dp)
                                .padding(top = 4.dp, bottom = 4.dp, start = 8.dp, end = 8.dp)
                        )
                    }
                } else {
                    Surface(
                        modifier = Modifier
                            .combinedClickable(
                                onClick = {},
                                onLongClick = {
                                    Util.copy(context, item.message)
                                }
                            )
                            .constrainAs(message) {
                                end.linkTo(parent.end, 60.dp)
                            },
                        elevation = 1.dp,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = item.message,
                            color = Color.Black,
                            fontSize = 15.sp,
                            modifier = Modifier
                                .requiredWidthIn(min = Dp.Unspecified, max = 200.dp)
                                .padding(top = 4.dp, bottom = 4.dp, start = 8.dp, end = 8.dp)
                        )
                    }
                }
                if (visibleTime) {
                    Text(
                        text = date,
                        color = Color.Gray,
                        fontSize = 10.sp,
                        modifier = Modifier.constrainAs(time) {
                            start.linkTo(message.start)
                            top.linkTo(message.bottom, 5.dp)
                        }
                    )
                }
            }
        }
    }
}
