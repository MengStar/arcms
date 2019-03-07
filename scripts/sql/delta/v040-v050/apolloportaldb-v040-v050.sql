# delta schema to upgrade apollo portal db from v0.4.0 to v0.5.0

Use ApolloPortalDB;

ALTER TABLE SubjectParty ADD KEY `IX_AppId` (`AppId`);
ALTER TABLE Subject DROP INDEX `Name`;
ALTER TABLE Subject ADD KEY `Name` (`Name`);