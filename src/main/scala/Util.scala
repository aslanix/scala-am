import scala.io.StdIn
import scala.concurrent.duration.Duration

object Util {
  def fileContent(file: String): String = {
    val f = scala.io.Source.fromFile(file)
    val content = f.getLines.mkString("\n")
    f.close()
    content
  }

  object Done extends Exception
  /* TODO: use jline? e.g. http://stackoverflow.com/questions/7913555/how-to-write-interactive-shell-with-readline-support-in-scala
   An empty line would submit to cb
   */
  /** Either run cb on the content of the given file, or run a REPL, each line being sent to cb */
  def replOrFile(file: Option[String], cb: String => Unit): Unit = {
    try {
      do {
        val program = file match {
          case Some(file) => fileContent(file)
          case None => StdIn.readLine(">>> ")
        }
        if (program == null) throw Done
        if (program.size > 0)
          cb(program)
      } while (file.isEmpty);
    } catch {
      case Done => ()
    }
  }

  /**
   * This is where we parse the arguments given to the implementation
   */
  object Config {
    object Machine extends Enumeration {
      val AAC, AAM, AAMGlobalStore, Free, ConcreteMachine = Value
    }
    implicit val machineRead: scopt.Read[Machine.Value] = scopt.Read.reads(Machine withName _)

    object Lattice extends Enumeration {
      val Concrete, ConcreteNew, TypeSet, BoundedInt = Value
    }
    implicit val latticeRead: scopt.Read[Lattice.Value] = scopt.Read.reads(Lattice withName _)

    object Address extends Enumeration {
      val Classical, ValueSensitive = Value
    }
    implicit val addressRead: scopt.Read[Address.Value] = scopt.Read.reads(Address withName _)

    case class Config(machine: Machine.Value = Machine.Free,
      lattice: Lattice.Value = Lattice.TypeSet, concrete: Boolean = false,
      file: Option[String] = None, dotfile: Option[String] = None,
      address: Address.Value = Address.Classical,
      inspect: Boolean = false,
      counting: Boolean = false,
      bound: Int = 100,
      timeout: Option[Duration] = None,
      workers: Int = 1)

    val parser = new scopt.OptionParser[Config]("scala-am") {
      head("scala-am", "0.0")
      opt[Machine.Value]('m', "machine") action { (x, c) => c.copy(machine = x) } text("Abstract machine to use (AAM, AAMGlobalStore, AAC, Free, ConcreteMachine)")
      opt[Lattice.Value]('l', "lattice") action { (x, c) => c.copy(lattice = x) } text("Lattice to use (Concrete, Type, TypeSet)")
      opt[Unit]('c', "concrete") action { (_, c) => c.copy(concrete = true) } text("Run in concrete mode")
      opt[String]('d', "dotfile") action { (x, c) => c.copy(dotfile = Some(x)) } text("Dot file to output graph to")
      opt[String]('f', "file") action { (x, c) => c.copy(file = Some(x)) } text("File to read program from")
      opt[Duration]('t', "timeout") action { (x, c) => c.copy(timeout = if (x.isFinite) Some(x) else None) } text("Timeout (none by default)")
      opt[Unit]('i', "inspect") action { (x, c) => c.copy(inspect = true) } text("Launch inspection REPL (disabled by default)")
      opt[Address.Value]('a', "address") action { (x, c) => c.copy(address = x) } text("Addresses to use (Classical, ValueSensitive)")
      opt[Unit]("counting") action { (x, c) => c.copy(counting = true) } text("Use abstract counting (on for concrete lattices)")
      opt[Int]('b', "bound") action { (x, c) => c.copy(bound = x) } text("Bound for bounded lattice (default to 100)")
      opt[Int]('w', "workers") action { (x, c) => c.copy(workers = x) } text("Number of workers")
    }
  }

  /* From http://stackoverflow.com/questions/7539831/scala-draw-table-to-console */
  object Tabulator {
    def format(table: Seq[Seq[Any]]) = table match {
      case Seq() => ""
      case _ =>
        val sizes = for (row <- table) yield (for (cell <- row) yield if (cell == null) 0 else cell.toString.length)
        val colSizes = for (col <- sizes.transpose) yield col.max
        val rows = for (row <- table) yield formatRow(row, colSizes)
        formatRows(rowSeparator(colSizes), rows)
    }

    def formatRows(rowSeparator: String, rows: Seq[String]): String = (
      rowSeparator ::
        rows.head ::
        rowSeparator ::
        rows.tail.toList :::
        rowSeparator ::
        List()).mkString("\n")

    def formatRow(row: Seq[Any], colSizes: Seq[Int]) = {
      val cells = (for ((item, size) <- row.zip(colSizes)) yield if (size == 0) "" else ("%" + size + "s").format(item))
      cells.mkString("|", "|", "|")
    }

    def rowSeparator(colSizes: Seq[Int]) = colSizes map { "-" * _ } mkString("+", "+", "+")
  }
}
