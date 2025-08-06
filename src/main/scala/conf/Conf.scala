package conf

import io.circe
import io.circe.config.parser
import io.circe.generic.auto.*
case class DbConf(
    user: String,
    dbname: String,
    pswd: String,
    host: String,
    port: Int
)

case class ApplicationConf(db: DbConf)
object ApplicationConf {
  def apply(): Either[circe.Error, ApplicationConf] =
    parser.decode[ApplicationConf]()
}
