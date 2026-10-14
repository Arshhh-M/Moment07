// SocialX Web Application Node.js Server
// Listens on port 3000 and serves both static assets & JSON API

const http = require("http");
const fs = require("fs");
const path = require("path");
const crypto = require("crypto");

const PORT = process.env.PORT || 3000;
const PUBLIC_DIR = path.join(__dirname, "public");
const DATA_FILE = path.join(__dirname, "web-data.json");

// Default initial database state (Real user created posts & community accounts)
let db = {
  users: [
    {
      id: "user_official",
      username: "socialx",
      displayName: "SocialX Official",
      email: "official@socialx.app",
      bio: "Welcome to SocialX! Moments, Reels, Stories & 1M+ Songs ✨",
      photoUrl: "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=200&h=200&fit=crop",
      passwordHash: "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8", // "password"
      passwordSalt: "defaultsalt123",
      followersCount: 124,
      followingCount: 12,
      savedPostIds: []
    }
  ],
  posts: [
    {
      id: "post_welcome",
      authorId: "user_official",
      authorUsername: "socialx",
      authorDisplayName: "SocialX Official",
      authorPhotoUrl: "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=200&h=200&fit=crop",
      type: "post",
      caption: "Welcome to the all-new SocialX Web App! 🚀 End-to-End Encrypted messaging, 1,000,000+ tracks for Stories, Reels, Posts & Notes! ✨",
      mediaUrl: "https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?w=800&fit=crop",
      songTitle: "Espresso",
      songArtist: "Sabrina Carpenter",
      songCoverUrl: "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=300&fit=crop",
      likesCount: 18,
      likedBy: [],
      commentsCount: 2,
      repostsCount: 4,
      createdAt: new Date().toISOString()
    }
  ],
  stories: [
    {
      id: "story_1",
      authorId: "user_official",
      authorUsername: "socialx",
      authorDisplayName: "SocialX Official",
      authorPhotoUrl: "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=200&h=200&fit=crop",
      mediaUrl: "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800&fit=crop",
      songTitle: "Kesariya",
      songArtist: "Arijit Singh",
      createdAt: new Date().toISOString()
    }
  ],
  notes: [
    {
      id: "note_welcome",
      userId: "user_official",
      username: "socialx",
      displayName: "SocialX Official",
      userPhotoUrl: "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=200&h=200&fit=crop",
      text: "Vibing to 1M+ music tracks 🎶",
      songTitle: "Blinding Lights",
      songArtist: "The Weeknd",
      createdAt: new Date().toISOString()
    }
  ],
  conversations: [],
  messages: {}
};

// Load saved data if file exists
try {
  if (fs.existsSync(DATA_FILE)) {
    const raw = fs.readFileSync(DATA_FILE, "utf8");
    db = JSON.parse(raw);
  }
} catch (e) {
  console.warn("Could not load DATA_FILE, using defaults", e);
}

function persistDb() {
  try {
    fs.writeFileSync(DATA_FILE, JSON.stringify(db, null, 2), "utf8");
  } catch (e) {
    console.error("Failed to write DATA_FILE", e);
  }
}

function hashPassword(password, salt) {
  return crypto.createHash("sha256").update(salt + password).digest("hex");
}

function sendJson(res, statusCode, data) {
  res.writeHead(statusCode, {
    "Content-Type": "application/json",
    "Access-Control-Allow-Origin": "*",
    "Access-Control-Allow-Methods": "GET, POST, PUT, DELETE, OPTIONS",
    "Access-Control-Allow-Headers": "Content-Type"
  });
  res.end(JSON.stringify(data));
}

function serveStatic(req, res, filePath) {
  const ext = path.extname(filePath).toLowerCase();
  const mimeTypes = {
    ".html": "text/html",
    ".js": "application/javascript",
    ".css": "text/css",
    ".json": "application/json",
    ".png": "image/png",
    ".jpg": "image/jpeg",
    ".svg": "image/svg+xml"
  };

  const contentType = mimeTypes[ext] || "application/octet-stream";
  fs.readFile(filePath, (err, content) => {
    if (err) {
      if (err.code === "ENOENT") {
        // Fallback to index.html for SPA routing
        fs.readFile(path.join(PUBLIC_DIR, "index.html"), (err2, fallback) => {
          if (err2) {
            res.writeHead(404, { "Content-Type": "text/plain" });
            res.end("Not Found");
          } else {
            res.writeHead(200, { "Content-Type": "text/html" });
            res.end(fallback);
          }
        });
      } else {
        res.writeHead(500, { "Content-Type": "text/plain" });
        res.end("Server Error");
      }
    } else {
      res.writeHead(200, { "Content-Type": contentType });
      res.end(content);
    }
  });
}

function parseBody(req) {
  return new Promise((resolve, reject) => {
    let body = "";
    req.on("data", chunk => { body += chunk; });
    req.on("end", () => {
      try {
        resolve(body ? JSON.parse(body) : {});
      } catch (e) {
        reject(e);
      }
    });
    req.on("error", reject);
  });
}

const server = http.createServer(async (req, res) => {
  const parsedUrl = new URL(req.url, `http://${req.headers.host || "localhost"}`);
  const pathname = parsedUrl.pathname;

  if (req.method === "OPTIONS") {
    res.writeHead(204, {
      "Access-Control-Allow-Origin": "*",
      "Access-Control-Allow-Methods": "GET, POST, PUT, DELETE, OPTIONS",
      "Access-Control-Allow-Headers": "Content-Type"
    });
    res.end();
    return;
  }

  // --- API Endpoints ---
  if (pathname.startsWith("/api/")) {
    // Auth: Login with Username & Password only
    if (pathname === "/api/auth/login" && req.method === "POST") {
      try {
        const { username, password } = await parseBody(req);
        const clean = (username || "").trim().toLowerCase().replace(/^@/, "");
        const user = db.users.find(u => u.username.toLowerCase() === clean);

        if (!user) {
          return sendJson(res, 401, { error: `No account found for @${clean}. Please check username or sign up.` });
        }

        const computedHash = hashPassword(password, user.passwordSalt || "defaultsalt123");
        if (computedHash !== user.passwordHash) {
          return sendJson(res, 401, { error: "Incorrect password. Please try again." });
        }

        return sendJson(res, 200, {
          user: {
            id: user.id,
            username: user.username,
            displayName: user.displayName,
            email: user.email,
            bio: user.bio,
            photoUrl: user.photoUrl,
            followersCount: user.followersCount || 0,
            followingCount: user.followingCount || 0,
            savedPostIds: user.savedPostIds || []
          }
        });
      } catch (e) {
        return sendJson(res, 500, { error: e.message });
      }
    }

    // Auth: Register with Email, Username, Name, Password
    if (pathname === "/api/auth/register" && req.method === "POST") {
      try {
        const { email, username, displayName, password, photoUrl } = await parseBody(req);
        const cleanUser = (username || "").trim().toLowerCase().replace(/^@/, "");
        const cleanEmail = (email || "").trim().toLowerCase();

        if (!cleanEmail || !cleanUser || !password || password.length < 6) {
          return sendJson(res, 400, { error: "Invalid registration parameters." });
        }

        if (db.users.some(u => u.username.toLowerCase() === cleanUser)) {
          return sendJson(res, 409, { error: `Username @${cleanUser} is already taken.` });
        }

        const salt = crypto.randomBytes(12).toString("hex");
        const hash = hashPassword(password, salt);
        const newUser = {
          id: `user_${Date.now()}`,
          username: cleanUser,
          displayName: displayName || cleanUser,
          email: cleanEmail,
          bio: "Hey there! I am using SocialX ✨",
          photoUrl: photoUrl || "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&h=200&fit=crop",
          passwordHash: hash,
          passwordSalt: salt,
          followersCount: 0,
          followingCount: 0,
          savedPostIds: []
        };

        db.users.push(newUser);
        persistDb();

        return sendJson(res, 201, {
          user: {
            id: newUser.id,
            username: newUser.username,
            displayName: newUser.displayName,
            email: newUser.email,
            bio: newUser.bio,
            photoUrl: newUser.photoUrl,
            followersCount: 0,
            followingCount: 0,
            savedPostIds: []
          }
        });
      } catch (e) {
        return sendJson(res, 500, { error: e.message });
      }
    }

    // Posts: GET /api/posts
    if (pathname === "/api/posts" && req.method === "GET") {
      return sendJson(res, 200, db.posts);
    }

    // Posts: POST /api/posts
    if (pathname === "/api/posts" && req.method === "POST") {
      try {
        const data = await parseBody(req);
        const newPost = {
          id: `post_${Date.now()}`,
          authorId: data.authorId,
          authorUsername: data.authorUsername,
          authorDisplayName: data.authorDisplayName,
          authorPhotoUrl: data.authorPhotoUrl,
          type: data.type || "post",
          caption: data.caption || "",
          mediaUrl: data.mediaUrl || "",
          songTitle: data.songTitle || "",
          songArtist: data.songArtist || "",
          songCoverUrl: data.songCoverUrl || "",
          likesCount: 0,
          likedBy: [],
          commentsCount: 0,
          repostsCount: 0,
          createdAt: new Date().toISOString()
        };
        db.posts.unshift(newPost);
        persistDb();
        return sendJson(res, 201, newPost);
      } catch (e) {
        return sendJson(res, 500, { error: e.message });
      }
    }

    // Like Post: POST /api/posts/:id/like
    if (pathname.match(/^\/api\/posts\/[^/]+\/like$/) && req.method === "POST") {
      const postId = pathname.split("/")[3];
      const { userId, isLiked } = await parseBody(req);
      const post = db.posts.find(p => p.id === postId);
      if (post) {
        post.likedBy = post.likedBy || [];
        if (isLiked) {
          if (!post.likedBy.includes(userId)) post.likedBy.push(userId);
        } else {
          post.likedBy = post.likedBy.filter(id => id !== userId);
        }
        post.likesCount = post.likedBy.length;
        persistDb();
        return sendJson(res, 200, { success: true, likesCount: post.likesCount });
      }
      return sendJson(res, 404, { error: "Post not found" });
    }

    // Stories: GET /api/stories
    if (pathname === "/api/stories" && req.method === "GET") {
      return sendJson(res, 200, db.stories);
    }

    // Stories: POST /api/stories
    if (pathname === "/api/stories" && req.method === "POST") {
      try {
        const data = await parseBody(req);
        const story = {
          id: `story_${Date.now()}`,
          authorId: data.authorId,
          authorUsername: data.authorUsername,
          authorDisplayName: data.authorDisplayName,
          authorPhotoUrl: data.authorPhotoUrl,
          mediaUrl: data.mediaUrl || "",
          songTitle: data.songTitle || "",
          songArtist: data.songArtist || "",
          createdAt: new Date().toISOString()
        };
        db.stories.unshift(story);
        persistDb();
        return sendJson(res, 201, story);
      } catch (e) {
        return sendJson(res, 500, { error: e.message });
      }
    }

    // Notes: GET /api/notes
    if (pathname === "/api/notes" && req.method === "GET") {
      return sendJson(res, 200, db.notes);
    }

    // Notes: POST /api/notes
    if (pathname === "/api/notes" && req.method === "POST") {
      try {
        const data = await parseBody(req);
        // Replace existing note for user if any
        db.notes = db.notes.filter(n => n.userId !== data.userId);
        const note = {
          id: `note_${Date.now()}`,
          userId: data.userId,
          username: data.username,
          displayName: data.displayName,
          userPhotoUrl: data.userPhotoUrl,
          text: data.text || "",
          songTitle: data.songTitle || "",
          songArtist: data.songArtist || "",
          createdAt: new Date().toISOString()
        };
        db.notes.unshift(note);
        persistDb();
        return sendJson(res, 201, note);
      } catch (e) {
        return sendJson(res, 500, { error: e.message });
      }
    }

    // Conversations: GET /api/conversations
    if (pathname === "/api/conversations" && req.method === "GET") {
      const userId = parsedUrl.searchParams.get("userId");
      const userConvs = db.conversations.filter(c => (c.participantIds || []).includes(userId));
      return sendJson(res, 200, userConvs);
    }

    // Messages: GET /api/conversations/:id/messages
    if (pathname.match(/^\/api\/conversations\/[^/]+\/messages$/) && req.method === "GET") {
      const convId = pathname.split("/")[3];
      const msgs = db.messages[convId] || [];
      return sendJson(res, 200, msgs);
    }

    // Messages: POST /api/conversations/:id/messages (Stores E2EE Ciphertext)
    if (pathname.match(/^\/api\/conversations\/[^/]+\/messages$/) && req.method === "POST") {
      try {
        const convId = pathname.split("/")[3];
        const data = await parseBody(req);
        const msg = {
          id: `msg_${Date.now()}`,
          conversationId: convId,
          senderId: data.senderId,
          senderUsername: data.senderUsername,
          text: data.text, // Encrypted ciphertext
          createdAt: new Date().toISOString()
        };
        db.messages[convId] = db.messages[convId] || [];
        db.messages[convId].push(msg);

        // Update conversation preview
        let conv = db.conversations.find(c => c.id === convId);
        if (conv) {
          conv.lastMessage = "Encrypted message";
          conv.lastMessageSenderId = data.senderId;
          conv.updatedAt = msg.createdAt;
        }
        persistDb();
        return sendJson(res, 201, msg);
      } catch (e) {
        return sendJson(res, 500, { error: e.message });
      }
    }

    // Notifications: GET /api/notifications
    if (pathname === "/api/notifications" && req.method === "GET") {
      return sendJson(res, 200, []);
    }

    return sendJson(res, 404, { error: "API route not found" });
  }

  // --- Serve Static Frontend Files ---
  let filePath = path.join(PUBLIC_DIR, pathname === "/" ? "index.html" : pathname);
  serveStatic(req, res, filePath);
});

server.listen(PORT, "0.0.0.0", () => {
  console.log(`SocialX Web Server running on http://0.0.0.0:${PORT}`);
});
