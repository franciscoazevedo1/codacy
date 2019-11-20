package models

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

case class GitCommitLog(merge: Option[String], commit: GitCommit,  author: GitAuthor, date: LocalDateTime, description: Option[String])

object GitCommitLog {
  private def dateFromCommitString(dateString: String): LocalDateTime = {
    val DATE_FORMAT = "E MMM dd HH:mm:ss yyyy Z"
    val LOCAL_DATE_TIME_FORMATTER =  DateTimeFormatter.ofPattern(DATE_FORMAT)
    val cleanDateString = dateString.replaceAll("Date:   ", "")
    LocalDateTime.parse(cleanDateString, LOCAL_DATE_TIME_FORMATTER)
  }

  private def descriptionFromCommitString(description: String): String =
    description.trim

  private def mergeFromCommitString(mergeString: String): Option[String] =
    Some(mergeString.replace("Merge: ", ""))

  def fromString(commitString: String): GitCommitLog = {
    val commitInList = commitString.split("\n").filterNot(_ == "").toList

    commitInList match {
      case merge :: hash :: author :: date :: description :: Nil =>
        GitCommitLog(
          mergeFromCommitString(merge),
          GitCommit.fromString(hash),
          GitAuthor.fromString(author),
          dateFromCommitString(date),
          Some(descriptionFromCommitString(description))
        )

      case hash :: author :: date :: description :: Nil =>
        GitCommitLog(
          None,
          GitCommit.fromString(hash),
          GitAuthor.fromString(author),
          dateFromCommitString(date),
          Some(descriptionFromCommitString(description))
        )

      case hash :: author :: date :: Nil =>
        GitCommitLog(
          None,
          GitCommit.fromString(hash),
          GitAuthor.fromString(author),
          dateFromCommitString(date),
          None
        )

      case _ => throw new Exception
    }
  }
}