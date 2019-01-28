package example

import scala.concurrent.ExecutionContext.Implicits.global
import slick.driver.SQLiteDriver.api._
import scala.util.{Failure, Success}

case class Movie(imdbID: String, title: String, year: String, director: String, actors: String, plot: String)
class Movies(tag: Tag) extends Table[Movie](tag, "Movies") {
  def imdbID = column[String]("imdbID", O.PrimaryKey)
  def title = column[String]("Title")
  def year = column[String]("Year")
  def director = column[String]("Director")
  def actors = column[String]("Actors")
  def plot = column[String]("Plot")
  /*
      http://slick.lightbend.com/doc/3.2.3/schemas.html
      Every table requires a * method containing a default projection.
      This describes what you get back when you return rows (in the form of a table row object) from a query.

      It is possible to define a mapped table that uses a custom type for its * projection by adding a bi-directional mapping with the <> operator
   */
  def * = (imdbID, title, year, director, actors, plot) <> (Movie.tupled, Movie.unapply)
}

object SlickExample extends App {

  // Alongside the Table row class you also need a TableQuery value which represents the actual database table
  val movies = TableQuery[Movies]

  val db = Database.forConfig("movies")
  try {
    val setup = DBIO.seq(
      (movies.schema).create
    )
    val setupFuture = db.run(setup)
    setupFuture.onComplete({
      case Success(response) => println(response)
      case Failure(e) => println(e.getMessage)
    })
    val q1 = for(m <- movies) yield m.title
    db.stream(q1.result).foreach(println)

    // Insert
    val insert = DBIO.seq(
      movies += Movie("1", "Foo1", "1970", "Bar", "Meh, Hmm", "Hmmmmmmm"),

      movies ++= Seq(
        Movie("2", "Foo2", "1970", "Bar", "Meh, Hmm", "Hmmmmmmm"),
        Movie("3", "Foo3", "1970", "Bar", "Meh, Hmm", "Hmmmmmmm")
        )
    )
    val insertFuture = db.run(insert)
    insertFuture.onComplete( _ => db.stream(q1.result).foreach(println))

    // Select
    val select = movies.filter(movie => movie.title.length <= 5).result
    val selectFuture = db.run(select)
    selectFuture.onComplete {
      case Success(response) => println(response)
      case Failure(e) => println(e.getMessage)
    }

    // Update

    // Delete

    // Join

  } finally db.close
}
