#!/bin/bash

# Setup locale.
sed -i -e 's/# en_US.UTF-8 UTF-8/en_US.UTF-8 UTF-8/' /etc/locale.gen
locale-gen

# Drop the default cluster that is installed with postgres.
pg_dropcluster --stop 13 main

# Add a user to run the application as.
useradd $POSTGRES_USER
chpasswd <<<"$POSTGRES_USER:$POSTGRES_PASSWORD"
usermod -a -G postgres $POSTGRES_USER

sudo -E -u $POSTGRES_USER mkdir -p $PGDATA
sudo -E chown -R $POSTGRES_USER $PGDATA


# Create a cluster for the rook system.
sudo -E -u $POSTGRES_USER /usr/lib/postgresql/13/bin/initdb -D $PGDATA

echo
echo "starting postgres..."
echo

sudo -E -u $POSTGRES_USER /usr/lib/postgresql/13/bin/pg_ctl -D $PGDATA -o "-p 5432" start

echo
echo "postgres service started."
echo

echo
echo "ensuring database exists"
echo

sudo -E -u $POSTGRES_USER createdb rook
sudo -E -u $POSTGRES_USER psql -d rook -f ./user_init.sql

# Environment is ready, run the rook application server
echo "starting rook application server..."
sudo -E -u $POSTGRES_USER java -jar rook_deploy.jar $ROOK_ARGS &

# Define cleanup procedure
cleanup() {
  echo "SIGTERM: cleaning up..."
  sudo -E -u $POSTGRES_USER /usr/lib/postgresql/13/bin/pg_ctl -D $PGDATA stop
  echo "done."
}

trap 'cleanup' SIGTERM
wait $!
