package com.example.unochessprototype.online.repository

import com.example.unochessprototype.online.model.OnlineProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class OnlineUserRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    database: FirebaseDatabase = FirebaseDatabase.getInstance(),
) {
    private val usersRef: DatabaseReference = database.reference.child("users")

    fun ensureSignedIn(
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            onSuccess(currentUser.uid)
            return
        }

        auth.signInAnonymously()
            .addOnSuccessListener { result ->
                val uid = result.user?.uid
                if (uid == null) {
                    onError("Firebase 사용자 UID를 가져오지 못했습니다.")
                } else {
                    onSuccess(uid)
                }
            }
            .addOnFailureListener { error ->
                onError(error.localizedMessage ?: "Firebase 익명 로그인에 실패했습니다.")
            }
    }

    fun observeProfile(
        uid: String,
        onChanged: (OnlineProfile) -> Unit,
        onError: (String) -> Unit,
    ): ValueEventListener {
        val userRef = usersRef.child(uid)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    createInitialProfile(uid, onError)
                    return
                }

                val loaded = snapshot.getValue(OnlineProfile::class.java)
                if (loaded == null) {
                    onError("온라인 프로필 데이터를 읽지 못했습니다.")
                    return
                }

                onChanged(
                    loaded.copy(
                        uid = uid,
                        nickname = loaded.nickname.ifBlank { defaultNickname(uid) },
                        avatar = loaded.avatar.ifBlank { "♞" },
                    )
                )
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error.message)
            }
        }

        userRef.addValueEventListener(listener)
        return listener
    }

    fun removeProfileListener(uid: String, listener: ValueEventListener) {
        usersRef.child(uid).removeEventListener(listener)
    }

    fun updateIdentity(
        uid: String,
        nickname: String,
        avatar: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        val cleanNickname = nickname.trim().ifBlank { defaultNickname(uid) }.take(16)
        val updates = mapOf<String, Any>(
            "nickname" to cleanNickname,
            "avatar" to avatar.ifBlank { "♞" },
        )

        usersRef.child(uid)
            .updateChildren(updates)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { error ->
                onError(error.localizedMessage ?: "온라인 프로필 저장에 실패했습니다.")
            }
    }

    private fun createInitialProfile(uid: String, onError: (String) -> Unit) {
        val profile = OnlineProfile(
            uid = uid,
            nickname = defaultNickname(uid),
            avatar = "♞",
            onlineGames = 0,
            onlineWins = 0,
            onlineLosses = 0,
            onlineDraws = 0,
        )

        usersRef.child(uid)
            .setValue(profile)
            .addOnFailureListener { error ->
                onError(error.localizedMessage ?: "온라인 프로필 생성에 실패했습니다.")
            }
    }

    private fun defaultNickname(uid: String): String =
        "PLAYER-${uid.takeLast(6).uppercase()}"
}
