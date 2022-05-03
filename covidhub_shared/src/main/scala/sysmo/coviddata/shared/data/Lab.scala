package sysmo.coviddata.shared.data

import sysmo.reform.shared.data.{graph => G}
import sysmo.reform.shared.data.{Property => Prop}

object Lab {
  def add_standard_panel(sb: G.Schema.VertexSchemaBuilder): G.Schema.VertexSchemaBuilder =
    sb.prop(Prop.string("Xr"))
    .prop(Prop.string("Леук"))
    .prop(Prop.string("Неутр"))
    .prop(Prop.string("Мо"))
    .prop(Prop.string("Тром"))
    .prop(Prop.string("Урея"))
    .prop(Prop.string("СУЕ"))
    .prop(Prop.string("СРП"))
    .prop(Prop.string("Креатинин"))
    .prop(Prop.string("Пик.к-на"))
    .prop(Prop.string("АСАТ"))
    .prop(Prop.string("АЛАТ"))
    .prop(Prop.string("Фибр."))
    .prop(Prop.string("Кр.захар"))
    .prop(Prop.string("Д.димер"))
    .prop(Prop.string("Тропонин"))
    .prop(Prop.string("ЛДХ"))
    .prop(Prop.string("СК"))
    .prop(Prop.string("СК..МБ"))
    .prop(Prop.string("INR"))
    .prop(Prop.string("ПВ%"))
    .prop(Prop.string("Феритин"))
    .prop(Prop.string("Прокалцитонин"))
    .prop(Prop.string("Интерлевкин 6"))
    .prop(Prop.string("O2 сатурация"))
    .prop(Prop.string("Калий"))
    .prop(Prop.string("Натрий"))
    .prop(Prop.string("Хлор"))
    .prop(Prop.string("Калций"))


}
