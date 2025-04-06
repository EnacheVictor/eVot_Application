const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

exports.deleteExpiredInvites = functions.pubsub
  .schedule("every 24 hours")
  .onRun(async (context) => {
    const db = admin.firestore();
    const now = Date.now();
    const expireTime = now - 24 * 60 * 60 * 1000;

    try {
      const snapshot = await db.collection("invites")
        .where("timestamp", "<", expireTime)
        .where("used", "==", false)
        .get();

      const deletions = [];

      snapshot.forEach(doc => {
        deletions.push(doc.ref.delete());
      });

      console.log(`✅ Deleted ${deletions.length} expired codes.`);
      return Promise.all(deletions);
    } catch (error) {
      console.error("❌ Error deleting expired invites:", error);
      return null;
    }
  });

exports.deleteExpiredInvitesTest = functions.https.onRequest(async (req, res) => {
  const db = admin.firestore();
  const now = Date.now();
  const expireTime = now - 24 * 60 * 60 * 1000;

  try {
    const snapshot = await db.collection("invites")
      .where("timestamp", "<", expireTime)
      .where("used", "==", false)
      .get();

    const deletions = [];

    snapshot.forEach(doc => {
      deletions.push(doc.ref.delete());
    });

    await Promise.all(deletions);

    console.log(`✅ Deleted ${deletions.length} expired codes.`);
    res.status(200).send(`✅ Deleted ${deletions.length} expired codes.`);
  } catch (error) {
    console.error("❌ Error deleting expired invites:", error);
    res.status(500).send("❌ Error deleting expired invites.");
  }
});