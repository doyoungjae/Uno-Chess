package com.example.unochessprototype.viewmodel
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.google.firebase.database.*
data class FriendRequest(val sender: String = "", val receiver: String = "", val status: String = "pending")
data class FriendPresence(val name: String = "", val sessions: Map<String, Boolean> = emptyMap())
class FriendsViewModel : ViewModel() {
 private val root = FirebaseDatabase.getInstance().reference
 private var uid = ""
 private val subscriptions = mutableListOf<Pair<Query, ValueEventListener>>()
 private val friendSubscriptions = mutableMapOf<String, Pair<Query, ValueEventListener>>()
 private var session: DatabaseReference? = null
 var requests by mutableStateOf<Map<String, FriendRequest>>(emptyMap()); private set
 var people by mutableStateOf<Map<String, FriendPresence>>(emptyMap()); private set
 var error by mutableStateOf<String?>(null); private set
 var busy by mutableStateOf(false); private set
 val friendIds get() = requests.values.filter { it.status == "accepted" }.map { if(it.sender == uid) it.receiver else it.sender }.toSet()
 val incoming get() = requests.filterValues { it.receiver == uid && it.status == "pending" }
 private fun watch(q: Query, f: (DataSnapshot)->Unit): Pair<Query, ValueEventListener> {
  val l = object: ValueEventListener {
   override fun onDataChange(s: DataSnapshot) = f(s)
   override fun onCancelled(e: DatabaseError) { error = e.message }
  }; q.addValueEventListener(l); return q to l
 }
 fun connect(id: String) {
  if(id.isBlank() || id == uid) return
  cleanup(); uid = id; requests = emptyMap(); people = emptyMap()
  listOf("sender", "receiver").forEach { field ->
   subscriptions += watch(root.child("friendships").orderByChild(field).equalTo(uid)) { s ->
    val kept = requests.filterValues { (if(field == "sender") it.sender else it.receiver) != uid }
    requests = kept + s.children.mapNotNull { c -> c.getValue(FriendRequest::class.java)?.let { c.key!! to it } }.toMap()
    friendIds.forEach { friend -> if(friend !in friendSubscriptions) {
     friendSubscriptions[friend] = watch(root.child("socialPresence").child(friend)) { p ->
      people = people + (friend to (p.getValue(FriendPresence::class.java) ?: FriendPresence(friend)))
     }
    } }
   }
  }
  val ref = root.child("socialPresence").child(uid).child("sessions").push(); session = ref
  subscriptions += watch(root.child(".info/connected")) { s ->
   if(s.getValue(Boolean::class.java) == true) ref.onDisconnect().removeValue().addOnSuccessListener {
    if(session == ref) ref.setValue(true).addOnFailureListener { error = it.message }
   }.addOnFailureListener { error = it.message }
  }
 }
 fun name(value: String) { if(uid.isNotBlank()) root.child("socialPresence").child(uid).child("name").setValue(value).addOnFailureListener { error = it.message } }
 fun request(value: String) {
  val other = value.trim()
  if(busy) return
  if(other.isBlank() || other == uid || other.length > 128 || other.any { it in ".#$[]/" }) { error = "상대 친구 ID를 확인해 주세요."; return }
  if(requests.values.any { it.sender == other || it.receiver == other }) { error = "이미 친구이거나 요청이 대기 중입니다."; return }
  busy = true; error = null
  root.child("users").child(other).get().addOnSuccessListener { s ->
   if(!s.exists()) { busy = false; error = "사용자를 찾을 수 없습니다." }
   else root.child("friendships").child(uid + ":" + other).setValue(FriendRequest(uid,other))
    .addOnCompleteListener { busy = false; error = if(it.isSuccessful) "친구 요청을 보냈습니다." else it.exception?.message }
  }.addOnFailureListener { busy = false; error = it.message }
 }
 fun respond(key: String, accept: Boolean) {
  if(busy || key !in incoming) return
  busy = true; error = null
  val ref = root.child("friendships").child(key)
  val task = if(accept) ref.child("status").setValue("accepted") else ref.removeValue()
  task.addOnCompleteListener { busy = false; error = it.exception?.message }
 }
 private fun cleanup() {
  (subscriptions + friendSubscriptions.values).forEach { (q,l) -> q.removeEventListener(l) }
  subscriptions.clear(); friendSubscriptions.clear(); session?.removeValue(); session = null
 }
 override fun onCleared() { cleanup(); super.onCleared() }
}
