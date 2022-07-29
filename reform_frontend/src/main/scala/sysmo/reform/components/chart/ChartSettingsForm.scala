//package sysmo.reform.components.chart
//
//import japgolly.scalajs.react.vdom.html_<^._
//import sysmo.reform.components.ReactComponent
//import sysmo.reform.managers.ChartController
//import sysmo.reform.shared.chart.ChartSettings
//import sysmo.reform.util.TypeSingleton
//import scala.reflect.ClassTag
//
//object ChartSettingsFormDefs {
//  case class Props[U <: ChartSettings](chart_manager: ChartController[U])
//}
//
//class ChartSettingsForm[U <: ChartSettings : ClassTag] extends ReactComponent {
//
//  import japgolly.scalajs.react._
//
//  type Props = ChartSettingsFormDefs.Props[U]
//
//  case class State()
//
//  final class Backend($: BackendScope[Props, State]) {
//    def render(p: Props, s: State): VdomElement = {
//      <.div(
//        <.h2("Settings"),
//        // rec_manager: StreamingRecordManager[U], record_id: String,
//        //                         meta: RecordMeta[U], layout : FormLayout = ColumnarLayout(2)
////        StreamingFormEditor(
////          p.chart_manager.rec_mngr, "",
////          p.chart_manager.settings_meta
////        )
//      )
//    }
//  }
//
//  // : Scala.Component[Props, State, Backend, _]
//  val component =
//    ScalaComponent.builder[Props]("ChartSettings")
//      .initialState(State())
//      .renderBackend[Backend]
//      .build
//
//}
//
//object ChartSettingsForm extends TypeSingleton[ChartSettingsForm, ChartSettings] {
//  import ChartSettingsFormDefs._
//  override def create_instance[U <: ChartSettings](implicit tag: ClassTag[U]): ChartSettingsForm[U] = new ChartSettingsForm[U]
//  def apply[U <: ChartSettings : ClassTag](chart_manager: ChartController[U])(implicit tag: ClassTag[U]) = {
//    get_instance(tag).component(Props[U](chart_manager))
//  }
//}