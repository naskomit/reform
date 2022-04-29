package sysmo.coviddata.shared.data

import sysmo.reform.shared.data.{graph => G}
import sysmo.reform.shared.data.{Property => Prop}

object Immunology {
  val schema : G.VertexSchema = G.Schema.vertex_builder("Immunology")
    .label("Имунология")
    .prop(Prop.int("1").label("№"))
    .prop(Prop.string("1a").label("Име"))
    .prop(Prop.string("1b").label("Презиме"))
    .prop(Prop.string("1c").label("Фамилия"))
    .prop(Prop.string("2").label("ЕГН"))
    .prop(Prop.string("x1").label("Лаб. № Работен пакет II (Доц. Мария Атанасова, дм)"))
    .prop(Prop.string("3").label("Лаб. № Работен пакет (3_II Имунен отговор)"))
    .prop(Prop.string("4").label("PCR от назофар. секрет"))
    .prop(Prop.string("5").label("Ct E-gene"))
    .prop(Prop.string("6").label("Ct RdRp gene"))
    .prop(Prop.string("7").label("PCR от БАЛ"))
    .prop(Prop.string("8").label("Ct E-gene"))
    .prop(Prop.string("9").label("Ct RdRp gene"))
    .prop(Prop.string("10").label("IgM-постъпване"))
    .prop(Prop.string("11").label("IgG-постъпване"))
    .prop(Prop.string("12").label("IgM-изписване"))
    .prop(Prop.string("13").label("IgG-изписване"))
    .prop(Prop.string("14").label("Професионален риск"))
    .prop(Prop.string("15").label("Human IgG"))
    .prop(Prop.string("16").label("Human IgA"))
    .prop(Prop.string("17").label("Human IgM"))
    .prop(Prop.string("18").label("човешки IL-10"))
    .prop(Prop.string("19").label("човешки IL-33"))
    .prop(Prop.string("20").label("човешки IL-28А"))
    .prop(Prop.string("21").label("човешки CD40L"))
    .prop(Prop.string("22").label("анти-ендотелни антитела AECA"))
    .prop(Prop.string("23").label("анти-неутрофил цитоплазмени антитела pANCA"))
    .prop(Prop.string("24").label("анти-неутрофил цитоплазмени антитела cANCA"))
    .prop(Prop.int("day").label("Ден на изследване"))
    .prop(Prop.real("25").label("Lymphosum"))
    .prop(Prop.real("26").label("% T-Sum"))
    .prop(Prop.real("27").label("CD3/CD16+56/CD45/CD4/CD19/CD8 TruC Lymph Events"))
    .prop(Prop.real("28").label("CD3/CD16+56/CD45/CD4/CD19/CD8 TruC Bead Events"))
    .prop(Prop.real("29").label("CD3/CD16+56/CD45/CD4/CD19/CD8 TruC CD3+ %Lymphs"))
    .prop(Prop.real("30").label("CD3/CD16+56/CD45/CD4/CD19/CD8 TruC CD3+ Abs Cnt"))
    .prop(Prop.real("31").label("CD3/CD16+56/CD45/CD4/CD19/CD8 TruC CD3+CD8+ %Lymphs"))
    .prop(Prop.real("32").label("CD3/CD16+56/CD45/CD4/CD19/CD8 TruC CD3+CD8+ Abs Cnt"))
    .prop(Prop.real("33").label("CD3/CD16+56/CD45/CD4/CD19/CD8 TruC CD3+CD4+ %Lymphs"))
    .prop(Prop.real("34").label("CD3/CD16+56/CD45/CD4/CD19/CD8 TruC CD3+CD4+ Abs Cnt"))
    .prop(Prop.real("35").label("CD3/CD16+56/CD45/CD4/CD19/CD8 TruC CD3+CD4+CD8+ %Lymphs"))
    .prop(Prop.real("36").label("CD3/CD16+56/CD45/CD4/CD19/CD8 TruC CD3+CD4+CD8+ Abs Cnt"))
    .prop(Prop.real("37").label("CD3/CD16+56/CD45/CD4/CD19/CD8 TruC CD16+CD56+ %Lymphs"))
    .prop(Prop.real("38").label("CD3/CD16+56/CD45/CD4/CD19/CD8 TruC CD16+CD56+ Abs Cnt"))
    .prop(Prop.real("39").label("CD3/CD16+56/CD45/CD4/CD19/CD8 TruC CD19+ %Lymphs"))
    .prop(Prop.real("40").label("CD3/CD16+56/CD45/CD4/CD19/CD8 TruC CD19+ Abs Cnt"))
    .prop(Prop.real("41").label("CD3/CD16+56/CD45/CD4/CD19/CD8 TruC CD45+ Abs Cnt"))
    .prop(Prop.real("42").label("CD3/CD16+56/CD45/CD4/CD19/CD8 TruC 4/8 Ratio"))
    .build
}
