default:
  @just -choose

DEFAULT_MFJSON := 'src/cli/src/test/resources/examples/example4/millennium-falcon.json'
DEFAULT_EMPIREJSON := 'src/cli/src/test/resources/examples/example4/empire.json'

# example : cli src/cli/src/test/resources/examples/example4/millennium-falcon.json src/cli/src/test/resources/examples/example4/empire.json
cli mfjson=DEFAULT_MFJSON empirejson=DEFAULT_EMPIREJSON:
  sbt "cli/run -- {{mfjson}} {{empirejson}}"


alias t := test
test:
  sbt test


