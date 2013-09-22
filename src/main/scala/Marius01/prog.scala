/**
 * Created with IntelliJ IDEA.
 * User: paris
 * Date: 12/08/13
 * Time: 12:36
 * To change this template use File | Settings | File Templates.
 */
/*
import fr.geocite.simpuzzle._
import fr.geocite.marius._
import fr.geocite.marius.zero.one._
import util.Random
import spray.json._
import DefaultJsonProtocol._
import java.io._
import math._

//class Marius01 {

implicit val rng = new Random(42)

val marius = new StepByStep with MariusInitialState with MariusStep with TimeEndingCondition with MariusFile {
 def hydrocarbonBonus: Double = 0.01
 def rate: Double = 0.01
 def stdRate: Double = 0.005
 def maxStep: Int = 10
}



def takeOneStep(n:Int):List[Seq[marius.City]] = {
 if (marius.maxStep>n) {
   val l=List(marius.states.drop(n-1).next.cities)
   l
 }
 else List(List())
}

def takeAllSteps():List[Seq[marius.City]] = {
 val it=marius.states
 var res=List(it.next.cities)
 while (it.hasNext) {
   res=res++List(it.next.cities)
 }
 res
}

def takeOneCity(id:String):List[Seq[marius.City]] = {
 val it=marius.states
 val n=id.length
 var res=it.next.cities.filter(e=>e.okato.take(n)==id)
 while (it.hasNext) {
   val l=it.next.cities.filter(e=>e.okato.take(n)==id)
   res=res++l
 }
 List(res)
}

def takeNLoopBis(n:Int,l:List[Seq[marius.City]]):List[Seq[marius.City]] = n match {
 case 0 => l
 case _ => takeNLoopBis(n-1,l++takeOneStep(marius.maxStep-1))
 //case _ => takeNLoopBis(n-1,l++List(List(n,takeOneStep(marius.maxStep-1))))
}

def takeNLoop(n:Int)=takeNLoopBis(n,List())

def sendPop(l:List[Seq[marius.City]]):List[Seq[Double]] = l.map(_.map(_.population))

def getReal2010():List[Seq[Double]] = List(marius.startingCities.map(_.drop(17)).map(_.apply(0)).map(_.toDouble))

//}



//class Operator extends Marius01 {

def order(l:List[Seq[Double]]):List[Seq[Double]] = l.map(_.sortWith(_>_))

def round(l:List[Seq[Double]]):List[Seq[Double]] = l.map(_.map(e => (math.rint(e*100))/100))

def simplify(l:List[Seq[Double]]):List[Seq[Double]] = round(order(l))

def mean(l:Seq[Double]):Double = l.sum/l.length

def sumProduct(l:Seq[(Double,Double)]):Double = l.map{case(x,y)=>x*y}.sum

//}



// PROG

def genRangTaille(n:Int) = {

 val simu=simplify(sendPop(takeNLoop(n)))
 val real=simplify(getReal2010())


 val xyzMappedTableSimu = simu.map { case(v) =>
   Map[String, Seq[Double]]("population" -> v)
 }.toIndexedSeq

 val xyzMappedTableReal = real.map { case(v) =>
   Map[String, Seq[Double]]("population" -> v)
 }.toIndexedSeq

 val writer=new PrintWriter(new File("C:\\wamp\\www\\Vizu\\files\\rangtaille.js"));


 writer.write("var valeursSimu ="+xyzMappedTableSimu.toJson.prettyPrint)
 writer.write("\n")
 writer.write("var valeursReal ="+xyzMappedTableReal.toJson.prettyPrint)

 writer.close()
}

def genArea(n:Int) = {

 val simu=simplify(sendPop(takeNLoop(n)))
 val real=simplify(getReal2010())
 var res:List[List[Double]]=List(List())

 for (i <- 0 until simu(0).length) {
   val max= simu.map(l=>l(i)).max
   val min= simu.map(l=>l(i)).min
   res=res++List(List(i+1,max,min))
 }

 res=res.tail

 val xyzMappedTableSimu = List(res).map { v ⇒
   Map[String, List[List[Double]]]("population" -> v)
 }.toIndexedSeq

 val xyzMappedTableReal = real.map { v ⇒
   Map[String, Seq[Double]]("population" -> v)
 }.toIndexedSeq

 val writer=new PrintWriter(new File("C:\\wamp\\www\\Vizu\\files\\area.js"))


 writer.write("var valeursSimu ="+xyzMappedTableSimu.toJson.prettyPrint)
 writer.write("\n")
 writer.write("var valeursReal ="+xyzMappedTableReal.toJson.prettyPrint)

 writer.close()
}

def genPente() = {
 val simu=simplify(sendPop(takeAllSteps())).map(_.zipWithIndex).map(_.map{case (a,b)=>(log(a),log(b+1))})
 val moyenne=simu.map(mean)


}

genRangTaille(5)
*/