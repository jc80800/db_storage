# db_storage

# How to run the program
- cd inside src folder
- javac main/Main.java
- java main/Main \<db loc> \<page size> \<buffer size> \<indexOn>

# Database Directory

- If the directory does not exist, we will create a new directory and use that path
- If the directory exist, return that directory to work with

# Commands

- select * from \<table name>;
- display schema;
- display info \<table name>;
- insert into \<table name> values (list of values[comma seperated]);
- create table \<table name> (\<attribute name> \<attribute type> ?\<isPrimary>)
- drop table \<table name> 
- select * from \<table name> where \<conditions> orderBy \<attribute>
- select * from \<table name>, \<table name2>, ... where\<conditions> orderBy \<tableName.attribute>