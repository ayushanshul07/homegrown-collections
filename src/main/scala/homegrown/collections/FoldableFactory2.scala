package homegrown.collections

trait FoldableFactory2[Key, +Value, SubtypeOfFoldableFactory[Key, +Value] <: FoldableFactory2[Key, Value, SubtypeOfFoldableFactory]]
  extends Foldable[(Key, Value)] {
  protected def factory: Factory2[SubtypeOfFoldableFactory]

  def add[SuperValue >: Value](input: (Key, SuperValue)): SubtypeOfFoldableFactory[Key, SuperValue]

  final def filterNot(predicate: Tuple2[Key, Value] => Boolean): SubtypeOfFoldableFactory[Key, Value] =
    filter(!predicate(_))

  def filter(predicate: Tuple2[Key, Value] => Boolean): SubtypeOfFoldableFactory[Key, Value] =
    foldLeft[SubtypeOfFoldableFactory[Key, Value]](factory.empty) { (acc, current) =>
      if (predicate(current))
        acc.add(current)
      else
        acc
    }

  final def withFilter(predicate: Tuple2[Key, Value] => Boolean): FoldableFactory2.Wrapper[Key, Value, SubtypeOfFoldableFactory] =
    new FoldableFactory2.Wrapper(this, predicate)

  def map[ResultKey, ResultValue](function: Tuple2[Key, Value] => Tuple2[ResultKey, ResultValue]): SubtypeOfFoldableFactory[ResultKey, ResultValue] =
    foldLeft[SubtypeOfFoldableFactory[ResultKey, ResultValue]](factory.empty)(_ add function(_))

  def flatMap[ResultKey, ResultValue](function: Tuple2[Key, Value] => Foldable[Tuple2[ResultKey, ResultValue]]): SubtypeOfFoldableFactory[ResultKey, ResultValue] =
    foldLeft[SubtypeOfFoldableFactory[ResultKey, ResultValue]](factory.empty) { (acc, current) =>
      function(current).foldLeft(acc)(_ add _)
    }

  def flatten[ResultKey, ResultValue](implicit view: Tuple2[Key, Value] => Foldable[Tuple2[ResultKey, ResultValue]]): SubtypeOfFoldableFactory[ResultKey, ResultValue] =
    foldLeft[SubtypeOfFoldableFactory[ResultKey, ResultValue]](factory.empty) { (acc, current) =>
      view(current).foldLeft(acc)(_ add _)
    }
}

object FoldableFactory2 {
  final class Wrapper[Key, +Value, SubtypeOfFoldableFactory[Key, +Value] <: FoldableFactory2[Key, Value, SubtypeOfFoldableFactory]](
      foldableFactory: FoldableFactory2[Key, Value, SubtypeOfFoldableFactory],
      predicate: Tuple2[Key, Value] => Boolean
  ) {
    final def foreach[Result](function: Tuple2[Key, Value] => Result): Unit = {
      foldableFactory.foldLeft(()) { (_, current) =>
        if (predicate(current))
          function(current)
      }
    }

    final def map[ResultKey, ResultValue](function: Tuple2[Key, Value] => Tuple2[ResultKey, ResultValue]): SubtypeOfFoldableFactory[ResultKey, ResultValue] =
      foldableFactory.foldLeft[SubtypeOfFoldableFactory[ResultKey, ResultValue]](foldableFactory.factory.empty) { (acc, current) =>
        if (predicate(current))
          acc.add(function(current))
        else
          acc
      }

    def flatMap[ResultKey, ResultValue](function: Tuple2[Key, Value] => Foldable[Tuple2[ResultKey, ResultValue]]): SubtypeOfFoldableFactory[ResultKey, ResultValue] =
      foldableFactory.foldLeft[SubtypeOfFoldableFactory[ResultKey, ResultValue]](foldableFactory.factory.empty) { (acc, current) =>
        if (predicate(current))
          function(current).foldLeft(acc)(_ add _)
        else
          acc
      }
  }
}
