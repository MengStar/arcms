#!/usr/bin/env bash
#docker run  --network arcms -p 8080:8080 sync
#
#docker run  --network arcms -p 8090:8090 admin
#
#docker run  --network arcms -p 8070:8070 portal

docker network create -d overlay --subnet=192.168.0.0/24 --attachable arcms_arcms
docker stack deploy -c deploy.yml arcms
