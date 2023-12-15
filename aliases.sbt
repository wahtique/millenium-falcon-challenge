import MyUtil._

addCommandAlias("l", "projects")
addCommandAlias("ll", "projects")
addCommandAlias("ls", "projects")
addCommandAlias("cd", "project")
addCommandAlias("root", "cd millenium-falcon-challenge")
addCommandAlias("c", "compile")
addCommandAlias("ca", "Test / compile")
addCommandAlias("t", "test")
addCommandAlias("r", "reload")
addCommandAlias("star", "thankYouStars")
addCommandAlias(
  "check",
  "scalafmtSbtCheck; scalafmtCheckAll; Test / compile; scalafixAll --check"
)
addCommandAlias(
  "fix",
  "Test / compile; scalafixAll; scalafmtSbt; scalafmtAll"
)
addCommandAlias(
  "fmt",
  "scalafmtSbt; scalafmtAll"
)
addCommandAlias(
  "up2date",
  "reload plugins; dependencyUpdates; reload return; dependencyUpdates"
)

onLoadMessage +=
  s"""|
      |╭─────────────────────────────────╮
      |│     List of defined ${styled("aliases")}     │
      |├─────────────┬───────────────────┤
      |│ ${styled("l")} | ${styled("ll")} | ${styled("ls")} │ projects          │
      |│ ${styled("cd")}          │ project           │
      |│ ${styled("root")}        │ cd root           │
      |│ ${styled("c")}           │ compile           │
      |│ ${styled("ca")}          │ compile all       │
      |│ ${styled("t")}           │ test              │
      |│ ${styled("r")}           │ reload sbt        │
      |│ ${styled("star")}        │ thankYouStars     │
      |│ ${styled("check")}       │ fmt & fix check   │
      |│ ${styled("fix")}         │ fix then fmt      │
      |│ ${styled("fmt")}         │ fmt               │
      |│ ${styled("up2date")}     │ dependencyUpdates │
      |╰─────────────┴───────────────────╯""".stripMargin
