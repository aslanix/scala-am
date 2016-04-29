import SchemeOps._
/**
 * Product lattice, combines two lattices X and Y as a product (X, Y).
 * Here's an example on how to use it, to combine the type lattice with the typeset lattice:
 *   val prod = new ProductLattice[AbstractType, AbstractTypeSet] // create the product lattice
 *   import prod._ // import its elements (and most importantly, Product)
 *   run(new Free[SchemeExp, Product, ClassicalAddress], new SchemeSemantics[Product, ClassicalAddress]) _ // run a machine with it
 */
class ProductLattice[X : AbstractValue, Y : AbstractValue] {
  val xabs = implicitly[AbstractValue[X]]
  val yabs = implicitly[AbstractValue[Y]]

  trait Product
  case class Prod(x: X, y: Y) extends Product

  implicit object ProductAbstractValue extends AbstractValue[Product] {
    val name = s"(${xabs.name}, ${yabs.name})"
    val counting = xabs.counting && yabs.counting

    private def err(reason: String) = error(inject(reason))

    def isTrue(p: Product) = p match {
      case Prod(x, y) => xabs.isTrue(x) || yabs.isTrue(y)
    }
    def isFalse(p: Product) = p match {
      case Prod(x, y) => xabs.isFalse(x) || yabs.isFalse(y)
    }
    def isError(p: Product) = p match {
      case Prod(x, y) => xabs.isError(x) || yabs.isError(y)
    }
    def isPrimitiveValue(p: Product) = p match {
      case Prod(x, y) => xabs.isPrimitiveValue(x) && yabs.isPrimitiveValue(y)
    }
    def unaryOp(op: UnaryOperator)(p: Product) = p match {
      case Prod(x, y) => Prod(xabs.unaryOp(op)(x), yabs.unaryOp(op)(y))
    }
    def binaryOp(op: BinaryOperator)(p1: Product, p2: Product) = (p1, p2) match {
      case (Prod(x1, y1), Prod(x2, y2)) => Prod(xabs.binaryOp(op)(x1, x2), yabs.binaryOp(op)(y1, y2))
      case _ => err("operator $op cannot work on primitives (arguments were $p1 and $p2)")
    }
    def join(p1: Product, p2: Product) = (p1, p2) match {
      case (Prod(x1, y1), Prod(x2, y2)) => Prod(xabs.join(x1, x2), yabs.join(y1, y2))
      case _ => ???
    }
    def subsumes(p1: Product, p2: Product) = (p1, p2) match {
      case (Prod(x1, y1), Prod(x2, y2)) => xabs.subsumes(x1, x2) && yabs.subsumes(y1, y2)
      case _ => false
    }
    def and(p1: Product, p2: => Product) = (p1, p2) match {
      case (Prod(x1, y1), Prod(x2, y2)) => Prod(xabs.and(x1, x2), yabs.and(y1, y2))
      case _ => err(s"and cannot work on primitives (arguments were $p1 and $p2)")
    }
    def or(p1: Product, p2: => Product) = (p1, p2) match {
      case (Prod(x1, y1), Prod(x2, y2)) => Prod(xabs.or(x1, x2), yabs.or(y1, y2))
      case _ => err(s"or cannot work on primitives (arguments were $p1 and $p2)")
    }
    def car[Addr : Address](p: Product) = p match {
      case Prod(x, y) => xabs.car[Addr](x) ++ yabs.car[Addr](y)
      case _ => Set[Addr]()
    }
    def cdr[Addr : Address](p: Product) = p match {
      case Prod(x, y) => xabs.cdr[Addr](x) ++ yabs.cdr[Addr](y)
      case _ => Set[Addr]()
    }
    def toString[Addr : Address](p: Product, store: Store[Addr, Product]) = p.toString // s"(${xabs.toString(p.x, store)}, ${yabs.toString(p.y, store)})"
    def getClosures[Exp : Expression, Addr : Address](p: Product) = p match {
      case Prod(x, y) => xabs.getClosures[Exp, Addr](x) ++ yabs.getClosures[Exp, Addr](y)
      case _ => Set()
    }
    def getPrimitives[Addr : Address, Abs : JoinLattice](p: Product) = p match {
      case Prod(x, y) => xabs.getPrimitives[Addr, Abs](x) ++ yabs.getPrimitives[Addr, Abs](y)
      case _ => Set()
    }
    def getTids[TID : ThreadIdentifier](p: Product) = p match {
      case Prod(x, y) => xabs.getTids[TID](x) ++ yabs.getTids[TID](y)
      case _ => Set()
    }
    def getLocks[Addr: Address](p: Product) = p match {
      case Prod(x, y) => xabs.getLocks[Addr](x) ++ yabs.getLocks[Addr](y)
      case _ => Set()
    }

    def bottom = Prod(xabs.bottom, yabs.bottom)
    def error(p: Product) = p match {
      case Prod(x, y) => Prod(xabs.error(x), yabs.error(y))
    }
    def inject(x: Int) = Prod(xabs.inject(x), yabs.inject(x))
    def inject(x: Float) = Prod(xabs.inject(x), yabs.inject(x))
    def inject(x: String) = Prod(xabs.inject(x), yabs.inject(x))
    def inject(x: Char) = Prod(xabs.inject(x), yabs.inject(x))
    def inject(x: Boolean) = Prod(xabs.inject(x), yabs.inject(x))
    def inject[Addr : Address, Abs : JoinLattice](x: Primitive[Addr, Abs]) = Prod(xabs.inject[Addr, Abs](x), yabs.inject[Addr, Abs](x))
    def inject[Exp : Expression, Addr : Address](x: (Exp, Environment[Addr])) = Prod(xabs.inject[Exp, Addr](x), yabs.inject[Exp, Addr](x))
    def injectTid[TID : ThreadIdentifier](t: TID) = Prod(xabs.injectTid[TID](t), yabs.injectTid[TID](t))
    def injectSymbol(x: String) = Prod(xabs.injectSymbol(x), yabs.injectSymbol(x))
    def nil = Prod(xabs.nil, yabs.nil)
    def cons[Addr : Address](car: Addr, cdr: Addr) = Prod(xabs.cons[Addr](car, cdr), yabs.cons[Addr](car, cdr))
    /* TODO: implement vectors */
    def vector[Addr : Address](addr: Addr, size: Product, init: Addr) = ???
    def vectorRef[Addr : Address](vector: Product, index: Product) = ???
    def vectorSet[Addr : Address](vector: Product, index: Product, addr: Addr) = ???
    def getVectors[Addr : Address](x: Product) = ???

    def lock[Addr : Address](addr: Addr) = Prod(xabs.lock(addr), yabs.lock(addr))
    def lockedValue = Prod(xabs.lockedValue, yabs.lockedValue)
    def unlockedValue = Prod(xabs.unlockedValue, yabs.unlockedValue)

  }
}