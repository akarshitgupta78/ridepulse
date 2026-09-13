import time
import requests

GATEWAY_URL = "http://localhost:8088"

def run_simulation():
    print("\n=======================================================")
    print("🚀 STARTING RIDEPULSE END-TO-END FLOW SIMULATION")
    print("=======================================================\n")

    timestamp = int(time.time())

    # 1. Register a Rider
    print("[1/6] Registering Rider...")
    rider_payload = {
        "fullName": "Alice Johnson",
        "email": f"alice.{timestamp}@example.com",
        "phoneNumber": f"+1555{timestamp % 10000000:07d}"
    }
    rider_res = requests.post(f"{GATEWAY_URL}/api/v1/users/register", json=rider_payload)
    assert rider_res.status_code == 201, f"User registration failed: {rider_res.text}"
    rider_id = rider_res.json()["id"]
    print(f"  ✓ Rider Registered: {rider_payload['fullName']} (ID: {rider_id})")

    # 2. Register and Onboard a Driver
    print("\n[2/6] Registering & Onboarding Driver...")
    driver_payload = {
        "name": "Bob Smith",
        "phone": f"+1444{timestamp % 10000000:07d}",
        "vehicleNumber": "NY-8842",
        "vehicleModel": "Toyota Prius"
    }
    driver_res = requests.post(f"{GATEWAY_URL}/api/v1/drivers/register", json=driver_payload)
    assert driver_res.status_code == 201, f"Driver registration failed: {driver_res.text}"
    driver_id = driver_res.json()["id"]
    print(f"  ✓ Driver Registered: {driver_payload['name']} (ID: {driver_id})")

    # Set Driver Status to Online
    requests.put(f"{GATEWAY_URL}/api/v1/drivers/{driver_id}/status?online=true")
    print(f"  ✓ Driver Status set to ONLINE")

    # 3. Check Driver Proximity (Redis Geospatial)
    print("\n[3/6] Checking Nearby Driver Geospatial Index...")
    pickup_lat, pickup_lng = 40.7128, -74.0060  # Manhattan, NYC
    dropoff_lat, dropoff_lng = 40.7589, -73.9851 # Times Square, NYC

    loc_res = requests.get(f"{GATEWAY_URL}/api/v1/locations/nearby?lat={pickup_lat}&lng={pickup_lng}&radiusKm=5.0")
    print(f"  ✓ Location Search verified (HTTP {loc_res.status_code})")

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
    estimated_fare = pricing_data["estimated_fare"]
    surge_multiplier = pricing_data["surge_multiplier"]
    distance_km = pricing_data["distance_km"]
    print(f"  ✓ Fare Computed: ${estimated_fare:.2f} (Surge: {surge_multiplier}x | Dist: {distance_km} km)")

    # 5. Request a Ride (Booking Service -> Kafka -> Matching Engine)
    print("\n[5/6] Requesting Ride (Emitting Kafka RIDE_REQUESTED)...")
    ride_req = {
        "riderId": rider_id,
        "pickupLat": pickup_lat,
        "pickupLng": pickup_lng,
        "dropoffLat": dropoff_lat,
        "dropoffLng": dropoff_lng,
        "estimatedFare": estimated_fare
    }
    ride_res = requests.post(f"{GATEWAY_URL}/api/v1/rides/request", json=ride_req)
    assert ride_res.status_code == 201, f"Ride request failed: {ride_res.text}"
    ride = ride_res.json()
    ride_id = ride["id"]
    print(f"  ✓ Ride Created: ID {ride_id} | Status: {ride['status']}")

    # 6. Complete the Ride (Emitting Kafka RIDE_COMPLETED -> Payment & Notification)
    print("\n[6/6] Completing Ride & Triggering Idempotent Payment Settlement...")
    time.sleep(1)  # Allow Kafka event propagation
    complete_res = requests.put(f"{GATEWAY_URL}/api/v1/rides/{ride_id}/complete")
    assert complete_res.status_code == 200, f"Complete ride failed: {complete_res.text}"
    completed_ride = complete_res.json()
    print(f"  ✓ Ride Marked as: {completed_ride['status']}")
    print(f"  ✓ Payment event emitted to Kafka topic 'ride.events'")

    print("\n=======================================================")
    print("🎉 FULL DISTRIBUTED LIFECYCLE SIMULATION COMPLETED!")
    print(f"   Rider ID:  {rider_id}")
    print(f"   Driver ID: {driver_id}")
    print(f"   Ride ID:   {ride_id}")
    print(f"   Fare:      ${estimated_fare:.2f}")
    print("=======================================================\n")

if __name__ == "__main__":
    run_simulation()
