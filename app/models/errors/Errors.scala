package models.errors

import controllers.HomeController.SearchForm
import play.api.data.Form

object Errors {
  case class ErrorParsingCommit(msg: String = "Error tranforming string into GitCommit") extends Exception
  case class ErrorInForm(msg: String = "Error filling the form", errorForm: Form[SearchForm]) extends Exception
  case class ErrorFromGithub(msg: String = "Error fetching data from GitHubApi") extends Exception
  case class ErrorParsingSearch(msg: String = "Error parsing search into gitHub api search format") extends Exception


}


