CREATE DATABASE TestCustomerDB;
USE TestCustomerDB;
CREATE TABLE IF NOT EXISTS Customer (Name VARCHAR (20), Category VARCHAR (20), Region VARCHAR (20));
INSERT INTO Customer(Name, Category, Region) VALUES ('Bar Bank','Banking', 'Asia');
INSERT INTO Customer(Name, Category, Region) VALUES ('Doo International','Travel', 'Europe');
INSERT INTO Customer(Name, Category, Region) VALUES ('Foo','Retail', 'USA');
