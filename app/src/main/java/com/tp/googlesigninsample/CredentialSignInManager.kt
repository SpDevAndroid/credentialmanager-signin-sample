package com.tp.googlesigninsample

import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.PasswordCredential
import androidx.credentials.PublicKeyCredential
import androidx.credentials.exceptions.GetCredentialException

import androidx.fragment.app.FragmentActivity
import com.example.googlesigninsample.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class CredentialSignInManager(private val activity: FragmentActivity?) {
    private val TAG = "CredentialSignInManager"
    private val noneStr = "TEST"


    fun googleSignInWithCredentialManager() {
        activity?.let {
            val clientId = activity.resources.getString(R.string.server_client_id_web)
            val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(clientId)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            GlobalScope.launch {
                try {
                    val credentialManager = CredentialManager.create(activity)
                    val result = credentialManager.getCredential(
                        request = request,
                        context = activity,
                    )
                    handleSignIn(result)
                } catch (e: GetCredentialException) {
                    Log.e(TAG, "Exception handleSignInResult:error ${e.message}")
                    e.printStackTrace()
                }
            }


        }
    }

    fun handleSignIn(result: GetCredentialResponse) {
        // Handle the successfully returned credential.
        when (val credential = result.credential) {
            is PublicKeyCredential -> {
                // Share responseJson such as a GetCredentialResponse on your server to
                // validate and authenticate
                val responseJson = credential.authenticationResponseJson
                Log.d(TAG, "responseJson : $responseJson")
            }

            is PasswordCredential -> {
                // Send ID and password to your server to validate and authenticate.
                val username = credential.id
                val password = credential.password
                Log.d(TAG, "username : $username password : $password")
            }

            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        // Use googleIdTokenCredential and extract id to validate and
                        // authenticate on your server.
                        val googleIdTokenCredential = GoogleIdTokenCredential
                            .createFrom(credential.data)

                        Log.d(
                            TAG,
                            "googleIdTokenCredential idToken : ${googleIdTokenCredential.idToken}"
                        )
                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e(TAG, "Received an invalid google id token response", e)
                    }
                } else {
                    // Catch any unrecognized custom credential type here.
                    Log.e(TAG, "Unexpected type of credential")
                }
            }

            else -> {
                // Catch any unrecognized credential type here.
                Log.e(TAG, "Unexpected type of credential")
            }
        }
    }

}