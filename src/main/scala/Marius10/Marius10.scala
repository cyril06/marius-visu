package fr.geocite.mariusvisu.Marius10

import fr.geocite.simpuzzle._
import fr.geocite.marius.one.zero._
import fr.geocite.marius._
import util.Random
import spray.json._
import DefaultJsonProtocol._
import java.io._
import math._


object Marius10 extends App {

implicit val rng = new Random(42)

val marius = new StepByStep
  with MariusInitialState
  with MariusStep
  with TimeEndingCondition
  with MariusFile
  with PowerInitialWealth
  {
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

def mean(l:Seq[Double]):Double = l.sum/l.length
def median(l:Seq[Double]):Double = if (l.length%2==1) l((l.length+1)/2) else mean(List(l(l.length/2),l(l.length/2+1)))

def genJSONReal()={
  val long=marius.startingCities.map(_(5)).toList
  val lat=marius.startingCities.map(_(4)).toList
  val okato=marius.cities.map(_.okato)
  val pop2010=marius.startingCities.map(_(17)).toList
  val name=marius.startingCities.map(_(1)).toList

  val data=(okato.toList zip long zip lat zip pop2010 zip name).map{
    case ((((o,lo),la),pop),na) => List(o,lo,la,pop,na)
  }

  val xyzMappedTable = data.map(v=>
    //(v(0),Map[String,String]("long"->v(1),"lat"->v(2),"pop2010"->v(3)))
    Map[String,String]("id"->v(0),"long"->v(1),"lat"->v(2),"pop2010"->v(3),"name"->v(4))
  ).toIndexedSeq

  val writer=new PrintWriter(new File("C:\\wamp\\www\\Vizu\\files\\mariusreal.js"))

  writer.write("var cities ="+xyzMappedTable.toJson.prettyPrint)

  writer.close()

  println(data.toIndexedSeq.toJson.prettyPrint)
}

def getFlows(n:Int,text:String) = {
  val pop2010=marius.startingCities.map(_(17))
  val cities=marius.states.drop(n-1).next.value.cities

  val echangeFrom=cities.map(_.exchangeFrom)
  val echangeTo=cities.map(_.exchangeTo)
  val nbFrom=cities.map(_.exchangeFrom).map(_.length)
  val nbTo=cities.map(_.exchangeTo).map(_.length)
  val pop=cities.map(_.population)
  val wealth=cities.map(_.wealth)
  val totalOut=echangeFrom.map{case z =>z.map{case(a,b)=>b}}.map(_.sum)
  val totalIn=echangeTo.map{case z =>z.map{case(a,b)=>b}}.map(_.sum)

  val citiesIndex=echangeFrom.map(_.map{case(a,b)=>(marius.cities.map(_.okato) zipWithIndex).find{case(x,y)=>x==a}.map{case(a,b)=>b}}) zipWithIndex
  val distance=marius.distances
  val distanceCities=citiesIndex.map{case(a,b)=>a.map{case z=>(b,z)}}.map(_.map{case(a,b)=>b.map{case z=>distance(z)(a)}})
  val echangeFromwDist=(echangeFrom zip distanceCities).map{case (a,b)=>a zip b}.map(_.map{case (a,b)=>List(a._1,a._2)++b}).map(_.map{case a=>(a(0).toString,a(1).toString.toDouble,a(2).toString.toDouble)})

  val data=(cities.map(_.okato) zip echangeFromwDist zip pop zip wealth zip totalOut zip totalIn zip pop2010 zip nbFrom zip nbTo)//filterNot{case(((a,b),c),d)=>b.isEmpty}
  val xyzMappedTableId=cities.map(_.okato).map{case o =>
  (Map[String,String]("okato"->o))}
  val xyzMappedTable = data.map{case ((((((((v,w),x),y),z),u),t),s),r)=>
    (Map[String,String]("orig"->v,"pop"->x.toString,"wealth"->y.toString,"totalOut"->z.toString,"totalIn"->u.toString,"total"->(z-u).toString,"pop2010"->t.toString,"nbFrom"->s.toString,"nbTo"->r.toString))
  }.toIndexedSeq
  val xyzMappedTableBis = data.map{case ((((((((v,w),x),y),z),u),t),s),r)=>w.map{case(x,y,z)=>
    (Map[String,String]("dest"->x,"weight"->y.toString,"distance"->z.toString))
    //Map[String,String]("orig"->v(0),"long"->v(1),"lat"->v(2),"pop2010"->v(3))
  }}.toIndexedSeq
  //val xyzMappedTableGen=Map[String,Seq[Map[String,String]]]("id"->xyzMappedTableId,"nodes"->xyzMappedTable,"links"->xyzMappedTableBis)

  val writer=new PrintWriter(new File("C:\\wamp\\www\\Vizu\\files\\mariusexchange.js"))

  val dataGenTableHeader=Map[String,String]("version"->text,"totalStep"->marius.maxStep.toString,"step"->n.toString,"oneStep"->"1","yearBegin"->"1959","yearEnd"->"2010")
  writer.write("var header="+dataGenTableHeader.toJson.prettyPrint)
  writer.write("\n")

  writer.write("var cities_flows ="+(xyzMappedTable zip xyzMappedTableBis).toJson.prettyPrint)
  //writer.write("var cities_flows ="+xyzMappedTableGen.toJson.prettyPrint)
  writer.write("\n")

  val dataGenTablePop=Map[String,Double]("mean"->mean(pop),"min"->pop.min,"max"->pop.max)
  val dataGenTableWealth=Map[String,Double]("mean"->rint(mean(wealth)*100)/100,"min"->rint(wealth.min*100)/100,"max"->rint(wealth.max*100)/100)

  val notNullFlow=data.map{case ((((((((v,w),x),y),z),u),t),s),r)=>w.map{case(x,y,z)=>y}}.filterNot{case a=>a.isEmpty}.flatten
  val meanFlow=rint(mean(notNullFlow)*100)/100
  val minFlow=rint(notNullFlow.min*100)/100
  val maxFlow=rint(notNullFlow.max*100)/100
  val q1Flow=rint(notNullFlow(ceil(notNullFlow.length/4.0).toInt)*100)/100
  val q3Flow=rint(notNullFlow(ceil((notNullFlow.length/4.0)*3.0).toInt)*100)/100
  val medianFlow=rint(median(notNullFlow)*100)/100

  val total=(totalIn zip totalOut).map{case(a,b)=>a-b}.filterNot{case a=>a==0}.partition(a=>a>0)
  val totalPos=List(total).map{case (a,b)=>a}.flatten
  val totalPosMean=rint(mean(totalPos)*100)/100
  val totalPosMax=rint(totalPos.max*100)/100
  val totalPosMin=rint(totalPos.min*100)/100
  val totalPosNb=totalPos.length
  val totalNeg=List(total).map{case (a,b)=>b}.flatten
  val totalNegMean=rint(mean(totalNeg)*100)/100
  val totalNegMax=rint(totalNeg.max*100)/100
  val totalNegMin=rint(totalNeg.min*100)/100
  val totalNegNb=totalNeg.length

  val dataGenTableTotal=Map[String,Double]("nbPos"->totalPosNb,"meanPos"->totalPosMean,"minPos"->totalPosMin,"maxPos"->totalPosMax,"nbNeg"->totalNegNb,"meanNeg"->totalNegMean,"minNeg"->totalNegMin,"maxNeg"->totalNegMax)

  val dataGenTableFlow=Map[String,Double]("nb"->notNullFlow.length,"mean"->meanFlow,"min"->minFlow,"max"->maxFlow,"q1"->q1Flow,"q3"->q3Flow,"median"->medianFlow)

  val dataGenTableExchange=Map[String,Double]("nbmax"->(nbFrom zip nbTo).map{case (a,b)=>a+b}.max,"maxSumIn"->totalIn.max,"maxSumOut"->totalOut.max)

  val dataGenTable=Map[String,Map[String,Double]]("pop"->dataGenTablePop,"wealth"->dataGenTableWealth,"flow"->dataGenTableFlow,"exchange"->dataGenTableExchange,"commercial"->dataGenTableTotal)
  writer.write("var dataGen="+dataGenTable.toJson.prettyPrint)

  writer.write("\n")

  val popCitiesPos=data.map{case ((((((((v,w),x),y),z),u),t),s),r)=>(x,z-u)}.filter{case (a,b)=>b>0}.map{case(a,b)=>a}.sortWith(_<_)
  val popCitiesNeg=data.map{case ((((((((v,w),x),y),z),u),t),s),r)=>(x,z-u)}.filter{case (a,b)=>b<0}.map{case(a,b)=>a}.sortWith(_<_)
  val meanCitiesPos=rint(mean(popCitiesPos)*100)/100
  val meanCitiesNeg=rint(mean(popCitiesNeg)*100)/100
  val q1Pos=rint(popCitiesPos(ceil(popCitiesPos.length/4.0).toInt)*100)/100
  val q3Pos=rint(popCitiesPos(ceil((popCitiesPos.length/4.0)*3.0).toInt)*100)/100
  val medianPos=rint(median(popCitiesPos)*100)/100
  val q1Neg=rint(popCitiesNeg(ceil(popCitiesNeg.length/4.0).toInt)*100)/100
  val q3Neg=rint(popCitiesNeg(ceil((popCitiesNeg.length/4.0)*3.0).toInt)*100)/100
  val medianNeg=rint(median(popCitiesNeg)*100)/100

  val dataGenTablePos=Map[String,Double]("min"->rint(popCitiesPos.min*100)/100,"max"->rint(popCitiesPos.max*100)/100,"q1"->q1Pos,"q3"->q3Pos,"median"->medianPos,"mean"->meanCitiesPos)
  val dataGenTableNeg=Map[String,Double]("min"->rint(popCitiesNeg.min*100)/100,"max"->rint(popCitiesNeg.max*100)/100,"q1"->q1Neg,"q3"->q3Neg,"median"->medianNeg,"mean"->meanCitiesNeg)

  val dataGenTableWhiskers=Map[String,Map[String,Double]]("positive"->dataGenTablePos,"negative"->dataGenTableNeg)
  writer.write("var whiskers="+dataGenTableWhiskers.toJson.prettyPrint)
  writer.close()
}

  for{ s <- marius.states.take(2) }{
    println((s.value.cities.map(_.population).sum))
  }

//getFlows(3,"Marius10")

//genJSONReal()

}
