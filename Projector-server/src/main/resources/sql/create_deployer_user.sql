-- Create a deployer account for desktop release uploads (ROLE_DEPLOYER = 3).
--
-- Default password in this script: ChangeMeDeploy123!
-- Generate a new BCrypt hash (Spring BCrypt, cost 10), for example:
--   python -c "import bcrypt; print(bcrypt.hashpw(b'YOUR_PASSWORD', bcrypt.gensalt(rounds=10)).decode())"
--
-- Role values in the user table:
--   0 = USER
--   1 = ADMIN
--   2 = REVIEWER
--   3 = DEPLOYER

INSERT INTO `user` (uuid,
                    email,
                    password,
                    role,
                    preferred_language,
                    activated,
                    had_uploaded_songs,
                    deleted,
                    created_date,
                    modified_date)
SELECT UUID(),
       'deployer@example.com',
       'password',
       3,
       'en',
       1,
       0,
       0,
       NOW(),
       NOW() WHERE NOT EXISTS (
    SELECT 1 FROM `user` WHERE email = 'deployer@example.com'
);

-- Optional: promote an existing account instead of creating a new one.
-- UPDATE `user`
-- SET role = 3,
--     activated = 1,
--     modified_date = NOW()
-- WHERE email = 'your-existing@email.com';
