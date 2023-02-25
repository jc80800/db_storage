# db_storage

# How to run the program
- inside src folder
- javac main/Main.java
- java main/Main \<db loc> \<page size> \<buffer size>

# Starting Functionality

- java src.main <db loc> <page size> <buffer size>
- May need to compile beforehand if using terminal

# Database Directory

- If the directory does not exist, we will create a new directory and use that path
- If the directory exist, return that directory to work with

# Commands

- select * from \<table name>;
- display schema;
- display info \<table name>;
- insert into \<table name> values (list of values[comma seperated]);
- create table \<table name> (\<attribute name> \<attribute type> ?\<isPrimary>)