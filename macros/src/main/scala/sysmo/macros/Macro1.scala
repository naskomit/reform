package sysmo.macros

import scala.meta._

case class ClassField
(
  name: String,
  tpe: Type
)

object Macro1 extends App {
  def get_input = {
    val path = java.nio.file.Paths.get("macros/src/main/scala/sysmo/macros/Input.scala")
//    println(path.toAbsolutePath)
    val bytes = java.nio.file.Files.readAllBytes(path)
    val text = new String(bytes, "UTF-8")
    val input = Input.VirtualFile(path.toString, text)
    input.parse[Source].get
  }

  def expand() = {
    val input = get_input

//    class scala.meta.Source$SourceImpl
//    class scala.meta.Pkg$PkgImpl
//    class scala.meta.Term$Select$TermSelectImpl
//    class scala.meta.Term$Name$TermNameImpl
//    class scala.meta.Term$Name$TermNameImpl
//    class scala.meta.Defn$Class$DefnClassImpl
//    class scala.meta.Mod$Case$ModCaseImpl
//    class scala.meta.Type$Name$TypeNameImpl
//    class scala.meta.Ctor$Primary$CtorPrimaryImpl
//    class scala.meta.Name$Anonymous$NameAnonymousImpl
//    class scala.meta.Term$Param$TermParamImpl
//    class scala.meta.Term$Name$TermNameImpl
//    input.stats.foreach {
//        case x: Pkg => None
//        case x: Defn => p_defn(x)
//        case _ => throw new Error
//    }
        input.traverse {
          case x : Defn.Class => defn_class(x)
        }
  }

  def defn_class(node: Defn.Class) = {
    val name = node.name.value
    val comp_name = Term.Name(name)
    val fields = node.ctor.paramss.flatten.map(param => {
      ClassField(name = param.name.value, tpe = param.decltpe.get)
    })

    val enum_vals = fields.map(field => {
      val term = Pat.Var(Term.Name(field.name))
      q"val $term = Value(${field.name})"
    })


    val fields_enum =
      q"""
         object Fields extends Enumeration {
           type Fields = Value
             ..$enum_vals
         }
      """

    val meta_object =
      q"""
         object ${Term.Name(name + "Meta")} {
            $fields_enum
         }
       """

    val companion =
      q"""
          object $comp_name {
            def _meta : Any = ${Term.Name(name + "Meta")}
          }"""

    val result =
      q"""${node}
          ${meta_object}
          $companion
       """

    println(result)

//    node.children.foreach {
////      case q"case class $name (...$member)" => println(name)
//      case x : Type => p_type(x)
//      case x => println(s"Unexpected ${x.getClass} ==> ${x}")
//    }
//    println(value)
  }

  def p_type(node : Type) = {
    print(node)
  }

  expand()
}
