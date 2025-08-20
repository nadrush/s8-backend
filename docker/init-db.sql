-- Database initialization script
CREATE DATABASE IF NOT EXISTS ebanking;

-- Create user if not exists
DO
$do$
BEGIN
   IF NOT EXISTS (
      SELECT FROM pg_catalog.pg_roles
      WHERE  rolname = 'ebanking_user') THEN

      CREATE ROLE ebanking_user LOGIN PASSWORD 'ebanking_password';
   END IF;
END
$do$;

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE ebanking TO ebanking_user;
