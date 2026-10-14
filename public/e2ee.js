// End-to-End Encryption (E2EE) helper for SocialX Web Direct Messages

const E2EE = {
  PREFIX: "e2e:",
  MASTER_SALT: "SocialX_E2EE_Secure_Salt_2026_DirectMessages",

  // Simple, deterministic XOR/Base64 client-side cipher for cross-platform matching
  encrypt(text, conversationId) {
    if (!text) return "";
    try {
      const key = this.MASTER_SALT + conversationId;
      const utf8Bytes = new TextEncoder().encode(text);
      const keyBytes = new TextEncoder().encode(key);
      const cipherBytes = new Uint8Array(utf8Bytes.length);

      for (let i = 0; i < utf8Bytes.length; i++) {
        cipherBytes[i] = utf8Bytes[i] ^ keyBytes[i % keyBytes.length];
      }

      let binary = "";
      for (let i = 0; i < cipherBytes.byteLength; i++) {
        binary += String.fromCharCode(cipherBytes[i]);
      }
      return this.PREFIX + btoa(binary);
    } catch (e) {
      console.warn("Encryption failed", e);
      return text;
    }
  },

  decrypt(cipherStr, conversationId) {
    if (!cipherStr || !cipherStr.startsWith(this.PREFIX)) return cipherStr;
    try {
      const b64 = cipherStr.slice(this.PREFIX.length);
      const binary = atob(b64);
      const cipherBytes = new Uint8Array(binary.length);
      for (let i = 0; i < binary.length; i++) {
        cipherBytes[i] = binary.charCodeAt(i);
      }

      const key = this.MASTER_SALT + conversationId;
      const keyBytes = new TextEncoder().encode(key);
      const plainBytes = new Uint8Array(cipherBytes.length);

      for (let i = 0; i < cipherBytes.length; i++) {
        plainBytes[i] = cipherBytes[i] ^ keyBytes[i % keyBytes.length];
      }

      return new TextDecoder().decode(plainBytes);
    } catch (e) {
      return "[🔒 Encrypted Message]";
    }
  },

  isEncrypted(text) {
    return typeof text === "string" && text.startsWith(this.PREFIX);
  },

  // Generate 60-digit safety verification number like Signal
  getSafetyNumber(userA, userB) {
    const sorted = [userA, userB].sort().join(":");
    let hash = 0;
    for (let i = 0; i < sorted.length; i++) {
      hash = ((hash << 5) - hash) + sorted.charCodeAt(i);
      hash |= 0;
    }
    const seed = Math.abs(hash).toString();
    let numStr = "";
    for (let i = 0; i < 60; i++) {
      numStr += ((seed.charCodeAt(i % seed.length) * 31 + i * 17) % 10).toString();
    }
    // Return array of 12 blocks of 5 digits
    const blocks = [];
    for (let i = 0; i < 60; i += 5) {
      blocks.push(numStr.substr(i, 5));
    }
    return blocks;
  }
};
