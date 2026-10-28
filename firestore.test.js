const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

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

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read or write anything", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("games").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("games").doc("game1").set({
    id: "game1",
    userId: ALICE_UID,
    title: "Game 1",
    platform: "PC",
    status: "playing",
    createdAt: new Date(),
  }));
});

test("Authenticated user: cannot read another user's profile, games, or sessions", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    const db = context.firestore();
    await db.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      createdAt: new Date(),
    });
    await db.collection("users").doc(ALICE_UID).collection("games").doc("game1").set({
      id: "game1",
      userId: ALICE_UID,
      title: "Alice Game",
      platform: "PC",
      status: "playing",
      createdAt: new Date(),
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("games").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("games").doc("game1").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("sessions").get());
});

test("Alice can create and read her own profile, games, and sessions", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  
  // Profile
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).set({
    userId: ALICE_UID,
    displayName: "Alice",
    createdAt: new Date(),
  }));
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());

  // Game
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("games").doc("game_1").set({
    id: "game_1",
    userId: ALICE_UID,
    title: "Zelda: Breath of the Wild",
    platform: "Nintendo Switch",
    status: "playing",
    releaseYear: 2017,
    rating: 10,
    review: "Masterpiece",
    totalPlayTimeSeconds: 3600,
    createdAt: new Date(),
  }));
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("games").get());

  // Session
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("sessions").doc("session_1").set({
    id: "session_1",
    userId: ALICE_UID,
    gameId: "game_1",
    gameTitle: "Zelda",
    platform: "Switch",
    durationSeconds: 1800,
    createdAt: new Date(),
  }));
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("sessions").get());
});

test("Validation rejects invalid game schema (e.g. rating out of range or wrong userId)", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();

  // Invalid userId in payload
  await assertFails(aliceDb.collection("users").doc(ALICE_UID).collection("games").doc("game_bad").set({
    id: "game_bad",
    userId: BOB_UID,
    title: "Invalid Game",
    platform: "PC",
    status: "playing",
    createdAt: new Date(),
  }));

  // Invalid rating > 10
  await assertFails(aliceDb.collection("users").doc(ALICE_UID).collection("games").doc("game_bad2").set({
    id: "game_bad2",
    userId: ALICE_UID,
    title: "Invalid Game",
    platform: "PC",
    status: "playing",
    rating: 15,
    createdAt: new Date(),
  }));
});
