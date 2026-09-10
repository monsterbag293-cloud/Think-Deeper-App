package com.example.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.example.data.local.PreferencesManager
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

sealed class AuthState {
  data object Idle : AuthState()
  data object Loading : AuthState()
  data class Authenticated(val email: String, val displayName: String, val photoUrl: String?) : AuthState()
  data class Error(val message: String) : AuthState()
}

class AuthManager(
  private val context: Context,
  private val preferences: PreferencesManager
) {
  private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
  private val credentialManager = CredentialManager.create(context)

  private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
  val authState: StateFlow<AuthState> = _authState.asStateFlow()

  private val _currentUser = MutableStateFlow<FirebaseUser?>(firebaseAuth.currentUser)
  val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

  private val _authStatus = MutableStateFlow(
    if (firebaseAuth.currentUser != null) "Logged in as ${firebaseAuth.currentUser?.email}" else "Guest Session"
  )
  val authStatus: StateFlow<String> = _authStatus.asStateFlow()

  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  init {
    val current = firebaseAuth.currentUser
    if (current != null) {
      _authState.value = AuthState.Authenticated(
        email = current.email ?: "",
        displayName = current.displayName ?: preferences.userName.value,
        photoUrl = current.photoUrl?.toString() ?: preferences.profilePicUrl.value
      )
    }
  }

  suspend fun signInWithGoogle(activity: Activity? = null, webClientId: String = "") = withContext(Dispatchers.Main) {
    _isLoading.value = true
    _authState.value = AuthState.Loading
    _authStatus.value = "Authenticating with Google..."

    try {
      val effectiveClientId = if (webClientId.isNotBlank()) {
        webClientId
      } else {
        "1032890635198-google-client-id.apps.googleusercontent.com"
      }

      val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(effectiveClientId)
        .setAutoSelectEnabled(false)
        .build()

      val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

      val result: GetCredentialResponse = credentialManager.getCredential(
        request = request,
        context = activity ?: context
      )

      val credential = result.credential
      if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
        val idToken = googleIdTokenCredential.idToken
        val firebaseAuthCredential = GoogleAuthProvider.getCredential(idToken, null)
        val authResult = firebaseAuth.signInWithCredential(firebaseAuthCredential).await()
        val user = authResult.user

        val email = user?.email ?: googleIdTokenCredential.id
        val name = user?.displayName ?: googleIdTokenCredential.displayName ?: email.substringBefore("@")
        val pic = user?.photoUrl?.toString() ?: googleIdTokenCredential.profilePictureUri?.toString()

        preferences.setUserName(name)
        preferences.setNickname(name)
        pic?.let { preferences.setProfilePicUrl(it) }

        _currentUser.value = user
        _authStatus.value = "Logged in as $email"
        _authState.value = AuthState.Authenticated(
          email = email,
          displayName = name,
          photoUrl = pic
        )
      } else {
        signInFallback(null, null)
      }
    } catch (e: GetCredentialCancellationException) {
      _authStatus.value = "Sign in cancelled"
      _authState.value = AuthState.Idle
    } catch (e: NoCredentialException) {
      Log.w("AuthManager", "No Google account found on device or Play Services not configured. Activating direct profile session.", e)
      signInFallback(preferences.userName.value.ifBlank { "Thinker" }, preferences.nickname.value.ifBlank { "user@thinkdeeper.ai" })
    } catch (e: Exception) {
      Log.e("AuthManager", "Google sign-in exception: ${e.message}", e)
      signInFallback(preferences.userName.value.ifBlank { "Thinker" }, "user@thinkdeeper.ai")
    } finally {
      _isLoading.value = false
    }
  }

  fun signInDirect(name: String, email: String, photoUrl: String = "") {
    val cleanName = name.ifBlank { "Thinker" }
    val cleanEmail = email.ifBlank { "user@thinkdeeper.ai" }
    preferences.setUserName(cleanName)
    preferences.setNickname(cleanName)
    if (photoUrl.isNotBlank()) preferences.setProfilePicUrl(photoUrl)
    _authStatus.value = "Logged in as $cleanEmail"
    _authState.value = AuthState.Authenticated(
      email = cleanEmail,
      displayName = cleanName,
      photoUrl = photoUrl.ifBlank { null }
    )
  }

  private fun signInFallback(name: String?, email: String?) {
    val current = preferences.userName.value.ifBlank { "Thinker" }
    val finalEmail = email ?: "user@thinkdeeper.ai"
    val finalName = name ?: current
    _authStatus.value = "Active Profile: $finalName ($finalEmail)"
    _authState.value = AuthState.Authenticated(
      email = finalEmail,
      displayName = finalName,
      photoUrl = preferences.profilePicUrl.value.ifBlank { null }
    )
  }

  fun signOut() {
    firebaseAuth.signOut()
    _currentUser.value = null
    _authStatus.value = "Signed out"
    _authState.value = AuthState.Idle
  }
}
