package sysmo.coviddata.shared.data

//import sysmo.reform.shared.data.{table => sdt}

//object SocioDemographic {
//  val fields = Seq(
//    sdt.Field.Int("1").label("№"),
//    sdt.Field.Char("1a").label("Име"),
//    sdt.Field.Char("1b").label("Презиме"),
//    sdt.Field.Char("1c").label("Фамилия"),
//    sdt.Field.Char("1d").label("ЕГН"),
//    sdt.Field.Char("3").label("Етнос"),
//    sdt.Field.Char("4a").label("Възраст по групи"),
//    sdt.Field.Int("4").label("Възраст"),
//    sdt.Field.Char("5").label("Пол"),
//    sdt.Field.Char("5a").label("Имунизация да - не"),
//    sdt.Field.Char("5b").label("Ако има имунизация вид ваксина"),
//    sdt.Field.Char("5c").label("Дата на последна апликация"),
//    sdt.Field.Char("5d").label("Бустер да - не"),
//    sdt.Field.Char("6").label("Местоживеене"),
//    sdt.Field.Char("7").label("Образование"),
//    sdt.Field.Char("8").label("Вероятен контакт"),
//    sdt.Field.Char("9").label("Вид контакт"),
//    sdt.Field.Int("10").label("Дни от контакта"),
//    sdt.Field.Char("11").label("Социален статус"),
//    sdt.Field.Char("12").label("Наличие на придружаващи заболявания"),
//    sdt.Field.Char("13").label("Вид придружаващо заболяване"),
//    sdt.Field.Char("14a").label("Рискови фактори / Тютюн, бр. цигари"),
//    sdt.Field.Char("14a1").label("Брой цигари дневно"),
//    sdt.Field.Char("14b").label("Рискови фактори / Алкохол"),
//    sdt.Field.Char("14b1").label("Алкохол  мл/алк."),
//    sdt.Field.Char("14c").label("Рискови фактори / Злоупотреба с медикаменти"),
//    sdt.Field.Char("14d").label("Рискови фактори / Психо-травми"),
//    sdt.Field.Char("14e").label("Рискови фактори / Пренапрежение на ОДА"),
//    sdt.Field.Char("14f").label("Други")
//  ).map(x => x.get_options)
//  val schema = sdt.Schema(fields)
//}

object SocioDemographic {
  import sysmo.reform.shared.data.graph._
  val schema : VertexSchema = Schema.vertex_builder("SocioDemographic")
    .prop(Prop.int("1").label("№"))
    .prop(Prop.string("1a").label("Име"))
    .prop(Prop.string("1b").label("Презиме"))
    .prop(Prop.string("1c").label("Фамилия"))
    .prop(Prop.string("1d").label("ЕГН"))
    .prop(Prop.string("3").label("Етнос"))
    .prop(Prop.string("4a").label("Възраст по групи"))
    .prop(Prop.int("4").label("Възраст"))
    .prop(Prop.string("5").label("Пол"))
    .prop(Prop.string("5a").label("Имунизация да - не"))
    .prop(Prop.string("5b").label("Ако има имунизация вид ваксина"))
    .prop(Prop.string("5c").label("Дата на последна апликация"))
    .prop(Prop.string("5d").label("Бустер да - не"))
    .prop(Prop.string("6").label("Местоживеене"))
    .prop(Prop.string("7").label("Образование"))
    .prop(Prop.string("8").label("Вероятен контакт"))
    .prop(Prop.string("9").label("Вид контакт"))
    .prop(Prop.int("10").label("Дни от контакта"))
    .prop(Prop.string("11").label("Социален статус"))
    .prop(Prop.string("12").label("Наличие на придружаващи заболявания"))
    .prop(Prop.string("13").label("Вид придружаващо заболяване"))
    .prop(Prop.string("14a").label("Рискови фактори / Тютюн, бр. цигари"))
    .prop(Prop.string("14a1").label("Брой цигари дневно"))
    .prop(Prop.string("14b").label("Рискови фактори / Алкохол"))
    .prop(Prop.string("14b1").label("Алкохол  мл/алк."))
    .prop(Prop.string("14c").label("Рискови фактори / Злоупотреба с медикаменти"))
    .prop(Prop.string("14d").label("Рискови фактори / Психо-травми"))
    .prop(Prop.string("14e").label("Рискови фактори / Пренапрежение на ОДА"))
    .prop(Prop.string("14f").label("Други"))
    .build
}