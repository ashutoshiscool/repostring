import requests
from bs4 import BeautifulSoup
import datetime

session = requests.Session()

# 1. Login as Patient
response = session.get('http://localhost:8080/login')
soup = BeautifulSoup(response.text, 'html.parser')
csrf_token = soup.find('input', {'name': '_csrf'})['value']

login_data = {
    'username': 'aarav.sharma1@gmail.com',
    'password': 'patient123',
    '_csrf': csrf_token
}
response = session.post('http://localhost:8080/login', data=login_data, allow_redirects=True)
print("Patient logged in")

# 2. Go to Book Appointment page to get CSRF token and Doctor ID
response = session.get('http://localhost:8080/patient/book-appointment')
soup = BeautifulSoup(response.text, 'html.parser')
csrf_token = soup.find('input', {'name': '_csrf'})['value']

# Find doctor ID for Sanjay Thapa
doctor_id = None
select = soup.find('select', {'name': 'doctorId'})
for option in select.find_all('option'):
    if 'sanjay' in option.text.lower() or 'thapa' in option.text.lower():
        doctor_id = option['value']
        break

if not doctor_id:
    print("Could not find Doctor Sanjay Thapa")
    exit(1)

# 3. Submit Appointment
today_str = datetime.date.today().isoformat()
booking_data = {
    'doctorId': doctor_id,
    'appointmentDate': today_str,
    'timeSlot': '10:00',
    'type': 'NORMAL',
    'reason': 'Checkup',
    '_csrf': csrf_token
}
response = session.post('http://localhost:8080/patient/book-appointment', data=booking_data, allow_redirects=True)
print("Booking response URL:", response.url)

# 4. Login as Doctor to check Queue
session = requests.Session()
response = session.get('http://localhost:8080/login')
soup = BeautifulSoup(response.text, 'html.parser')
csrf_token = soup.find('input', {'name': '_csrf'})['value']

login_data = {
    'username': 'sanjay.thapa@rajdhanihealthline.com',
    'password': 'doctor123',
    '_csrf': csrf_token
}
session.post('http://localhost:8080/login', data=login_data, allow_redirects=True)
print("Doctor logged in")

# 5. Fetch Queue
response = session.get('http://localhost:8080/doctor/queue')
if "Aarav Sharma" in response.text or "aarav" in response.text.lower():
    print("SUCCESS: Patient IS in the queue!")
else:
    print("FAILURE: Patient is NOT in the queue!")

