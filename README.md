# DBTool
DBTool Project for Database Technology [Lecture Summer 2014 - TU/e](http://wwwis.win.tue.nl/~gfletcher/2imw20-spring16/)
* Status: July 2014

## Git commands (Terminal)

* git clone <url>
* git checkout -b <unser branch-name>
//.gitignore erstellen und adden
// Projekt kopieren
* git add —all
* git commit -m „…“
* git push origin <branch-name>

## Postgres
* 1. Install prostgres database from [postgres](http://www.postgresql.org/)
* 2. After installation open pgAdmin tool.
* 3. Give your server a password.
* 4. Make a new database with a name.
* 5. Change your Postgres.properties file:
  * - username=postgres
  * - password=*your password*
  * - port=5432 (default)
  * - uri=jdbc:postgresql://localhost:*port*/*database name*
* 6. In your Java IDE add PostgreSQL JDBC Driver to your project.
* 7. Make a Database service in the IDE:
	* 1. Driver = PostgreSQL
	* 2. Host = localhost
	* 3. Port = 5432 (default)
	* 4. Database = *database name*
	* 5. UserName = postgres
	* 6. Password = *password*
	* 7. Test connection to make sure it works.
