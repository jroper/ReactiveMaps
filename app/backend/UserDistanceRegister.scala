package backend

import akka.actor.{Props, Actor}
import models.backend.UserPosition
import play.extras.geojson.LatLng

object UserDistanceRegister {
  case class GetDistanceTravelled(userId: String)

  def props() = Props[UserDistanceRegister]
}

class UserDistanceRegister extends Actor {

  import UserDistanceRegister._

  val settings = Settings(context.system)

  var register = Map.empty[String, (LatLng, Double)]

  def receive = {
    case UserPosition(userId, _, position) =>
      register.get(userId) match {
        case Some((lastPosition, distance)) =>
          register += userId -> (position, distance + settings.GeoFunctions.distanceBetweenPoints(position, lastPosition))
        case None =>
          register += userId -> (position, 0)
      }

    case GetDistanceTravelled(userId) =>
      sender ! register.get(userId).map(_._2)
  }
}
