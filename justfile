default:
  @just --choose

DEFAULT_MFJSON := 'src/cli/src/test/resources/examples/example4/millennium-falcon.json'
DEFAULT_EMPIREJSON := 'src/cli/src/test/resources/examples/example4/empire.json'

# run the cli thru sbt
cli mfjson=DEFAULT_MFJSON empirejson=DEFAULT_EMPIREJSON:
  sbt "cli/run -- {{mfjson}} {{empirejson}}"

# run backend thru sbt => swagger at localhost:8080/docs
backend mfjson=DEFAULT_MFJSON:
  sbt "backend/run {{mfjson}}"

# compile and run the frontend
frontend:
  sbt "frontend/fastLinkJS"
  yarn --cwd src/frontend install
  yarn --cwd src/frontend start

# watch and compile frontend to JS
dev-frontend:
  sbt "~frontend/fastLinkJS"


alias t := test

# test
test:
  sbt test

# BETTER FOR REVIEW #
# AVOID SBT startup
# First build the JARs
# then run them

alias b := build

# build all projects
build:
  sbt "cli/assembly; backend/assembly; frontend/fastLinkJS"

# run the pre-built cli
r2d2 mfjson=DEFAULT_MFJSON empirejson=DEFAULT_EMPIREJSON:
  java -jar src/cli/target/scala-3.3.1/r2d2.jar -- {{mfjson}} {{empirejson}}

# run the pre-built backend
navicore mfjson=DEFAULT_MFJSON:
  java -jar src/backend/target/scala-3.3.1/navicore.jar {{mfjson}}

# run the pre-built frontend
c3p0:
  yarn --cwd src/frontend install
  yarn --cwd src/frontend start
