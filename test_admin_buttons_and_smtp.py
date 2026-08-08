import requests
from bs4 import BeautifulSoup
import time

BASE_URL = 'http://localhost:8080'

def test_admin_features():
    print("=" * 60)
    print("TESTING ADMIN GOOGLE SMTP CONFIG & ADD DOCTOR / PATIENT / APPOINTMENT BUTTONS")
    print("=" * 60)

    s = requests.Session()
    res = s.get(f'{BASE_URL}/login')
    soup = BeautifulSoup(res.text, 'html.parser')
    csrf_token = soup.find('input', {'name': '_csrf'})['value']

    admin_login = {
        'username': 'admin@hospital.com',
        'password': 'admin123',
        '_csrf': csrf_token
    }
    res = s.post(f'{BASE_URL}/login', data=admin_login, allow_redirects=True)
    assert res.status_code == 200 and '/admin/dashboard' in res.url, f"Admin login failed! URL: {res.url}"
    print("✅ [1/5] Admin Login SUCCESS!")

    # 1. Test Gmail Integration Page GET & Status API
    res = s.get(f'{BASE_URL}/admin/smtp')
    assert res.status_code == 200, "Failed to load /admin/smtp page!"
    
    res = s.get(f'{BASE_URL}/api/integrations/gmail/status')
    assert res.status_code == 200, "Failed to call /api/integrations/gmail/status endpoint!"
    print("✅ [2/5] Admin Gmail Integration Page & API Status SUCCESS!")

    # 2. Test Add Doctor POST
    res = s.get(f'{BASE_URL}/admin/doctors')
    soup = BeautifulSoup(res.text, 'html.parser')
    csrf_token = soup.find('input', {'name': '_csrf'})['value']

    doc_data = {
        'firstName': 'Anita',
        'lastName': 'Gurung',
        'email': f'anita.gurung.{int(time.time())}@rajdhanihealthline.com',
        'password': 'doctorpassword123',
        'specialization': 'Pediatrician',
        'phone': '9801122334',
        'experienceYears': 8,
        '_csrf': csrf_token
    }
    res = s.post(f'{BASE_URL}/admin/doctors/add', data=doc_data, allow_redirects=True)
    assert 'success=DoctorAdded' in res.url, f"Add Doctor failed! URL: {res.url}"
    print("✅ [3/5] Admin Add Doctor Button & Action SUCCESS!")

    # 3. Test Add Patient POST
    res = s.get(f'{BASE_URL}/admin/patients')
    soup = BeautifulSoup(res.text, 'html.parser')
    csrf_token = soup.find('input', {'name': '_csrf'})['value']

    pat_data = {
        'firstName': 'Ramesh',
        'lastName': 'Karki',
        'email': f'ramesh.karki.{int(time.time())}@gmail.com',
        'password': 'patientpassword123',
        'phone': '9841998877',
        'gender': 'Male',
        'bloodGroup': 'B+',
        '_csrf': csrf_token
    }
    res = s.post(f'{BASE_URL}/admin/patients/add', data=pat_data, allow_redirects=True)
    assert 'success=PatientAdded' in res.url, f"Add Patient failed! URL: {res.url}"
    print("✅ [4/5] Admin Add Patient Button & Action SUCCESS!")

    # 4. Test Add Appointment POST
    res = s.get(f'{BASE_URL}/admin/appointments')
    soup = BeautifulSoup(res.text, 'html.parser')
    csrf_token = soup.find('input', {'name': '_csrf'})['value']

    # Select doctor 1 and patient 1
    appt_data = {
        'patientId': 1,
        'doctorId': 1,
        'appointmentDate': '2026-08-15',
        'timeSlot': '14:30',
        'type': 'NORMAL',
        'reason': 'Admin Scheduled Checkup',
        '_csrf': csrf_token
    }
    res = s.post(f'{BASE_URL}/admin/appointments/add', data=appt_data, allow_redirects=True)
    assert 'success=Booked' in res.url, f"Add Appointment failed! URL: {res.url}"
    print("✅ [5/5] Admin Add Appointment Button & Action SUCCESS!")

    print("=" * 60)
    print("ALL ADMIN GOOGLE SMTP & ADD BUTTON TESTS PASSED PERFECTLY!")
    print("=" * 60)

if __name__ == '__main__':
    test_admin_features()
