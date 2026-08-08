import requests
from bs4 import BeautifulSoup
import subprocess
import os

BASE_URL = 'http://localhost:8080'

def test_gmail_oauth_integration():
    print("=" * 60)
    print("TESTING GMAIL API & OAUTH 2.0 INTEGRATION ENDPOINTS")
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

    # 1. Test Status Endpoint
    res = s.get(f'{BASE_URL}/api/integrations/gmail/status')
    assert res.status_code == 200, f"Gmail status endpoint failed! Status: {res.status_code}"
    status_data = res.json()
    assert 'connected' in status_data, "Status payload missing 'connected' key!"
    print(f"✅ [2/5] Gmail API Status Endpoint SUCCESS! Payload: {status_data}")

    # 2. Test Connect Endpoint (OAuth redirect)
    res = s.get(f'{BASE_URL}/api/integrations/gmail/connect', allow_redirects=False)
    assert res.status_code in [302, 303], f"Connect endpoint did not redirect! Status: {res.status_code}"
    redirect_target = res.headers.get('Location', '')
    if 'accounts.google.com' in redirect_target:
        assert 'scope=https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fgmail.send' in redirect_target or 'gmail.send' in redirect_target
        assert 'access_type=offline' in redirect_target
        assert 'state=' in redirect_target
        print("✅ [3/5] Gmail OAuth Connect Authorization URL Generation SUCCESS!")
    else:
        assert 'error=MissingClientCredentials' in redirect_target, f"Unexpected redirect: {redirect_target}"
        print("✅ [3/5] Gmail OAuth Connect safely reported MissingClientCredentials when unconfigured!")

    # 3. Test Disconnect Endpoint
    res = s.get(f'{BASE_URL}/admin/smtp')
    soup = BeautifulSoup(res.text, 'html.parser')
    csrf_token = soup.find('input', {'name': '_csrf'})['value'] if soup.find('input', {'name': '_csrf'}) else ''

    res = s.post(f'{BASE_URL}/api/integrations/gmail/disconnect', data={'_csrf': csrf_token}, allow_redirects=True)
    assert res.status_code == 200 and 'success=Disconnected' in res.url, f"Disconnect endpoint failed! URL: {res.url}"
    print("✅ [4/5] Gmail Disconnect Endpoint SUCCESS!")

    # 4. Verify Zero Legacy SMTP References Remain in Repository
    result = subprocess.run(['grep', '-ri', 'smtp.gmail.com', '/home/ubuntu/repostring/src'], capture_output=True, text=True)
    assert result.returncode != 0, f"Found legacy smtp.gmail.com references: {result.stdout}"
    
    result2 = subprocess.run(['grep', '-ri', 'spring.mail', '/home/ubuntu/repostring/src'], capture_output=True, text=True)
    assert result2.returncode != 0, f"Found legacy spring.mail references: {result2.stdout}"

    print("✅ [5/5] Legacy SMTP Audit PASSED! Zero SMTP references remain in src/.")
    print("=" * 60)
    print("ALL GMAIL API & OAUTH 2.0 TESTS PASSED PERFECTLY!")
    print("=" * 60)

if __name__ == '__main__':
    test_gmail_oauth_integration()
