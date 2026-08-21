import requests
import random
import time

API_URL = "http://localhost:8080/api/telemetry"

DEVICE_IDS = [1, 2, 3]

INTERVAL_SECONDS = 5

def generate_reading(device_id):
    return {
        "deviceId": device_id,
        "capacityUtilization": round(random.uniform(20.0, 95.0), 1),
        "temperature": round(random.uniform(30.0, 70.0), 1)
    }

def send_reading(reading):
    try:
        response = requests.post(API_URL, json=reading)
        print(f"device {reading['deviceId']}: {response.status_code} - {response.text}")
    except requests.exceptions.ConnectionError:
        print(f"device {reading['deviceId']}: could not reach server, is it running?")

def main():
    print(f"Starting telemetry agent. Simulating devices {DEVICE_IDS}, every {INTERVAL_SECONDS}s. Ctrl+C to stop.")
    while True:
        for device_id in DEVICE_IDS:
            reading = generate_reading(device_id)
            send_reading(reading)
        time.sleep(INTERVAL_SECONDS)

if __name__ == "__main__":
    main()