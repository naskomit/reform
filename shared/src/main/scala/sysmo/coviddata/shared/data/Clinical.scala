package sysmo.coviddata.shared.data

import sysmo.reform.shared.data.{table => sdt}

//object Clinical_Table {
//  val fields = Seq(
//    sdt.Field.Int("1").label("№"),
//    sdt.Field.Char("1a").label("Име"),
//    sdt.Field.Char("1b").label("Презиме"),
//    sdt.Field.Char("1c").label("Фамилия"),
//
//    sdt.Field.Char("14").label("Хоспитализиран"),
//    sdt.Field.Char("14a").label("Дата на хоспитализация"),
//    sdt.Field.Int("15").label("Болничен престой, дни"),
//    sdt.Field.Char("16").label("Изход от заболяването"),
//    sdt.Field.Char("17").label("Потвърждаване на диагнозата"),
//    sdt.Field.Char("18").label("Форми на диагнозата"),
//    sdt.Field.Char("19a").label("Дебют от заболяването"),
//    sdt.Field.Char("19b").label("Дебют от заболяването"),
//    sdt.Field.Char("19c").label("Дебют от заболяването"),
//    sdt.Field.Char("19d").label("Дебют от заболяването"),
//    sdt.Field.Char("19e").label("Дебют от заболяването"),
//    sdt.Field.Char("19f").label("Дебют от заболяването"),
//    sdt.Field.Char("19g").label("Дебют от заболяването"),
//    sdt.Field.Char("19h").label("Дебют от заболяването"),
//    sdt.Field.Char("19i").label("Дебют от заболяването"),
//    sdt.Field.Char("19j").label("Дебют от заболяването"),
//    sdt.Field.Char("19k").label("Дебют от заболяването"),
//    sdt.Field.Char("19l").label("Дебют от заболяването"),
//    sdt.Field.Int("20a").label("Оценка на фебрилитета, дни"),
//    sdt.Field.Char("20b").label("Оценка на фебрилитета"),
//    sdt.Field.Char("21").label("Наличие на кашлица"),
//    sdt.Field.Int("22a").label("Характеристика на кашлицата / Ден на поява"),
//    sdt.Field.Char("22b").label("Характеристика на кашлицата"),
//    sdt.Field.Char("23").label("Наличие на болкови-ставен синдром"),
//    sdt.Field.Char("24a").label("Локализациа на БСС"),
//    sdt.Field.Char("24b").label("Локализациа на БСС"),
//    sdt.Field.Char("24c").label("Локализациа на БСС"),
//    sdt.Field.Char("24d").label("Локализациа на БСС"),
//    sdt.Field.Char("25").label("Наличие на главоболие"),
//    sdt.Field.Char("26").label("Вид на главоболието"),
//    sdt.Field.Char("27").label("Наличие на световъртеж"),
//    sdt.Field.Char("28").label("Наличие на колаптоидни прояви"),
//    sdt.Field.Char("29").label("Наличие на отпадналост"),
//    sdt.Field.Char("30a").label("Характеристики на отпадналостта"),
//    sdt.Field.Char("30b").label("Наличие на безапетитие"),
//    sdt.Field.Char("30c").label("Характеристики на безапетитието"),
//  ).map(x => x.get)
//  val schema = sdt.Schema(fields)
//
//}

object Clinical {
  import sysmo.reform.shared.data.graph._
  val schema : VertexSchema = Schema.vertex_builder("Clinical")
    // Част 2 - Клин.I
//    .link(Link.builder("1", SocioDemographic_Graph.schema))
    .prop(Prop.int("1").label("№"))
    .prop(Prop.string("1a").label("Име"))
    .prop(Prop.string("1b").label("Презиме"))
    .prop(Prop.string("1c").label("Фамилия"))
    .prop(Prop.string("14").label("Хоспитализиран"))
    .prop(Prop.string("14a").label("Дата на хоспитализация"))
    .prop(Prop.int("15").label("Болничен престой, дни"))
    .prop(Prop.string("16").label("Изход от заболяването"))
    .prop(Prop.string("17").label("Потвърждаване на диагнозата"))
    .prop(Prop.string("18").label("Форми на диагнозата"))
    .prop(Prop.string("19a").label("Дебют от заболяването"))
    .prop(Prop.string("19b").label("Дебют от заболяването"))
    .prop(Prop.string("19c").label("Дебют от заболяването"))
    .prop(Prop.string("19d").label("Дебют от заболяването"))
    .prop(Prop.string("19e").label("Дебют от заболяването"))
    .prop(Prop.string("19f").label("Дебют от заболяването"))
    .prop(Prop.string("19g").label("Дебют от заболяването"))
    .prop(Prop.string("19h").label("Дебют от заболяването"))
    .prop(Prop.string("19i").label("Дебют от заболяването"))
    .prop(Prop.string("19j").label("Дебют от заболяването"))
    .prop(Prop.string("19k").label("Дебют от заболяването"))
    .prop(Prop.string("19l").label("Дебют от заболяването"))
    .prop(Prop.int("20a").label("Оценка на фебрилитета) дни"))
    .prop(Prop.string("20b").label("Оценка на фебрилитета"))
    .prop(Prop.string("21").label("Наличие на кашлица"))
    .prop(Prop.int("22a").label("Характеристика на кашлицата / Ден на поява"))
    .prop(Prop.string("22b").label("Характеристика на кашлицата"))
    .prop(Prop.string("23").label("Наличие на болкови-ставен синдром"))
    .prop(Prop.string("24a").label("Локализациа на БСС"))
    .prop(Prop.string("24b").label("Локализациа на БСС"))
    .prop(Prop.string("24c").label("Локализациа на БСС"))
    .prop(Prop.string("24d").label("Локализациа на БСС"))
    .prop(Prop.string("25").label("Наличие на главоболие"))
    .prop(Prop.string("26").label("Вид на главоболието"))
    .prop(Prop.string("27").label("Наличие на световъртеж"))
    .prop(Prop.string("28").label("Наличие на колаптоидни прояви"))
    .prop(Prop.string("29").label("Наличие на отпадналост"))
    .prop(Prop.string("30a").label("Характеристики на отпадналостта"))
    .prop(Prop.string("30b").label("Наличие на безапетитие"))
    .prop(Prop.string("30c").label("Характеристики на безапетитието"))
    // Част 3-Клин.II
    .prop(Prop.string("31").label("Наличие на нарушено обоняние"))
    .prop(Prop.string("32").label("Времетраене на липсата на обоняние"))
    .prop(Prop.string("33").label("Наличие на нарушен вкус"))
    .prop(Prop.string("34").label("Времетраене на липсата на вкус"))
    .prop(Prop.string("35a").label("Проведено лечение до момента на хоспитализация"))
    .prop(Prop.string("35a1").label("Доза 1"))
    .prop(Prop.string("35b").label(""))
    .prop(Prop.string("35b1").label("Доза 2"))
    .prop(Prop.string("35c").label(""))
    .prop(Prop.string("35c1").label("Доза 3"))
    .prop(Prop.string("35d").label(""))
    .prop(Prop.string("35d1").label("Доза 4"))
    .prop(Prop.string("35e").label(""))
    .prop(Prop.string("35e1").label("Доза 5"))
    .prop(Prop.string("36").label("Физикален статус"))
    .prop(Prop.string("36a").label("Общо състояние"))
    .prop(Prop.string("36b").label("ДС"))
    .prop(Prop.string("36c").label("ССС"))
    .prop(Prop.string("36d").label("Храносмилатена система"))
    .prop(Prop.string("36e").label("Отделителна система"))
    .prop(Prop.string("36f").label("Кожа"))
    .prop(Prop.string("36g").label("ОДА"))
    // Прием на медикаменти за други заболявания
    .prop(Prop.string("37a").label("Натихипертензивни"))
    .prop(Prop.string("37b").label("Антикоагуланти"))
    .prop(Prop.string("37c").label("Противодиабетни"))
    .prop(Prop.string("37d").label("НСПВ"))
    .prop(Prop.string("37e").label("Онкотерапия"))
    .prop(Prop.string("37f").label("Други"))

    // Част 5-Клин.V
    .prop(Prop.string("39a").label("Описание на газов анализ"))
    .prop(Prop.string("39b").label("Описание на КАР"))
    .prop(Prop.string("40a").label("Проведена Rö графия на бял дроб"))
    .prop(Prop.string("40b").label("Описание - Закл."))
    .prop(Prop.string("41a").label("Проведена CT на бял дроб"))
    .prop(Prop.string("41b").label("Описание - Закл."))
    .prop(Prop.string("42a").label("Проведена ехокардиография"))
    .prop(Prop.string("42b").label("Описание - Закл."))
    .prop(Prop.string("43a").label("Проведена абдоминална ехография"))
    .prop(Prop.string("43b").label("Описание - Закл."))
    .prop(Prop.string("44a").label("Проведена СТ на корем"))
    .prop(Prop.string("44b").label("Описание - Закл."))
    // Част 7-Терап.II
    .prop(Prop.string("45").label("О2 лечение"))
    .prop(Prop.int("45a").label("Дни"))
    .prop(Prop.string("45b").label("Дозировка"))
    .prop(Prop.string("45c").label("Апаратна вентилация"))
    .prop(Prop.string("46").label("Ход на болестта"))
    .prop(Prop.string("47").label("Ден на поява на усложнения"))
    .prop(Prop.string("48").label("Вид усложнения"))
    .prop(Prop.string("49").label("Клинично влошаване"))
    .prop(Prop.string("50").label("Ден от влошаването"))
    .build
}