-- Input: key (token key), field (field name), value (revoke value)
-- Return:
--   1 = Token valid and successfully revoked
--   0 = Token already revoked (possible replay attack)
--  -1 = Token doesn't exist (expired)

local token_key = KEYS[1]
local field_name = ARGV[1]
local revoke_value = ARGV[2]

-- Check if token exists
if redis.call("EXISTS", token_key) == 0 then
    return -1
end

-- Check if token already revoked (field exists and set)
local is_revoked = redis.call("HGET", token_key, field_name)
if is_revoked == revoke_value then
    return 0  -- Already revoked 
end

-- Revoke token atomically
redis.call("HSET", token_key, field_name, revoke_value)
return 1  