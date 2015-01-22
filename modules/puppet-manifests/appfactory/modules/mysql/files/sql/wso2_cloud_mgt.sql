
create table SUBSCRIPTIONS (
   tenantDomain varchar(120) NOT NULL,
   app_cloud      tinyint(1),
   integration_cloud tinyint(1),
   api_cloud tinyint(1)
);