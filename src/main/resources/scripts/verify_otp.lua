-- Input: KEYS[1] = token key pattern (e.g., "signup_otp:*" or "password_reset_otp:*")
--        ARGV[1] = mssv to search
--        ARGV[2] = otp to verify
--        ARGV[3] = max attempts
-- Return:
--   1 = OTP valid and verified
--   0 = OTP invalid, attempts incremented
--  -1 = Token not found or already revoked
--  -2 = Max attempts exceeded

local pattern = KEYS[1]
local mssv = ARGV[1]
local otp_input = ARGV[2]
local max_attempts = tonumber(ARGV[3])

-- Find token by mssv index
local cursor = "0"
local found_key = nil

repeat
    local result = redis.call("SCAN", cursor, "MATCH", pattern, "COUNT", 100)
    cursor = result[1]
    local keys = result[2]
    
    for _, key in ipairs(keys) do
        local token_mssv = redis.call("HGET", key, "mssv")
        local is_revoked = redis.call("HGET", key, "isRevoked")
        
        if token_mssv == mssv and is_revoked == "0" then
            found_key = key
            break
        end
    end
until cursor == "0" or found_key

if not found_key then
    return -1
end

-- Check attempts
local attempts = tonumber(redis.call("HGET", found_key, "attempts") or "0")
if attempts >= max_attempts then
    redis.call("DEL", found_key)
    return -2
end

-- Verify OTP
local stored_otp = redis.call("HGET", found_key, "otp")
if stored_otp ~= otp_input then
    redis.call("HINCRBY", found_key, "attempts", 1)
    return 0
end

-- OTP valid - delete token
redis.call("DEL", found_key)
return 1
