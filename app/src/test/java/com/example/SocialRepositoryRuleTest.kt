package com.example

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.Post
import com.example.data.model.UserProfile
import com.example.data.repository.SocialRepository
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SocialRepositoryRuleTest : FirestoreEmulatorTestBase() {

    private lateinit var repository: SocialRepository

    @Before
    override fun setUpFirebase() {
        super.setUpFirebase()
        repository = SocialRepository(firestore)
    }

    @Test
    fun testAuthenticatedUserCanSaveAndFetchProfile() = runBlocking {
        val aliceUid = signInTestUser("alice@test.com")
        val profile = UserProfile(
            id = aliceUid,
            username = "alice",
            displayName = "Alice Wonder",
            bio = "Social explorer"
        )
        val success = repository.saveUserProfile(profile)
        assertTrue(success)

        val fetched = repository.getUserProfile(aliceUid)
        assertNotNull(fetched)
        assertEquals("alice", fetched?.username)
        assertEquals("Alice Wonder", fetched?.displayName)
    }

    @Test
    fun testUnauthenticatedUserCannotCreatePost() = runBlocking {
        auth.signOut()
        val fakeProfile = UserProfile(id = "unauthed_user", username = "anon")
        val post = Post(
            type = "post",
            caption = "Should fail without auth"
        )
        val result = repository.createPost(post, fakeProfile)
        assertNull(result)
    }

    @Test
    fun testAliceCreatesPostAndBobCanView() = runBlocking {
        val aliceUid = signInTestUser("alice_post@test.com")
        val aliceProfile = UserProfile(
            id = aliceUid,
            username = "alice_post",
            displayName = "Alice Poster"
        )
        repository.saveUserProfile(aliceProfile)

        val post = Post(
            type = "post",
            caption = "Hello SocialX world! #first"
        )
        val postId = repository.createPost(post, aliceProfile)
        assertNotNull(postId)

        // Sign in as Bob
        signInTestUser("bob_reader@test.com")
        val postDoc = firestore.collection("posts").document(postId!!).get().await()
        assertTrue(postDoc.exists())
    }
}
