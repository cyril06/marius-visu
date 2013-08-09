/**
 * Created with IntelliJ IDEA.
 * User: paris
 * Date: 07/08/13
 * Time: 15:27
 * To change this template use File | Settings | File Templates.
 */
import fr.geocite.simpuzzle._
import fr.geocite.marius._
import fr.geocite.marius.zero.one._
import util.Random
import scala.util.Try

object Test {

  implicit val rng = new Random(42)

  val marius = new StepByStep with MariusInitialState with MariusStep with TimeEndingCondition with MariusFile {
    def hydrocarbonBonus: Double = 0.01
    def rate: Double = 0.01
    def stdRate: Double = 0.005
    def maxStep: Int = 10
  }

  def takeOneStep(n:Int):Seq[marius.City] = {
    if (marius.maxStep>n) {
      val l=marius.states.drop(n-1).next.cities
      l
    }
    else List()
  }

  def takeOneCity(id:String) = {
    val it=marius.states
    val n=id.length
    var res=it.next.cities.filter(e=>e.okato.take(n)==id)
    while (it.hasNext) {
      val l=it.next.cities.filter(e=>e.okato.take(n)==id)
      res=res++l
    }
    res
  }

  def takeNLoopBis(n:Int,l:Seq[marius.City],):Seq[marius.City] = n match {
    case 0 => l
    case _ => l++takeNLoopBis(n-1,takeOneStep(marius.maxStep-1))
  }

  def takeNLoop(n:Int)=takeNLoopBis(n,List())


}