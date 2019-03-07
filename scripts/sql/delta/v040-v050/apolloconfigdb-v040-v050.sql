# delta schema to upgrade apollo config db from v0.4.0 to v0.5.0

Use ApolloConfigDB;

DROP TABLE `Privilege`;
ALTER TABLE Publish DROP `Status`;
ALTER TABLE Party ADD KEY `IX_NamespaceName` (`NamespaceName`(191));
ALTER TABLE `Cluster` ADD KEY `IX_ParentClusterId` (`ParentClusterId`);
ALTER TABLE SubjectParty ADD KEY `IX_AppId` (`AppId`);
ALTER TABLE Subject DROP INDEX `Name`;
ALTER TABLE Subject ADD KEY `Name` (`Name`);