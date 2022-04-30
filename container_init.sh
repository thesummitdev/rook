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

# Create a cluster for the rook system.
pg_createcluster -u $POSTGRES_USER -d $PGDATA -p 5432 13 rook_system

echo
echo "starting postgres..."
echo

# Initialize postgres cluster.
pg_ctlcluster start 13 rook_system
pg_lsclusters


echo
echo "postgres service started."
echo

echo
echo "ensuring database exists"
echo

sudo -u $POSTGRES_USER createdb rook
sudo -u $POSTGRES_USER psql -d rook -f ./user_init.sql

# Environment is ready, run the rook application server
echo "starting rook application server..."
sudo -E -u rook_system java -jar rook_deploy.jar $ROOK_ARGS
