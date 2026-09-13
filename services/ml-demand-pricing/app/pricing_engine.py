import h3
import redis
import numpy as np

class DynamicPricingEngine:
    def __init__(self, redis_host="localhost", redis_port=6379):
        self.redis_client = redis.Redis(host=redis_host, port=redis_port, decode_responses=True)
        self.base_fare = 5.0
        self.per_km_rate = 1.75
        self.per_minute_rate = 0.35

    def get_h3_index(self, lat: float, lng: float, resolution: int = 7) -> str:
        """Indexes GPS coordinates into an H3 hexagonal spatial cell (Resolution 7 ~ 1.2km radius)."""
        if hasattr(h3, 'latlng_to_cell'):
            return h3.latlng_to_cell(lat, lng, resolution)
        return h3.geo_to_h3(lat, lng, resolution)

    def calculate_surge(self, pickup_lat: float, pickup_lng: float) -> float:
        h3_cell = self.get_h3_index(pickup_lat, pickup_lng)
        demand_key = f"h3:demand:{h3_cell}"

        # Increment demand count for this geo cell (5 min TTL)
        demand_count = self.redis_client.incr(demand_key)
        if demand_count == 1:
            self.redis_client.expire(demand_key, 300)

        # Count available drivers in proximity
        active_drivers = self.redis_client.georadius(
            name="active_drivers:geo",
            longitude=pickup_lng,
            latitude=pickup_lat,
            radius=3.0,
            unit="km"
        )
        supply_count = max(len(active_drivers), 1)

        # Demand-to-Supply Ratio heuristic / ML-bounded curve
        ratio = demand_count / supply_count

        if ratio <= 1.0:
            surge = 1.0
        elif ratio <= 2.5:
            surge = 1.0 + (ratio - 1.0) * 0.4
        elif ratio <= 5.0:
            surge = 1.6 + (ratio - 2.5) * 0.3
        else:
            surge = 2.5  # Max surge cap

        return round(float(surge), 2)

    def compute_fare_estimate(self, pickup_lat: float, pickup_lng: float, dropoff_lat: float, dropoff_lng: float) -> dict:
        # Haversine / Great Circle distance estimation
        if hasattr(h3, 'great_circle_distance'):
            distance_km = round(h3.great_circle_distance((pickup_lat, pickup_lng), (dropoff_lat, dropoff_lng), unit='km'), 2)
        else:
            distance_km = round(h3.point_dist((pickup_lat, pickup_lng), (dropoff_lat, dropoff_lng), unit='km'), 2)
        
        est_duration_minutes = round(distance_km * 2.5 + 3.0, 1)

        surge_multiplier = self.calculate_surge(pickup_lat, pickup_lng)

        standard_cost = self.base_fare + (distance_km * self.per_km_rate) + (est_duration_minutes * self.per_minute_rate)
        final_fare = round(standard_cost * surge_multiplier, 2)

        return {
            "distance_km": distance_km,
            "estimated_duration_mins": est_duration_minutes,
            "base_fare": self.base_fare,
            "surge_multiplier": surge_multiplier,
            "estimated_fare": final_fare
        }

pricing_engine = DynamicPricingEngine()
