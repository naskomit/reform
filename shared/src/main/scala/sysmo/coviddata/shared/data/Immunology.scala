package sysmo.coviddata.shared.data

import sysmo.reform.shared.data.{graph => G}

object Immunology {
  val schema : G.VertexSchema = G.Schema.vertex_builder("Immunology")
    .prop(G.Prop.int("1").label("№"))
    .prop(G.Prop.string("1a").label("Име"))
    .prop(G.Prop.string("1b").label("Презиме"))
    .prop(G.Prop.string("1c").label("Фамилия"))
    .prop(G.Prop.string("2").label("ЕГН"))
    .prop(G.Prop.string("x1").label("Лаб. № Работен пакет II (Доц. Мария Атанасова, дм)"))
    .prop(G.Prop.string("3").label("Лаб. № Работен пакет (3_II Имунен отговор)"))
    .prop(G.Prop.string("4").label("PCR от назофар. секрет"))
    .prop(G.Prop.string("5").label("Ct E-gene"))
    .prop(G.Prop.string("6").label("Ct RdRp gene"))
    .prop(G.Prop.string("7").label("PCR от БАЛ"))
    .prop(G.Prop.string("8").label("Ct E-gene"))
    .prop(G.Prop.string("9").label("Ct RdRp gene"))
    .prop(G.Prop.string("10").label("IgM-постъпване"))
    .prop(G.Prop.string("11").label("IgG-постъпване"))
    .prop(G.Prop.string("12").label("IgM-изписване"))
    .prop(G.Prop.string("13").label("IgG-изписване"))
    .prop(G.Prop.string("14").label("Професионален риск"))
    .prop(G.Prop.string("15").label("Human IgG"))
    .prop(G.Prop.string("16").label("Human IgA"))
    .prop(G.Prop.string("17").label("Human IgM"))
    .prop(G.Prop.string("18").label("човешки IL-10"))
    .prop(G.Prop.string("19").label("човешки IL-33"))
    .prop(G.Prop.string("20").label("човешки IL-28А"))
    .prop(G.Prop.string("21").label("човешки CD40L"))
    .prop(G.Prop.string("22").label("анти-ендотелни антитела AECA"))
    .prop(G.Prop.string("23").label("анти-неутрофил цитоплазмени антитела pANCA"))
    .prop(G.Prop.string("24").label("анти-неутрофил цитоплазмени антитела cANCA"))
    .prop(G.Prop.int("day").label("Ден на изследване"))
    .prop(G.Prop.real("25").label("Lymphosum"))
    .prop(G.Prop.real("26").label("% T-Sum"))
    .prop(G.Prop.real("27").label("CD3/CD16+56/CD45/CD4/CD19/CD8 TruC Lymph Events"))
    .prop(G.Prop.real("28").label("CD3/CD16+56/CD45/CD4/CD19/CD8 TruC Bead Events"))
    .prop(G.Prop.real("29").label("CD3/CD16+56/CD45/CD4/CD19/CD8 TruC CD3+ %Lymphs"))
    .prop(G.Prop.real("30").label("CD3/CD16+56/CD45/CD4/CD19/CD8 TruC CD3+ Abs Cnt"))
    .prop(G.Prop.real("31").label("CD3/CD16+56/CD45/CD4/CD19/CD8 TruC CD3+CD8+ %Lymphs"))
    .prop(G.Prop.real("32").label("CD3/CD16+56/CD45/CD4/CD19/CD8 TruC CD3+CD8+ Abs Cnt"))
    .prop(G.Prop.real("33").label("CD3/CD16+56/CD45/CD4/CD19/CD8 TruC CD3+CD4+ %Lymphs"))
    .prop(G.Prop.real("34").label("CD3/CD16+56/CD45/CD4/CD19/CD8 TruC CD3+CD4+ Abs Cnt"))
    .prop(G.Prop.real("35").label("CD3/CD16+56/CD45/CD4/CD19/CD8 TruC CD3+CD4+CD8+ %Lymphs"))
    .prop(G.Prop.real("36").label("CD3/CD16+56/CD45/CD4/CD19/CD8 TruC CD3+CD4+CD8+ Abs Cnt"))
    .prop(G.Prop.real("37").label("CD3/CD16+56/CD45/CD4/CD19/CD8 TruC CD16+CD56+ %Lymphs"))
    .prop(G.Prop.real("38").label("CD3/CD16+56/CD45/CD4/CD19/CD8 TruC CD16+CD56+ Abs Cnt"))
    .prop(G.Prop.real("39").label("CD3/CD16+56/CD45/CD4/CD19/CD8 TruC CD19+ %Lymphs"))
    .prop(G.Prop.real("40").label("CD3/CD16+56/CD45/CD4/CD19/CD8 TruC CD19+ Abs Cnt"))
    .prop(G.Prop.real("41").label("CD3/CD16+56/CD45/CD4/CD19/CD8 TruC CD45+ Abs Cnt"))
    .prop(G.Prop.real("42").label("CD3/CD16+56/CD45/CD4/CD19/CD8 TruC 4/8 Ratio"))
    .build
}