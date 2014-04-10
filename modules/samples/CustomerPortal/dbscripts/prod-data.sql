CREATE DATABASE CusotmerDB;
USE CusotmerDB;
CREATE TABLE IF NOT EXISTS Customer (Name VARCHAR (20), Category VARCHAR (20), Region VARCHAR (20));
INSERT INTO Customer(Name, Category, Region) VALUES ('Bank of Ceylon','Banking', 'Asia');
INSERT INTO Customer(Name, Category, Region) VALUES ('Sun Travels','Travel', 'Europe');
INSERT INTO Customer(Name, Category, Region) VALUES ('My Home','Retail', 'USA');
INSERT INTO Customer(Name, Category, Region) VALUES ('Herbal Spa','Health', 'USA');
INSERT INTO Customer(Name, Category, Region) VALUES ('Kids Unlimited','Retail', 'USA');
INSERT INTO Customer(Name, Category, Region) VALUES ('Cologne Mart','Retail', 'Midle East');
