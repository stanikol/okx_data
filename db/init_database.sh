#! /usr/bin/env zsh
sudo docker exec -i db-db-1 psql postgres postgres <<-SQL
  create user okx;
  create database okx;
  grant all privileges on database okx to okx;
  grant all privileges on schema public to okx;
  \\dt
SQL