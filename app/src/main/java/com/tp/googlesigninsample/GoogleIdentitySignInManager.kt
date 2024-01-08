package com.tp.googlesigninsample

import android.app.Activity
import android.content.IntentSender.SendIntentException
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.fragment.app.FragmentActivity
import com.example.googlesigninsample.R
import com.google.android.gms.auth.api.identity.GetSignInIntentRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException


private const val TAG = "GoogleIdentitySignInManager"

class GoogleIdentitySignInManager(private val activity: FragmentActivity?) {
    fun requestSignIn(signInLauncherIntent: ActivityResultLauncher<IntentSenderRequest>) {
        Log.d(TAG, "requestSignIn() called()")
        activity?.let {
            val request = GetSignInIntentRequest.builder()
                .setServerClientId(it.resources.getString(R.string.server_client_id_web))
                .build()
            Identity.getSignInClient(activity)
                .getSignInIntent(request)
                .addOnSuccessListener { result ->
                    Log.d(TAG, "OnSuccessListener() result : $result")
                    try {
                        val intentSenderRequest =
                            IntentSenderRequest.Builder(result.intentSender).build()
                        signInLauncherIntent.launch(intentSenderRequest)
                    } catch (e: SendIntentException) {
                        Log.e(TAG, "Google Sign-in failed")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Google Sign-in failed", e)
                    e.printStackTrace()
                }
        }
    }

    fun handleSignInResult(result: ActivityResult) {
        Log.d(TAG, "handleSignInResult() called()")
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                activity?.let {
                    val credential =
                        Identity.getSignInClient(activity)
                            .getSignInCredentialFromIntent(result.data)
                    // Signed in successfully - show authenticated UI
                    val googleIdToken = credential.googleIdToken

                    Log.d(TAG, "handleSignInResult() idToken : $googleIdToken")
                    Log.d(TAG, "handleSignInResult() credential : $credential")

                }
            } catch (e: ApiException) {
                // The ApiException status code indicates the detailed failure reason.
                Log.e(TAG, "handleSignInResult:ApiException : ${e.message}")
                e.printStackTrace()
            }

        }
    }
}
