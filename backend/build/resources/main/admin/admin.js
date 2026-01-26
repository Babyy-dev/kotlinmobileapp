const $ = (id) => document.getElementById(id);
const output = $("output");
const logEl = $("log");
const baseUrlInput = $("baseUrl");
const tokenInput = $("token");
const configKey = "kappa_admin_config";

const columns = {
  users: [
    { key: "id", label: "ID" },
    { key: "username", label: "Username" },
    { key: "role", label: "Role" },
    { key: "status", label: "Status" },
    { key: "email", label: "Email" }
  ],
  rooms: [
    { key: "id", label: "ID" },
    { key: "name", label: "Name" },
    { key: "seatMode", label: "Seat mode" },
    { key: "participantCount", label: "Participants" },
    { key: "maxSeats", label: "Max seats" },
    { key: "isActive", label: "Active" }
  ],
  coinPacks: [
    { key: "id", label: "ID" },
    { key: "name", label: "Name" },
    { key: "coinAmount", label: "Coins" },
    { key: "priceUsd", label: "Price USD" },
    { key: "isActive", label: "Active" }
  ],
  gifts: [
    { key: "id", label: "ID" },
    { key: "name", label: "Name" },
    { key: "giftType", label: "Type" },
    { key: "costCoins", label: "Cost" },
    { key: "diamondPercent", label: "Diamond %" },
    { key: "isActive", label: "Active" }
  ],
  rewards: [
    { key: "id", label: "ID" },
    { key: "userId", label: "User" },
    { key: "diamonds", label: "Diamonds" },
    { key: "status", label: "Status" },
    { key: "createdAt", label: "Created" }
  ],
  announcements: [
    { key: "id", label: "ID" },
    { key: "title", label: "Title" },
    { key: "message", label: "Message" },
    { key: "isActive", label: "Active" }
  ],
  apps: [
    { key: "id", label: "ID" },
    { key: "userId", label: "User" },
    { key: "agencyName", label: "Agency" },
    { key: "status", label: "Status" }
  ],
  resellerApps: [
    { key: "id", label: "ID" },
    { key: "userId", label: "User" },
    { key: "status", label: "Status" }
  ],
  agencies: [
    { key: "id", label: "ID" },
    { key: "name", label: "Name" },
    { key: "status", label: "Status" },
    { key: "commissionValueUsd", label: "Commission USD" },
    { key: "commissionBlockDiamonds", label: "Block" }
  ],
  commissions: [
    { key: "id", label: "ID" },
    { key: "agencyId", label: "Agency" },
    { key: "userId", label: "User" },
    { key: "diamondsAmount", label: "Diamonds" },
    { key: "commissionUsd", label: "Commission USD" }
  ]
};

function log(message) {
  const time = new Date().toLocaleTimeString();
  const entry = document.createElement("div");
  entry.innerHTML = "<strong>[" + time + "]</strong> " + message;
  logEl.prepend(entry);
}

function setOutput(data) {
  if (data === undefined) {
    output.textContent = "No response body.";
    return;
  }
  output.textContent = typeof data === "string" ? data : JSON.stringify(data, null, 2);
}

function normalizeBaseUrl(value) {
  const fallback = window.location.origin + "/api";
  if (!value) return fallback;
  const trimmed = value.trim();
  return trimmed.endsWith("/") ? trimmed.slice(0, -1) : trimmed;
}

function getConfig() {
  const stored = localStorage.getItem(configKey);
  if (stored) {
    try {
      return JSON.parse(stored);
    } catch (error) {
      return {};
    }
  }
  return {};
}

function saveConfig() {
  const config = {
    baseUrl: normalizeBaseUrl(baseUrlInput.value),
    token: tokenInput.value.trim()
  };
  localStorage.setItem(configKey, JSON.stringify(config));
  log("Config saved.");
}

function loadConfig() {
  const config = getConfig();
  baseUrlInput.value = config.baseUrl || normalizeBaseUrl("");
  tokenInput.value = config.token || "";
}

function getHeaders() {
  const headers = { "Content-Type": "application/json" };
  const token = tokenInput.value.trim();
  if (token) {
    headers.Authorization = "Bearer " + token;
  }
  return headers;
}

async function apiFetch(path, options = {}) {
  const baseUrl = normalizeBaseUrl(baseUrlInput.value);
  const url = baseUrl + path;
  const response = await fetch(url, {
    ...options,
    headers: { ...getHeaders(), ...(options.headers || {}) }
  });
  const text = await response.text();
  let payload = text;
  try {
    payload = text ? JSON.parse(text) : null;
  } catch (error) {
    payload = text;
  }
  if (!response.ok) {
    throw new Error("HTTP " + response.status + ": " + (payload?.error || payload?.message || text));
  }
  return payload;
}

function renderTable(containerId, rows, columnDefs) {
  const container = $(containerId);
  if (!rows || rows.length === 0) {
    container.innerHTML = "<div class=\"output\">No data.</div>";
    return;
  }
  const header = columnDefs.map((col) => "<th>" + col.label + "</th>").join("");
  const body = rows.map((row) => {
    const cells = columnDefs.map((col) => "<td>" + (row[col.key] ?? "") + "</td>").join("");
    return "<tr>" + cells + "</tr>";
  }).join("");
  container.innerHTML = "<table class=\"table\"><thead><tr>" + header + "</tr></thead><tbody>" + body + "</tbody></table>";
}

async function handleAction(label, fn) {
  try {
    const data = await fn();
    setOutput(data);
    log(label + " success.");
    return data;
  } catch (error) {
    log(label + " failed: " + error.message);
    setOutput(error.message);
  }
}

$("saveConfig").addEventListener("click", saveConfig);
$("clearOutput").addEventListener("click", () => {
  output.textContent = "Cleared.";
});
$("testToken").addEventListener("click", () => handleAction("Test token", async () => {
  return apiFetch("/users/me");
}));

$("loadUsers").addEventListener("click", () => handleAction("Load users", async () => {
  const role = $("userRoleFilter").value;
  const status = $("userStatusFilter").value;
  const limit = $("userLimit").value || 50;
  const query = new URLSearchParams();
  if (role) query.set("role", role);
  if (status) query.set("status", status);
  query.set("limit", limit);
  const data = await apiFetch("/admin/users?" + query.toString());
  renderTable("usersTable", data.data, columns.users);
  return data;
}));

$("updateUserRole").addEventListener("click", () => handleAction("Update user role", async () => {
  const userId = $("userIdInput").value.trim();
  const role = $("userRoleUpdate").value;
  if (!userId) throw new Error("User ID required");
  return apiFetch("/users/" + userId + "/role", {
    method: "POST",
    body: JSON.stringify({ role })
  });
}));

$("updateUserStatus").addEventListener("click", () => handleAction("Update user status", async () => {
  const userId = $("userIdInput").value.trim();
  const status = $("userStatusUpdate").value;
  if (!userId) throw new Error("User ID required");
  return apiFetch("/admin/users/" + userId + "/status", {
    method: "POST",
    body: JSON.stringify({ status })
  });
}));

$("loadRooms").addEventListener("click", () => handleAction("Load rooms", async () => {
  const status = $("roomStatusFilter").value;
  const data = await apiFetch("/admin/rooms?status=" + status);
  renderTable("roomsTable", data.data, columns.rooms);
  return data;
}));

$("loadCoinPacks").addEventListener("click", () => handleAction("Load coin packages", async () => {
  const data = await apiFetch("/coin-packages");
  renderTable("coinPackTable", data.data, columns.coinPacks);
  return data;
}));

$("createCoinPack").addEventListener("click", () => handleAction("Create coin package", async () => {
  const name = $("coinPackName").value.trim();
  const coinAmount = Number($("coinPackAmount").value);
  const priceUsd = $("coinPackPrice").value.trim();
  const isActive = $("coinPackActive").value === "true";
  return apiFetch("/admin/coin-packages", {
    method: "POST",
    body: JSON.stringify({ name, coinAmount, priceUsd, isActive })
  });
}));

$("updateCoinPack").addEventListener("click", () => handleAction("Update coin package", async () => {
  const id = $("coinPackId").value.trim();
  if (!id) throw new Error("Package ID required");
  const name = $("coinPackNameUpdate").value.trim();
  const coinAmount = Number($("coinPackAmountUpdate").value);
  const priceUsd = $("coinPackPriceUpdate").value.trim();
  const isActive = $("coinPackActiveUpdate").value === "true";
  return apiFetch("/admin/coin-packages/" + id, {
    method: "POST",
    body: JSON.stringify({ name, coinAmount, priceUsd, isActive })
  });
}));

$("refundPurchase").addEventListener("click", () => handleAction("Refund purchase", async () => {
  const id = $("purchaseId").value.trim();
  if (!id) throw new Error("Purchase ID required");
  return apiFetch("/admin/coin-purchases/" + id + "/refund", { method: "POST" });
}));

$("loadGifts").addEventListener("click", () => handleAction("Load gifts", async () => {
  const data = await apiFetch("/gifts/catalog");
  renderTable("giftsTable", data.data, columns.gifts);
  return data;
}));

$("createGift").addEventListener("click", () => handleAction("Create gift", async () => {
  const name = $("giftName").value.trim();
  const giftType = $("giftType").value;
  const costCoins = Number($("giftCost").value);
  const diamondPercent = Number($("giftPercent").value);
  return apiFetch("/gifts/catalog", {
    method: "POST",
    body: JSON.stringify({ name, giftType, costCoins, diamondPercent })
  });
}));

$("updateGift").addEventListener("click", () => handleAction("Update gift", async () => {
  const id = $("giftIdUpdate").value.trim();
  if (!id) throw new Error("Gift ID required");
  const payload = {};
  const name = $("giftNameUpdate").value.trim();
  const giftType = $("giftTypeUpdate").value.trim();
  const costCoins = $("giftCostUpdate").value;
  const diamondPercent = $("giftPercentUpdate").value;
  const isActive = $("giftActiveUpdate").value;
  if (name) payload.name = name;
  if (giftType) payload.giftType = giftType;
  if (costCoins) payload.costCoins = Number(costCoins);
  if (diamondPercent) payload.diamondPercent = Number(diamondPercent);
  if (isActive) payload.isActive = isActive === "true";
  return apiFetch("/gifts/catalog/" + id, {
    method: "POST",
    body: JSON.stringify(payload)
  });
}));

$("loadRewards").addEventListener("click", () => handleAction("Load rewards", async () => {
  const status = $("rewardStatusFilter").value;
  const data = await apiFetch("/admin/rewards" + (status ? "?status=" + status : ""));
  renderTable("rewardsTable", data.data, columns.rewards);
  return data;
}));

$("approveReward").addEventListener("click", () => handleAction("Approve reward", async () => {
  const id = $("rewardId").value.trim();
  if (!id) throw new Error("Reward ID required");
  return apiFetch("/admin/rewards/" + id + "/approve", { method: "POST" });
}));

$("rejectReward").addEventListener("click", () => handleAction("Reject reward", async () => {
  const id = $("rewardId").value.trim();
  if (!id) throw new Error("Reward ID required");
  return apiFetch("/admin/rewards/" + id + "/reject", { method: "POST" });
}));

$("loadAnnouncements").addEventListener("click", () => handleAction("Load announcements", async () => {
  const data = await apiFetch("/announcements");
  renderTable("announcementsTable", data.data, columns.announcements);
  return data;
}));

$("createAnnouncement").addEventListener("click", () => handleAction("Create announcement", async () => {
  const title = $("announcementTitle").value.trim();
  const message = $("announcementMessage").value.trim();
  const isActive = $("announcementActive").value === "true";
  return apiFetch("/admin/announcements", {
    method: "POST",
    body: JSON.stringify({ title, message, isActive })
  });
}));

$("updateAnnouncement").addEventListener("click", () => handleAction("Update announcement", async () => {
  const id = $("announcementIdUpdate").value.trim();
  if (!id) throw new Error("Announcement ID required");
  const title = $("announcementTitle").value.trim();
  const message = $("announcementMessage").value.trim();
  const isActive = $("announcementActive").value === "true";
  return apiFetch("/admin/announcements/" + id, {
    method: "POST",
    body: JSON.stringify({ title, message, isActive })
  });
}));

$("loadAgencyApps").addEventListener("click", () => handleAction("Load agency applications", async () => {
  const data = await apiFetch("/admin/agency-applications");
  renderTable("appsTable", data.data, columns.apps);
  return data;
}));

$("loadResellerApps").addEventListener("click", () => handleAction("Load reseller applications", async () => {
  const data = await apiFetch("/admin/reseller-applications");
  renderTable("appsTable", data.data, columns.resellerApps);
  return data;
}));

$("approveApp").addEventListener("click", () => handleAction("Approve application", async () => {
  const id = $("agencyAppId").value.trim();
  const type = $("appType").value;
  if (!id) throw new Error("Application ID required");
  return apiFetch("/admin/" + type + "-applications/" + id + "/approve", { method: "POST" });
}));

$("rejectApp").addEventListener("click", () => handleAction("Reject application", async () => {
  const id = $("agencyAppId").value.trim();
  const type = $("appType").value;
  if (!id) throw new Error("Application ID required");
  return apiFetch("/admin/" + type + "-applications/" + id + "/reject", { method: "POST" });
}));

$("loadAgencies").addEventListener("click", () => handleAction("Load agencies", async () => {
  const status = $("agencyStatusFilter").value;
  const limit = $("agencyLimit").value || 50;
  const query = new URLSearchParams();
  if (status) query.set("status", status);
  query.set("limit", limit);
  const data = await apiFetch("/admin/agencies?" + query.toString());
  renderTable("agencyTable", data.data, columns.agencies);
  return data;
}));

$("updateAgency").addEventListener("click", () => handleAction("Update agency", async () => {
  const id = $("agencyIdUpdate").value.trim();
  if (!id) throw new Error("Agency ID required");
  const payload = {};
  const commissionValueUsd = $("agencyCommissionUsd").value.trim();
  const commissionBlockDiamonds = $("agencyCommissionBlock").value.trim();
  const status = $("agencyStatusUpdate").value.trim();
  if (commissionValueUsd) payload.commissionValueUsd = commissionValueUsd;
  if (commissionBlockDiamonds) payload.commissionBlockDiamonds = Number(commissionBlockDiamonds);
  if (status) payload.status = status;
  return apiFetch("/admin/agencies/" + id, {
    method: "POST",
    body: JSON.stringify(payload)
  });
}));

$("loadCommissions").addEventListener("click", () => handleAction("Load commissions", async () => {
  const agencyId = $("commissionAgencyId").value.trim();
  const data = await apiFetch("/admin/commissions" + (agencyId ? "?agencyId=" + agencyId : ""));
  renderTable("commissionTable", data.data, columns.commissions);
  return data;
}));

loadConfig();
