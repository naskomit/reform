package sysmo.reform

import play.api.mvc._
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._
import javax.inject.{Inject, Singleton}

@Singleton
class AppRouter @Inject()(controller: AppController)(app: String) extends SimpleRouter {
  override def routes: Routes = {
    case GET(p"api/query") => controller.query("metadata")
  }
}