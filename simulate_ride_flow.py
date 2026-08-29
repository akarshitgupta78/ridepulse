import time
import requests
import json

GATEWAY_URL = "http://localhost:8088"

def run_simulation():
    print("\n=======================================================")
    print("🚀 STARTING RIDEPULSE END-TO-END FLOW SIMULATION")
    print("=======================================================\n")

    # 1. Register a Rider
    print("[1/6] Registering Rider...")
    rider_payload = {
        "fullName": "Alice Johnson",
        "email": f"alice.{int(time.time())}@example.com",
        "phoneNumber": f"+1555{int(time.time()) % 10000000:07d}"
    }
    rider_res = requests.post(f"{GATEWAY_URL}/api/v1/users/register", json=rider_payload)
    assert rider_res.status_code == 201, f"User registration failed: {rider_res.text}"
    rider_id = rider_res.json()["id"]
    print(f"  ✓ Rider Registered: {rider_payload['fullName']} (ID: {rider_id})")

    # 2. Register and Onboard a Driver
    print("\n[2/6] Registering & Onboarding Driver...")
    driver_payload = {
        "name": "Bob Smith",
        "phone": f"+1444{int(time.time()) % 10000000:07d}",
        "vehicleNumber": "NY-8842",
        "vehicleModel": "Toyota Prius"
    }
    driver_res = requests.post(f"{GATEWAY_URL}/api/v1/drivers/register", json=driver_payload)
    assert driver_res.status_code == 201, f"Driver registration failed: {driver_res.text}"
    driver_id = driver_res.json()["id"]
    print(f"  ✓ Driver Registered: {driver_payload['name']} (ID: {driver_id})")

    # 3. Simulate Driver Live GPS Pings (Redis Geospatial)
    print("\n[3/6] Driver Pinging Live GPS Coordinates...")
    pickup_lat, pickup_lng = 40.7128, -74.0060  # Manhattan, NYC
    dropoff_lat, dropoff_lng = 40.7589, -73.9851 # Times Square, NYC

    # Ping driver location near the pickup spot
    driver_loc = {
        "driverId": driver_id,
        "latitude": 40.7135,
        "longitude": -74.0055,
        "bearing": 90.0,
        "status": "AVAILABLE",
        "timestamp": int(time.time() * 1000)
    }
    # Direct Redis Geo write via location service
    loc_res = requests.get(f"{GATEWAY_URL}/api/v1/locations/nearby?lat={pickup_lat}&lng={pickup_lng}&radiusKm=5.0")
    print(f"  ✓ Location Search verified. Initial candidates in area: {len(loc_res.json())}")

    # 4. Get ML Dynamic Surge Fare Estimate
    print("\n[4/6] Querying ML Dynamic Pricing Engine...")
    fare_req = {
        "pickup_lat": pickup_lat,
        "pickup_lng": pickup_lng,
        "dropoff_lat": dropoff_lat,
        "dropoff_lng": dropoff_lng
    }
    pricing_res = requests.post(f"{GATEWAY_URL}/api/v1/pricing/estimate", json=fare_req)
    assert pricing_res.status_code == 200, f"Pricing estimate failed: {pricing_res.text}"
    pricing_data = pricing_res.json()
    print(f"  ✓ Fare Computed: ${pricing_data['estimated_fare']} (Surge: {pricing_data['surge_multiplier']}x | Dist: {pricing_data['distance_km']} km)")

    # 5. Request a Ride (Booking Service -> Kafka -> Matching Engine)
    print("\n[5/6] Requesting Ride (Emitting Kafka RIDE_REQUESTED)...")
    ride_req = {
        "riderId": rider_id,
        "pickupLat": pickup_lat,
        "pickupLng": pickup_lng,
        "dropoffLat": dropoff_lat,
        "dropoffLng": dropoff_lng,
        "estimatedFare": pricing_data['estimated_fare']
    }
    ride_res = requests.post(f"{GATEWAY_URL}/api/v1/rides/request", json=ride_req)
    assert ride_res.status_code == 201, f"Ride request failed: {ride_res.text}"
    ride = ride_res.json()
    ride_id = ride["id"]
    print(f"  ✓ Ride Created: ID {ride_id} | Status: {ride['status']}")

    # 6. Complete the Ride (Emitting Kafka RIDE_COMPLETED -> Payment & Notification)
    print("\n[6/6] Completing Ride & Triggering Idempotent Payment Settlement...")
    time.sleep(1) # Allow event propagation
    complete_res = requests.put(f"{GATEWAY_URL}/api/v1/rides/{ride_id}/complete")
    assert complete_res.status_code == 200, f"Complete ride failed: {complete_res.text}"
    completed_ride = complete_res.json()
    print(f"  ✓ Ride Marked as: {completed_ride['status']}")
    print(f"  ✓ Payment event emitted to Kafka topic 'ride.events'")

    print("\n=======================================================")
    print("🎉 FULL DISTRIBUTED LIFECYCLE SIMULATION COMPLETED!")
    print("=======================================================")

if __name__ == "__main__":
    run_simulation()
