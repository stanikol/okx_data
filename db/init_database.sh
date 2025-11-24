#! /usr/bin/env bash
sudo docker exec -i db-db-1 psql postgres postgres <<-SQL
  create user okx with password 'example';
  create database okx owner okx;
  grant all privileges on database okx to okx;
  # grant all privileges on schema public to okx;
  grant create on schema public to okx;
  \\dt
SQL
