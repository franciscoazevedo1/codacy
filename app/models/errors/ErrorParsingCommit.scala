package models.errors

case class ErrorParsingCommit(msg: String = "Error tranforming string into GitCommit") extends Exception
