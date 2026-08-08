# Google Cloud & Gmail API Setup Guide

This guide walks you through setting up a Google Cloud Console OAuth 2.0 Web Application client to connect your Gmail account to VirtualQueue for sending automated OTPs, prescription emails, and notifications using the official **Gmail API (OAuth 2.0)**.

---

## 10-Step Setup Guide

### Step 1: Create or Select a Google Cloud Project
1. Go to the [Google Cloud Console](https://console.cloud.google.com/).
2. In the top navigation bar, click the project drop-down menu.
3. Click **New Project**, enter a project name (e.g. `VirtualQueue Hospital`), and click **Create**.

### Step 2: Enable the Gmail API
1. In the Google Cloud Console, open the left navigation menu and go to **APIs & Services > Library**.
2. Search for **Gmail API**.
3. Select **Gmail API** from the search results and click **Enable**.

### Step 3: Configure the OAuth Consent Screen
1. Go to **APIs & Services > OAuth consent screen**.
2. Select User Type:
   - **Internal** (if using a Google Workspace organization), OR
   - **External** (if using a personal `@gmail.com` account).
3. Click **Create**.
4. Fill in the required fields:
   - **App name:** `VirtualQueue Health Management`
   - **User support email:** Select your Gmail address.
   - **Developer contact info:** Enter your email address.
5. Click **Save and Continue**.

### Step 4: Add Authorized Scopes
1. On the Scopes step, click **Add or Remove Scopes**.
2. Filter or search for `gmail.send`.
3. Select the check box for:
   `https://www.googleapis.com/auth/gmail.send` (Send messages on your behalf)
4. Click **Update**, then click **Save and Continue**.
5. On the Test Users step (if using External user type), click **Add Users** and add the Gmail address you will use to connect.
6. Click **Save and Continue**.

### Step 5: Create OAuth 2.0 Web Application Credentials
1. Go to **APIs & Services > Credentials**.
2. Click **+ Create Credentials** at the top and select **OAuth client ID**.
3. Select **Application type:** `Web application`.
4. Name: `VirtualQueue Web Client`.

### Step 6: Configure Authorized Redirect URIs
1. Under **Authorized redirect URIs**, click **+ Add URI**.
2. Enter the exact application callback URL:
   ```text
   http://localhost:8080/api/integrations/gmail/callback
   ```
   *(For production deployments, add your production domain: `https://yourdomain.com/api/integrations/gmail/callback`)*.
3. Click **Create**.
4. Copy your **Client ID** and **Client Secret**.

### Step 7: Configure Environment Variables
Set the environment variables on your server or update your `.env` file:

```bash
export GOOGLE_CLIENT_ID="your-client-id.apps.googleusercontent.com"
export GOOGLE_CLIENT_SECRET="your-client-secret"
export GOOGLE_REDIRECT_URI="http://localhost:8080/api/integrations/gmail/callback"
```

Or run the application with environment variables:
```bash
GOOGLE_CLIENT_ID="..." GOOGLE_CLIENT_SECRET="..." ./start.sh
```

### Step 8: Run VirtualQueue Application
Execute `./start.sh` or run Maven:
```bash
mvn spring-boot:run
```

### Step 9: Open Admin Console & Click Connect Gmail
1. Log into the VirtualQueue Admin Portal (`http://localhost:8080/admin/dashboard`).
2. Navigate to **Gmail Integration** in the left sidebar (`http://localhost:8080/admin/smtp`).
3. Click the **[ Connect Gmail ]** button.

### Step 10: Grant Permission & Complete Integration
1. Google will display the sign-in and authorization consent screen.
2. Sign into your Gmail account and click **Allow** to grant email sending permissions.
3. Google will redirect back to VirtualQueue. You will see:
   `✓ Gmail Connected`
   `admin@example.com`
4. Click **[ Send Test Email ]** to verify real email delivery.
