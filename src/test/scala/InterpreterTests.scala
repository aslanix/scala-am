import org.scalatest._
import org.scalatest.prop._

abstract class Benchmarks[Exp, Abs, Addr]
  (implicit abs: AbstractValue[Abs], absi: AbstractInjection[Abs],
  addr: Address[Addr], addri: AddressInjection[Addr])
    extends FlatSpec with Matchers {
  val sem: Semantics[Exp, Abs, Addr]
  val machine: AbstractMachine[Exp, Abs, Addr]

  def checkResult(file: String, expected: Abs): Unit = {
    val result = machine.eval(sem.parse(Main.fileContent(s"test/$file")), sem, false)
    assert(result.containsFinalValue(expected))
    println(s"${machine.name}, $file: ${result.numberOfStates}, ${result.time}")
  }
  def check(file: String, expected: Abs): Unit =
    file should s"eval to $expected" in { checkResult(file, expected) }

  check("blur.scm", absi.inject(true))
  check("count.scm", absi.inject("done"))
  check("cpstak.scm", absi.inject(6))
  check("fib.scm", absi.inject(3))
  check("eta.scm", absi.inject(false))
  check("fact.scm", absi.inject(120))
  check("gcipd.scm", absi.inject(36))
  check("inc.scm", absi.inject(4))
  check("kcfa2.scm", absi.inject(false))
  check("kcfa3.scm", absi.inject(false))
  check("loop2.scm", absi.inject(550))
  check("mj09.scm", absi.inject(2))
  check("mut-rec.scm", absi.inject(true))
  check("rotate.scm", absi.inject("hallo"))
  check("sq.scm", absi.inject(9))
  check("sym.scm", absi.injectSymbol("foo"))
  check("ack.scm", absi.inject(4))
  check("collatz.scm", absi.inject(5))
  check("widen.scm", absi.inject(10))
  /* The following tests are disabled because they can take a long time to evaluate */
  // check("rsa.scm", absi.inject(true))
  // check("sat.scm", absi.inject(true))
  // check("primtest.scm", absi.inject(1))
  // check("nqueens.scm", absi.inject(92))
  // check("church.scm", absi.inject(true))
  // check("boyer.scm", absi.inject(true))
  // check("dderiv.scm", absi.inject(true))
  // check("takl.scm", absi.inject(true))
}

abstract class AACBenchmarks[Abs, Addr]
  (implicit abs: AbstractValue[Abs], absi: AbstractInjection[Abs],
    addr: Address[Addr], addri: AddressInjection[Addr])
    extends Benchmarks[SchemeExp, Abs, Addr] {
  val sem = new SchemeSemantics[Abs, Addr]
  val machine = new AAC[SchemeExp, Abs, Addr]
}

abstract class AAMBenchmarks[Abs, Addr]
  (implicit abs: AbstractValue[Abs], absi: AbstractInjection[Abs],
    addr: Address[Addr], addri: AddressInjection[Addr])
    extends Benchmarks[SchemeExp, Abs, Addr] {
  val sem = new SchemeSemantics[Abs, Addr]
  val machine = new AAM[SchemeExp, Abs, Addr]
}

abstract class FreeBenchmarks[Abs, Addr]
  (implicit abs: AbstractValue[Abs], absi: AbstractInjection[Abs],
    addr: Address[Addr], addri: AddressInjection[Addr])
    extends Benchmarks[SchemeExp, Abs, Addr] {
  val sem = new SchemeSemantics[Abs, Addr]
  val machine = new Free[SchemeExp, Abs, Addr]
}

/* Concrete tests are disabled because of cpstak takes too much time to compute since it requires more than 75k recursive calls */
/* Type tests are disabled because they fail due to their inability to support a join between a closure and other abstract values */
//class AACConcreteBenchmarks extends AACBenchmarks[AbstractConcrete, ConcreteAddress]
//class AACTypeBenchmarks extends AACBenchmarks[AbstractType, ClassicalAddress]
class AACTypeSetBenchmarks extends AACBenchmarks[AbstractTypeSet, ClassicalAddress]

//class AAMConcreteBenchmarks extends AAMBenchmarks[AbstractConcrete, ConcreteAddress]
//class AAMTypeBenchmarks extends AAMBenchmarks[AbstractType, ClassicalAddress]
class AAMTypeSetBenchmarks extends AAMBenchmarks[AbstractTypeSet, ClassicalAddress]

//class FreeConcreteBenchmarks extends FreeBenchmarks[AbstractConcrete, ConcreteAddress]
//class FreeTypeBenchmarks extends FreeBenchmarks[AbstractType, ClassicalAddress]
class FreeTypeSetBenchmarks extends FreeBenchmarks[AbstractTypeSet, ClassicalAddress]
