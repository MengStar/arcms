#!/usr/bin/env bash
#docker run  --network arcms -p 8080:8080 sync
#
#docker run  --network arcms -p 8090:8090 admin
#
#docker run  --network arcms -p 8070:8070 portal

docker stack deploy -c deploy.yml arcms
