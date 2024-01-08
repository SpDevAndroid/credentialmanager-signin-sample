package com.tp.googlesigninsample

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.googlesigninsample.R
import com.example.googlesigninsample.databinding.FragmentFirstBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val tag ="GOOGLE_SIGN_IN"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonFirst.setOnClickListener {
//            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
//            googleSignInSample()
            activity?.let {
                val credentialSignInManager = CredentialSignInManager(it)
                credentialSignInManager.googleSignInWithCredentialManager()
            }
        }
    }


    private fun googleSignInSample() {

        // Configure sign-in to request the user's ID, email address, and basic
// profile. ID and basic profile are included in DEFAULT_SIGN_IN.

        val clientId = getString(R.string.server_client_id_web)
        val gso: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes( Scope(Scopes.DRIVE_APPFOLDER)) //todo need to explore all option to chose correct one finally DRIVE_APPFOLDER
            .requestServerAuthCode(clientId)
            .requestIdToken(clientId)
            .requestScopes(Scope("https://www.googleapis.com/auth/gmail.readonly"))
            .requestEmail()
            .build()


        val mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);

        val signInIntent: Intent = mGoogleSignInClient.getSignInIntent()
        signInLauncherIntent.launch(signInIntent)
//
        Log.w(tag, "clientId 1111:::: $clientId" )
    }
    private val signInLauncherIntent = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        // This task is always completed immediately, there is no need to attach an
        // asynchronous listener.
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        handleSignInResult(task)
    }


    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            updateUI(account)
        } catch (e: ApiException) {
            Toast.makeText(requireContext(),"Exception 111 :  ${e.cause}",Toast.LENGTH_LONG).show()
            Log.w(tag, "handleSignInResult:error", e)
        }
    }


    private fun updateUI(account : GoogleSignInAccount){
        try {
            //  NOTE  if requestIdToken used in GoogleSignInOptions then use account.idToken
//            and if requestServerAuthCode used in GoogleSignInOptions then  get value in  account.getServerAuthCode   not in idToken
//            val idToken = account.getServerAuthCode()
            val idToken = account.idToken
            val servercode = account.getServerAuthCode()
            account.email

            Log.d(tag,"handleSignInResult()        account.id :: ${       account.id}         account.email ${ account.email }     account.account  ${   account.account}")
            Log.d(tag,"handleSignInResult() idToken : $idToken")
            Log.d(tag,"handleSignInResult() servercode : $servercode ")
            Toast.makeText(requireContext(),"idToken 555 $idToken",Toast.LENGTH_LONG).show()


            val shareMessage = "Id Token :\n\n$idToken \n\n Server Auth Code :\n\n$servercode"
            try {
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, shareMessage)
                    type = "text/plain"
                }

                val shareIntent = Intent.createChooser(sendIntent, null)
                activity?.startActivity(shareIntent)
            } catch (ignored: Exception) {
                Log.e(tag, "Exception handleSignInResult:error ${ignored.message}")
                    ignored.printStackTrace()

            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(),"Exception 222 :  ${e.cause}",Toast.LENGTH_LONG).show()
            Log.w(tag, "Exception :error", e)
        }
    }






    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}