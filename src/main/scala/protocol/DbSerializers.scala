package protocol

import java.sql.Timestamp
import java.time.LocalDateTime

import protocol.domain.QtyUnit
import protocol.domain.QtyUnit.QtyUnit

trait DbSerializers {

  val databaseComponent: DatabaseImpl

  import databaseComponent.database.profile.api._

  //maps java.time.LocalDateTime via java.sql.Timestamp -> DATATYPE(DATETIME)
  implicit val localDateTimeMapper = MappedColumnType.base[LocalDateTime, Timestamp](
    localDateTime => Timestamp.valueOf(localDateTime),
    date => date.toLocalDateTime
  )

  implicit val qtyUnitMapper = MappedColumnType.base[QtyUnit, String](
    e => e.toString,
    s => QtyUnit.withName(s)
  )

}
