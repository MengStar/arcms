#!/usr/bin/env bash
cd admin/
docker build -t admin .
cd ../config/
docker build -t sync .
cd ../portal/
docker build -t portal .

