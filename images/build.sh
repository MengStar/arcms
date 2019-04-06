#!/usr/bin/env bash
cd admin/
docker build -t adminv1 .
cd ../config/
docker build -t syncv1 .
cd ../portal/
docker build -t portalv1 .

