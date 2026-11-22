// Run from the repository root with mongosh --nodb --file backend/dev/atlas.js.
const uri = process.env.MONGODB_URI;
if (!uri) throw new Error("Set MONGODB_URI in this terminal to the same URI used by Spring Boot.");
const connection = connect(uri);
if (connection.getName() !== "EcoZap") {
    throw new Error("Select the EcoZap database in MONGODB_URI (insert /EcoZap before the query string).");
}
db = connection;
const mode = process.env.ECOZAP_DB_ACTION || "check";
if (mode === "import") {
    process.env.ECOZAP_STATIONS_FILE = "app/src/main/assets/delhi_stations.json";
    load("backend/dev/seed.js");
} else if (mode !== "check") {
    throw new Error("Use check or import for ECOZAP_DB_ACTION.");
}
printjson({
    database: db.getName(),
    stations: db.stations.countDocuments({}),
    slots: db.station_slots.countDocuments({}),
    vehicleModels: db.vehicle_specifications.countDocuments({}),
    bookings: db.bookings.countDocuments({})
});
if (process.env.ECOZAP_BOOKING_ID) {
    const booking = db.bookings.findOne({ _id: process.env.ECOZAP_BOOKING_ID.trim() },
        { _id: 1, status: 1, stationId: 1, slotId: 1 });
    if (booking) printjson(booking);
    else print("No booking with that ID in this database.");
}
