/*
 * btc-hotspot
 * Copyright (C) 2016  Andrea Raspitzu
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package util
import Helpers._
import com.typesafe.scalalogging.LazyLogging
import org.specs2.mutable.BeforeAfter
import org.specs2.specification.BeforeAfterEach
import registry.SessionRepositoryRegistry

object CleanRepository {

  trait CleanSessionRepository extends BeforeAfterEach with LazyLogging {

    import registry.DatabaseRegistry.database.database.profile.api._

    override def before = registry.DatabaseRegistry.database.db.run {
      SessionRepositoryRegistry
        .sessionRepositoryImpl
        .sessionsTable
        .delete
    } futureValue

    override def after = {
      logger.info("Shutting down DB")
      registry.DatabaseRegistry.database.db.shutdown.futureValue
    }

  }

}
