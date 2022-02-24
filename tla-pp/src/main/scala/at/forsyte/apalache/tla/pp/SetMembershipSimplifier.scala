package at.forsyte.apalache.tla.pp

import at.forsyte.apalache.tla.lir._
import at.forsyte.apalache.tla.lir.oper._
import at.forsyte.apalache.tla.lir.transformations.standard.FlatLanguagePred
import at.forsyte.apalache.tla.lir.transformations.{LanguageWatchdog, TransformationTracker}
import at.forsyte.apalache.tla.lir.values._

/**
 * A simplifier that rewrites vacuously true membership tests based on type information.
 *
 * For example, `x \in BOOLEAN` is rewritten to `TRUE` if x is typed BoolT1.
 *
 * @author
 *   Thomas Pani
 */
class SetMembershipSimplifier(tracker: TransformationTracker) extends AbstractTransformer(tracker) {
  private val boolTag = Typed(BoolT1())
  private def trueVal: ValEx = ValEx(TlaBool(true))(boolTag)

  override val partialTransformers = List(transformMembership)

  override def apply(expr: TlaEx): TlaEx = {
    LanguageWatchdog(FlatLanguagePred()).check(expr)
    transform(expr)
  }

  private def transformMembership: PartialFunction[TlaEx, TlaEx] = {
    case OperEx(TlaSetOper.in, name, ValEx(TlaBoolSet)) if name.typeTag == Typed(BoolT1()) => trueVal
    case OperEx(TlaSetOper.in, name, ValEx(TlaStrSet)) if name.typeTag == Typed(StrT1())   => trueVal
    case OperEx(TlaSetOper.in, name, ValEx(TlaIntSet)) if name.typeTag == Typed(IntT1())   => trueVal
    case OperEx(TlaSetOper.in, name, ValEx(TlaRealSet)) if name.typeTag == Typed(RealT1()) => trueVal
    case OperEx(TlaSetOper.in, name, OperEx(TlaSetOper.seqSet, ValEx(TlaBoolSet)))
        if name.typeTag == Typed(SeqT1(BoolT1())) =>
      trueVal
    case OperEx(TlaSetOper.in, name, OperEx(TlaSetOper.seqSet, ValEx(TlaStrSet)))
        if name.typeTag == Typed(SeqT1(StrT1())) =>
      trueVal
    case OperEx(TlaSetOper.in, name, OperEx(TlaSetOper.seqSet, ValEx(TlaIntSet)))
        if name.typeTag == Typed(SeqT1(IntT1())) =>
      trueVal
    case OperEx(TlaSetOper.in, name, OperEx(TlaSetOper.seqSet, ValEx(TlaRealSet)))
        if name.typeTag == Typed(SeqT1(RealT1())) =>
      trueVal
    case ex => ex
  }
}

object SetMembershipSimplifier {
  def apply(tracker: TransformationTracker): SetMembershipSimplifier = new SetMembershipSimplifier(tracker)
}
