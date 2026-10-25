const POLL_MS = 2000;
const COLORS = ["#1a73e8", "#e8710a", "#188038", "#a142f4"];
const $ = (id) => document.getElementById(id);

const map = L.map("map").setView([23.23, 77.415], 13);
L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", {
    maxZoom: 19,
    attribution: "&copy; OpenStreetMap contributors",
}).addTo(map);

const busMarkers = new Map();   // bus code -> Leaflet marker
const latestBuses = new Map();  // bus code -> latest data from /api/buses
let selectedCode = null;
let shownStopIds = "";          // remembers which stops the dropdown currently lists

async function api(path, options) {
    const res = await fetch(path, options);
    const body = await res.json().catch(() => ({}));
    if (!res.ok) {
        const detail = body.fieldErrors && Object.keys(body.fieldErrors).length
            ? " (" + Object.values(body.fieldErrors).join("; ") + ")" : "";
        throw new Error((body.message || "Request failed") + detail);
    }
    return body;
}

function el(tag, text, className) {
    const node = document.createElement(tag);
    node.textContent = text;
    if (className) node.className = className;
    return node;
}

function formatEta(seconds) {
    return seconds < 60 ? "under 1 min" : "about " + Math.round(seconds / 60) + " min";
}

/* ---------- routes and stops (static, drawn once) ---------- */
async function loadRoutes() {
    const routes = await api("/api/routes");
    const drawnStops = new Set();
    routes.forEach((route, i) => {
        const color = COLORS[i % COLORS.length];
        const points = route.stops.map((s) => [s.latitude, s.longitude]);
        L.polyline(points, { color, weight: 4, opacity: 0.6 }).addTo(map);
        route.stops.forEach((s) => {
            if (drawnStops.has(s.id)) return;   // shared stops are drawn once
            drawnStops.add(s.id);
            L.circleMarker([s.latitude, s.longitude], { radius: 6, color: "#333", fillColor: "#fff", fillOpacity: 1, weight: 2 })
                .bindTooltip(el("div", s.name + " (" + s.locality + ")"))
                .addTo(map);
        });
        const li = el("li", "");
        const swatch = el("span", "", "swatch");
        swatch.style.background = color;
        li.append(swatch, document.createTextNode(route.routeNumber + " " + route.name + ": " + route.origin + " to " + route.destination));
        $("legend").appendChild(li);
    });
}

/* ---------- simulated buses (polled) ---------- */
function busIcon(bus) {
    const div = el("div", bus.routeNumber, "bus " + bus.occupancy.toLowerCase() + (bus.code === selectedCode ? " selected" : ""));
    return L.divIcon({ className: "", html: div, iconSize: [38, 22], iconAnchor: [19, 11] });
}

async function refreshBuses() {
    let buses;
    try {
        buses = await api("/api/buses");
    } catch (err) {
        $("request-status").textContent = "Cannot load buses: " + err.message;
        $("request-status").className = "error";
        return;
    }
    const seen = new Set();
    buses.forEach((bus) => {
        seen.add(bus.code);
        latestBuses.set(bus.code, bus);
        const pos = [bus.latitude, bus.longitude];
        let marker = busMarkers.get(bus.code);
        if (!marker) {
            marker = L.marker(pos, { icon: busIcon(bus), zIndexOffset: 1000 }).addTo(map);
            marker.on("click", () => selectBus(bus.code));
            busMarkers.set(bus.code, marker);
        } else {
            marker.setLatLng(pos);
            marker.setIcon(busIcon(bus));
        }
    });
    for (const [code, marker] of busMarkers) {
        if (!seen.has(code)) { map.removeLayer(marker); busMarkers.delete(code); latestBuses.delete(code); }
    }
    renderBusPanel();
}

function selectBus(code) {
    selectedCode = code;
    shownStopIds = "";
    $("request-status").textContent = "";
    renderBusPanel();
}

function renderBusPanel() {
    const bus = latestBuses.get(selectedCode);
    $("bus-panel").hidden = !bus;
    $("bus-hint").hidden = !!bus;
    if (!bus) return;
    $("bus-title").textContent = "Bus " + bus.code + ": route " + bus.routeNumber + " " + bus.routeName;
    $("bus-detail").textContent = "Heading to " + bus.destination + ". Crowding (simulated): " + bus.occupancy
        + (bus.nextStopName ? ". Next stop: " + bus.nextStopName + ", " + formatEta(bus.nextStopEtaSeconds) + " (simulated)." : ".");

    const select = $("stop-select");
    const ids = bus.upcomingStops.map((s) => s.stopId).join(",");
    if (ids !== shownStopIds) {              // rebuild the dropdown only when the list of stops changes
        const previous = select.value;
        select.replaceChildren();
        bus.upcomingStops.forEach((s) => {
            const option = el("option", s.name);
            option.value = s.stopId;
            select.appendChild(option);
        });
        if ([...select.options].some((o) => o.value === previous)) select.value = previous;
        shownStopIds = ids;
    }
    const chosen = bus.upcomingStops.find((s) => String(s.stopId) === select.value);
    $("stop-eta").textContent = chosen ? "Estimated arrival: " + formatEta(chosen.etaSeconds) + " (simulated)" : "No upcoming stops.";
    $("request-btn").disabled = !chosen;
}

$("stop-select").addEventListener("change", renderBusPanel);

/* ---------- stop requests ---------- */
$("request-btn").addEventListener("click", async () => {
    const status = $("request-status");
    try {
        const created = await api("/api/stop-requests", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ busCode: selectedCode, stopId: Number($("stop-select").value) }),
        });
        status.textContent = "Request #" + created.id + " saved with status " + created.status + ". " + created.notice;
        status.className = "ok";
        await loadRequests();
    } catch (err) {
        status.textContent = err.message;
        status.className = "error";
    }
});

async function loadRequests() {
    try {
        const requests = await api("/api/stop-requests");
        const list = $("requests");
        list.replaceChildren();
        requests.forEach((r) => list.appendChild(el("li",
            "#" + r.id + " bus " + r.busCode + " at " + r.stopName + ": " + r.status + " (" + new Date(r.createdAt).toLocaleTimeString() + ")")));
        if (requests.length === 0) list.appendChild(el("li", "No requests yet.", "muted"));
    } catch (err) {
        console.error(err);
    }
}

/* ---------- nearby stops ---------- */
$("nearby-btn").addEventListener("click", async () => {
    const c = map.getCenter();
    const list = $("nearby");
    list.replaceChildren();
    try {
        const stops = await api("/api/stops/nearby?lat=" + c.lat + "&lng=" + c.lng + "&radius=1500");
        stops.forEach((s) => {
            const li = el("li", s.name + ": " + s.distanceMeters + " m", "clickable");
            li.addEventListener("click", () => map.flyTo([s.latitude, s.longitude], 16));
            list.appendChild(li);
        });
        if (stops.length === 0) list.appendChild(el("li", "No stops within 1.5 km of the map centre.", "muted"));
    } catch (err) {
        list.appendChild(el("li", err.message, "error"));
    }
});

/* ---------- start ---------- */
loadRoutes().then(refreshBuses).then(loadRequests).catch((err) => console.error(err));
setInterval(refreshBuses, POLL_MS);
setInterval(loadRequests, 10000);
