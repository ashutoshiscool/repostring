import requests
from bs4 import BeautifulSoup
import datetime
import io

BASE_URL = 'http://localhost:8080'

def test_full_system_and_prescriptions():
    print("=" * 60)
    print("STARTING END-TO-END TEST: AUTH, BOOKING, PRESCRIPTION, & ENDPOINTS")
    print("=" * 60)

    # -------------------------------------------------------------
    # 1. Test Admin Login
    # -------------------------------------------------------------
    session_admin = requests.Session()
    res = session_admin.get(f'{BASE_URL}/login')
    soup = BeautifulSoup(res.text, 'html.parser')
    csrf_token = soup.find('input', {'name': '_csrf'})['value']

    admin_login = {
        'username': 'admin@hospital.com',
        'password': 'admin123',
        '_csrf': csrf_token
    }
    res = session_admin.post(f'{BASE_URL}/login', data=admin_login, allow_redirects=True)
    assert res.status_code == 200 and '/admin/dashboard' in res.url, f"Admin login failed! URL: {res.url}"
    print("✅ [1/6] Admin Login SUCCESS! Dashboard accessible.")

    # -------------------------------------------------------------
    # 2. Test Patient Login
    # -------------------------------------------------------------
    session_patient = requests.Session()
    res = session_patient.get(f'{BASE_URL}/login')
    soup = BeautifulSoup(res.text, 'html.parser')
    csrf_token = soup.find('input', {'name': '_csrf'})['value']

    patient_login = {
        'username': 'aarav.sharma1@gmail.com',
        'password': 'patient123',
        '_csrf': csrf_token
    }
    res = session_patient.post(f'{BASE_URL}/login', data=patient_login, allow_redirects=True)
    assert res.status_code == 200 and '/patient/dashboard' in res.url, f"Patient login failed! URL: {res.url}"
    print("✅ [2/6] Patient Login SUCCESS! Dashboard accessible.")

    # -------------------------------------------------------------
    # 3. Patient Books Appointment for Today
    # -------------------------------------------------------------
    res = session_patient.get(f'{BASE_URL}/patient/book-appointment')
    soup = BeautifulSoup(res.text, 'html.parser')
    csrf_token = soup.find('input', {'name': '_csrf'})['value']

    select = soup.find('select', {'name': 'doctorId'})
    doctor_id = None
    for opt in select.find_all('option'):
        if 'sanjay' in opt.text.lower() or 'thapa' in opt.text.lower():
            doctor_id = opt['value']
            break
    assert doctor_id is not None, "Could not find Doctor Sanjay Thapa in booking form!"

    today_str = datetime.date.today().isoformat()
    booking_data = {
        'doctorId': doctor_id,
        'appointmentDate': today_str,
        'timeSlot': '11:00',
        'type': 'NORMAL',
        'reason': 'Routine Health Evaluation and Prescription Request',
        '_csrf': csrf_token
    }
    res = session_patient.post(f'{BASE_URL}/patient/book-appointment', data=booking_data, allow_redirects=True)
    assert 'success=booked' in res.url, f"Appointment booking failed! URL: {res.url}"
    print("✅ [3/6] Patient Appointment Booking SUCCESS!")

    # Find the appointment ID for Dr. Sanjay Thapa from Patient Appointments page
    res = session_patient.get(f'{BASE_URL}/patient/appointments')
    soup = BeautifulSoup(res.text, 'html.parser')
    appointment_id = None
    for tr in soup.find_all('tr'):
        if 'sanjay' in tr.text.lower() or 'thapa' in tr.text.lower():
            link = tr.find('a', href=lambda h: h and '/patient/appointment/' in h and '/prescription' in h)
            if link:
                appointment_id = link['href'].split('/appointment/')[1].split('/prescription')[0]
                break
    
    if not appointment_id:
        first_link = soup.find('a', href=lambda h: h and '/patient/appointment/' in h and '/prescription' in h)
        appointment_id = first_link['href'].split('/appointment/')[1].split('/prescription')[0]

    print(f"   Target Appointment ID for Prescription: {appointment_id}")

    # -------------------------------------------------------------
    # 4. Test Doctor Login
    # -------------------------------------------------------------
    session_doctor = requests.Session()
    res = session_doctor.get(f'{BASE_URL}/login')
    soup = BeautifulSoup(res.text, 'html.parser')
    csrf_token = soup.find('input', {'name': '_csrf'})['value']

    doctor_login = {
        'username': 'sanjay.thapa@rajdhanihealthline.com',
        'password': 'doctor123',
        '_csrf': csrf_token
    }
    res = session_doctor.post(f'{BASE_URL}/login', data=doctor_login, allow_redirects=True)
    assert res.status_code == 200 and '/doctor/dashboard' in res.url, f"Doctor login failed! URL: {res.url}"
    print("✅ [4/6] Doctor Login SUCCESS! Dashboard accessible.")

    # -------------------------------------------------------------
    # 5. Doctor Attaches Prescription (Text + Image Upload)
    # -------------------------------------------------------------
    res = session_doctor.get(f'{BASE_URL}/doctor/appointment/{appointment_id}/prescription')
    soup = BeautifulSoup(res.text, 'html.parser')
    csrf_token = soup.find('input', {'name': '_csrf'})['value']

    # Generate a large 5MB dummy PNG payload to test unlimited upload size configuration
    header_bytes = (
        b'\x89PNG\r\n\x1a\n\x00\x00\x00\rIHDR\x00\x00\x00\x01\x00\x00\x00\x01'
        b'\x08\x06\x00\x00\x00\x1f\x15c4\x00\x00\x00\nIDATx\x9cc\x00\x01\x00\x00'
        b'\x05\x00\x01\r\n-\xb4\x00\x00\x00\x00IEND\xaeB`\x82'
    )
    # Add 5MB of padded bytes
    large_png_bytes = header_bytes + (b'0' * (5 * 1024 * 1024))

    form_data = {
        'notes': 'Paracetamol 500mg - 1 tab every 8 hours for fever.\nAmoxicillin 500mg - 1 capsule twice daily for 5 days after meals.',
        '_csrf': csrf_token
    }
    files = {
        'image': ('rx_large_hd_scan.png', io.BytesIO(large_png_bytes), 'image/png')
    }

    res = session_doctor.post(
        f'{BASE_URL}/doctor/appointment/{appointment_id}/prescription',
        data=form_data,
        files=files,
        allow_redirects=True
    )
    assert 'success=PrescriptionSaved' in res.url, f"Prescription upload failed! URL: {res.url}"
    print("✅ [5/6] Doctor Prescription Attachment (Large 5MB Image Upload) SUCCESS!")

    # -------------------------------------------------------------
    # 6. Patient Views Prescription & Verifies Image Static Endpoint
    # -------------------------------------------------------------
    res = session_patient.get(f'{BASE_URL}/patient/appointment/{appointment_id}/prescription')
    soup = BeautifulSoup(res.text, 'html.parser')
    
    assert 'Paracetamol 500mg' in res.text, "Prescription text notes missing from Patient view!"
    
    img_tag = soup.find('img', alt=lambda a: a and 'Prescription' in a)
    assert img_tag is not None and img_tag.get('src'), "Prescription image tag missing from Patient view!"
    
    img_src = img_tag['src']
    print(f"   Patient Prescription Image URL: {img_src}")

    # Fetch the image URL directly using patient session
    img_res = session_patient.get(f'{BASE_URL}{img_src}')
    assert img_res.status_code == 200, f"Prescription image static endpoint returned {img_res.status_code}!"
    assert len(img_res.content) > 0, "Prescription image content is empty!"
    print("✅ [6/6] Patient Prescription View & Static Image Fetch SUCCESS (HTTP 200 OK)!")

    # Test patient prescriptions list endpoint
    res_list = session_patient.get(f'{BASE_URL}/patient/prescriptions')
    assert res_list.status_code == 200 and 'Prescription History' in res_list.text, "Patient /prescriptions page failed!"

    print("=" * 60)
    print("ALL TESTS PASSED PERFECTLY! Prescriptions & Endpoints Verified.")
    print("=" * 60)

if __name__ == '__main__':
    test_full_system_and_prescriptions()
