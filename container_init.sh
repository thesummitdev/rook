#!/bin/bash

# Setup locale.
sed -i -e 's/# en_US.UTF-8 UTF-8/en_US.UTF-8 UTF-8/' /etc/locale.gen
locale-gen

# Add a user to run the application as.
useradd $ROOK_USER
chpasswd <<<"$ROOK_USER:$ROOK_PASSWORD"

sudo mkdir -p $DATA
sudo -E chown -R $ROOK_USER $DATA

# Remove any trailing slashes on the DATA path.
TRIMMED=$(echo "$DATA" | sed 's:/*$::')
DATABASE_FILE="${TRIMMED}/rook.db"

if [ -f "$DATABASE_FILE" ]; then
  # Application will connect to the existing database.
  echo "Database file already exists."
else
  # Create an empty database file.
  sqlite3 $DATABASE_FILE
fi

# Environment is ready, run the rook application server
echo "starting rook application server..."
sudo -E -u $ROOK_USER java -jar rook_deploy.jar $ROOK_ARGS &

# Define cleanup procedure
cleanup() {
  echo "SIGTERM: cleaning up..."
  # anything that needs to be done when the container stops should go here.
  echo "done."
}

trap 'cleanup' SIGTERM
wait $!
