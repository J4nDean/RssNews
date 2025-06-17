package pl.edu.pjwstk.rssnews.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import pl.edu.pjwstk.rssnews.model.NewsItem

class UserNewsRepository {
    private val db = FirebaseFirestore.getInstance()
    private val userId: String? get() = FirebaseAuth.getInstance().currentUser?.uid
    private val TAG = "UserNewsRepository"

    fun markAsRead(newsItem: NewsItem) {
        userId?.let { uid ->
            val normalizedLink = normalizeLink(newsItem.link)
            Log.d(TAG, "Marking as read: $normalizedLink")

            db.collection("users").document(uid)
                .collection("readNews") // Zmienione z "read" na "readNews"
                .document(normalizedLink)
                .set(mapOf(
                    "link" to newsItem.link,
                    "title" to newsItem.title,
                    "timestamp" to System.currentTimeMillis()
                ))
                .addOnSuccessListener {
                    Log.d(TAG, "Successfully marked as read: ${newsItem.title}")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error marking as read", e)
                }
        }
    }

    fun addToFavorites(newsItem: NewsItem) {
        userId?.let { uid ->
            val normalizedLink = normalizeLink(newsItem.link)
            Log.d(TAG, "Adding to favorites: $normalizedLink")

            db.collection("users").document(uid)
                .collection("favorites")
                .document(normalizedLink)
                .set(mapOf(
                    "link" to newsItem.link,
                    "title" to newsItem.title,
                    "timestamp" to System.currentTimeMillis()
                ))
        }
    }

    fun removeFromFavorites(newsItem: NewsItem) {
        userId?.let { uid ->
            val normalizedLink = normalizeLink(newsItem.link)
            Log.d(TAG, "Removing from favorites: $normalizedLink")

            db.collection("users").document(uid)
                .collection("favorites")
                .document(normalizedLink)
                .delete()
        }
    }

    fun getReadLinksLive(onResult: (Set<String>) -> Unit): ListenerRegistration {
        return userId?.let { uid ->
            db.collection("users").document(uid)
                .collection("readNews") // Zmienione z "read" na "readNews"
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error listening to read news", error)
                        onResult(emptySet())
                        return@addSnapshotListener
                    }

                    val links = snapshot?.documents?.mapNotNull { doc ->
                        doc.getString("link")
                    }?.toSet() ?: emptySet()

                    Log.d(TAG, "Read links updated: ${links.size} items")
                    onResult(links)
                }
        } ?: DummyListenerRegistration()
    }

    fun getFavoritesLinksLive(onResult: (Set<String>) -> Unit): ListenerRegistration {
        return userId?.let { uid ->
            db.collection("users").document(uid)
                .collection("favorites")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error listening to favorites", error)
                        onResult(emptySet())
                        return@addSnapshotListener
                    }

                    val links = snapshot?.documents?.mapNotNull { doc ->
                        doc.getString("link")
                    }?.toSet() ?: emptySet()

                    Log.d(TAG, "Favorite links updated: ${links.size} items")
                    onResult(links)
                }
        } ?: DummyListenerRegistration()
    }

    private fun normalizeLink(link: String): String {
        return link.trim().lowercase()
            .removePrefix("http://")
            .removePrefix("https://")
            .removePrefix("www.")
            .split("?")[0]
            .split("#")[0]
            .replace("/", "_")
            .replace(".", "_")
    }

    class DummyListenerRegistration : ListenerRegistration {
        override fun remove() {}
    }
}
