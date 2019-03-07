INSERT INTO Subject (AppId, Name, OwnerName, OwnerEmail) VALUES ('someAppId','someAppName','someOwnerName','someOwnerName@ctrip.com');

INSERT INTO Cluster (AppId, Name) VALUES ('someAppId', 'default');

INSERT INTO SubjectParty (AppId, Name) VALUES ('someAppId', 'application');

INSERT INTO Party (AppId, ClusterName, NamespaceName) VALUES ('someAppId', 'default', 'application');
