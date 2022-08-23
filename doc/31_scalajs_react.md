
```scala
/** Invoked once, only on the client (not on the server), immediately after the initial rendering occurs. At this point
  * in the lifecycle, the component has a DOM representation which you can access via `ReactDOM.findDOMNode(this)`.
  * The `componentDidMount()` method of child components is invoked before that of parent components.
  *
  * If you want to integrate with other JavaScript frameworks, set timers using `setTimeout` or `setInterval`, or send
  * AJAX requests, perform those operations in this method.
  */
def componentDidMount[G[_]](f: ComponentDidMount[P, S, B] => G[Unit])(implicit G: Dispatch[G]): This =
  lcAppendDispatch(LifecycleF.componentDidMount)(f)
```

```scala
trait Monad[F[_]] {
  def pure   [A]   (a: A)                  : F[A]
  def map    [A, B](fa: F[A])(f: A => B)   : F[B]
  def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]
  def tailrec[A, B](a: A)(f: A => F[Either[A, B]]): F[B]
}

trait Effect[F[_]] extends Monad[F] {
    def delay      [A]         (a: => A)                           : F[A]
    def handleError[A, AA >: A](fa: => F[A])(f: Throwable => F[AA]): F[AA]
    def suspend    [A]         (fa: => F[A])                       : F[A]   
    /** Wraps this callback in a `try-finally` block and runs the given callback in the `finally` clause, after the
     * current callback completes, be it in error or success.
     */
    def finallyRun[A, B](fa: => F[A], runFinally: => F[B]): F[A]
}

trait Dispatch[F[_]] extends Effect[F] {
    /** Fire and forget, could be sync or async */
    def dispatch[A](fa: F[A]): Unit
    def dispatchFn[A](fa: => F[A]): js.Function0[Unit]
}

trait UnsafeSync[F[_]] extends Dispatch[F] {
    def runSync[A](fa: => F[A]): A   
    def sequence_[A](fas: => Iterable[F[A]]): F[Unit]
    def sequenceList[A](fas: => List[F[A]]): F[List[A]]   
    def toJsFn[A](fa: => F[A]): js.Function0[A]   
    def traverse_[A, B](as: => Iterable[A])(f: A => F[B]): F[Unit]   
    def traverseList[A, B](as: => List[A])(f: A => F[B]): F[List[B]]   
    def when_[A](cond: => Boolean)(fa: => F[A]): F[Unit]
}

trait Sync[F[_]] extends UnsafeSync[F] {
    val empty: F[Unit]
    val semigroupSyncOr: Semigroup[F[Boolean]]
    implicit val semigroupSyncUnit: Semigroup[F[Unit]]
    
    def fromJsFn0[A](f: js.Function0[A]): F[A]
    def isEmpty  [A](f: F[A])           : Boolean
    def reset    [A](fa: F[A])          : F[Unit]
    def runAll   [A](callbacks: F[A]*)  : F[Unit]
}

trait Async[F[_]] extends Dispatch[F] {
  def async[A](f: Async.Untyped[A]): F[A]
  def first[A](f: Async.Untyped[A]): F[A]
  def runAsync[A](fa: => F[A]): Async.Untyped[A]
  def toJsPromise[A](fa: => F[A]): () => js.Promise[A]
  def fromJsPromise[A](pa: => js.Thenable[A]): F[A]
}

```



```scala

```

```scala

```

```scala

```

```scala

```

```scala

```