#!/bin/bash

# Add a user to run the application as.
useradd flink_system

# Initialize postgres
echo "starting postgres"
service postgresql start

# Setup database
echo "starting database setup"
sudo -u postgres psql -f ./database_init.sql
sudo -u postgres psql -d flink -f ./schema_init.sql
sudo -u postgres psql -d flink -f ./database_test_data.sql

# Environment is ready, run the flink application server
echo "starting flink application server"
sudo -u flink_system java -jar flink_deploy.jar
