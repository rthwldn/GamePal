package com.example

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.firestore.FirestoreGame
import com.example.data.firestore.FirestorePlaySession
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class FirestoreRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun authenticatedUser_canCreateAndReadOwnGame() = runBlocking {
        val aliceUid = signInTestUser("alice@example.com")

        val gamePayload = mapOf(
            "id" to "game_100",
            "userId" to aliceUid,
            "title" to "Cyberpunk 2077",
            "platform" to "PC",
            "status" to "playing",
            "rating" to 9,
            "totalPlayTimeSeconds" to 12000L,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        val gameDoc = firestore.collection("users").document(aliceUid)
            .collection("games").document("game_100")

        gameDoc.set(gamePayload).await()

        val snap = gameDoc.get().await()
        assertTrue(snap.exists())
        assertEquals("Cyberpunk 2077", snap.getString("title"))
    }

    @Test
    fun crossUser_cannotAccessOtherUserData() = runBlocking {
        val aliceUid = signInTestUser("alice_cross@example.com")
        val gameDoc = firestore.collection("users").document(aliceUid)
            .collection("games").document("game_200")

        gameDoc.set(
            mapOf(
                "id" to "game_200",
                "userId" to aliceUid,
                "title" to "Elden Ring",
                "platform" to "PlayStation 5",
                "status" to "playing",
                "createdAt" to FieldValue.serverTimestamp()
            )
        ).await()

        // Switch to Bob
        signInTestUser("bob_cross@example.com")

        try {
            gameDoc.get().await()
            fail("Expected PERMISSION_DENIED when Bob tries to read Alice's game")
        } catch (e: FirebaseFirestoreException) {
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, e.code)
        }
    }

    @Test
    fun unauthenticated_cannotAccessData() = runBlocking {
        auth.signOut()

        try {
            firestore.collection("users").document("some_user")
                .collection("games").document("game_300").get().await()
            fail("Expected PERMISSION_DENIED for unauthenticated caller")
        } catch (e: FirebaseFirestoreException) {
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, e.code)
        }
    }
}
