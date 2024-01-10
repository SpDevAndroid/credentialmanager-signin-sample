package com.tp.googlesigninsample

import android.app.Activity
import android.content.IntentSender.SendIntentException
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.fragment.app.FragmentActivity
import com.example.googlesigninsample.R
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.GetSignInIntentRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import java.util.Arrays


private const val TAG = "GoogleIdentitySignInManager"

class GoogleIdentitySignInManager(private val activity: FragmentActivity?) {
    fun requestSignIn(signInLauncherIntent: ActivityResultLauncher<IntentSenderRequest>) {
        Log.d(TAG, "requestSignIn() called()")
        activity?.let {
            val request = GetSignInIntentRequest.builder()
                .setServerClientId(it.resources.getString(R.string.server_client_id_web)).build()
            Identity.getSignInClient(activity).getSignInIntent(request)
                .addOnSuccessListener { result ->
                    Log.d(TAG, "OnSuccessListener() result : $result")
                    try {
                        val intentSenderRequest =
                            IntentSenderRequest.Builder(result.intentSender).build()
                        signInLauncherIntent.launch(intentSenderRequest)
                    } catch (e: SendIntentException) {
                        Log.e(TAG, "Google Sign-in failed")
                    }
                }.addOnFailureListener { e ->
                    Log.e(TAG, "Google Sign-in failed", e)
                    e.printStackTrace()
                }
        }
    }

    fun handleSignInResult(result: ActivityResult, authorizationLauncherIntent: ActivityResultLauncher<IntentSenderRequest>) {
        Log.d(TAG, "handleSignInResult() called()")
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                activity?.let {
                    val credential = Identity.getSignInClient(activity)
                        .getSignInCredentialFromIntent(result.data)
                    // Signed in successfully - show authenticated UI
                    val googleIdToken = credential.googleIdToken

                    Log.d(TAG, "handleSignInResult() idToken : $googleIdToken")
                    Log.d(TAG, "handleSignInResult() credential : $credential")

                    /** **/
                    requestGmailReadOnlyAccess(authorizationLauncherIntent)
                }
            } catch (e: ApiException) {
                // The ApiException status code indicates the detailed failure reason.
                Log.e(TAG, "handleSignInResult:ApiException : ${e.message}")
                e.printStackTrace()
            }

        }
    }

    fun requestGmailReadOnlyAccess(authorizationLauncherIntent: ActivityResultLauncher<IntentSenderRequest>) {
        activity?.let {
            val scopeReadOnly = Scope("https://www.googleapis.com/auth/gmail.readonly")
            val requestedScopes = listOf(scopeReadOnly)
//            val requestedScopes = Arrays.asList(Scope(Scopes.EMAIL)
            val authorizationRequest =
                AuthorizationRequest.builder()
                    .setRequestedScopes(requestedScopes)
                    .requestOfflineAccess(it.resources.getString(R.string.server_client_id_web))
                    .build()
            Identity.getAuthorizationClient(it)
                .authorize(authorizationRequest)
                .addOnSuccessListener { authorizationResult: AuthorizationResult ->
                    if (authorizationResult.hasResolution()) {
                        // Access needs to be granted by the user
                        val pendingIntent = authorizationResult.pendingIntent
                        try {
                            pendingIntent?.let { pendingIntentObj ->
                                val intentSenderRequest =
                                    IntentSenderRequest.Builder(pendingIntentObj.intentSender)
                                        .build()
                                authorizationLauncherIntent.launch(intentSenderRequest)
                            }
                        } catch (e: SendIntentException) {
                            Log.e(
                                TAG, "Couldn't start Authorization UI: " + e.localizedMessage
                            )
                            e.printStackTrace()
                        }
                    } else {
                        // Access already granted, continue with user action
                        Log.d(
                            TAG,
                            "Access already granted, continue with user action : ",
                        )
                    }
                }.addOnFailureListener { e: Exception ->
                    Log.e(
                        TAG, "Failed to authorize", e
                    )
                    e.printStackTrace()
                }
        }
    }

    fun handleAuthorizationResult(result: ActivityResult) {
        Log.d(TAG, "handleAuthorizationResult() called()")

        try {
            activity?.let {
                val authorizationResult =
                    Identity.getAuthorizationClient(it).getAuthorizationResultFromIntent(
                        result.data
                    )

                Log.d(
                    TAG,
                    "handleAuthorizationResult() accessToken : ${authorizationResult.accessToken}"
                )
                Log.d(
                    TAG,
                    "handleAuthorizationResult() serverAuthCode : ${authorizationResult.serverAuthCode}"
                )
                Log.d(TAG, "handleAuthorizationResult() authorizationResult : $authorizationResult")
            }
        } catch (e: Exception) {
            // The ApiException status code indicates the detailed failure reason.
            Log.e(TAG, "handleAuthorizationResult:Exception : ${e.message}")
            e.printStackTrace()
        }
    }
}
