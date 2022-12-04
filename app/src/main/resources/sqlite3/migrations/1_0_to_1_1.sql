ALTER TABLE users ADD COLUMN isAdmin boolean NOT NULL DEFAULT false;

-- One time grant all existing users admin status.
-- NOTE: this is for deployments upgrading from < 1.2 database version
-- where isAdmin does not exist. Admins managing the upgrade will need to
-- correctly manage the chosen admins on the deployment.
UPDATE users SET isAdmin = true;

-- Finally, update the system version.
PRAGMA user_version=110;
