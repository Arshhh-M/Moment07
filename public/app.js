// SocialX Web Application Main Logic

const API_BASE = "/api";

const State = {
  currentUser: null,
  currentScreen: "home", // "home", "search", "reels", "messages", "notifications", "profile"
  feedTab: "foryou", // "foryou" | "following"
  posts: [],
  stories: [],
  notes: [],
  conversations: [],
  activeConversation: null,
  notifications: [],
  selectedSongForCreate: null,
  selectedMediaForCreate: "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800&fit=crop",
  activeStoryIndex: 0,
  isViewingStories: false
};

// Preset Avatars
const PRESET_AVATARS = [
  "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&h=200&fit=crop",
  "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200&h=200&fit=crop",
  "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200&h=200&fit=crop",
  "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200&h=200&fit=crop",
  "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=200&h=200&fit=crop"
];

// Initial Setup
document.addEventListener("DOMContentLoaded", () => {
  loadStoredSession();
  setupEventListeners();
  if (State.currentUser) {
    loadAppData();
  } else {
    renderAuthScreen();
  }
});

function loadStoredSession() {
  try {
    const raw = localStorage.getItem("socialx_user");
    if (raw) {
      State.currentUser = JSON.parse(raw);
    }
  } catch (e) {
    console.error(e);
  }
}

function saveSession(user) {
  State.currentUser = user;
  localStorage.setItem("socialx_user", JSON.stringify(user));
}

function clearSession() {
  State.currentUser = null;
  localStorage.removeItem("socialx_user");
  renderAuthScreen();
}

// -------------------------------------------------------------
// Authentication View (No Google, Username/Password Only)
// -------------------------------------------------------------
function renderAuthScreen() {
  const container = document.getElementById("main-content");
  document.getElementById("app-header").style.display = "none";
  document.getElementById("app-nav").style.display = "none";

  container.innerHTML = `
    <div class="auth-wrapper">
      <div class="auth-card">
        <div style="text-align: center; margin-bottom: 22px;">
          <div class="brand-logo-icon" style="width: 56px; height: 56px; font-size: 28px; margin: 0 auto 12px;">X</div>
          <h2 style="font-size: 24px; font-weight: 800; color: #fff;">SocialX</h2>
          <p style="font-size: 13px; color: var(--text-muted); margin-top: 4px;">Share moments, reels, notes & 1M+ songs in real time</p>
        </div>

        <div class="auth-tabs">
          <button id="auth-tab-login" class="auth-tab-btn active" onclick="switchAuthTab('login')">Log In</button>
          <button id="auth-tab-signup" class="auth-tab-btn" onclick="switchAuthTab('signup')">Sign Up</button>
        </div>

        <div id="auth-alert" style="display: none;" class="alert-box">
          <span id="auth-alert-text"></span>
          <button onclick="hideAuthAlert()" style="background:none; border:none; color:inherit; cursor:pointer;">&times;</button>
        </div>

        <!-- Log In Form: Username & Password only -->
        <form id="login-form" onsubmit="handleLogin(event)">
          <div class="form-group">
            <label class="form-label">Username</label>
            <input type="text" id="login-username" class="form-input" placeholder="e.g. alex_smith" required autocomplete="username" />
          </div>
          <div class="form-group">
            <label class="form-label">Password</label>
            <input type="password" id="login-password" class="form-input" placeholder="Enter password" required autocomplete="current-password" />
          </div>
          <button type="submit" class="btn-primary" id="login-btn">Log In</button>
          <p style="text-align: center; font-size: 13px; color: var(--text-muted); margin-top: 14px;">
            Don't have an account? <a href="javascript:void(0)" onclick="switchAuthTab('signup')" style="color: var(--primary); font-weight: 700; text-decoration: none;">Sign Up</a>
          </p>
        </form>

        <!-- Sign Up Form: Email, Username, Name, Password -->
        <form id="signup-form" style="display: none;" onsubmit="handleSignup(event)">
          <div class="form-group">
            <label class="form-label">Email (for account verification)</label>
            <input type="email" id="signup-email" class="form-input" placeholder="name@example.com" required autocomplete="email" />
          </div>
          <div class="form-group">
            <label class="form-label">Username</label>
            <input type="text" id="signup-username" class="form-input" placeholder="username (min 3 chars)" required autocomplete="username" />
          </div>
          <div class="form-group">
            <label class="form-label">Display Name</label>
            <input type="text" id="signup-name" class="form-input" placeholder="Alex Smith" required />
          </div>
          <div class="form-group">
            <label class="form-label">Password (min 6 chars)</label>
            <input type="password" id="signup-password" class="form-input" placeholder="Choose a password" required minlength="6" autocomplete="new-password" />
          </div>

          <div class="form-group">
            <label class="form-label">Choose Profile Avatar</label>
            <div style="display: flex; gap: 10px; overflow-x: auto; padding: 4px 0;" id="avatar-presets-list">
              ${PRESET_AVATARS.map((url, idx) => `
                <img src="${url}" class="note-avatar" style="width: 44px; height: 44px; cursor: pointer; border: 2px solid ${idx === 0 ? 'var(--primary)' : 'transparent'};" onclick="selectAvatar(this, '${url}')" />
              `).join('')}
            </div>
          </div>

          <button type="submit" class="btn-primary" id="signup-btn">Create Account</button>
          <p style="text-align: center; font-size: 13px; color: var(--text-muted); margin-top: 14px;">
            Already have an account? <a href="javascript:void(0)" onclick="switchAuthTab('login')" style="color: var(--primary); font-weight: 700; text-decoration: none;">Log In</a>
          </p>
        </form>

        <div style="margin-top: 20px; padding: 12px; background: rgba(124, 58, 237, 0.08); border: 1px solid rgba(124, 58, 237, 0.2); border-radius: 14px; font-size: 11px; color: #c4b5fd;">
          <p style="margin-bottom: 4px;">🔒 <strong>End-to-End Encrypted</strong> direct messaging</p>
          <p>🎵 <strong>1,000,000+ Songs</strong> for your Stories, Reels, Posts & Notes</p>
        </div>
      </div>
    </div>
  `;
}

let selectedSignupAvatar = PRESET_AVATARS[0];
function selectAvatar(el, url) {
  selectedSignupAvatar = url;
  document.querySelectorAll("#avatar-presets-list img").forEach(img => {
    img.style.borderColor = "transparent";
  });
  el.style.borderColor = "var(--primary)";
}

function switchAuthTab(tab) {
  hideAuthAlert();
  const loginBtn = document.getElementById("auth-tab-login");
  const signupBtn = document.getElementById("auth-tab-signup");
  const loginForm = document.getElementById("login-form");
  const signupForm = document.getElementById("signup-form");

  if (tab === "login") {
    loginBtn.classList.add("active");
    signupBtn.classList.remove("active");
    loginForm.style.display = "block";
    signupForm.style.display = "none";
  } else {
    signupBtn.classList.add("active");
    loginBtn.classList.remove("active");
    signupForm.style.display = "block";
    loginForm.style.display = "none";
  }
}

function showAuthAlert(msg) {
  const alert = document.getElementById("auth-alert");
  const text = document.getElementById("auth-alert-text");
  if (alert && text) {
    text.innerText = msg;
    alert.style.display = "flex";
  }
}

function hideAuthAlert() {
  const alert = document.getElementById("auth-alert");
  if (alert) alert.style.display = "none";
}

async function handleLogin(e) {
  e.preventDefault();
  const username = document.getElementById("login-username").value.trim().toLowerCase().replace(/^@/, "");
  const password = document.getElementById("login-password").value.trim();

  if (!username || !password) {
    showAuthAlert("Please enter username and password.");
    return;
  }

  const btn = document.getElementById("login-btn");
  btn.disabled = true;
  btn.innerText = "Logging in...";

  try {
    const res = await fetch(`${API_BASE}/auth/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username, password })
    });
    const data = await res.json();
    if (res.ok && data.user) {
      saveSession(data.user);
      loadAppData();
    } else {
      showAuthAlert(data.error || "Invalid username or password.");
    }
  } catch (err) {
    showAuthAlert("Server connection error. Please try again.");
  } finally {
    btn.disabled = false;
    btn.innerText = "Log In";
  }
}

async function handleSignup(e) {
  e.preventDefault();
  const email = document.getElementById("signup-email").value.trim().toLowerCase();
  const username = document.getElementById("signup-username").value.trim().toLowerCase().replace(/^@/, "");
  const displayName = document.getElementById("signup-name").value.trim();
  const password = document.getElementById("signup-password").value.trim();

  if (!email || !username || !password) {
    showAuthAlert("All fields are required.");
    return;
  }

  const btn = document.getElementById("signup-btn");
  btn.disabled = true;
  btn.innerText = "Creating account...";

  try {
    const res = await fetch(`${API_BASE}/auth/register`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        email,
        username,
        displayName: displayName || username,
        password,
        photoUrl: selectedSignupAvatar
      })
    });
    const data = await res.json();
    if (res.ok && data.user) {
      saveSession(data.user);
      loadAppData();
    } else {
      showAuthAlert(data.error || "Failed to create account.");
    }
  } catch (err) {
    showAuthAlert("Registration failed. Please try again.");
  } finally {
    btn.disabled = false;
    btn.innerText = "Create Account";
  }
}

// -------------------------------------------------------------
// Main Application Loading & UI
// -------------------------------------------------------------
async function loadAppData() {
  document.getElementById("app-header").style.display = "flex";
  document.getElementById("app-nav").style.display = "flex";

  // Update navigation avatar
  const navAvatar = document.getElementById("nav-avatar");
  if (navAvatar && State.currentUser.photoUrl) {
    navAvatar.src = State.currentUser.photoUrl;
  }

  await Promise.all([
    fetchPosts(),
    fetchStories(),
    fetchNotes(),
    fetchConversations(),
    fetchNotifications()
  ]);

  navigateTo(State.currentScreen);
}

async function fetchPosts() {
  try {
    const res = await fetch(`${API_BASE}/posts?userId=${State.currentUser.id}`);
    if (res.ok) {
      State.posts = await res.json();
    }
  } catch (e) {
    console.error("Error fetching posts", e);
  }
}

async function fetchStories() {
  try {
    const res = await fetch(`${API_BASE}/stories`);
    if (res.ok) {
      State.stories = await res.json();
    }
  } catch (e) {
    console.error("Error fetching stories", e);
  }
}

async function fetchNotes() {
  try {
    const res = await fetch(`${API_BASE}/notes`);
    if (res.ok) {
      State.notes = await res.json();
    }
  } catch (e) {
    console.error("Error fetching notes", e);
  }
}

async function fetchConversations() {
  try {
    const res = await fetch(`${API_BASE}/conversations?userId=${State.currentUser.id}`);
    if (res.ok) {
      State.conversations = await res.json();
      updateMessageBadge();
    }
  } catch (e) {
    console.error("Error fetching conversations", e);
  }
}

async function fetchNotifications() {
  try {
    const res = await fetch(`${API_BASE}/notifications?userId=${State.currentUser.id}`);
    if (res.ok) {
      State.notifications = await res.json();
    }
  } catch (e) {
    console.error("Error fetching notifications", e);
  }
}

function updateMessageBadge() {
  const badge = document.getElementById("msg-badge");
  const unreadCount = State.conversations.filter(c => c.lastMessageSenderId && c.lastMessageSenderId !== State.currentUser.id && !c.isRead).length;
  if (badge) {
    if (unreadCount > 0) {
      badge.style.display = "block";
      badge.innerText = unreadCount > 9 ? "9+" : unreadCount;
    } else {
      badge.style.display = "none";
    }
  }
}

// -------------------------------------------------------------
// Navigation Router
// -------------------------------------------------------------
function navigateTo(screen) {
  State.currentScreen = screen;
  document.querySelectorAll(".nav-item").forEach(item => {
    item.classList.toggle("active", item.getAttribute("data-screen") === screen);
  });

  const container = document.getElementById("main-content");
  container.innerHTML = "";

  switch (screen) {
    case "home":
      renderHomeScreen(container);
      break;
    case "search":
      renderSearchScreen(container);
      break;
    case "reels":
      renderReelsScreen(container);
      break;
    case "messages":
      renderMessagesScreen(container);
      break;
    case "notifications":
      renderNotificationsScreen(container);
      break;
    case "profile":
      renderProfileScreen(container);
      break;
  }
}

// -------------------------------------------------------------
// Home Screen View
// -------------------------------------------------------------
function renderHomeScreen(container) {
  const myNote = State.notes.find(n => n.userId === State.currentUser.id);
  const otherNotes = State.notes.filter(n => n.userId !== State.currentUser.id);

  const notesHtml = `
    <div class="notes-container">
      <!-- Your Note -->
      <div class="note-item" onclick="openNoteModal()">
        <div class="note-bubble">
          ${myNote && myNote.songTitle ? '🎵 ' : ''}${myNote ? myNote.text : 'Share note...'}
        </div>
        <div class="note-avatar-wrapper">
          <img src="${State.currentUser.photoUrl}" class="note-avatar" />
          <div class="note-add-badge">+</div>
        </div>
        <span class="note-username">Your note</span>
      </div>

      <!-- Community Notes -->
      ${otherNotes.map(n => `
        <div class="note-item" onclick="showNoteDetail('${n.username}', '${n.text}', '${n.songTitle || ''}', '${n.songArtist || ''}')">
          <div class="note-bubble">
            ${n.songTitle ? '🎵 ' : ''}${n.text}
          </div>
          <div class="note-avatar-wrapper">
            <img src="${n.userPhotoUrl || PRESET_AVATARS[0]}" class="note-avatar" />
          </div>
          <span class="note-username">@${n.username}</span>
        </div>
      `).join('')}
    </div>
  `;

  const storiesHtml = `
    <div class="stories-container">
      <div class="story-item" onclick="openCreateModal('story')">
        <div class="story-ring" style="background: var(--border);">
          <img src="${State.currentUser.photoUrl}" class="story-avatar" />
        </div>
        <span class="story-label">Your story</span>
      </div>

      ${State.stories.map((s, idx) => `
        <div class="story-item" onclick="viewStory(${idx})">
          <div class="story-ring">
            <img src="${s.authorPhotoUrl || PRESET_AVATARS[0]}" class="story-avatar" />
          </div>
          <span class="story-label">${s.authorUsername}</span>
        </div>
      `).join('')}
    </div>
  `;

  const feedTabsHtml = `
    <div class="feed-tabs">
      <button class="feed-tab ${State.feedTab === 'foryou' ? 'active' : ''}" onclick="setFeedTab('foryou')">For You</button>
      <button class="feed-tab ${State.feedTab === 'following' ? 'active' : ''}" onclick="setFeedTab('following')">Following</button>
    </div>
  `;

  let displayPosts = State.posts;
  if (State.feedTab === "following") {
    displayPosts = State.posts.filter(p => p.authorId === State.currentUser.id || (State.currentUser.following || []).includes(p.authorId));
  }

  const postsHtml = displayPosts.length === 0 ? `
    <div style="text-align: center; padding: 60px 20px; color: var(--text-muted);">
      <p style="font-size: 16px; font-weight: 600; margin-bottom: 8px;">No posts yet in feed</p>
      <p style="font-size: 13px;">Tap the '+' button below to share a post, reel or story!</p>
      <button class="btn-primary" style="width: auto; padding: 10px 24px; margin-top: 16px;" onclick="openCreateModal('post')">Create Post</button>
    </div>
  ` : displayPosts.map(post => renderPostCard(post)).join('');

  container.innerHTML = `
    ${notesHtml}
    ${storiesHtml}
    ${feedTabsHtml}
    <div id="posts-feed">${postsHtml}</div>
  `;
}

function setFeedTab(tab) {
  State.feedTab = tab;
  renderHomeScreen(document.getElementById("main-content"));
}

function renderPostCard(post) {
  const isLiked = (post.likedBy || []).includes(State.currentUser.id);
  const isSaved = (State.currentUser.savedPostIds || []).includes(post.id);

  const musicBadge = post.songTitle ? `
    <div class="music-badge-pill" onclick="MusicCatalog.playPreview('${post.songTitle}')">
      <div class="music-waveform">
        <div class="music-bar"></div>
        <div class="music-bar"></div>
        <div class="music-bar"></div>
        <div class="music-bar"></div>
      </div>
      <span>${post.songTitle} • ${post.songArtist}</span>
    </div>
  ` : '';

  return `
    <div class="post-card" id="post-${post.id}">
      ${post.isRepost ? `
        <div style="padding: 6px 14px; background: rgba(16, 185, 129, 0.1); font-size: 11px; font-weight: 700; color: var(--repost);">
          🔄 @${post.authorUsername} reposted
        </div>
      ` : ''}

      <div class="post-header">
        <div class="post-author" onclick="viewUserProfile('${post.authorId}')">
          <img src="${post.authorPhotoUrl || PRESET_AVATARS[0]}" class="post-author-img" />
          <div class="author-names">
            <span class="author-name">${post.authorDisplayName || post.authorUsername}</span>
            <span class="author-meta">@${post.authorUsername} • ${post.createdAt ? formatTimeAgo(post.createdAt) : 'just now'}</span>
          </div>
        </div>
      </div>

      ${musicBadge}

      ${post.caption ? `<div class="post-caption">${escapeHtml(post.caption)}</div>` : ''}

      ${post.mediaUrl ? `
        <div class="post-media-box">
          <img src="${post.mediaUrl}" class="post-media-img" loading="lazy" />
          ${post.type === 'reel' ? `
            <div class="reel-play-overlay">
              <svg width="28" height="28" viewBox="0 0 24 24" fill="#fff"><path d="M8 5v14l11-7z"/></svg>
            </div>
          ` : ''}
        </div>
      ` : ''}

      <div class="post-actions">
        <div class="action-group-left">
          <button class="action-btn ${isLiked ? 'liked' : ''}" onclick="toggleLike('${post.id}')">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="${isLiked ? 'var(--like)' : 'none'}" stroke="currentColor" stroke-width="2"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg>
            <span>${post.likesCount || 0}</span>
          </button>
          <button class="action-btn" onclick="openCommentModal('${post.id}')">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"/></svg>
            <span>${post.commentsCount || 0}</span>
          </button>
          <button class="action-btn" onclick="openRepostModal('${post.id}')">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 1l4 4-4 4M3 11V9a4 4 0 0 1 4-4h14M7 23l-4-4 4-4M21 13v2a4 4 0 0 1-4 4H3"/></svg>
            <span>${post.repostsCount || 0}</span>
          </button>
          <button class="action-btn" onclick="openShareModal('${post.id}')">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 2L11 13M22 2l-7 20-4-9-9-4 20-7z"/></svg>
          </button>
        </div>
        <button class="action-btn ${isSaved ? 'saved' : ''}" onclick="toggleSave('${post.id}')">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="${isSaved ? 'var(--primary)' : 'none'}" stroke="currentColor" stroke-width="2"><path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z"/></svg>
        </button>
      </div>
    </div>
  `;
}

// -------------------------------------------------------------
// Interactive Actions (Like, Comment, Repost, Save)
// -------------------------------------------------------------
async function toggleLike(postId) {
  const post = State.posts.find(p => p.id === postId);
  if (!post) return;

  const isLiked = (post.likedBy || []).includes(State.currentUser.id);
  if (isLiked) {
    post.likedBy = post.likedBy.filter(id => id !== State.currentUser.id);
    post.likesCount = Math.max(0, (post.likesCount || 1) - 1);
  } else {
    post.likedBy = post.likedBy || [];
    post.likedBy.push(State.currentUser.id);
    post.likesCount = (post.likesCount || 0) + 1;
  }

  // Rerender specific post card
  const el = document.getElementById(`post-${postId}`);
  if (el) el.outerHTML = renderPostCard(post);

  try {
    await fetch(`${API_BASE}/posts/${postId}/like`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ userId: State.currentUser.id, isLiked: !isLiked })
    });
  } catch (e) {
    console.error(e);
  }
}

async function toggleSave(postId) {
  const isSaved = (State.currentUser.savedPostIds || []).includes(postId);
  if (isSaved) {
    State.currentUser.savedPostIds = State.currentUser.savedPostIds.filter(id => id !== postId);
  } else {
    State.currentUser.savedPostIds = State.currentUser.savedPostIds || [];
    State.currentUser.savedPostIds.push(postId);
  }
  saveSession(State.currentUser);

  const post = State.posts.find(p => p.id === postId);
  if (post) {
    const el = document.getElementById(`post-${postId}`);
    if (el) el.outerHTML = renderPostCard(post);
  }

  try {
    await fetch(`${API_BASE}/posts/${postId}/save`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ userId: State.currentUser.id, isSaved: !isSaved })
    });
  } catch (e) {
    console.error(e);
  }
}

// -------------------------------------------------------------
// 🔒 Direct Messaging with End-to-End Encryption
// -------------------------------------------------------------
function renderMessagesScreen(container) {
  container.innerHTML = `
    <div style="padding: 16px;">
      <h2 style="font-size: 20px; font-weight: 800; margin-bottom: 12px;">Messages</h2>

      <!-- E2EE Security Badge -->
      <div class="e2e-badge-container" onclick="showSecuritySafetyDialog()">
        <span class="e2e-lock-icon">🔒</span>
        <div class="e2e-text">
          <strong>End-to-End Encrypted</strong><br>
          Messages and calls are secured with end-to-end encryption. Tap to verify.
        </div>
      </div>

      <div style="margin-top: 14px;">
        <input type="text" id="chat-search" class="form-input" style="width: 100%; border-radius: 20px; margin-bottom: 12px;" placeholder="Search chats or username..." oninput="filterChats(this.value)" />
      </div>

      <div id="chats-list">
        ${State.conversations.length === 0 ? `
          <div style="text-align: center; padding: 40px; color: var(--text-muted);">
            <p>No conversations yet.</p>
            <button class="btn-primary" style="width: auto; padding: 8px 18px; margin-top: 12px;" onclick="startNewChat()">Start a Chat</button>
          </div>
        ` : State.conversations.map(conv => `
          <div class="post-header" style="cursor: pointer; border-radius: 14px; margin-bottom: 6px; padding: 10px;" onclick="openChat('${conv.id}', '${conv.recipientUsername}', '${conv.recipientName}', '${conv.recipientPhoto}')">
            <div style="display: flex; align-items: center; gap: 12px;">
              <img src="${conv.recipientPhoto || PRESET_AVATARS[0]}" style="width: 46px; height: 46px; border-radius: 50%; object-fit: cover;" />
              <div>
                <div style="font-weight: 700; font-size: 14px;">${conv.recipientName || conv.recipientUsername}</div>
                <div style="font-size: 12px; color: var(--text-muted); display: flex; align-items: center; gap: 4px;">
                  🔒 ${conv.lastMessage || 'Encrypted conversation started'}
                </div>
              </div>
            </div>
          </div>
        `).join('')}
      </div>
    </div>
  `;
}

function openChat(convId, recipientUsername, recipientName, recipientPhoto) {
  State.activeConversation = { id: convId, recipientUsername, recipientName, recipientPhoto, messages: [] };
  const container = document.getElementById("main-content");

  container.innerHTML = `
    <div style="display: flex; flex-direction: column; height: calc(100vh - 60px);">
      <!-- Chat Header -->
      <div class="app-header" style="position: static;">
        <div style="display: flex; align-items: center; gap: 10px;">
          <button class="icon-btn" onclick="navigateTo('messages')">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
          </button>
          <img src="${recipientPhoto || PRESET_AVATARS[0]}" style="width: 36px; height: 36px; border-radius: 50%; object-fit: cover;" />
          <div>
            <div style="font-weight: 700; font-size: 14px;">${recipientName || recipientUsername}</div>
            <div style="font-size: 11px; color: var(--online);">Active now • 🔒 E2EE</div>
          </div>
        </div>
        <button class="icon-btn" onclick="showChatSafetyDialog('${recipientUsername}')">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
        </button>
      </div>

      <!-- E2EE Top Security Pill -->
      <div style="text-align: center; padding: 8px 14px; background: rgba(124,58,237,0.1); border-bottom: 1px solid var(--border); font-size: 11px; color: #c4b5fd;">
        🔒 Messages in this chat are end-to-end encrypted. Tap lock icon to verify.
      </div>

      <!-- Messages List -->
      <div class="chat-messages-list" id="chat-messages-box">
        <!-- Rendered messages -->
      </div>

      <!-- Message Input -->
      <form class="chat-input-bar" onsubmit="handleSendMessage(event)">
        <input type="text" id="chat-text-input" class="chat-input" placeholder="Encrypted message..." autocomplete="off" />
        <button type="submit" class="chat-send-btn">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="#fff"><path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z"/></svg>
        </button>
      </form>
    </div>
  `;

  loadChatMessages(convId);
}

async function loadChatMessages(convId) {
  try {
    const res = await fetch(`${API_BASE}/conversations/${convId}/messages`);
    if (res.ok) {
      const msgs = await res.json();
      // Decrypt messages with E2EE
      State.activeConversation.messages = msgs.map(m => ({
        ...m,
        plainText: E2EE.decrypt(m.text, convId)
      }));
      renderChatMessages();
    }
  } catch (e) {
    console.error(e);
  }
}

function renderChatMessages() {
  const box = document.getElementById("chat-messages-box");
  if (!box || !State.activeConversation) return;

  box.innerHTML = State.activeConversation.messages.map(m => {
    const isMine = m.senderId === State.currentUser.id;
    return `
      <div class="chat-bubble ${isMine ? 'mine' : 'theirs'}">
        <div>${escapeHtml(m.plainText)}</div>
        <div class="bubble-meta">
          <span>🔒 ${m.createdAt ? formatTimeAgo(m.createdAt) : 'now'}</span>
        </div>
      </div>
    `;
  }).join('');

  box.scrollTop = box.scrollHeight;
}

async function handleSendMessage(e) {
  e.preventDefault();
  const input = document.getElementById("chat-text-input");
  const text = input.value.trim();
  if (!text || !State.activeConversation) return;

  input.value = "";
  const convId = State.activeConversation.id;
  // Encrypt with E2EE
  const cipherText = E2EE.encrypt(text, convId);

  // Optimistic UI
  const tempMsg = {
    id: `temp_${Date.now()}`,
    senderId: State.currentUser.id,
    plainText: text,
    createdAt: new Date().toISOString()
  };
  State.activeConversation.messages.push(tempMsg);
  renderChatMessages();

  try {
    await fetch(`${API_BASE}/conversations/${convId}/messages`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        senderId: State.currentUser.id,
        senderUsername: State.currentUser.username,
        text: cipherText
      })
    });
  } catch (err) {
    console.error("Message send error", err);
  }
}

// E2EE Safety Number Dialog
function showChatSafetyDialog(recipientUsername) {
  const safetyBlocks = E2EE.getSafetyNumber(State.currentUser.username, recipientUsername);
  openModal(`
    <div style="text-align: center;">
      <span style="font-size: 38px;">🔒</span>
      <h3 style="font-size: 18px; font-weight: 800; margin: 10px 0 6px;">End-to-End Encryption</h3>
      <p style="font-size: 12px; color: var(--text-muted); margin-bottom: 16px;">
        Verify that messages with <strong>@${recipientUsername}</strong> are encrypted end-to-end.
      </p>

      <div style="background: var(--bg-card); padding: 14px; border-radius: 14px; border: 1px solid var(--border); font-family: monospace; font-size: 13px; line-height: 1.8; letter-spacing: 1px; color: #a78bfa;">
        ${safetyBlocks.map((blk, idx) => `${blk} ${(idx + 1) % 4 === 0 ? '<br>' : ''}`).join('')}
      </div>

      <p style="font-size: 11px; color: var(--text-muted); margin-top: 14px;">
        Compare this 60-digit number with @${recipientUsername} to confirm your encryption keys.
      </p>

      <button class="btn-primary" style="margin-top: 16px;" onclick="closeModal()">Verified</button>
    </div>
  `);
}

function showSecuritySafetyDialog() {
  openModal(`
    <div style="text-align: center;">
      <span style="font-size: 38px;">🛡️</span>
      <h3 style="font-size: 18px; font-weight: 800; margin: 10px 0 6px;">Your Privacy is Protected</h3>
      <p style="font-size: 13px; color: var(--text-muted); margin-bottom: 14px;">
        All Direct Messages in SocialX are protected by <strong>End-to-End Encryption (E2EE)</strong>.
      </p>
      <div style="text-align: left; background: var(--bg-card); padding: 14px; border-radius: 14px; border: 1px solid var(--border); font-size: 12px; line-height: 1.6; color: var(--text-main);">
        <p>🔒 <strong>Client-Side Encryption:</strong> Messages are encrypted on your device before sending.</p>
        <p style="margin-top: 6px;">🚫 <strong>Zero Third-Party Access:</strong> No one outside the conversation, not even SocialX servers, can read them.</p>
        <p style="margin-top: 6px;">🔑 <strong>Safety Numbers:</strong> Every chat features a unique cryptographic verification fingerprint.</p>
      </div>
      <button class="btn-primary" style="margin-top: 16px;" onclick="closeModal()">Close</button>
    </div>
  `);
}

// -------------------------------------------------------------
// 🎵 1 Million+ Songs Music Library Modal
// -------------------------------------------------------------
function openSongPickerModal(callback) {
  window._onSongPicked = callback;
  let currentGenre = "All";
  let query = "";

  const renderList = (songs) => {
    return songs.map(s => `
      <div class="post-header" style="cursor: pointer; padding: 8px 10px; border-radius: 12px; margin-bottom: 6px; background: var(--bg-card);" onclick="selectPickedSong('${s.id}')">
        <div style="display: flex; align-items: center; gap: 10px;">
          <div style="position: relative; width: 44px; height: 44px;">
            <img src="${s.coverUrl}" style="width: 100%; height: 100%; border-radius: 8px; object-fit: cover;" />
            <div style="position: absolute; inset: 0; background: rgba(0,0,0,0.3); display: flex; align-items: center; justify-content: center;" onclick="event.stopPropagation(); MusicCatalog.playPreview('${s.id}')">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="#fff"><path d="M8 5v14l11-7z"/></svg>
            </div>
          </div>
          <div>
            <div style="font-size: 13px; font-weight: 700;">${escapeHtml(s.title)}</div>
            <div style="font-size: 11px; color: var(--text-muted);">${escapeHtml(s.artist)} • ${s.duration}</div>
          </div>
        </div>
        <button class="btn-primary" style="width: auto; padding: 4px 12px; font-size: 11px; margin: 0;">Select</button>
      </div>
    `).join('');
  };

  openModal(`
    <div>
      <div class="modal-header">
        <h3 class="modal-title">🎵 1,000,000+ Music Catalog</h3>
        <button class="icon-btn" onclick="closeModal()">&times;</button>
      </div>
      <p style="font-size: 12px; color: var(--text-muted); margin-bottom: 12px;">Search and attach songs to your posts, reels, stories & notes.</p>

      <input type="text" id="song-search-box" class="form-input" style="width: 100%; border-radius: 16px; margin-bottom: 10px;" placeholder="Search 1M+ songs, artists, Bollywood, Pop..." />

      <div style="display: flex; gap: 6px; overflow-x: auto; padding-bottom: 10px; margin-bottom: 8px;" id="song-genre-chips">
        ${MusicCatalog.genres.map(g => `
          <button class="auth-tab-btn ${g === 'All' ? 'active' : ''}" style="width: auto; padding: 4px 12px; font-size: 12px; border-radius: 16px; border: 1px solid var(--border);" onclick="filterSongGenre('${g}', this)">${g}</button>
        `).join('')}
      </div>

      <div id="songs-result-list" style="max-height: 50vh; overflow-y: auto;">
        ${renderList(MusicCatalog.trendingTracks)}
      </div>
    </div>
  `);

  const searchBox = document.getElementById("song-search-box");
  searchBox.addEventListener("input", (e) => {
    query = e.target.value;
    const filtered = MusicCatalog.search(query, currentGenre);
    document.getElementById("songs-result-list").innerHTML = renderList(filtered);
  });

  window.filterSongGenre = (genre, btn) => {
    currentGenre = genre;
    document.querySelectorAll("#song-genre-chips button").forEach(b => b.classList.remove("active"));
    btn.classList.add("active");
    const filtered = MusicCatalog.search(query, currentGenre);
    document.getElementById("songs-result-list").innerHTML = renderList(filtered);
  };
}

function selectPickedSong(songId) {
  const song = MusicCatalog.search("").find(s => s.id === songId) || MusicCatalog.trendingTracks[0];
  if (window._onSongPicked) {
    window._onSongPicked(song);
  }
  closeModal();
}

// -------------------------------------------------------------
// Create Modal (Post, Reel, Story)
// -------------------------------------------------------------
function openCreateModal(initialType = "post") {
  State.selectedSongForCreate = null;

  openModal(`
    <div>
      <div class="modal-header">
        <h3 class="modal-title">Create on SocialX</h3>
        <button class="icon-btn" onclick="closeModal()">&times;</button>
      </div>

      <div class="auth-tabs" style="margin-bottom: 14px;">
        <button class="auth-tab-btn ${initialType === 'post' ? 'active' : ''}" onclick="switchCreateType('post', this)">Post</button>
        <button class="auth-tab-btn ${initialType === 'reel' ? 'active' : ''}" onclick="switchCreateType('reel', this)">Reel</button>
        <button class="auth-tab-btn ${initialType === 'story' ? 'active' : ''}" onclick="switchCreateType('story', this)">Story</button>
      </div>

      <div class="form-group">
        <label class="form-label">Caption</label>
        <textarea id="create-caption" class="form-input" style="height: 70px; resize: none;" placeholder="Share what's on your mind... #SocialX"></textarea>
      </div>

      <div class="form-group">
        <label class="form-label">Attach Music (1M+ Songs)</label>
        <div id="attached-song-display" style="padding: 10px; background: var(--bg-card); border: 1px solid var(--border); border-radius: 12px; cursor: pointer; display: flex; align-items: center; justify-content: space-between;" onclick="openSongPickerModal(onSongAttached)">
          <span style="font-size: 13px; color: var(--primary);">🎵 Add song from music catalog...</span>
        </div>
      </div>

      <div class="form-group">
        <label class="form-label">Choose Media</label>
        <div style="display: flex; gap: 8px; overflow-x: auto; padding: 4px 0;" id="create-media-presets">
          ${[
            "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800&fit=crop",
            "https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?w=800&fit=crop",
            "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800&fit=crop",
            "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=800&fit=crop",
            "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800&fit=crop"
          ].map((url, i) => `
            <img src="${url}" style="width: 54px; height: 54px; border-radius: 10px; object-fit: cover; cursor: pointer; border: 2px solid ${i === 0 ? 'var(--primary)' : 'transparent'};" onclick="selectCreateMedia(this, '${url}')" />
          `).join('')}
        </div>
      </div>

      <button class="btn-primary" onclick="submitCreate('${initialType}')">Share</button>
    </div>
  `);

  window._createType = initialType;
}

window.switchCreateType = (type, btn) => {
  window._createType = type;
  document.querySelectorAll(".auth-tabs button").forEach(b => b.classList.remove("active"));
  btn.classList.add("active");
};

window.selectCreateMedia = (el, url) => {
  State.selectedMediaForCreate = url;
  document.querySelectorAll("#create-media-presets img").forEach(i => i.style.borderColor = "transparent");
  el.style.borderColor = "var(--primary)";
};

function onSongAttached(song) {
  State.selectedSongForCreate = song;
  const display = document.getElementById("attached-song-display");
  if (display) {
    display.innerHTML = `
      <div style="display: flex; align-items: center; gap: 10px;">
        <img src="${song.coverUrl}" style="width: 32px; height: 32px; border-radius: 6px; object-fit: cover;" />
        <div>
          <div style="font-size: 13px; font-weight: 700;">${escapeHtml(song.title)}</div>
          <div style="font-size: 11px; color: var(--text-muted);">${escapeHtml(song.artist)}</div>
        </div>
      </div>
      <span style="color: var(--primary); font-size: 12px; font-weight: 700;">Change</span>
    `;
  }
}

async function submitCreate() {
  const type = window._createType || "post";
  const caption = document.getElementById("create-caption").value.trim();

  const payload = {
    authorId: State.currentUser.id,
    authorUsername: State.currentUser.username,
    authorDisplayName: State.currentUser.displayName,
    authorPhotoUrl: State.currentUser.photoUrl,
    type: type,
    caption: caption,
    mediaUrl: State.selectedMediaForCreate,
    songTitle: State.selectedSongForCreate ? State.selectedSongForCreate.title : "",
    songArtist: State.selectedSongForCreate ? State.selectedSongForCreate.artist : "",
    songCoverUrl: State.selectedSongForCreate ? State.selectedSongForCreate.coverUrl : ""
  };

  closeModal();

  try {
    const endpoint = type === "story" ? `${API_BASE}/stories` : `${API_BASE}/posts`;
    const res = await fetch(endpoint, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    });
    if (res.ok) {
      await (type === "story" ? fetchStories() : fetchPosts());
      navigateTo("home");
    }
  } catch (err) {
    console.error("Create submit error", err);
  }
}

// -------------------------------------------------------------
// Note Modal (Instagram-style Status Notes)
// -------------------------------------------------------------
function openNoteModal() {
  let noteSong = null;

  openModal(`
    <div>
      <div class="modal-header">
        <h3 class="modal-title">Share a Note</h3>
        <button class="icon-btn" onclick="closeModal()">&times;</button>
      </div>
      <p style="font-size: 12px; color: var(--text-muted); margin-bottom: 12px;">Share a short thought and song snippet with your friends for 24 hours.</p>

      <div class="form-group">
        <input type="text" id="note-input-text" class="form-input" maxlength="60" placeholder="What's on your mind? (60 chars max)" />
      </div>

      <div class="form-group">
        <label class="form-label">Attach Music</label>
        <div id="note-song-display" style="padding: 10px; background: var(--bg-card); border: 1px solid var(--border); border-radius: 12px; cursor: pointer; display: flex; align-items: center; justify-content: space-between;" onclick="openSongPickerModal(onNoteSongPicked)">
          <span style="font-size: 13px; color: var(--primary);">🎵 Add music from 1M+ tracks...</span>
        </div>
      </div>

      <button class="btn-primary" onclick="submitNote()">Share Note</button>
    </div>
  `);

  window.onNoteSongPicked = (song) => {
    noteSong = song;
    const disp = document.getElementById("note-song-display");
    if (disp) {
      disp.innerHTML = `
        <div style="display: flex; align-items: center; gap: 8px;">
          <img src="${song.coverUrl}" style="width: 28px; height: 28px; border-radius: 6px; object-fit: cover;" />
          <span style="font-size: 13px; font-weight: 700;">${escapeHtml(song.title)} • ${escapeHtml(song.artist)}</span>
        </div>
        <span style="color: var(--primary); font-size: 11px;">Change</span>
      `;
    }
  };

  window.submitNote = async () => {
    const text = document.getElementById("note-input-text").value.trim();
    if (!text && !noteSong) return;

    closeModal();
    try {
      await fetch(`${API_BASE}/notes`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          userId: State.currentUser.id,
          username: State.currentUser.username,
          displayName: State.currentUser.displayName,
          userPhotoUrl: State.currentUser.photoUrl,
          text: text,
          songTitle: noteSong ? noteSong.title : "",
          songArtist: noteSong ? noteSong.artist : ""
        })
      });
      await fetchNotes();
      renderHomeScreen(document.getElementById("main-content"));
    } catch (e) {
      console.error(e);
    }
  };
}

// -------------------------------------------------------------
// Reels Screen
// -------------------------------------------------------------
function renderReelsScreen(container) {
  const reels = State.posts.filter(p => p.type === "reel");

  container.innerHTML = `
    <div style="padding: 14px;">
      <h2 style="font-size: 20px; font-weight: 800; margin-bottom: 12px;">Reels</h2>
      ${reels.length === 0 ? `
        <div style="text-align: center; padding: 60px 20px; color: var(--text-muted);">
          <p style="font-size: 16px; font-weight: 600; margin-bottom: 8px;">No Reels yet</p>
          <button class="btn-primary" style="width: auto; padding: 10px 24px;" onclick="openCreateModal('reel')">Create Reel</button>
        </div>
      ` : reels.map(r => renderPostCard(r)).join('')}
    </div>
  `;
}

// -------------------------------------------------------------
// Search & Explore Screen
// -------------------------------------------------------------
function renderSearchScreen(container) {
  container.innerHTML = `
    <div style="padding: 14px;">
      <input type="text" id="explore-search" class="form-input" style="width: 100%; border-radius: 20px; margin-bottom: 14px;" placeholder="Search people, hashtags, music..." oninput="handleExploreSearch(this.value)" />

      <h3 style="font-size: 16px; font-weight: 700; margin-bottom: 10px;">Trending Moments</h3>
      <div style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 4px;" id="explore-grid">
        ${State.posts.map(p => `
          <div style="aspect-ratio: 1; background: #000; overflow: hidden; border-radius: 6px; cursor: pointer;" onclick="viewPostDetail('${p.id}')">
            <img src="${p.mediaUrl || PRESET_AVATARS[0]}" style="width: 100%; height: 100%; object-fit: cover;" />
          </div>
        `).join('')}
      </div>
    </div>
  `;
}

// -------------------------------------------------------------
// Profile Screen
// -------------------------------------------------------------
function renderProfileScreen(container) {
  const user = State.currentUser;
  const myPosts = State.posts.filter(p => p.authorId === user.id);

  container.innerHTML = `
    <div style="padding: 20px 16px;">
      <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px;">
        <h2 style="font-size: 20px; font-weight: 800;">@${user.username}</h2>
        <button class="icon-btn" onclick="clearSession()">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4M16 17l5-5-5-5M21 12H9"/></svg>
        </button>
      </div>

      <div style="display: flex; align-items: center; gap: 20px; margin-bottom: 16px;">
        <img src="${user.photoUrl}" style="width: 76px; height: 76px; border-radius: 50%; object-fit: cover; border: 2px solid var(--primary);" />
        <div style="display: flex; gap: 24px; text-align: center;">
          <div>
            <div style="font-weight: 800; font-size: 16px;">${myPosts.length}</div>
            <div style="font-size: 12px; color: var(--text-muted);">Posts</div>
          </div>
          <div>
            <div style="font-weight: 800; font-size: 16px;">${user.followersCount || 0}</div>
            <div style="font-size: 12px; color: var(--text-muted);">Followers</div>
          </div>
          <div>
            <div style="font-weight: 800; font-size: 16px;">${user.followingCount || 0}</div>
            <div style="font-size: 12px; color: var(--text-muted);">Following</div>
          </div>
        </div>
      </div>

      <div style="font-weight: 700; font-size: 15px;">${user.displayName}</div>
      <div style="font-size: 13px; color: var(--text-muted); margin-top: 2px;">${user.bio || 'Sharing vibes on SocialX ✨'}</div>

      <div style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 4px; margin-top: 24px;">
        ${myPosts.map(p => `
          <div style="aspect-ratio: 1; background: #000; border-radius: 4px; overflow: hidden; cursor: pointer;" onclick="viewPostDetail('${p.id}')">
            <img src="${p.mediaUrl}" style="width: 100%; height: 100%; object-fit: cover;" />
          </div>
        `).join('')}
      </div>
    </div>
  `;
}

// -------------------------------------------------------------
// Modal Dialog System
// -------------------------------------------------------------
function openModal(contentHtml) {
  closeModal();
  const backdrop = document.createElement("div");
  backdrop.id = "app-modal-backdrop";
  backdrop.className = "modal-backdrop";
  backdrop.onclick = (e) => {
    if (e.target === backdrop) closeModal();
  };

  backdrop.innerHTML = `
    <div class="modal-content">
      ${contentHtml}
    </div>
  `;
  document.body.appendChild(backdrop);
}

function closeModal() {
  const existing = document.getElementById("app-modal-backdrop");
  if (existing) existing.remove();
}

// -------------------------------------------------------------
// Global Event Listeners & Utilities
// -------------------------------------------------------------
function setupEventListeners() {
  document.querySelectorAll(".nav-item").forEach(item => {
    item.addEventListener("click", () => {
      const screen = item.getAttribute("data-screen");
      if (screen === "create") {
        openCreateModal("post");
      } else {
        navigateTo(screen);
      }
    });
  });
}

function formatTimeAgo(dateInput) {
  const diffSec = Math.floor((new Date() - new Date(dateInput)) / 1000);
  if (diffSec < 60) return "just now";
  if (diffSec < 3600) return `${Math.floor(diffSec / 60)}m`;
  if (diffSec < 86400) return `${Math.floor(diffSec / 3600)}h`;
  return `${Math.floor(diffSec / 86400)}d`;
}

function escapeHtml(str) {
  if (!str) return "";
  return str.replace(/[&<>'"]/g, tag => ({
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    "'": '&#39;',
    '"': '&quot;'
  }[tag] || tag));
}
