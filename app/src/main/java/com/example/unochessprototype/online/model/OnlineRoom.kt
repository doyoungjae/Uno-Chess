package com.example.unochessprototype.online.model

data class OnlineRoom(
    val roomId: String = "",
    val roomName: String = "",
    val ownerUid: String = "",
    val ownerName: String = "",
    val ownerAvatar: String = "♞",
    val guestUid: String = "",
    val guestName: String = "",
    val guestAvatar: String = "♜",
    val ownerReady: Boolean = false,
    val guestReady: Boolean = false,
    val status: String = RoomStatus.WAITING,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val game: OnlineGameState? = null,
) {
    fun isParticipant(uid: String): Boolean = uid == ownerUid || uid == guestUid
    fun isOwner(uid: String): Boolean = uid == ownerUid
    fun otherUid(uid: String): String = if (uid == ownerUid) guestUid else ownerUid

    fun displayNameFor(uid: String): String = when (uid) {
        ownerUid -> ownerName.ifBlank { "PLAYER 1" }
        guestUid -> guestName.ifBlank { "PLAYER 2" }
        else -> "PLAYER"
    }

    fun avatarFor(uid: String): String = when (uid) {
        ownerUid -> ownerAvatar.ifBlank { "♞" }
        guestUid -> guestAvatar.ifBlank { "♜" }
        else -> "♟"
    }
}

object RoomStatus {
    const val WAITING = "WAITING"
    const val FULL = "FULL"
    const val PLAYING = "PLAYING"
    const val FINISHED = "FINISHED"
}
