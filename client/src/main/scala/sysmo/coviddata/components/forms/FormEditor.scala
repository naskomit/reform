package sysmo.coviddata.components.forms

import sysmo.coviddata.components.editors.StringEditor
import sysmo.coviddata.data.AsyncFormDataInterface
import sysmo.coviddata.shared.data
import sysmo.coviddata.shared.data.RecordMeta
//import japgolly.scalajs.react.util.Effect.Dispatch
//import scalajs.concurrent.JSExecutionContext.Implicits.global
//import scala.concurrent.ExecutionContext.Implicits.global
import scalajs.concurrent.JSExecutionContext.Implicits.queue

// TODO Handle premature component unmount

//object FormEditor {
//  import japgolly.scalajs.react._
//  import japgolly.scalajs.react.vdom.html_<^._
//
//  case class Props[U](data_interface: AsyncFormDataInterface[U], record_id: String, meta : RecordMeta[U], layout : FormLayout)
//  case class State[U](value : Option[U])
//
//  final class Backend[U]($: BackendScope[Props[U], State[U]]) {
//    def render (p: Props[U], s: State[U]): VdomElement = {
//      s.value match {
//        case Some(data) => {
//          val field_editors = p.meta.field_keys.map(
//            k => StringEditor(
//              k.toString, p.record_id, p.meta.fields(k).label,
//              p.meta.get_value(data, k).toString)
//          )
//          val k = p.meta.field_keys(0)
//          <.form(^.className:="form", //field_editors.map(x => x.vdomElement))
//            p.layout.to_component(field_editors.map(x => x.vdomElement)))
//        }
//
//        case None => <.div("Loading form data")
//      }
//
//    }
//
//
//    def load_initial_data(p: Props[U], s: State[U]): Callback = AsyncCallback.fromFuture(
//      p.data_interface.get_record(p.record_id)
//    ).flatMap(x => {
//      $.setState(State(Some(x))).asAsyncCallback
//    }).toCallback
//  }
//
//  def component[U] = ScalaComponent.builder[Props[U]]("FormEditor")
//    .initialState(State[U](None))
//    .renderBackend[Backend[U]]
//    .componentDidMount(f => f.backend.load_initial_data(f.props, f.state))
//    .build
//
//  def apply[U](data_interface: AsyncFormDataInterface[U], record_id: String, layout : FormLayout = ColumnarLayout(2))(implicit meta_holder: data.RecordWithMeta[U]) = {
//    component[U].apply(Props(data_interface, record_id, meta_holder._meta, layout))
//  }
//
//}
//
