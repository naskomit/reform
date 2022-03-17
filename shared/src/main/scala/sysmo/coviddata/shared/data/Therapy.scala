package sysmo.coviddata.shared.data

import sysmo.reform.shared.data.{graph => G}

object Therapy {
  val schema : G.VertexSchema = G.Schema.vertex_builder("Therapy")
    .prop(G.Prop.int("1").label("№"))
    .prop(G.Prop.string("1a").label("Име"))
    .prop(G.Prop.string("1b").label("Презиме"))
    .prop(G.Prop.string("1c").label("Фамилия"))
    .prop(G.Prop.string("45").label("О2 лечение"))
    .prop(G.Prop.int("45a").label("Дни"))
    .prop(G.Prop.string("45b").label("Дозировка"))
    .prop(G.Prop.string("45c").label("Апаратна вентилация"))
    .prop(G.Prop.string("46").label("Ход на болестта"))
    .prop(G.Prop.string("47").label("Ден на поява на усложнения"))
    .prop(G.Prop.string("48").label("Вид усложнения"))
    .prop(G.Prop.string("49").label("Клинично влошаване"))
    .prop(G.Prop.string("50").label("Ден от влошаването"))
    .build
}
