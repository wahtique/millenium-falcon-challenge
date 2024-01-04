package frontendd

import cats.effect.IO
import java.util.Base64
import scala.scalajs.js.annotation.*
import tyrian.*
import tyrian.Html.*
import tyrian.cmds.*
import tyrian.http.*

type Model = String

enum Msg:
  case NoOp
  case InputStolenData
  case GiveMeTheOdds
  case Odds(oddsOrError: String)
  case Read(contents: String)

@JSExportTopLevel("TyrianApp")
object Main extends TyrianApp[Msg, Model]:

  def router: Location => Msg = Routing.none(Msg.NoOp)

  def init(flags: Map[String, String]): (Model, Cmd[IO, Msg]) = ("", Cmd.None)

  def update(model: Model): Msg => (Model, Cmd[IO, Msg]) =
    case Msg.NoOp => (model, Cmd.None)
    case Msg.InputStolenData =>
      val readFile: Cmd[IO, Msg] = FileReader.readText("stolenImperialData"):
        case FileReader.Result.Error(msg)                 => Msg.NoOp
        case FileReader.Result.File(name, path, contents) => Msg.Read(contents)
      (model, readFile)
    case Msg.Read(contents) =>
      /*
       * Read content will look like :
        ```
        data:application/json;base64,ewogICJjb3VudGRvd24iOiAxMCwKICAiYm91bnR5X2h1bnRlcnMiOiBbCiAgICB7CiAgICAgICJwbGFuZXQiOiAiSG90aCIsCiAgICAgICJkYXkiOiA2CiAgICB9LAogICAgewogICAgICAicGxhbmV0IjogIkhvdGgiLAogICAgICAiZGF5IjogNwogICAgfSwKICAgIHsKICAgICAgInBsYW5ldCI6ICJIb3RoIiwKICAgICAgImRheSI6IDgKICAgIH0KICBdCn0=
        ```
       */
      val base64only        = contents.split(",").last
      val json              = new String(Base64.getDecoder.decode(base64only))
      val log: Cmd[IO, Msg] = Logger.info(s"update model with $json")
      (json, log)
    case Msg.GiveMeTheOdds =>
      val postSimulationParameters: Cmd[IO, Msg.Odds] = Http.send(
        Request(
          method = Method.Post,
          url = "http://localhost:8080/givemetheodds", // todo use a configuration for base url
          body = Body.json(model)
        ),
        Decoder.asString(Msg.Odds.apply)
      )
      (model, postSimulationParameters) // todo do stuff here
    case Msg.Odds(oddsOrError) =>
      val displayResult = Cmd.SideEffect[IO](org.scalajs.dom.window.alert(oddsOrError))
      (model, displayResult)

  def view(model: Model): Html[Msg] =
    div(`class` := "container d-flex flex-column align-items-center justify-content-center")(
      div(`class` := "row")(
        div(`class` := "col-12")(
          h1("C3P0")
        )
      ),
      div(`class` := "row")(
        div(`class` := "bs-component")(
          div(`class` := "col-12")(
            div(`class` := "form-group")(
              input(
                id      := "stolenImperialData",
                `type`  := "file",
                `class` := "form-control",
                onInput(_ => Msg.InputStolenData)
              )
            )
          )
        )
      ),
      div(`class` := "row")(
        div(`class` := "col-12")(
          button(
            `class` := "btn btn-primary text-dark mr-2 mb-2",
            onClick(Msg.GiveMeTheOdds)
          )(text("Give me the odds"))
        )
      )
    )

  def subscriptions(model: Model): Sub[IO, Msg] =
    Sub.None
