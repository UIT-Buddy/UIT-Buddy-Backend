-- Input: KEYS[1] = pattern (e.g., "signup_otp:*")
--        ARGV[1] = mssv to revoke
-- Return: number of revoked OTPs

local pattern = KEYS[1]
local mssv = ARGV[1]
local count = 0

local cursor = "0"
repeat
    local result = redis.call("SCAN", cursor, "MATCH", pattern, "COUNT", 100)
    cursor = result[1]
    local keys = result[2]
    
    for _, key in ipairs(keys) do
        -- Skip index keys
        if not string.match(key, ":idx") and not string.match(key, ":mssv:") then
            local stored_mssv = redis.call("HGET", key, "mssv")
            local is_revoked = redis.call("HGET", key, "isRevoked")
            
            if stored_mssv == mssv and is_revoked == "0" then
                redis.call("HSET", key, "isRevoked", "1")
                count = count + 1
            end
        end
    end
until cursor == "0"

return count