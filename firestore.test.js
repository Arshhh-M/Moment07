const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = "demo-no-project";
const DATABASE_ID = "ai-studio-android-socialx-53955436-3590-486a-9596-4af600cf8199";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";
const CHARLIE_UID = "charlie_789";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

beforeEach(async () => {
  await testEnv.clearFirestore();
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

test("Unauthenticated read of users is rejected", async () => {
  const unauthedDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthedDb.collection("users").doc(ALICE_UID).get());
});

test("Alice can create her user profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      username: "alice",
      displayName: "Alice Wonderland",
      bio: "Hello world!",
      followersCount: 0,
      followingCount: 0,
      postsCount: 0,
      reelsCount: 0,
      isOnline: true,
    })
  );
});

test("Alice cannot create Bob's profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).set({
      username: "bob",
      displayName: "Bob Smith",
    })
  );
});

test("Alice can create a post and Bob can read and like it", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  const postRef = aliceDb.collection("posts").doc("post_1");
  await assertSucceeds(
    postRef.set({
      authorId: ALICE_UID,
      authorUsername: "alice",
      type: "post",
      caption: "My first SocialX post! #exciting",
      likesCount: 0,
      commentsCount: 0,
      createdAt: new Date(),
    })
  );

  // Bob reads the post
  await assertSucceeds(bobDb.collection("posts").doc("post_1").get());

  // Bob likes the post
  await assertSucceeds(
    bobDb.collection("likes").doc("post_post_1_bob_456").set({
      targetType: "post",
      targetId: "post_1",
      userId: BOB_UID,
      userUsername: "bob",
      createdAt: new Date(),
    })
  );

  // Bob cannot delete Alice's post
  await assertFails(bobDb.collection("posts").doc("post_1").delete());
});

test("Conversation privacy: Charlie cannot read Alice and Bob's chat", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const charlieDb = testEnv.authenticatedContext(CHARLIE_UID).firestore();

  const convRef = aliceDb.collection("conversations").doc("conv_alice_bob");
  await assertSucceeds(
    convRef.set({
      participantIds: [ALICE_UID, BOB_UID],
      lastMessage: "Hi Bob!",
      createdAt: new Date(),
    })
  );

  // Alice sends a message
  const msgRef = convRef.collection("messages").doc("msg_1");
  await assertSucceeds(
    msgRef.set({
      conversationId: "conv_alice_bob",
      senderId: ALICE_UID,
      recipientId: BOB_UID,
      text: "Hello Bob!",
      createdAt: new Date(),
    })
  );

  // Bob can read the message
  await assertSucceeds(bobDb.collection("conversations").doc("conv_alice_bob").collection("messages").doc("msg_1").get());

  // Charlie CANNOT read the conversation or message
  await assertFails(charlieDb.collection("conversations").doc("conv_alice_bob").get());
  await assertFails(charlieDb.collection("conversations").doc("conv_alice_bob").collection("messages").doc("msg_1").get());
});

test("Notification: Bob can notify Alice, but user cannot notify self", async () => {
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();

  // Bob notifies Alice
  await assertSucceeds(
    bobDb.collection("notifications").doc("notif_1").set({
      recipientId: ALICE_UID,
      actorId: BOB_UID,
      actorUsername: "bob",
      type: "like_post",
      targetId: "post_1",
      createdAt: new Date(),
    })
  );

  // Alice cannot notify herself
  await assertFails(
    aliceDb.collection("notifications").doc("notif_2").set({
      recipientId: ALICE_UID,
      actorId: ALICE_UID,
      actorUsername: "alice",
      type: "like_post",
      targetId: "post_1",
      createdAt: new Date(),
    })
  );
});
