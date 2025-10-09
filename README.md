# ♕ BYU CS 240 Chess

This project demonstrates mastery of proper software design, client/server architecture, networking using HTTP and WebSocket, database persistence, unit testing, serialization, and security.

## 10k Architecture Overview

The application implements a multiplayer chess server and a command line chess client.

[![Sequence Diagram](10k-architecture.png)](https://sequencediagram.org/index.html#initialData=C4S2BsFMAIGEAtIGckCh0AcCGAnUBjEbAO2DnBElIEZVs8RCSzYKrgAmO3AorU6AGVIOAG4jUAEyzAsAIyxIYAERnzFkdKgrFIuaKlaUa0ALQA+ISPE4AXNABWAexDFoAcywBbTcLEizS1VZBSVbbVc9HGgnADNYiN19QzZSDkCrfztHFzdPH1Q-Gwzg9TDEqJj4iuSjdmoMopF7LywAaxgvJ3FC6wCLaFLQyHCdSriEseSm6NMBurT7AFcMaWAYOSdcSRTjTka+7NaO6C6emZK1YdHI-Qma6N6ss3nU4Gpl1ZkNrZwdhfeByy9hwyBA7mIT2KAyGGhuSWi9wuc0sAI49nyMG6ElQQA)

## Modules

The application has three modules.

- **Client**: The command line program used to play a game of chess over the network.
- **Server**: The command line program that listens for network requests from the client and manages users and games.
- **Shared**: Code that is used by both the client and the server. This includes the rules of chess and tracking the state of a game.

## Starter Code

As you create your chess application you will move through specific phases of development. This starts with implementing the moves of chess and finishes with sending game moves over the network between your client and server. You will start each phase by copying course provided [starter-code](starter-code/) for that phase into the source code of the project. Do not copy a phases' starter code before you are ready to begin work on that phase.

## IntelliJ Support

Open the project directory in IntelliJ in order to develop, run, and debug your code using an IDE.

## Maven Support

You can use the following commands to build, test, package, and run your code.

| Command                    | Description                                     |
| -------------------------- | ----------------------------------------------- |
| `mvn compile`              | Builds the code                                 |
| `mvn package`              | Run the tests and build an Uber jar file        |
| `mvn package -DskipTests`  | Build an Uber jar file                          |
| `mvn install`              | Installs the packages into the local repository |
| `mvn test`                 | Run all the tests                               |
| `mvn -pl shared test`      | Run all the shared tests                        |
| `mvn -pl client exec:java` | Build and run the client `Main`                 |
| `mvn -pl server exec:java` | Build and run the server `Main`                 |

These commands are configured by the `pom.xml` (Project Object Model) files. There is a POM file in the root of the project, and one in each of the modules. The root POM defines any global dependencies and references the module POM files.

## Running the program using Java

Once you have compiled your project into an uber jar, you can execute it with the following command.

```sh
java -jar client/target/client-jar-with-dependencies.jar

♕ 240 Chess Client: chess.ChessPiece@7852e922
```

Phase 2 design draft
https://sequencediagram.org/index.html#initialData=IYYwLg9gTgBAwgGwJYFMB2YBQAHYUxIhK4YwDKKUAbpTngUSWDABLBoAmCtu+hx7ZhWqEUdPo0EwAIsDDAAgiBAoAzqswc5wAEbBVKGBx2ZM6MFACeq3ETQBzGAAYAdAE5M9qBACu2AMQALADMABwATG4gMP7I9gAWYDoIPoYASij2SKoWckgQaJiIqKQAtAB85JQ0UABcMADaAAoA8mQAKgC6MAD0PgZQADpoAN4ARP2UaMAAtihjtWMwYwA0y7jqAO7QHAtLq8soM8BICHvLAL6YwjUwFazsXJT145NQ03PnB2MbqttQu0WyzWYyOJzOQLGVzYnG4sHuN1E9SgmWyYEoAAoMlkcpQMgBHVI5ACU12qojulVk8iUKnU9XsKDAAFUBhi3h8UKTqYplGpVJSjDpagAxJCcGCsyg8mA6SwwDmzMQ6FHAADWkoGME2SDA8QVA05MGACFVHHlKAAHmiNDzafy7gjySp6lKoDyySIVI7KjdnjAFKaUMBze11egAKKWlTYAgFT23Ur3YrmeqBJzBYbjObqYCMhbLCNQbx1A1TJXGoMh+XyNXoKFmTiYO189Q+qpelD1NA+BAIBMUp2d+q-f4cNiqfUAXhg8X08Qxo52pMR3vuLbpqlqIFV6LdGJGirmayXAIn8Qu3O09rb92MtQUHA4mul2gHa6p19bW53wfRCh8PUMWAQD4h5K8aW-QV70fZ8AL1D1V0MJNKhhJ5S2xNE8TUXssDQuF2z9UtXkNJUlnqfYQRAvV2ggOs0HI5ZLgTSh2xTDB6nCJwnCzCZSM+GAKOBZZqPiWj6MY-YrnQDhTC8XwAmgdhGRiEU4AjaQ4AUGAABkICyQp2OYIcanqZo2i6XoDHUfI0CzI8UBPfQ-h2K4iMFfD-RI8tPkhEFT0BKTME8+FfWdLsYAQfTxQxPSDIJIkwBXcLBQ3fkGSZfcHIg3lN2g4UxQlN0ZTlMt3iVTAeh6JAADMyqNbIYBq3wmxNZg3ialrZLS28TKRF93TfJDBSQkdnLHc8YBnOdJ0Xcbl2bL88r6l0Bp5Oath2EVvBmaQACFks7EbwvqEAIBmXgUHPDaXLPecnM2gFtvO-bSSq2qYACmBjjAEB4jUTA2tnedvrkP6AaQioeoFU7dxQeCFzecDFsgzcKmMQSAyfANQMQlKUIeWF-Ti8UMlUXDgseOECfcl4+J8+ZMcokTQPE+smeY9yCaMzGuJ40Z6fKgShO+US2YYjmGxQBADGBydvuyH6-vfZCTNY+oADkIAAWTB+IoxjONCi55NkHMTH02CGAAEk0CoE0kGfX8OHMJATQ0GSzBlwwOrQCBmGanwm2hwiTpgHs+1MYaaeqdWYC13Xfv16MUFjWyWPhU2SmYNMMxtu2HadlEXYwN2ZcbWTPc8bw-H8LwUHQGI4kSevG7i3wsB5tXSwaaQIx0iN2gjboems1RbOGMW6PQCu5JrgJi5ibBxQ1HS0RgABxJUNC7sLqH9BoN8Hkf7CVSfWenwoq-k2uOAAdjcJwUCcGII2COB1IANngOHN4rIozZlD3qZRorQOgnzPtMC+9EsynzmBrJU0kmzVwUv4Sw0soqbCbkgBIYB0F9ggFggAUhAcUf85gxGSKANUADs7HX3j3JozJLI9DgSgc+NFL5ZmwAgYA6CoBwAgFFKAaw2HW2kEg2SKDa5eD4dg3Bsj5SIGDLAYA2AeGEDyAUGAtDzbdzMn3AeQ8R7GFnkAA
