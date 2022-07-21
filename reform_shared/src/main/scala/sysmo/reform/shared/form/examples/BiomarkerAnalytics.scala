package sysmo.reform.shared.form.examples

import sysmo.reform.shared.expr.{Expression => E}
import sysmo.reform.shared.form.runtime.Group
import sysmo.reform.shared.form.{FormModelBuilder, FormModelHelper, runtime => FR}


object BiomarkerAnalytics extends FormModelHelper {
  class Builder extends FormModelBuilder {
    import FR.FieldValue.implicits._
    import FR.instantiation._

    override val name: String = "BiomarkerAnalytics"
    override def create(build_graph: BuildGraph, runtime: Runtime): Group = {

      // AnalysisGeneral
      val AnalysisGeneralBuilder = field_group("AnalysisGeneral")

      val AnalysisGeneral = AnalysisGeneralBuilder
        .field(_("name"), _.char)
        .build

      // AnalysisVariables

      val BuiomarkerBuilder = field_group("Biomarker")
      val Buiomarker = BuiomarkerBuilder
        .field(_("type").descr("Biomarker type"), _.select)
        .field(_("variable").descr("Variable"), _.select)
        .field(_("panel").descr("Panel"), _.select)
        .field(_("xform").descr("Transformation"), _.select)
        .field(_("biomarker").descr("Biomarker"), _.select)
        .build

      val ClinicalOutcomeBuilder = field_group("ClinicalOutcome")
      val ClinicalOutcome = ClinicalOutcomeBuilder
        .field(_("variable").descr("Variable"), _.select)
        .field(_("neg").descr("Negative (-)"), _.select.multiple())
        .field(_("pos").descr("Positive (-)"), _.select.multiple())
        .build

      val VisitContrastBuilder = field_group("VisitContrast")
        VisitContrastBuilder
          .field(_("variable").descr("Variable"), _.select)
          .field(_("neg").descr("Negative (-)"), _.select.multiple())
          .field(_("pos").descr("Positive (-)"), _.select.multiple())
          .build

      val BuiomarkerClassBuilder = field_group("BuiomarkerClass")
      val BuiomarkerClass = BuiomarkerClassBuilder
        .field(_("type").descr("Biomarker type"), _.select)
        .field(_("variable").descr("Variable"), _.select)
        .field(_("xform").descr("Transformation"), _.select)
        .field(_("panel").descr("Panel"), _.select)
        .field(_("timepoint").descr("Timepoint"), _.select)
        .field(_("scale").descr("Scale"), _.bool)
        .build

      val BiomarkerContrastBuilder = field_group("BiomarkerContrast")
        .field(_("type").descr("Biomarker type"), _.select)
        .field(_("variable").descr("Variable"), _.select)
        .field(_("neg").descr("Negative (-)"), _.select.multiple())
        .field(_("pos").descr("Positive (-)"), _.select.multiple())



      val CorrAnn_DepVar_Builder = union("CA_DepVar", BuiomarkerBuilder, ClinicalOutcomeBuilder)

      val CorrelationAnalysisBuilder = field_group("CorrelationAnalysis")
      val CorrelationAnalysis = CorrelationAnalysisBuilder
        .array(_("dep_var").descr("Dependent variable"), CorrAnn_DepVar_Builder)
        .array(_("indep_var").descr("Independent variables"), BuiomarkerClassBuilder)
        .build


      val DE_IndepVar_Builder = union("DE_IndepVar", ClinicalOutcomeBuilder, VisitContrastBuilder)

      val DifferentialExpressionBuilder = field_group("DifferentialExpression")
      val DifferentialExpression = DifferentialExpressionBuilder
        .array(_("dep_var").descr("Dependent variable"), BuiomarkerClassBuilder)
        .array(_("indep_var").descr("Independent variables"), DE_IndepVar_Builder)
        .build

      val PCAnalysisBuilder = field_group("PCA")
      val PCAnalysis = PCAnalysisBuilder
        .array(_("vars").descr("Variables"), BuiomarkerClassBuilder)
        .field(_("ann_var").descr("Annotation Variables"), _.select.multiple())
        .field(_("n_dim").descr("Num PC dimensions"), _.int)
        .field(_("show_ci").descr("Show 95% CI"), _.bool)

        .build


      val AnalysisVariablesBuilder = union("AnalysisVariables", CorrelationAnalysisBuilder, DifferentialExpressionBuilder, PCAnalysisBuilder)
//      field_group("AnalysisVariables")
//      val AnalysisVariables = AnalysisVariablesBuilder
//        .group(_("dependent"), DependentVariableBuilder)
//        .group(_("independent"), IndependentVariableBuilder)
//        .build

      val AnalysisDefinitionBuilder = field_group("AnalysisDefinition")
      val AnalysisDefinition = AnalysisDefinitionBuilder
        .array(_("variables").descr("Variables"), AnalysisVariablesBuilder)
        .build

      // AnalysisOptions
      val SubjectSelectionBuilder = field_group("SubjectSelection")
      val SubjectStratificationBuilder = field_group("SubjectStratificationBuilder") // This uses metadata -> columns of subjects...
      val AnalysisOptionsBuilder = field_group("AnalysisOptions")
      val AnalysisOptions = AnalysisOptionsBuilder
        .ref(_("population").descr("Analysis population"), _(SubjectSelectionBuilder, "subjectid"))
        .ref(_("stratification").descr("Stratification variable"), _(SubjectStratificationBuilder, "name"))
        .build

      val BiomarkerAnalyticsBuilder = field_group("BiomarkerAnalytics")
      val BiomarkerAnalytics = BiomarkerAnalyticsBuilder
        .group(_("general").descr("General"), AnalysisGeneralBuilder)
        .group(_("definition").descr("Analysis definition"), AnalysisDefinitionBuilder)
        .group(_("options").descr("Analysis options"), AnalysisOptionsBuilder)
        .layout("tabbed")
        .build


      val my_analytics = runtime.instantiate(
        BiomarkerAnalytics()
      )

      my_analytics.asInstanceOf[FR.Group]

//      val CompanyBuilder = field_group("Company")
//      val CompanyInfoBuilder = field_group(name = "CompanyInfo")
//      val PersonBuilder = field_group("Person")
//      val ProjectBuilder = field_group("Project")
//      val DivisionBuilder = field_group("Division")
//      val GroupBuilder = field_group("Group")
//      val UnitBuilder = DivisionBuilder.union("Unit", _ | GroupBuilder)
//
//      val CompanyInfo = CompanyInfoBuilder
//        .field(_("name").descr("Name"), _.char)
//        .field(_("address").descr("Address"), _.char)
//        .field(_("city").descr("City"), _.char)
//        .field(_("country").descr("Country"), _.char)
//        .build
//
//      val Person = PersonBuilder
//        .field(_("name").descr("Name"), _.char)
//        .field(_("age").descr("Age"), _.int)
//        .field(_("cid").descr("Company ID"), _.char.unique())
//        .build
//
//      val Division = DivisionBuilder
//        .field(_("name").descr("Name"), _.char)
//        .field(_("symbol").descr("Symbol"), _.char)
//        .ref(_("manager").descr("Manager"), _(PersonBuilder, "cid").label_expr(E.field("name")))
//        .array(_("units").descr("Units"), UnitBuilder)
//        .ref(_("people").descr("People"), _(PersonBuilder, "cid").label_expr(E.field("name")).multiple())
//        .build
//
//      val Group = GroupBuilder
//        .field(_("name").descr("Name"), _.char)
//        .field(_("symbol").descr("Symbol"), _.char)
//        .ref(_("manager").descr("Manager"), _(PersonBuilder, "cid").label_expr(E.field("name")))
//        .ref(_("people").descr("People"), _(PersonBuilder, "cid").label_expr(E.field("name")).multiple())
//        .build
//
//      val Project = ProjectBuilder
//        .field(_("name").descr("Name"), _.char)
//        .ref(_("manager").descr("Manager"), _(PersonBuilder, "cid").label_expr(E.field("name")))
//        .build
//
//      val Company = CompanyBuilder
//        .group(_("info").descr("Info"), CompanyInfoBuilder)
//        .array(_("people").descr("People"), PersonBuilder)
//        .array(_("units").descr("Units"), UnitBuilder)
//        .array(_("projects").descr("Projects"), ProjectBuilder)
//        .layout("tabbed")
//        .build
//
//      val my_company = runtime.instantiate(
//        Company(
//          "info" -> CompanyInfo(
//            "name" -> "The Corporation Inc.",
//            "address" -> "123 Main str.",
//            "city" -> "New Paris",
//            "country" -> "New Europe",
//          ),
//          "people" -> Seq(
//            Person("cid" -> "u101", "name" -> "John Doe"),
//            Person("cid" -> "u201", "name" -> "Old Sam"),
//            Person("cid" -> "u202", "name" -> "Young Sam"),
//          ),
//          "units" -> Seq(
//            Division(
//              "name" -> "Engineering", "symbol" -> "E",
//              "manager" -> ref(E.field("cid") === "u101")
//            ),
//            Division("name" -> "Administration", "symbol" -> "A"),
//            Division("name" -> "Purchase", "symbol" -> "P"),
//            Division("name" -> "Marketing", "symbol" -> "M"),
//          ),
//          "projects" -> Seq(
//            Project("name" -> "Clean the world")
//          )
//        )
//      )
//      my_company.asInstanceOf[FR.Group]
    }
  }
  def builder: Builder = new Builder
}
