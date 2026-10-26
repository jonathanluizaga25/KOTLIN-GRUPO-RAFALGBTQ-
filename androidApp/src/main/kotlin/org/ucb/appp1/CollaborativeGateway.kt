package org.ucb.appp1

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.Query

data class CollaborativeItem(val id: String, val text: String, val owner: String)
data class CollaborationCount(val count: Long, val revision: Long)

class CollaborativeGateway {
    private val store = FirebaseFirestore.getInstance()
    private val items = store.collection("collaborative_items")
    private val summary = store.collection("collaboration").document("summary")

    fun observeItems(onItems: (List<CollaborativeItem>, Boolean) -> Unit, onError: (String) -> Unit): () -> Unit {
        val registration = items.orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
                if (error != null) { onError(error.localizedMessage ?: "Firestore error"); return@addSnapshotListener }
                if (snapshot != null) onItems(snapshot.documents.mapNotNull {
                    val text = it.getString("text") ?: return@mapNotNull null
                    CollaborativeItem(it.id, text, it.getString("owner") ?: "")
                }, snapshot.metadata.isFromCache)
            }
        return { registration.remove() }
    }

    fun observeCounter(onCounter: (CollaborationCount) -> Unit, onError: (String) -> Unit): () -> Unit {
        val ref = FirebaseDatabase.getInstance().getReference("collaborative_counter")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                onCounter(CollaborationCount(snapshot.child("count").getValue(Long::class.java) ?: 0,
                    snapshot.child("revision").getValue(Long::class.java) ?: 0))
            }
            override fun onCancelled(error: DatabaseError) { onError(error.message) }
        }
        ref.addValueEventListener(listener)
        return { ref.removeEventListener(listener) }
    }

    fun observeSummary(onSummary: (CollaborationCount) -> Unit, onError: (String) -> Unit): () -> Unit {
        val registration = summary.addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
            if (error != null) { onError(error.localizedMessage ?: "Firestore error"); return@addSnapshotListener }
            if (snapshot != null && snapshot.exists() && !snapshot.metadata.isFromCache && !snapshot.metadata.hasPendingWrites()) {
                onSummary(CollaborationCount(snapshot.getLong("count") ?: 0, snapshot.getLong("revision") ?: 0))
            }
        }
        return { registration.remove() }
    }

    fun add(text: String, done: (Boolean) -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return done(false)
        val item = items.document()
        store.runTransaction { tx ->
            val existing = tx.get(summary)
            val count = existing.getLong("count") ?: 0
            val revision = existing.getLong("revision") ?: 0
            tx.set(item, mapOf("text" to text.trim(), "owner" to uid, "createdAt" to System.currentTimeMillis()))
            tx.set(summary, mapOf("count" to count + 1, "revision" to revision + 1,
                "lastItemId" to item.id, "operation" to "add"))
        }.addOnCompleteListener { done(it.isSuccessful) }
    }

    fun delete(id: String, done: (Boolean) -> Unit) {
        val item = items.document(id)
        store.runTransaction { tx ->
            val existingItem = tx.get(item)
            val existingSummary = tx.get(summary)
            if (!existingItem.exists()) return@runTransaction
            val count = existingSummary.getLong("count") ?: 0
            val revision = existingSummary.getLong("revision") ?: 0
            tx.delete(item)
            tx.set(summary, mapOf("count" to (count - 1).coerceAtLeast(0), "revision" to revision + 1,
                "lastItemId" to item.id, "operation" to "delete"))
        }.addOnCompleteListener { done(it.isSuccessful) }
    }
}
