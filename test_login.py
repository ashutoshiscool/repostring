import requests
from bs4 import BeautifulSoup

session = requests.Session()

# 1. Get the login page to grab the CSRF token
response = session.get('http://localhost:8080/login')
soup = BeautifulSoup(response.text, 'html.parser')
csrf_token = soup.find('input', {'name': '_csrf'})['value']

# Patient Login
login_data = {
    'username': 'aarav.sharma1@gmail.com',
    'password': 'patient123',
    '_csrf': csrf_token
}
response = session.post('http://localhost:8080/login', data=login_data, allow_redirects=False)

if response.status_code == 302 and response.headers.get('Location') == 'http://localhost:8080/dashboard':
    print("Patient Login SUCCESS! Redirected to /dashboard")
else:
    print(f"Patient Login FAILED! Status: {response.status_code}, Location: {response.headers.get('Location')}")
