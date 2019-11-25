package models

case class GitCommit(hash: String, tags: Option[List[String]])

object GitCommit {
  private def fromStringToTagList(tagsListInString: String): List[String] = {
    tagsListInString
      .replaceAll("\\)","")
      .split(",").toList
  }
  def fromString(gitCommitString: String): GitCommit = {
    val rawList = gitCommitString.split("\\(").toList.map {_.trim}

    rawList match {
      case h :: t :: Nil => GitCommit(h.replace("commit", "").trim, Some(fromStringToTagList(t)))
      case h :: Nil => GitCommit(h.replace("commit", "").trim, None)
      case _ => throw new Exception(gitCommitString)
    }
  }
}
