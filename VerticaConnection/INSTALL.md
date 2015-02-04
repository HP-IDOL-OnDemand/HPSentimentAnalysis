INSTALL
=======

`gradle installApp`

COMPILE REQUIREMENTS
====================

VerticaConnection uses Vertica's jdbc driver to connect with the database, so you need to install the driver
in your local Maven repository.

`./build.gradle` includes a task to do it automatically for you (it runs every time).

If you want to skip this task because the driver is already installed, for instance during testing,
you can do so running `gradle test -x installJdbcLocally`

TEST REQUIREMENTS
=================

This system connects with a Vertica database, therefore you need to provide the connection details

`src/test/resources/vertica.properties.template` is a template file that you can use to facilitate
this during testing.

Rename (or make a copy) the file `vertica.properties` and write your connection details.

Also, the tests assume the connection with Vertica is possible and the database is populated with the sample files.