#!/bin/bash

# setup locale
# TODO get locale correctly set for postgres.
# sed -i -e 's/# en_US.UTF-8 UTF-8/en_US.UTF-8 UTF-8/' /etc/locale.gen
# locale-gen
#
# localedef -i en_US -c -f UTF-8 -A /usr/share/locale/locale.alias en_US.UTF-8
# LANG="en_US.utf8"

# Add a user to run the application as.
useradd $POSTGRES_USER
chpasswd <<<"$POSTGRES_USER:$POSTGRES_PASSWORD"
usermod -a -G postgres $POSTGRES_USER

if [[ -v RESET_DATABASE ]]; then
  echo "reset & drop database cluster was requested."
  pg_dropcluster --stop 13 flink_system
  echo "database reset complete."
fi

# Create a cluster for the flink system.
pg_createcluster -u $POSTGRES_USER -d $PGDATA -p 5432 13 flink_system

echo
echo "starting postgres..."
echo

# Initialize postgres cluster.
pg_ctlcluster start 13 flink_system
pg_lsclusters
echo
echo "postgres service started."
echo

# Setup database
echo
echo "beginning database setup..."
echo


echo
echo "creating database..."
echo

sudo -u $POSTGRES_USER createdb flink

echo
echo "updating postgres user..."
echo

sudo -u $POSTGRES_USER psql -d flink -f ./user_init.sql

echo
echo "applying flink schema to database..."
echo

sudo -u $POSTGRES_USER psql -d flink -f ./schema_init.sql

if [[ -v FLINK_ENABLE_TEST_DATA ]]; then
  echo "Test data was requested."
  sudo -u $POSTGRES_USER psql -d flink -f ./database_test_data.sql
fi

sudo -u $POSTGRES_USER psql -d flink -f ./populate.sql

echo
echo "database setup completed."
echo

# Environment is ready, run the flink application server
echo "starting flink application server..."
sudo -u flink_system java -jar flink_deploy.jar
