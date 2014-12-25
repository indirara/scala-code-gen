package net.yefremov.sample.codegen.macros

import net.yefremov.sample.codegen.schema.FieldType._

import scala.annotation.StaticAnnotation
import scala.language.experimental.macros
import scala.reflect.macros.Context

import net.yefremov.sample.codegen.schema.{FieldType, TypeSchema}

/**
 * A macro generator based on quasiquates.
 * @author Dmitriy Yefremov
 */
object QuasiquotesGenerator {

  def generate(c: Context)(annottees: c.Expr[Any]*) = {

    import c.universe._

    def getSchemaPath: String = {
      c.prefix.tree match {
        case Apply(_, List(Literal(Constant(x)))) => x.toString
        case _ => c.abort(c.enclosingPosition, "schema file path is not specified")
      }
    }

    def toType(fieldType: FieldType) = {
      fieldType match {
        case FieldType.String => typeOf[String]
        case FieldType.Integer => typeOf[Int]
        case FieldType.Boolean => typeOf[Boolean]
      }
    }

    annottees.map(_.tree) match {
      case List(q"class $name") =>

        val schema = TypeSchema.fromJson(getSchemaPath)

        val params = schema.fields.map { field =>
          val fieldType = toType(field.valueType)
          val fieldName = newTermName(field.name)
          q"val $fieldName: $fieldType"
        }

        c.Expr[Any](
          q"""
            class $name(..$params) {

              def schema = ${schema.toString}

            }
          """
        )
    }
  }

}

class FromSchema(schemaFile: String) extends StaticAnnotation {

  def macroTransform(annottees: Any*) = macro QuasiquotesGenerator.generate

}
