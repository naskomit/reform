package sysmo.coviddata.shared.data

import sysmo.reform.shared.data.{graph => G}
import sysmo.reform.shared.data.{Property => Prop}

object Therapy {
  val schema : G.VertexSchema = G.Schema.vertex_builder("Therapy")
    .prop(Prop.int("1").label("№"))
    .prop(Prop.string("1a").label("Име"))
    .prop(Prop.string("1b").label("Презиме"))
    .prop(Prop.string("1c").label("Фамилия"))
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
