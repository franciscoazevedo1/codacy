package models

case class GitAuthor(name: String, email: String)

object GitAuthor1 {
  private def clearAuthorName(name: String): String = name.replace("Author: ", "").trim

  private def clearAuthorEmail(email: String): String = email.replaceAll("\\>","")

  def fromString(commitLogAuthor: String): GitAuthor = {
    val listAuthorCommit = commitLogAuthor.split("<").toList
    listAuthorCommit match {
      case h :: t :: Nil => GitAuthor(clearAuthorName(h), clearAuthorEmail(t))
      case _ => throw new Exception("Failed parting author name")
    }
  }
}

