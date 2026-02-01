-- Input: KEYS[1] = token key
-- Return:
--   mssv = Token valid, return mssv
--   nil = Token not found, expired, or revoked

local token_key = KEYS[1]

-- Check if token exists
if redis.call("EXISTS", token_key) == 0 then
    return nil
end

-- Check if token is revoked
local is_revoked = redis.call("HGET", token_key, "isRevoked")
if is_revoked == "1" then
    return nil
end

-- Return mssv
return redis.call("HGET", token_key, "mssv")
