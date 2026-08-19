import requests

API_URL = "http://localhost:8080/api/telemetry"

fake_reading = {
    "deviceId": "disk-01",
    "capacityUtilization": 88.3,
    "temperature": 55.7
}

response = requests.post(API_URL, json=fake_reading)

print(f'Status code: {response.status_code}')
print(f'Response body: {response.text}')