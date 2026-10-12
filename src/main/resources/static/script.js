/**
 * CrowdCheck — Frontend Application Logic
 * Plain JavaScript fetch() client for Spring Boot REST API
 */

let cachedLocations = [];

document.addEventListener("DOMContentLoaded", () => {
    loadLocations();
    loadAlerts();
    loadHistory();
    loadHotspots();
    updateRoleUI();
});

function getCurrentRole() {
    return document.getElementById("currentRole").value;
}

function onRoleChange() {
    updateRoleUI();
    showToast(`Switched active role to: ${getCurrentRole()}`, "info");
}

function updateRoleUI() {
    const role = getCurrentRole();
    const canManageLoc = (role === "ADMIN" || role === "ORGANIZER");
    const canUpdateCrowd = (role === "ADMIN" || role === "ORGANIZER" || role === "STAFF");

    const manageLocSec = document.getElementById("manageLocationSection");
    if (manageLocSec) {
        manageLocSec.style.opacity = canManageLoc ? "1" : "0.5";
        document.getElementById("btnSaveLocation").disabled = !canManageLoc;
    }

    const updateCrowdSec = document.getElementById("updateCrowdSection");
    if (updateCrowdSec) {
        updateCrowdSec.style.opacity = canUpdateCrowd ? "1" : "0.5";
        document.getElementById("btnUpdateCrowd").disabled = !canUpdateCrowd;
    }

    renderLocations(cachedLocations);
}

function showToast(message, type = "info") {
    const toast = document.getElementById("toast");
    toast.className = `toast ${type}`;
    toast.innerText = message;
    toast.classList.remove("hidden");
    setTimeout(() => {
        toast.classList.add("hidden");
    }, 4000);
}

// =============================================================================
// LOCATIONS & LIVE STATUS
// =============================================================================

async function loadLocations() {
    try {
        const res = await fetch("/api/locations");
        if (!res.ok) throw new Error("Failed to fetch locations");
        cachedLocations = await res.json();
        renderLocations(cachedLocations);
        populateLocationDropdown(cachedLocations);
        updateOverviewMetrics();
    } catch (err) {
        showToast(err.message, "error");
    }
}

function renderLocations(locations) {
    const grid = document.getElementById("locationGrid");
    grid.innerHTML = "";

    if (!locations || locations.length === 0) {
        grid.innerHTML = "<p class='text-muted'>No locations matching current filter.</p>";
        return;
    }

    const role = getCurrentRole();
    const canManageLoc = (role === "ADMIN" || role === "ORGANIZER");

    locations.forEach(loc => {
        const card = document.createElement("div");
        card.className = "location-card";

        const badgeClass = getBadgeClass(loc.crowdLevel);
        const barColor = getBarColor(loc.crowdLevel);

        card.innerHTML = `
            <div>
                <div class="loc-header">
                    <div>
                        <div class="loc-name">${escapeHtml(loc.locationName)}</div>
                        <span class="loc-type">${loc.locationType}</span>
                    </div>
                    <span class="badge ${badgeClass}">${loc.crowdLevel}</span>
                </div>
                <div class="loc-stats">
                    <div class="count-row">
                        <span><strong>${loc.currentCrowdCount}</strong> / ${loc.capacity}</span>
                        <span><strong>${loc.occupancyPercentage}%</strong></span>
                    </div>
                    <div class="occupancy-bar-bg">
                        <div class="occupancy-bar-fill" style="width: ${Math.min(100, loc.occupancyPercentage)}%; background-color: ${barColor};"></div>
                    </div>
                </div>
            </div>
            <div class="loc-footer">
                <span>Updated: ${formatDate(loc.lastUpdated)}</span>
                ${canManageLoc ? `
                <div class="loc-actions">
                    <button class="btn btn-secondary btn-sm" onclick="editLocation(${loc.locationId})">Edit</button>
                    <button class="btn btn-danger btn-sm" onclick="deleteLocation(${loc.locationId})">Delete</button>
                </div>
                ` : ""}
            </div>
        `;
        grid.appendChild(card);
    });
}

function populateLocationDropdown(locations) {
    const select = document.getElementById("updateLocationSelect");
    select.innerHTML = '<option value="">-- Choose Facility --</option>';
    locations.forEach(loc => {
        const opt = document.createElement("option");
        opt.value = loc.locationId;
        opt.textContent = `${loc.locationName} (${loc.currentCrowdCount}/${loc.capacity} - ${loc.crowdLevel})`;
        select.appendChild(opt);
    });
}

// =============================================================================
// SEARCH & FILTERING
// =============================================================================

async function handleSearch() {
    const name = document.getElementById("searchName").value.trim();
    const type = document.getElementById("filterType").value;
    const level = document.getElementById("filterLevel").value;

    try {
        const params = new URLSearchParams();
        if (name) params.append("name", name);
        if (type && type !== "ALL") params.append("type", type);
        if (level && level !== "ALL") params.append("level", level);

        const res = await fetch(`/api/locations/search?${params.toString()}`);
        if (!res.ok) throw new Error("Search query failed");
        const results = await res.json();
        renderLocations(results);
    } catch (err) {
        showToast(err.message, "error");
    }
}

function resetSearch() {
    document.getElementById("searchName").value = "";
    document.getElementById("filterType").value = "ALL";
    document.getElementById("filterLevel").value = "ALL";
    loadLocations();
}

// =============================================================================
// UPDATE CROWD COUNT
// =============================================================================

async function handleUpdateCrowd(e) {
    e.preventDefault();
    const locationId = parseInt(document.getElementById("updateLocationSelect").value);
    const newCount = parseInt(document.getElementById("newCrowdCount").value);

    if (isNaN(locationId) || isNaN(newCount)) {
        showToast("Please enter valid facility and headcount", "error");
        return;
    }

    try {
        const res = await fetch("/api/crowd/update", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "X-User-Role": getCurrentRole()
            },
            body: JSON.stringify({ locationId, newCount })
        });

        const data = await res.json();
        if (!res.ok) {
            throw new Error(data.message || "Failed to update crowd count");
        }

        showToast(`Crowd count updated to ${data.currentCrowdCount} (${data.crowdLevel})`, "success");
        document.getElementById("newCrowdCount").value = "";
        loadLocations();
        loadAlerts();
        loadHistory();
        loadHotspots();
    } catch (err) {
        showToast(err.message, "error");
    }
}

// =============================================================================
// MANAGE LOCATIONS (ADD / EDIT / DELETE)
// =============================================================================

function editLocation(id) {
    const loc = cachedLocations.find(l => l.locationId === id);
    if (!loc) return;
    document.getElementById("editLocationId").value = loc.locationId;
    document.getElementById("locationName").value = loc.locationName;
    document.getElementById("locationType").value = loc.locationType;
    document.getElementById("locationCapacity").value = loc.capacity;
    document.getElementById("locationFormTitle").innerText = "✏️ Edit Facility Details";
    document.getElementById("btnCancelEdit").classList.remove("hidden");
    window.scrollTo({ top: document.getElementById("manageLocationSection").offsetTop - 20, behavior: "smooth" });
}

function cancelEditLocation() {
    document.getElementById("editLocationId").value = "";
    document.getElementById("locationName").value = "";
    document.getElementById("locationCapacity").value = "";
    document.getElementById("locationFormTitle").innerText = "🏢 Add Campus Facility";
    document.getElementById("btnCancelEdit").classList.add("hidden");
}

async function handleSaveLocation(e) {
    e.preventDefault();
    const id = document.getElementById("editLocationId").value;
    const locationName = document.getElementById("locationName").value.trim();
    const locationType = document.getElementById("locationType").value;
    const capacity = parseInt(document.getElementById("locationCapacity").value);

    const isEdit = Boolean(id);
    const url = isEdit ? `/api/locations/${id}` : "/api/locations";
    const method = isEdit ? "PUT" : "POST";

    try {
        const res = await fetch(url, {
            method: method,
            headers: {
                "Content-Type": "application/json",
                "X-User-Role": getCurrentRole()
            },
            body: JSON.stringify({ locationName, locationType, capacity })
        });

        const data = await res.json();
        if (!res.ok) throw new Error(data.message || "Failed to save facility");

        showToast(isEdit ? "Facility updated successfully" : "Facility registered successfully", "success");
        cancelEditLocation();
        loadLocations();
    } catch (err) {
        showToast(err.message, "error");
    }
}

async function deleteLocation(id) {
    if (!confirm("Are you sure you want to delete this facility?")) return;

    try {
        const res = await fetch(`/api/locations/${id}`, {
            method: "DELETE",
            headers: { "X-User-Role": getCurrentRole() }
        });

        if (!res.ok) {
            const data = await res.json();
            throw new Error(data.message || "Failed to delete location");
        }

        showToast("Facility deleted successfully", "success");
        loadLocations();
    } catch (err) {
        showToast(err.message, "error");
    }
}

// =============================================================================
// CROWD ALERTS
// =============================================================================

async function loadAlerts() {
    const activeOnly = document.getElementById("chkActiveOnly").checked;
    const url = activeOnly ? "/api/alerts/active" : "/api/alerts";

    try {
        const res = await fetch(url);
        if (!res.ok) throw new Error("Failed to load alerts");
        const alerts = await res.json();
        renderAlerts(alerts);
    } catch (err) {
        showToast(err.message, "error");
    }
}

function renderAlerts(alerts) {
    const tbody = document.getElementById("alertsTableBody");
    tbody.innerHTML = "";

    if (!alerts || alerts.length === 0) {
        tbody.innerHTML = "<tr><td colspan='7' class='text-center text-muted'>No alerts on record.</td></tr>";
        return;
    }

    const role = getCurrentRole();
    const canManageAlert = (role === "ADMIN" || role === "ORGANIZER" || role === "STAFF");

    alerts.forEach(a => {
        const tr = document.createElement("tr");
        const badgeClass = getBadgeClass(a.crowdLevel);
        const statusBadgeClass = `badge-${a.status.toLowerCase()}`;

        let actionBtns = "-";
        if (canManageAlert) {
            if (a.status === "ACTIVE") {
                actionBtns = `
                    <button class="btn btn-warning btn-sm" onclick="acknowledgeAlert(${a.alertId})">Ack</button>
                    <button class="btn btn-success btn-sm" onclick="resolveAlert(${a.alertId})">Resolve</button>
                `;
            } else if (a.status === "ACKNOWLEDGED") {
                actionBtns = `
                    <button class="btn btn-success btn-sm" onclick="resolveAlert(${a.alertId})">Resolve</button>
                `;
            }
        }

        tr.innerHTML = `
            <td>#${a.alertId}</td>
            <td><strong>${escapeHtml(a.location ? a.location.locationName : "N/A")}</strong></td>
            <td><span class="badge ${badgeClass}">${a.crowdLevel}</span></td>
            <td>${formatDate(a.alertTime)}</td>
            <td>${escapeHtml(a.alertMessage)}</td>
            <td><span class="badge ${statusBadgeClass}">${a.status}</span></td>
            <td>${actionBtns}</td>
        `;
        tbody.appendChild(tr);
    });
}

async function acknowledgeAlert(id) {
    try {
        const res = await fetch(`/api/alerts/${id}/acknowledge`, {
            method: "PUT",
            headers: { "X-User-Role": getCurrentRole() }
        });
        if (!res.ok) {
            const data = await res.json();
            throw new Error(data.message || "Failed to acknowledge alert");
        }
        showToast("Alert acknowledged", "info");
        loadAlerts();
    } catch (err) {
        showToast(err.message, "error");
    }
}

async function resolveAlert(id) {
    try {
        const res = await fetch(`/api/alerts/${id}/resolve`, {
            method: "PUT",
            headers: { "X-User-Role": getCurrentRole() }
        });
        if (!res.ok) {
            const data = await res.json();
            throw new Error(data.message || "Failed to resolve alert");
        }
        showToast("Alert resolved", "success");
        loadAlerts();
    } catch (err) {
        showToast(err.message, "error");
    }
}

// =============================================================================
// CROWD HISTORY & HOTSPOTS
// =============================================================================

async function loadHistory() {
    try {
        const res = await fetch("/api/crowd/history");
        if (!res.ok) throw new Error("Failed to load history");
        const list = await res.json();
        renderHistory(list);
    } catch (err) {
        showToast(err.message, "error");
    }
}

function renderHistory(list) {
    const tbody = document.getElementById("historyTableBody");
    tbody.innerHTML = "";

    if (!list || list.length === 0) {
        tbody.innerHTML = "<tr><td colspan='5' class='text-center text-muted'>No update logs recorded.</td></tr>";
        return;
    }

    list.slice(0, 30).forEach(u => {
        const tr = document.createElement("tr");
        const badgeClass = getBadgeClass(u.crowdLevel);
        tr.innerHTML = `
            <td>#${u.updateId}</td>
            <td><strong>${escapeHtml(u.location ? u.location.locationName : "N/A")}</strong></td>
            <td>${u.previousCount} &rarr; <strong>${u.newCount}</strong></td>
            <td><span class="badge ${badgeClass}">${u.crowdLevel}</span></td>
            <td>${formatDate(u.updateTime)}</td>
        `;
        tbody.appendChild(tr);
    });
}

async function loadHotspots() {
    try {
        const res = await fetch("/api/crowd/analysis/frequent-overcrowded");
        if (!res.ok) throw new Error("Failed to load hotspots");
        const list = await res.json();
        renderHotspots(list);
    } catch (err) {
        showToast(err.message, "error");
    }
}

function renderHotspots(list) {
    const tbody = document.getElementById("hotspotsTableBody");
    tbody.innerHTML = "";

    if (!list || list.length === 0) {
        tbody.innerHTML = "<tr><td colspan='4' class='text-center text-muted'>No high/critical overcrowding events logged.</td></tr>";
        return;
    }

    list.forEach(h => {
        const tr = document.createElement("tr");
        tr.innerHTML = `
            <td><strong>${escapeHtml(h.locationName)}</strong></td>
            <td>${h.locationType}</td>
            <td>${h.capacity}</td>
            <td><span class="badge badge-critical">${h.highCriticalCount} events</span></td>
        `;
        tbody.appendChild(tr);
    });
}

function updateOverviewMetrics() {
    document.getElementById("metricTotalLocations").innerText = cachedLocations.length;
    const criticals = cachedLocations.filter(l => l.crowdLevel === "CRITICAL").length;
    document.getElementById("metricCriticalCount").innerText = criticals;
    const totalCount = cachedLocations.reduce((sum, l) => sum + (l.currentCrowdCount || 0), 0);
    document.getElementById("metricTotalHeadcount").innerText = totalCount;

    fetch("/api/alerts/active")
        .then(res => res.json())
        .then(activeAlerts => {
            document.getElementById("metricActiveAlerts").innerText = activeAlerts.length;
        })
        .catch(() => {});
}

// Helpers
function getBadgeClass(level) {
    if (!level) return "badge-low";
    switch (level.toUpperCase()) {
        case "LOW": return "badge-low";
        case "MODERATE": return "badge-moderate";
        case "HIGH": return "badge-high";
        case "CRITICAL": return "badge-critical";
        default: return "badge-low";
    }
}

function getBarColor(level) {
    if (!level) return "#2e7d32";
    switch (level.toUpperCase()) {
        case "LOW": return "#2e7d32";
        case "MODERATE": return "#ed6c02";
        case "HIGH": return "#9c27b0";
        case "CRITICAL": return "#d32f2f";
        default: return "#2e7d32";
    }
}

function formatDate(dt) {
    if (!dt) return "N/A";
    const d = new Date(dt);
    if (isNaN(d.getTime())) return dt;
    return d.toLocaleString([], { month: "short", day: "numeric", hour: "2-digit", minute: "2-digit", second: "2-digit" });
}

function escapeHtml(str) {
    if (!str) return "";
    return str.replace(/[&<>"']/g, function(m) {
        return {
            "&": "&amp;",
            "<": "&lt;",
            ">": "&gt;",
            '"': "&quot;",
            "'": "&#039;"
        }[m];
    });
}
