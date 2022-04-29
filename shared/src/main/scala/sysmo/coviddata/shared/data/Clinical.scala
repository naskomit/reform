package sysmo.coviddata.shared.data

import sysmo.reform.shared.data.{graph => G}
import sysmo.reform.shared.data.{Property => Prop}

object Clinical {
  val schema : G.VertexSchema = G.Schema.vertex_builder("Clinical")
    .label("Клинични")
    // Част 2 - Клин.I
//    .link(Link.builder("1", SocioDemographic_Graph.schema))
    .prop(Prop.int("1").label("№"))
    .prop(Prop.string("1a").label("Име"))
    .prop(Prop.string("1b").label("Презиме"))
    .prop(Prop.string("1c").label("Фамилия"))
    .prop(Prop.string("14").label("Хоспитализиран").categorical)
    .prop(Prop.date("14a").label("Дата на хоспитализация"))
    .prop(Prop.int("15").label("Болничен престой, дни"))
    .prop(Prop.string("16").label("Изход от заболяването").categorical)
    .prop(Prop.string("17").label("Потвърждаване на диагнозата"))
    .prop(Prop.string("18").label("Форми на диагнозата").categorical)
    .prop(Prop.string("19a").label("Дебют от заболяването").categorical)
    .prop(Prop.string("19b").label("Дебют от заболяването").categorical)
    .prop(Prop.string("19c").label("Дебют от заболяването").categorical)
    .prop(Prop.string("19d").label("Дебют от заболяването").categorical)
    .prop(Prop.string("19e").label("Дебют от заболяването").categorical)
    .prop(Prop.string("19f").label("Дебют от заболяването").categorical)
    .prop(Prop.string("19g").label("Дебют от заболяването").categorical)
    .prop(Prop.string("19h").label("Дебют от заболяването").categorical)
    .prop(Prop.string("19i").label("Дебют от заболяването").categorical)
    .prop(Prop.string("19j").label("Дебют от заболяването").categorical)
    .prop(Prop.string("19k").label("Дебют от заболяването").categorical)
    .prop(Prop.string("19l").label("Дебют от заболяването").categorical)
    .prop(Prop.int("20a").label("Оценка на фебрилитета / дни"))
    .prop(Prop.string("20b").label("Оценка на фебрилитета").categorical)
    .prop(Prop.string("21").label("Наличие на кашлица").categorical)
    .prop(Prop.int("22a").label("Характеристика на кашлицата / Ден на поява"))
    .prop(Prop.string("22b").label("Характеристика на кашлицата").categorical)
    .prop(Prop.string("23").label("Наличие на болкови-ставен синдром").categorical)
    .prop(Prop.string("24a").label("Локализациа на БСС").categorical)
    .prop(Prop.string("24b").label("Локализациа на БСС").categorical)
    .prop(Prop.string("24c").label("Локализациа на БСС").categorical)
    .prop(Prop.string("24d").label("Локализациа на БСС").categorical)
    .prop(Prop.string("25").label("Наличие на главоболие").categorical)
    .prop(Prop.string("26").label("Вид на главоболието").categorical)
    .prop(Prop.string("27").label("Наличие на световъртеж").categorical)
    .prop(Prop.string("28").label("Наличие на колаптоидни прояви").categorical)
    .prop(Prop.string("29").label("Наличие на отпадналост").categorical)
    .prop(Prop.string("30a").label("Характеристики на отпадналостта").categorical)
    .prop(Prop.string("30b").label("Наличие на безапетитие").categorical)
    .prop(Prop.string("30c").label("Характеристики на безапетитието").categorical)
    // Част 3-Клин.II
    .prop(Prop.string("31").label("Наличие на нарушено обоняние").categorical)
    .prop(Prop.string("32").label("Времетраене на липсата на обоняние"))
    .prop(Prop.string("33").label("Наличие на нарушен вкус").categorical)
    .prop(Prop.string("34").label("Времетраене на липсата на вкус"))
    .prop(Prop.string("35a").label("Проведено лечение до момента на хоспитализация").categorical)
    .prop(Prop.string("35a1").label("Доза 1"))
    .prop(Prop.string("35b").label("").categorical)
    .prop(Prop.string("35b1").label("Доза 2"))
    .prop(Prop.string("35c").label("").categorical)
    .prop(Prop.string("35c1").label("Доза 3"))
    .prop(Prop.string("35d").label("").categorical)
    .prop(Prop.string("35d1").label("Доза 4"))
    .prop(Prop.string("35e").label("").categorical)
    .prop(Prop.string("35e1").label("Доза 5"))
    .prop(Prop.string("36").label("Физикален статус"))
    .prop(Prop.string("36a").label("Общо състояние").categorical)
    .prop(Prop.string("36b").label("ДС"))
    .prop(Prop.string("36c").label("ССС"))
    .prop(Prop.string("36d").label("Храносмилатена система"))
    .prop(Prop.string("36e").label("Отделителна система"))
    .prop(Prop.string("36f").label("Кожа"))
    .prop(Prop.string("36g").label("ОДА"))
    // Прием на медикаменти за други заболявания
    .prop(Prop.string("37a").label("Натихипертензивни").categorical)
    .prop(Prop.string("37b").label("Антикоагуланти").categorical)
    .prop(Prop.string("37c").label("Противодиабетни").categorical)
    .prop(Prop.string("37d").label("НСПВ").categorical)
    .prop(Prop.string("37e").label("Онкотерапия").categorical)
    .prop(Prop.string("37f").label("Други"))

    // Част 5-Клин.V
    .prop(Prop.string("39a").label("Описание на газов анализ"))
    .prop(Prop.string("39b").label("Описание на КАР"))
    .prop(Prop.string("40a").label("Проведена Rö графия на бял дроб").categorical)
    .prop(Prop.string("40b").label("Описание - Закл."))
    .prop(Prop.string("41a").label("Проведена CT на бял дроб").categorical)
    .prop(Prop.string("41b").label("Описание - Закл."))
    .prop(Prop.string("42a").label("Проведена ехокардиография").categorical)
    .prop(Prop.string("42b").label("Описание - Закл."))
    .prop(Prop.string("43a").label("Проведена абдоминална ехография").categorical)
    .prop(Prop.string("43b").label("Описание - Закл."))
    .prop(Prop.string("44a").label("Проведена СТ на корем").categorical)
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

//    .prop(Prop.string("").label(""))
    .build
}

object ClinicalLab {
  val schema: G.VertexSchema = G.Schema.vertex_builder("ClinicalLab")
    .label("Клинични лаборатория")
    .prop(Prop.int("1").label("№"))
    .prop(Prop.string("1a").label("Име"))
    .prop(Prop.string("1b").label("Презиме"))
    .prop(Prop.string("1c").label("Фамилия"))
    .prop(Prop.string("day").label("Ден от изследването"))
    .transform(Lab.add_standard_panel)
    .prop(Prop.string("pH"))
    .prop(Prop.string("pCO2"))
    .prop(Prop.string("pO2"))
    .prop(Prop.string("общ бел"))
    .prop(Prop.string("алб"))

    .build
}