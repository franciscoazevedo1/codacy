package models

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import sys.process._


case class GitCommitLog(merge: Option[String], commit: GitCommit,  author: GitAuthor, date: LocalDateTime, description: Option[String])

object GitCommitLog1 {
  private def dateFromCommitString(dateString: String): LocalDateTime = {
    val DATE_FORMAT = "E MMM d HH:mm:ss yyyy Z"
    val LOCAL_DATE_TIME_FORMATTER =  DateTimeFormatter.ofPattern(DATE_FORMAT)
    val cleanDateString = dateString.replaceAll("Date:   ", "")
    LocalDateTime.parse(cleanDateString, LOCAL_DATE_TIME_FORMATTER)
  }

  private def descriptionFromCommitString(description: List[String]): String =
    description.mkString(" ")

  private def mergeFromCommitString(mergeString: String): Option[String] =
    Some(mergeString.replace("Merge: ", ""))

  def fromOriginalString(originalCommitsString: String): List[Option[GitCommitLog]] = {
    val listOfCommits = originalCommitsString.trim.split("commit").filterNot(_ == "").toList
    listOfCommits.map { string =>
      fromString(string) }
  }

  private def removeDir(gitUrl: String) = {
    val folderName = gitUrl.split("\\/").last.replace(".git", "")
    s"cd ../".#&&(s"rm -rf $folderName") !
  }

  def fromGitUrl(gitUrl: String) = {
      val folderName = gitUrl.split("\\/").last.replace(".git", "")
      s"git clone $gitUrl".#&&(s"git --git-dir $folderName/.git log") !!

  }

  def begin(gitUrl: String) = {
    val commits = fromGitUrl(gitUrl)
    val listOfCommits = fromOriginalString(commits)
    removeDir(gitUrl)
    listOfCommits
  }

  def fromString(commitString: String): Option[GitCommitLog] = {
    println(commitString)
    val commitInList = commitString.split("\n").filterNot(_ == "").toList.map{_.trim}.filterNot(_ == "")

    commitInList match {
      case hash :: merge :: author :: date :: tail if !commitInList.filter(_.contains("Merge")).isEmpty =>
        Some(GitCommitLog(
          mergeFromCommitString(merge),
          GitCommit1.fromString(hash),
          GitAuthor1.fromString(author),
          dateFromCommitString(date),
          Some(descriptionFromCommitString(tail))
        ))

      case hash :: author :: date :: tail  =>
        Some(GitCommitLog(
          None,
          GitCommit1.fromString(hash),
          GitAuthor1.fromString(author),
          dateFromCommitString(date),
          Some(descriptionFromCommitString(tail))
        ))


      case hash :: author :: date :: Nil =>
        Some(GitCommitLog(
          None,
          GitCommit1.fromString(hash),
          GitAuthor1.fromString(author),
          dateFromCommitString(date),
          None
        ))

      case _ =>
        println(s"ERROR Parsing $commitString")
        None
    }
  }
}