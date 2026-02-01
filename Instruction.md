##Sign in / Sign up flow

**Sign in**
Nhập mssv, trả về accesstoken, userinfo trong response và refresh token ở header

**Sign up initiate**
Nhập mssv, gửi otp qua mail, otp được lưu vào redis với timeout 5p

**Sign up complete**
Nhập mssv, otp, mật khẩu và confirmed mật khẩu, trả về accesstoken, userinfo trong response và refresh token ở header

**Refresh token**
Đính refresh token ở header, trả về access token mới

**Sign out**
Xóa refresh token trong redis, accesstoken vẫn còn duy trì cho đến khi ttl

**Resend otp**
Check ng dùng đã đăng kí chưa, chưa thì gửi lại otp (thời gian 3p cooldown), tối đa 5 lần nhập cho 1 otp

**Forgot password**
Nhập mssv, gửi otp qua mail, otp được lưu vào redis với timeout 5p

**Reset password**
Nhập mssv, otp, mật khẩu và confirmed mật khẩu, trả về success/fail

**me**
Lấy thông tin user từ accesstoken
