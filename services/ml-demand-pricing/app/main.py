from fastapi import FastAPI, Query
from pydantic import BaseModel
from app.pricing_engine import pricing_engine

app = FastAPI(
    title="RidePulse ML Demand & Dynamic Pricing Engine",
    version="1.0.0",
    description="Real-time H3 spatial aggregation and dynamic surge pricing."
)

class FareEstimateRequest(BaseModel):
    pickup_lat: float
    pickup_lng: float
    dropoff_lat: float
    dropoff_lng: float

@app.get("/health")
def health_check():
    return {"status": "UP", "service": "ml-demand-pricing"}

@app.post("/api/v1/pricing/estimate")
def get_fare_estimate(request: FareEstimateRequest):
    estimate = pricing_engine.compute_fare_estimate(
        request.pickup_lat,
        request.pickup_lng,
        request.dropoff_lat,
        request.dropoff_lng
    )
    return estimate

@app.get("/api/v1/pricing/surge")
def get_current_surge(lat: float = Query(...), lng: float = Query(...)):
    surge = pricing_engine.calculate_surge(lat, lng)
    return {"latitude": lat, "longitude": lng, "surge_multiplier": surge}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("app.main:app", host="0.0.0.0", port=8000, reload=True)
