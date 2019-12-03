package models

import java.time.LocalDateTime

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{ JsPath, JsValue, Json, Reads}


case class GitHubAnswer(name: String, email: String, date: LocalDateTime, sha: String, message: String) {
  def toGitCommitLog: Option[GitCommitLog] = {
    val gitAuthor = GitAuthor(name, email)
    val gitCommit = GitCommit(sha, None)
    Some(GitCommitLog(None, gitCommit, gitAuthor, Some(date), Some(message)))
  }

  def fromString(string: String) =
    Json.parse(string).validate[List[JsValue]].asOpt.get
}

object GitHubAnswer {
  implicit val reads: Reads[GitHubAnswer] = (
    (JsPath \"commit" \ "author" \ "name").read[String] and
      (JsPath \ "commit" \ "author" \ "email").read[String] and
      (JsPath \ "commit" \ "author" \ "date").read[LocalDateTime] and
      (JsPath \"sha").read[String] and
      (JsPath \ "commit" \ "message").read[String]
    )(GitHubAnswer.apply _)
}

case class GitHubApiRequest(owner: String, repo: String) {
  val gitHubListCommitsUrls = s"https://api.github.com/repos/$owner/$repo/commits?per_page=100&type=owner"
}

object GitHubApi {
  def fromString(gitHubUrl: String): Option[GitHubApiRequest] = {
    val gitHubUrlToList: List[String] = gitHubUrl.split(":").last.split("/").toList
    gitHubUrlToList match {
      case owner :: repo :: Nil => Some(GitHubApiRequest(owner, repo.replace(".git", "")))
      case _ => None
    }

  }
}

