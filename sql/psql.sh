#!/usr/bin/env bash
SQL=$(< $1)
echo "$SQL"
sudo docker exec -i db-db-1 psql --user okx --dbname okx <<- EOF
  $SQL
EOF
