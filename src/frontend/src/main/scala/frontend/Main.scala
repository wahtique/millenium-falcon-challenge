package frontendd

import cats.effect.IO
import scala.scalajs.js.annotation.*
import tyrian.*
import tyrian.Html.*
import tyrian.cmds.*
import tyrian.http.*

type Model = String

enum Msg:
  case NoOp
  case InputStolenData(data: String)
  case GiveMeTheOdds
  case Odds(oddsOrError: String)

@JSExportTopLevel("TyrianApp")
object Main extends TyrianApp[Msg, Model]:

  def router: Location => Msg = Routing.none(Msg.NoOp)

  def init(flags: Map[String, String]): (Model, Cmd[IO, Msg]) = ("", Cmd.None)

  def update(model: Model): Msg => (Model, Cmd[IO, Msg]) =
    case Msg.NoOp => (model, Cmd.None)
    case Msg.InputStolenData(data) =>
      val log: Cmd[IO, Msg] = Logger.info(s"InputStolenData: $data")
      (data, log)
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
        div(`class` := "col-12")(
          textarea(
            id      := "stolenImperialData",
            rows    := 20,
            cols    := 50,
            `class` := "form-control",
            onInput(s => Msg.InputStolenData(s))
          )()
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
