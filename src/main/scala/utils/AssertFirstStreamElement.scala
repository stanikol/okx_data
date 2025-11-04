import cats.effect.Sync
import fs2.Pipe
import fs2.Pull

/** Asserts the first element of stream pass check, if not raise error.
  * @param check
  * @tparam F
  * @tparam O
  * @return
  */
def assertFirstElement[F[_]: Sync, O](check: O => Boolean): Pipe[F, O, O] = {
  def go(s: fs2.Stream[F, O]): Pull[F, O, Unit] = {
    s.pull.uncons.flatMap {
      case Some((hd, tl)) =>
        if (hd.head.exists(check))
          Pull.output(hd.drop(1)) >> tl.pull.echo
        else
          Pull.raiseError[F](new Exception("Error in assertFirstElement - assertion on the first element of fs2.Stream failed !"))
      case None => Pull.done
    }
  }
  in => go(in).stream
}
