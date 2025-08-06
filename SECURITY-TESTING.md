# Security Testing Guide

## Overview
This document describes how to test the authentication and authorization system in MyFinances.

## Security Configuration Summary

### Protected Endpoints (Require Authentication)
- `/dashboard` - Main dashboard
- `/transactions/**` - All transaction-related pages
- `/accounts/**` - All account-related pages
- `/investments/**` - All investment-related pages
- `/budgets/**` - All budget-related pages  
- `/reports/**` - All report-related pages
- `/security-test/protected` - Test endpoint (development only)

### Public Endpoints (No Authentication Required)
- `/` - Landing page
- `/home` - Alternative home route
- `/login` - Login page
- `/register` - Registration page
- `/error` - Error pages
- `/favicon.ico` - Favicon
- `/webjars/**` - WebJar resources
- `/css/**`, `/js/**`, `/images/**`, `/static/**` - Static resources
- `/h2-console/**` - H2 database console (development only)
- `/security-test/public` - Public test endpoint (development only)
- `/api/auth/login`, `/api/auth/register` - Future API endpoints

## Testing Authentication

### Manual Testing Steps

1. **Test Protected Endpoints Without Authentication:**
   - Open browser in incognito/private mode
   - Try to access: `http://localhost:8080/dashboard`
   - **Expected Result:** Should redirect to `/login`

2. **Test Public Endpoints:**
   - Access: `http://localhost:8080/`
   - Access: `http://localhost:8080/login`
   - Access: `http://localhost:8080/register`
   - **Expected Result:** Should load without redirect

3. **Test Authentication Flow:**
   - Go to `/login`
   - Enter valid credentials
   - **Expected Result:** Should redirect to `/dashboard`
   - Try accessing other protected endpoints
   - **Expected Result:** Should work without redirect

4. **Test Session Management:**
   - Login successfully
   - Close browser completely
   - Open new browser session
   - Try accessing `/dashboard`
   - **Expected Result:** Should redirect to login (unless "Remember Me" was selected)

### Security Test Endpoints (Development Only)

Access these endpoints to debug authentication issues:

- `/security-test/public` - Should be accessible without login
- `/security-test/protected` - Should require login

These endpoints show:
- Authentication status
- Session information
- User principal details
- Granted authorities

## Common Issues and Solutions

### Issue: Endpoints not redirecting to login

**Possible Causes:**
1. Request matcher not properly configured
2. Static resources blocking authentication
3. Exception handling not working correctly
4. Session management issues

**Debugging Steps:**
1. Check application logs for security debug information
2. Use security test endpoints to verify authentication state
3. Verify security configuration order
4. Test with browser developer tools to see redirect responses

### Issue: Login successful but still getting redirected

**Possible Causes:**
1. Session not persisting
2. Authentication principal not properly stored
3. Remember-me token issues

**Debugging Steps:**
1. Check session ID consistency
2. Verify user principal is properly loaded
3. Check database for persistent tokens

## Production Security Checklist

Before deploying to production:

- [ ] Remove or secure `/h2-console/**` endpoints
- [ ] Remove `/security-test/**` endpoints
- [ ] Enable CSRF protection if needed
- [ ] Configure proper HTTPS
- [ ] Review session timeout settings
- [ ] Verify password encoding is secure
- [ ] Test with security scanning tools
- [ ] Enable proper logging for security events

## Security Configuration Details

The security is configured in `SecurityConfig.java` with:

- **Password Encoding:** BCrypt
- **Session Management:** Single session per user
- **Remember Me:** 30-day token validity
- **Authentication Entry Point:** Custom handler for AJAX vs regular requests
- **Static Resources:** Properly excluded from authentication

## Troubleshooting Commands

```bash
# Check if application is running
curl -I http://localhost:8080/

# Test public endpoint
curl -I http://localhost:8080/login

# Test protected endpoint (should return 302 redirect)
curl -I http://localhost:8080/dashboard

# Test with authentication
curl -c cookies.txt -d "username=test@example.com&password=password" http://localhost:8080/login
curl -b cookies.txt -I http://localhost:8080/dashboard
```

## Important Notes

- The security test controller and endpoints are **FOR DEVELOPMENT ONLY**
- Remove `SecurityTestController.java` and related templates before production
- H2 console access should be disabled in production
- Consider enabling CSRF protection for production use
- Monitor authentication logs for security events