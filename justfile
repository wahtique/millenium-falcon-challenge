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

alias t := test

# test
test:
  sbt test


