// mongosh, run on the local Compose database when its data volume is first created.
const demo = db.getSiblingDB("EcoZap");
const stations = JSON.parse(require("fs").readFileSync("/demo/stations.json", "utf8"));
function addIfMissing(collection, id, doc) {
    collection.updateOne({ _id: id }, { $setOnInsert: doc }, { upsert: true });
}
for (const station of stations) {
    const id = "delhi-" + station.id;
    const {name, type, latitude, longitude} = station;
    addIfMissing(demo.stations, id, {name, type, latitude, longitude});
    station.slots.forEach((slot, index) => {
        const service = type === "EV" ? {chargerPower: 50} : {estimatedServiceTimeMinutes: 10};
        addIfMissing(demo.station_slots, id + "-" + (index + 1), {
            stationId: id, slotNumber: index + 1, time: slot.time, vehicleType: type, ...service
        });
    });
}
addIfMissing(demo.vehicle_specifications, "demo-ev", {
    brand: "Demo", model: "Electric car", type: "EV", batteryCapacity: 40, efficiency: 6
});
addIfMissing(demo.vehicle_specifications, "demo-cng", {
    brand: "Demo", model: "CNG car", type: "CNG", tankCapacity: 10, efficiency: 22
});
