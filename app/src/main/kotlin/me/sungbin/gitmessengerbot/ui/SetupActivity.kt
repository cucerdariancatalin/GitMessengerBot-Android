/*
 * GitMessengerBot © 2021 지성빈 & 구환. all rights reserved.
 * GitMessengerBot license is under the GPL-3.0.
 *
 * Please see: https://github.com/GitMessengerBot/GitMessengerBot-Android/blob/master/LICENSE.
 */

package me.sungbin.gitmessengerbot.ui

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AlertDialog
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import me.sungbin.androidutils.extensions.doDelay
import me.sungbin.androidutils.extensions.toast
import me.sungbin.androidutils.util.PermissionUtil
import me.sungbin.androidutils.util.StorageUtil
import me.sungbin.gitmessengerbot.R
import me.sungbin.gitmessengerbot.model.GithubData
import me.sungbin.gitmessengerbot.repo.GithubClient
import me.sungbin.gitmessengerbot.repo.GithubService
import me.sungbin.gitmessengerbot.theme.BindView
import me.sungbin.gitmessengerbot.theme.SystemUiController
import me.sungbin.gitmessengerbot.theme.colors
import me.sungbin.gitmessengerbot.theme.defaultFontFamily
import me.sungbin.gitmessengerbot.util.PathManager
import me.sungbin.gitmessengerbot.util.Web
import me.sungbin.gitmessengerbot.util.asCallbackFlow

/**
 * Created by SungBin on 2021/04/08.
 */

@ExperimentalCoroutinesApi
@ExperimentalComposeUiApi
class SetupActivity : ComponentActivity() {

    data class Permission(
        val permissions: List<String>,
        val name: String,
        val description: String,
        val painterResource: Int
    )

    val isStoragePermissionGranted = mutableStateOf(false)
    val isNotificationPermissionGranted = mutableStateOf(false)
    val personalKeyInputDialogIsOpening = mutableStateOf(false)

    private val permissionsContracts =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionRequest ->
            if (permissionRequest.values.first()) {
                isStoragePermissionGranted.value = true
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SystemUiController(window).setSystemBarsColor(colors.primary)

        setContent {
            BindView {
                SetupView()
                BindPersonalKeyInputDialog()
            }
        }
    }

    @Composable
    private fun BindPersonalKeyInputDialog() {
        if (personalKeyInputDialogIsOpening.value) {
            val personalKeyInput = remember { mutableStateOf(TextFieldValue()) }
            val keyboardController = LocalSoftwareKeyboardController.current

            StorageUtil.createFolder(PathManager.Bots)
            StorageUtil.createFolder(PathManager.Npm)
            StorageUtil.createFolder(PathManager.Setting)

            MaterialTheme {
                AlertDialog(
                    shape = RoundedCornerShape(10.dp),
                    properties = DialogProperties(
                        dismissOnClickOutside = false
                    ),
                    onDismissRequest = {
                        personalKeyInputDialogIsOpening.value = false
                    },
                    text = {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Image(
                                        painter = painterResource(R.drawable.ic_baseline_circle_24),
                                        contentDescription = null,
                                        colorFilter = ColorFilter.tint(Color.Black)
                                    )
                                    Image(
                                        modifier = Modifier.size(15.dp),
                                        painter = painterResource(R.drawable.ic_baseline_vpn_key_24),
                                        contentDescription = null
                                    )
                                }
                                Text(
                                    text = stringResource(R.string.setup_input_personal_key),
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                            TextField(
                                maxLines = 1,
                                singleLine = true,
                                modifier = Modifier.padding(top = 10.dp),
                                value = personalKeyInput.value,
                                colors = TextFieldDefaults.textFieldColors(
                                    backgroundColor = Color.White,
                                    cursorColor = Color.Black,
                                    textColor = Color.Black,
                                    focusedIndicatorColor = Color.Black
                                ),
                                keyboardActions = KeyboardActions {
                                    keyboardController?.hideSoftwareKeyboard() // todo: Do not working.
                                },
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                onValueChange = { personalKeyInput.value = it }
                            )
                            Row(
                                modifier = Modifier
                                    .padding(top = 20.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.setup_way_to_get_personal_key),
                                    modifier = Modifier.clickable {
                                        Web.open(this@SetupActivity, Web.Type.PersonalKeyGuide)
                                    },
                                    fontSize = 13.sp,
                                    color = Color.Black
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Text(
                                        text = stringResource(R.string.setup_open_github),
                                        modifier = Modifier.clickable {
                                            Web.open(this@SetupActivity, Web.Type.Github)
                                        },
                                        fontSize = 13.sp,
                                        color = Color.Black
                                    )
                                    Text(
                                        modifier = Modifier
                                            .padding(start = 8.dp)
                                            .clickable {
                                                var githubData =
                                                    GithubData(personalKey = personalKeyInput.value.text)

                                                CoroutineScope(Dispatchers.IO).launch {
                                                    GithubClient
                                                        .instance(githubData.personalKey, GithubService::class.java)
                                                        .getUserInfo()
                                                        .asCallbackFlow()
                                                        .catch { error ->
                                                            this@SetupActivity.run {
                                                                runOnUiThread {
                                                                    toast(
                                                                        getString(
                                                                            R.string.setup_github_connect_error,
                                                                            error.localizedMessage
                                                                        )
                                                                    )
                                                                }
                                                            }
                                                        }
                                                        .collect { user ->
                                                            githubData = githubData.copy(
                                                                userName = user.login,
                                                                profileImageUrl = user.avatarUrl
                                                            )

                                                            StorageUtil.save(
                                                                "${PathManager.Setting}/GithubData.json",
                                                                Gson().toJson(githubData)
                                                            )

                                                            finish()

                                                            this@SetupActivity.run {
                                                                runOnUiThread {
                                                                    toast(
                                                                        getString(
                                                                            R.string.setup_welcome_start,
                                                                            user.login
                                                                        )
                                                                    )
                                                                }
                                                            }
                                                        }
                                                }
                                            },
                                        text = stringResource(R.string.setup_start),
                                        fontSize = 13.sp,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    },
                    buttons = {}
                )
            }
        }
    }

    @Composable
    private fun SetupView() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = colors.primary)
                .padding(30.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_round_warning_amber_24),
                    modifier = Modifier.size(80.dp),
                    contentDescription = null,
                )
                Text(
                    modifier = Modifier.padding(top = 8.dp),
                    text = with(AnnotatedString.Builder(stringResource(R.string.setup_title))) {
                        addStyle(
                            SpanStyle(
                                fontWeight = FontWeight.Bold,
                                fontFamily = defaultFontFamily
                            ),
                            11, 19
                        )
                        toAnnotatedString()
                    },
                    color = Color.White,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )
            }
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                PermissionView(
                    Permission(
                        listOf(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ),
                        stringResource(R.string.setup_permission_storage_label),
                        stringResource(
                            R.string.setup_permission_storage_description
                        ),
                        R.drawable.ic_baseline_folder_24
                    ),
                    isStoragePermissionGranted,
                    listOf(0, 16)
                )
                PermissionView(
                    Permission(
                        listOf(PERMISSION_NOTIFICATION_READ),
                        stringResource(R.string.setup_permission_notification_label),
                        stringResource(
                            R.string.setup_permission_notification_description
                        ),
                        R.drawable.ic_baseline_notifications_24
                    ),
                    isNotificationPermissionGranted,
                    listOf(16, 16)
                )
            }
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = stringResource(R.string.setup_last_func),
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Column(
                    modifier = Modifier.background(
                        color = colors.primaryVariant,
                        RoundedCornerShape(15.dp)
                    )
                ) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isStoragePermissionGranted.value) {
                                    personalKeyInputDialogIsOpening.value = true
                                } else {
                                    this@SetupActivity.toast(getString(R.string.setup_need_manage_permission))
                                }
                            }
                            .padding(8.dp),
                        text = stringResource(R.string.setup_start_with_personal_key),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }

    @Composable
    private fun PermissionView(
        permission: Permission,
        isPermissionGrant: MutableState<Boolean>,
        padding: List<Int>
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(if (isPermissionGrant.value) 0.5f else 1f)
                .padding(top = padding[0].dp, bottom = padding[1].dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color.White, RoundedCornerShape(15.dp))
                    .clickable {
                        permission.requestAllPermissions()
                    }
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(R.drawable.ic_baseline_circle_24),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(Color.Black)
                    )
                    Image(
                        modifier = Modifier.size(15.dp),
                        painter = painterResource(permission.painterResource),
                        contentDescription = null
                    )
                }
                Text(
                    text = permission.name,
                    color = Color.Black,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                color = Color.White,
                text = permission.description,
                textAlign = TextAlign.Center,
                fontSize = 13.sp
            )
        }
    }

    private fun Permission.requestAllPermissions() =
        if (this.permissions.first() == PERMISSION_NOTIFICATION_READ) {
            PermissionUtil.requestReadNotification(this@SetupActivity)
            doDelay(1000) {
                isNotificationPermissionGranted.value = true
            }
        } else permissionsContracts.launch(this.permissions.toTypedArray())

    companion object {
        const val PERMISSION_NOTIFICATION_READ = "PERMISSION_FOR_NOTIFICATION_READ"
    }
}
