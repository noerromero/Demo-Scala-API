package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import scala.concurrent.Future

// Importar el repository y el contexto de ejecución global
import models.MovieRepository
import scala.concurrent.ExecutionContext.Implicits.global

import play.api.libs.json.Json
import play.api.libs.json.JsValue
import models.{Movie, MovieRepository}


/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class MovieController @Inject()(cc: ControllerComponents, movieRepository: MovieRepository) extends AbstractController(cc) {

  def dbInit() = Action.async { request =>
    movieRepository.dbInit
      .map(_ => Created("Tabla creada"))
      .recover { ex =>
        play.Logger.of("dbInit").debug("Error en dbInit", ex)
        InternalServerError(s"Hubo un error")
      }
  }

  implicit val serializador = Json.format[Movie]
  val logger = play.Logger.of("MovieController")

  def getMovies: Action[AnyContent] = Action.async {
    movieRepository
      .getAll
      .map(movies => {
        val j = Json.obj(
          fields = "data" -> movies,
          "message" -> "Movies listed"
        )
        Ok(j)
      }).recover {
      case ex =>
        logger.error("Falló en getMovies", ex)
        InternalServerError(s"Hubo un error: ${ex.getLocalizedMessage}")
    }
  }

  def getMovie(id: String): Action[AnyContent] = Action.async {
    movieRepository
      .getOne(id)
      .map(movie => {
        val j = Json.obj(
          fields = "data" -> movie,
          "message" -> "Movie listed"
        )
        Ok(j)
      }).recover {
      case ex =>
        logger.error("Falló en getMovie", ex)
        InternalServerError(s"Hubo un error: ${ex.getLocalizedMessage}")
    }
  }


  def createMovie: Action[JsValue] = Action.async(parse.json) { request =>
    val validador = request.body.validate[Movie]
    validador.asEither match {
      case Left(error) => Future.successful(BadRequest(error.toString()))
      case Right(movie) => {
        movieRepository
          .create(movie)
          .map(movie => {
            val j = Json.obj(
              fields = "data" -> movie,
              "message" -> "Movie created"
            )
            Ok(j)
          }).recover {
          case ex =>
            logger.error("Falló en createMovie", ex)
            InternalServerError(s"Hubo un error: ${ex.getLocalizedMessage}")
        }
      }
    }
  }

  def updateMovie(id: String): Action[JsValue] = Action.async(parse.json) { request =>
    val validador = request.body.validate[Movie]
    validador.asEither match {
      case Left(error) => Future.successful(BadRequest(error.toString()))
      case Right(movie) => {
        movieRepository
          .update(id, movie)
          .map(movie => {
            val j = Json.obj(
              fields = "data" -> movie,
              "message" -> "Movie updated"
            )
            Ok(j)
          }).recover {
          case ex =>
            logger.error("Falló en updateMovie", ex)
            InternalServerError(s"Hubo un error: ${ex.getLocalizedMessage}")
        }
      }
    }
  }

  def deleteMovie(id: String): Action[AnyContent] = Action.async {
    movieRepository
      .delete(id)
      .map(movie => {
        val j = Json.obj(
          fields = "data" -> movie,
          "message" -> "Movie listed"
        )
        Ok(j)
      }).recover {
      case ex =>
        logger.error("Falló en deleteMovie", ex)
        InternalServerError(s"Hubo un error: ${ex.getLocalizedMessage}")
    }
  }


}
