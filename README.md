http://appdev.openshift.io/docs/vertx-runtime.html#mission-crud-vertx


## Running the application locally

To run the application locally, you need to start a local PosgreSQL instance. For example, you can use Docker as follows:

```bash
docker run --name some-postgres -e POSTGRES_USER=user -e POSTGRES_PASSWORD=password -e POSTGRES_DB=my_data -p 5432:5432 -d postgres
```

By default (for local development) the application uses:

* `localhost` as host
* `5432` as port
* `user` as username
* `password` as password
* `my_data` as database name

Then run the application using: `mvn compile vertx:run`. The application runs on: http://localhost:8080.

