# Millenium Falson Challenge

See [Millenium Falcon Challenge](https://github.com/dataiku/millenium-falcon-challenge) challenge description.

## Design considerations

### Stack

To rise up to the challenge, I decided to use a combination of language and libraris I was either familiar with ... or simply wanted to try out :

- language : Scala 3 ( I did refrain from using experimental features even though I think we would both enjoy it )
- effect system : [cats-effect](https://typelevel.org/cats-effect/)
- http : [tapir](https://tapir.softwaremill.com) + [http4s](https://http4s.org/)
- cli : [decline](https://ben.kirw.in/decline/)
- other notable libraries :
  - [cats](https://typelevel.org/cats/) : functional programming toolkit
  - [circe](https://circe.github.io/circe/) : json
  - [iron](https://iltotore.github.io/iron/docs/modules/decline.html) : for even stricter type safety

### Structure

This project is structured as a multi-module sbt project. It contains the following modules:

- `core` : core logic and domain model of the applications
  - `model` : domain model
  - `io` : data access capabilities
  - `computer` : puzzle solving logic = the navigation computer of the Millennium Falcon
- `cli` : aka R2D2, a CLI app which provides arg parsing and depends on `core` for the actual logic
- `backend` : a REST API which depends on `core` for the actual logic
  - it also exposes the frontend, aka C3PO, as a SwaggerUI ... which I would argue is, in fact, a frontend SPA

### Core

The challenge require to have both a frontend, backend server and CLI app all sharing the same logic. To achieve this, I decided to extract the core logic in a separate module in which the other modules can depend on.

The domain model is fairly simple and defineds almost as it is described in the challenge.

Loading the data from the various input formats was somewhat straightforward, even though some of the choices might be debatable. The biggest assumption I made was that the Galaxy far far away could be represented in-memory without making my machine OOM. I did get the confirmation that this input would always be in the order of a few hundreds planet at most which should fit witout issue in memory. More, and I would have actuall used the database as a database and not only as a serilization format. This assumption also allowed me some leeway in implementing the pathfinding logic : since the Galaxy is relatively small, I can feed it all at once to the pathfinding algorithm.

The pathfinding algorithm should be considered a variant of the A\* algorithm ( which one if it's a named one I have no clue ). Implementing a straight A\* research is tricky as the heuristic function is not trivial to define. On the other hand, a simpler breadth-first or depth-first traversal would be both a bit boring and messier because of the bounty hunters and fuel mechanic. I decided to take a sort of middle ground with a A* like search which tries to minimize the number of encounters with the bounty hunters, while droping some of the optmizations like allowing a node to be present multiple times with different cost in the exploration queue. The results looked fine on the examples provided in the challenge description.

This approach should most likely reasonnably handle any Galaxy size, even though efficiently feeding it a bigger Galaxy would end up impacting its overall performance.

### CLI

Not much to say on this beside that it was very useful for testing purposes :)

### Backend

The backend itself is a REST API with only one `POST` endpoint taking the stolen imperial plans as input and returning the odds of success as output. The mission parameters such as the autonomy of the Millennium Falcon are read only once at startup.

A few things to note :

- the `api` package could be extracted in a separate module, published as an independant JAR and consumed by an eventual Java or ScalaJS frontend. As this would also requires to define DAOs distinct from the domain model, I decided to keep it simple and keep everything in the same module
- the app is defined as a  `cats-effect` `ResoureApp` which in this case is almost pure esthetics. However, since the specs specified this should be something prod ready of which I could be proud of, I decided to overengineer a bit on this side.
- same goes for defining everything as both a `trait` and a resource factory materilized by a `make` method in companion objects. This would usually allow for defining test implementations in order to unit test each leayer ... which I did not do here as the actual logic is ( relatively ) sufficiently `core` tested

The backend also exposes a Swagger UI which is technicall a frontend Single Page Application which allow to both send teh input JSON and display the output success odds. Since my own implementation of a frontend would have been both very similar and very ugly, I hope we can say the Swagger UI shall suffice :)

### Todo-list

Some things I would usually deem necessary for a production ready app but did not do here :

- proper CI in Github action : no PR => it would not be used anyway.
- proper container packaging for the backend
- use either GraalVM or Scala Native to compile the CLI to native executable : running from sbt console should be fine for now, but the startup time is a bit of a pain

## Usage

### Requirements

- build tool [sbt](https://www.scala-sbt.org/) : needed to actually build and run the project
- task runner [just](https://just.systems/) : some examples in this doc might use it, but it's not mandatory. Please refer to the [justfile](justfile) for the actual commands.

All the commands below can also be run from the sbt console ( copy the one in the recipe ).

### Run the cli

```bash
just cli {{ path to millenium-falcon.json }} {{ path to empire.json }}
```

### Run the backend

```bash
just backend {{ path to millenium-falcon.json }}
```

Swagger UI is available at <http://localhost:8080/docs>
