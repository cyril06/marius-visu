import fr.geocite.simpuzzle._
import fr.geocite.marius.one.zero._
import fr.geocite.marius._
import util.Random
import spray.json._
import DefaultJsonProtocol._
import java.io._

implicit val rng = new Random(42)

val marius = new StepByStep with MariusInitialState with MariusStep with TimeEndingCondition with MariusFile with PowerInitialWealth {
  // Members declared in fr.geocite.marius.one.zero.MariusStep
  def adjustConsumption: Double = 0.6
  def adjustProductivity: Double = 0.3
  def capitalShareOfTaxes: Double = 0.0
  def conversionFactor: Double = 50.0
  def distanceDecay: Double = 1.0
  def distanceOrderBuy: Double = 0.6
  def distanceOrderSell: Double = 0.4
  def partnerMultiplier: Double = 8.0
  def territorialTaxes: Double = 0.4

  // Members declared in fr.geocite.marius.one.zero.PowerInitialWealth
  def wealthExponent: Double = 1.3

  // Members declared in fr.geocite.simpuzzle.TimeEndingCondition
  def maxStep: Int = 3
}

def genJSONReal()={
  val long=marius.startingCities.map(_(5))
  val lat=marius.startingCities.map(_(4))
  val okato=marius.cities.map(_.okato)
  val pop2010=marius.startingCities.map(_(17))

  val data=(okato zip long zip lat zip pop2010).map{
    case (((o,lo),la),pop) => List(o,lo,la,pop)
  }

  val xyzMappedTable = data.map(v=>
    //(v(0),Map[String,String]("long"->v(1),"lat"->v(2),"pop2010"->v(3)))
    Map[String,String]("okato"->v(0),"long"->v(1),"lat"->v(2),"pop2010"->v(3))
  ).toIndexedSeq

  val writer=new PrintWriter(new File("C:\\wamp\\www\\Vizu\\files\\mariusreal.js"))

  writer.write("var cities_russia ="+xyzMappedTable.toJson.prettyPrint)

  writer.close()

  println(data.toIndexedSeq.toJson.prettyPrint)
}

def getFlows(n:Int) = {
  val echange=marius.states.drop(n-1).next.cities.map(_.exchange)
  val data=(marius.cities.map(_.okato) zip echange).filterNot{case(a,b)=>b.isEmpty}
  //val data=marius.states.drop(n-1).next.cities.filterNot(c=>c.exchange.isEmpty).map(c=>List(c.okato,c.exchange))
  val xyzMappedTable = data.map{case (v,List(w,x))=>
  (Map[String,Seq[String]]("orig"->Seq(v),"dest"->Seq(w,x.toString)))
  //Map[String,String]("orig"->v(0),"long"->v(1),"lat"->v(2),"pop2010"->v(3))
  }.toIndexedSeq

  val writer=new PrintWriter(new File("C:\\wamp\\www\\Vizu\\files\\mariusexchange.js"))

  writer.write("var cities_flows ="+xyzMappedTable.toJson.prettyPrint)

  writer.close()
}

getFlows(2)