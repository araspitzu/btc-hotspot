package protocol

import java.sql.Timestamp
import java.time.LocalDateTime

import protocol.domain.QtyUnit
import protocol.domain.QtyUnit.QtyUnit
import registry.DatabaseRegistry

trait DbSerializers {
  import DatabaseRegistry.database.database.profile.api._

  //mapped via java.time.LocalDateTime -> java.sql.Timestamp -> DATATYPE(DATETIME)
  implicit val localDateTimeMapper = MappedColumnType.base[LocalDateTime, Timestamp](
    localDateTime => Timestamp.valueOf(localDateTime),
    date => date.toLocalDateTime
  )

  implicit val qtyUnitMapper = MappedColumnType.base[QtyUnit, String](
    e => e.toString,
    s => QtyUnit.withName(s)
  )

}
