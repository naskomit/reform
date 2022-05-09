# Example form definitions

## Analytics engine form

```scala

  /** Library definitions */
  // Storage type
  enum ST {
    Int,
    Real,
    Bool,
    Char
  }

  // Presentation type
  enum PT {
    None,
    Category,
    Date
  }

  case class FixedOptions(option: Named*)
    

  /** Biomarker definition */
  val biomarker_map = Map(
    "nanostring" -> Map(
      "vars" -> Seq(
        Named("norm_count", "Normalized Count")
        Named("raw_count", "Raw Count")
      ),
      "bm" -> Seq(
        Named("A2M"), Named("AC2B"), Named("HMGL")
      )
    ), 
    "qpcr" -> Map(
      "vars" -> Seq(
      
      ),
      "bm" -> Seq(
        
      )
    ), 
    "fc" -> Map(
      "vars" -> Seq(),
      "bm" -> Seq()
    ), 
    "fmi_mutation" -> Map(
      "vars" -> Seq(),
      "bm" -> Seq()
    ), 
  )

  val biomarker_option_provider = new RecordOptionProvider() {
    override def get_options(record: ValueMap, field_id: String, flt: OptionFilter): Future[Seq[LabeledValue[_]]] = {
      field_id match {
        case "type" => FixedOptions(
          Named("nanostring", Some("Nanostring")),
          Named("qpcr", Some("qPCR")),
          Named("fc", Some("Flow Cytometry")),
          Named("fmi_mutation", Some("FMI Mutation")),
        )

        case "variable" => Future.successful(biomarker_map)
          .map(x => x(record("type"))("vars"))

        case "variable" => Future.successful(biomarker_map)
          .map(x => x(record("type"))("bm"))
      }
    }
  }

  val indep_biomarker_record = Record(
    Field(name = "type", label = Some("Type"), data_type = ST.Char, pres_type = PT.Category),
    Field(name = "variable", label = Some("Variable"), data_type = ST.Char, pres_type = PT.Category),
    Field(name = "panel", label = Some("Panel"), data_type = ST.Char, pres_type = PT.Category),
    Field(name = "timepoint", label = Some("Timepoint"), data_type = ST.Char, pres_type = PT.Category, multiple = true, allow_none = false, allow_all = true),
    Field(name = "xform", label = Some("Transformation"), data_type = ST.Char, pres_type = PT.Category, allow_none = true),
    Field(name = "scale", label = Some("Scale"), data_type = ST.Bool)
  )

  val dep_biomarker_record = dep_biomarker_record.drop_fields(Seq("timepoint", "scale"))


  /** Analysis definition */

  val analysis_def_record = Record(
    dep_biomarker_record + Record.Array(dep_biomarker_record)
  )
  

  val ae_option_provider = new RecordOptionProvider() {
    override def get_options(record: ValueMap, field_id: String, flt: OptionFilter): Future[Seq[LabeledValue[_]]] = {
      field_id match {
//          Named("", Some("")),

        case "analysis_type" => FixedOptions(
          Named("CA", Some("Correlation Analysis")),
          Named("DEA", Some("Differential Expression Analysis")),
          Named("PCA", Some("Principal Component Analysis")),
          Named("RCI", Some("RCI Analysis")),
        )

        case "var_type" => FixedOptions(
          Named("clin_outcome", Some("Clinical Outcome")),
          Named("biomarker", Some("Biomarker")),
        )


        // Reuse?
        case "dep_var/bm_type" => FixedOptions(
          Named("nanostring", Some("Nanostring")),
          Named("qpcr", Some("qPCR")),
          Named("fc", Some("Flow Cytometry")),
          Named("fmi_mutation", Some("FMI Mutation")),
        )
      }
    }
  }

  val ae_form_fields = FormFields(
    FieldGroup(name = "general", label = Some("General"), fields = Seq(
      Field(name = "analysis_type", label = Some("Analysis type"), data_type = ST.Char, pres_type = PT.Category),
      Field(name = "analysis_name", label = Some("Analysis name"), data_type = ST.Char)
    )),

    FieldGroup(name = "dep_var", label = Some("Dependant Variable"), fields = Seq(
      Field(name = "var_type", label = Some("Variable Type"), data_type = ST.Char, pres_type = PT.Category)
    )),

    FieldGroup(name = "indep_var", label = Some("Independant Variable(s)"), fields = Seq(
      
    ))
  )

  val form_model = FormModel(
    fields = ae_form_fields,
    option_provider = ae_option_provider
  )

```