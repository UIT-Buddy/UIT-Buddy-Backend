## Sign in / Sign up flow

### Sign in

Input mssv, return accesstoken, userinfo in response and refresh token in header

### Sign up initiate

Input mssv, send sign-up-otp to mail, otp is stored redis with timeout 5minutes

### Sign up complete

Input mssv, otp, password và confirmed password, return accesstoken, userinfo in response and refresh token in header

### Refresh token

Require refresh token in header, return new access token

### Sign out

Delete refresh token in redis, accesstoken still works till the ttl timeout

### Resend otp

Check if the user has registered, if not system will send otp (3 minutes cooldown), maximum 5 times per input

### Forgot password

Input mssv, send forgot-password-otp to email, otp is stored redis with timeout 5minutes

### Reset password

Input mssv, reset-password-otp, password và confirmed password, return success/fail

### me

Get userinfo via accesstoken
